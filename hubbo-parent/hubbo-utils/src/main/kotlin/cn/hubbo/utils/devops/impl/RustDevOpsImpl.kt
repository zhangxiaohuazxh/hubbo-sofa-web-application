package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.CommandLineUtils
import cn.hubbo.utils.devops.DevOps
import cn.hubbo.utils.devops.DevOpsConfiguration
import cn.hubbo.utils.devops.capability.Builder
import cn.hubbo.utils.devops.capability.Compiler
import cn.hubbo.utils.devops.capability.SyntaxChecker
import cn.hubbo.utils.devops.capability.Tester
import cn.hubbo.utils.devops.config.BuildOptions
import cn.hubbo.utils.devops.config.BuildTool
import cn.hubbo.utils.devops.config.CompileOptions
import cn.hubbo.utils.devops.config.SyntaxCheckOptions
import cn.hubbo.utils.devops.config.TestOptions
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.error.DevOpsError
import cn.hubbo.utils.devops.core.error.ErrorCode
import cn.hubbo.utils.devops.core.model.Artifact
import cn.hubbo.utils.devops.core.model.ArtifactType
import cn.hubbo.utils.devops.core.model.BuildResult
import cn.hubbo.utils.devops.core.model.CompileResult
import cn.hubbo.utils.devops.core.model.Language
import cn.hubbo.utils.devops.core.model.SyntaxCheckResult
import cn.hubbo.utils.devops.core.model.SyntaxError
import cn.hubbo.utils.devops.core.model.TestReport
import cn.hubbo.utils.devops.core.model.TestType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.time.Duration

/**
 * Rust / Cargo 语言的 DevOps 实现。
 *
 * 与 [JavaDevOpsImpl] 类似：保留旧版命令式方法，
 * 同时将新版能力接口映射到 Cargo 命令。
 */
@Suppress("DEPRECATION")
class RustDevOpsImpl(private val devOpsConfiguration: DevOpsConfiguration) : DevOps {

    private val logger: Logger = LoggerFactory.getLogger(RustDevOpsImpl::class.java)

    override fun getDevOpsConfiguration(): DevOpsConfiguration = devOpsConfiguration

    override fun getLogger(): Logger = logger

    // ==================== 旧版方法 ====================

    @Deprecated("请使用 compiler.compile(ctx, CompileOptions)")
    override suspend fun compileCommand(): String = "cargo check"

    @Deprecated("请使用 builder.build(ctx, BuildOptions)")
    override suspend fun build() {
        CommandLineUtils.execute("cargo build", workingDirectory = getDevOpsConfiguration().projectDirectory())
    }

    @Deprecated("请使用 builder.build 返回的 BuildResult.artifacts")
    override fun isFinalProduct(file: File): Boolean =
        file.isFile && file.canExecute() && file.absolutePath.contains("target")

    // ==================== 新版能力 ====================

    override val syntaxChecker: SyntaxChecker = object : SyntaxChecker {
        override val supportedLanguages: Set<Language> = setOf(Language.RUST)

        override suspend fun check(ctx: PipelineContext, options: SyntaxCheckOptions): SyntaxCheckResult {
            val result = CommandLineUtils.execute("cargo check", workingDirectory = devOpsConfiguration.projectDirectory())
            val passed = result.exitCode == 0
            return SyntaxCheckResult(
                passed = passed,
                errors = if (passed) {
                    emptyList()
                } else {
                    listOf(SyntaxError("<cargo>", 0, 0, result.output ?: "cargo check failed"))
                },
                filesChecked = 0,
                duration = Duration.ZERO,
            )
        }
    }

    override val tester: Tester = object : Tester {
        override val supportedTypes: Set<TestType> = setOf(TestType.UNIT, TestType.INTEGRATION)

        override suspend fun test(ctx: PipelineContext, options: TestOptions): TestReport {
            val result = CommandLineUtils.execute("cargo test", workingDirectory = devOpsConfiguration.projectDirectory())
            val passed = result.exitCode == 0
            return TestReport(
                type = options.type,
                total = if (passed) 1 else 0,
                passed = if (passed) 1 else 0,
                failed = if (passed) 0 else 1,
                skipped = 0,
                flaky = 0,
                duration = Duration.ZERO,
            )
        }
    }

    override val compiler: Compiler = object : Compiler {
        override suspend fun compile(ctx: PipelineContext, options: CompileOptions): CompileResult {
            val result = CommandLineUtils.execute("cargo check", workingDirectory = devOpsConfiguration.projectDirectory())
            if (result.exitCode != 0) {
                throw DevOpsError.recoverable(ErrorCode.COMPILE_FAILED, Stage.COMPILE, result.output ?: "cargo check failed")
            }
            return CompileResult(outputs = emptyList(), warnings = emptyList(), duration = Duration.ZERO)
        }
    }

    override val builder: Builder = object : Builder {
        override val supportedTools: Set<BuildTool> = setOf(BuildTool.CARGO)

        override suspend fun build(ctx: PipelineContext, options: BuildOptions): BuildResult {
            val result = CommandLineUtils.execute("cargo build --release", workingDirectory = devOpsConfiguration.projectDirectory())
            if (result.exitCode != 0) {
                throw DevOpsError.recoverable(ErrorCode.BUILD_FAILED, Stage.BUILD, result.output ?: "cargo build failed")
            }
            val artifacts = captureProduct().map { file ->
                val path = file.toPath()
                Artifact(
                    name = file.name,
                    type = options.artifactType,
                    file = path,
                    checksum = "sha256-mock",
                    sizeBytes = Files.size(path),
                )
            }
            return BuildResult(artifacts = artifacts, duration = Duration.ZERO)
        }
    }
}
