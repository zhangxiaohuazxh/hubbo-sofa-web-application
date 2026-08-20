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
import cn.hubbo.utils.devops.core.error.Errors
import cn.hubbo.utils.devops.core.model.Artifact
import cn.hubbo.utils.devops.core.model.ArtifactType
import cn.hubbo.utils.devops.core.model.BuildResult
import cn.hubbo.utils.devops.core.model.CompileResult
import cn.hubbo.utils.devops.core.model.Language
import cn.hubbo.utils.devops.core.model.SyntaxCheckResult
import cn.hubbo.utils.devops.core.model.SyntaxError
import cn.hubbo.utils.devops.core.model.TestReport
import cn.hubbo.utils.devops.core.model.TestType
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.time.Duration

/**
 * Java / Maven 语言的 DevOps 实现。
 *
 * 保留旧版命令式方法（[build]、[check]、[test]、[compileCommand]）以兼容既有调用；
 * 同时将新版能力接口（[SyntaxChecker]、[Tester]、[Compiler]、[Builder]）
 * 映射到 Maven 命令，供能力层与流水线编排调用。
 */
class JavaDevOpsImpl(private val devOpsConfiguration: DevOpsConfiguration) : DevOps {

    private val logger: Logger = LoggerFactory.getLogger(JavaDevOpsImpl::class.java)

    override fun getDevOpsConfiguration(): DevOpsConfiguration = devOpsConfiguration

    override fun getLogger(): Logger = logger

    // ==================== 旧版方法 ====================

    @Deprecated("请使用 builder.build(ctx, BuildOptions)")
    @Suppress("DEPRECATION")
    override suspend fun build() {
        CommandLineUtils.execute("mvn package -B -e", workingDirectory = getDevOpsConfiguration().projectDirectory())
    }

    @Deprecated("请使用 syntaxChecker.check(ctx, SyntaxCheckOptions)")
    @Suppress("DEPRECATION")
    override suspend fun check() {
        CommandLineUtils.execute("mvn validate", workingDirectory = getDevOpsConfiguration().projectDirectory())
    }

    @Deprecated("请使用 tester.test(ctx, TestOptions)")
    @Suppress("DEPRECATION")
    override suspend fun test() {
        CommandLineUtils.execute("mvn test", workingDirectory = getDevOpsConfiguration().projectDirectory())
    }

    @Deprecated("请使用 compiler.compile(ctx, CompileOptions)")
    @Suppress("DEPRECATION")
    override suspend fun compileCommand(): String = "mvn clean compile package"

    @Deprecated("请使用 builder.build 返回的 BuildResult.artifacts")
    @Suppress("DEPRECATION")
    override fun isFinalProduct(file: File): Boolean =
        file.isFile && file.name.endsWith(".jar") && cn.hubbo.utils.FileUtils.isExecutableJar(file)

    // ==================== 新版能力 ====================

    override val syntaxChecker: SyntaxChecker = object : SyntaxChecker {
        override val supportedLanguages: Set<Language> = setOf(Language.JAVA, Language.KOTLIN)

        override suspend fun check(ctx: PipelineContext, options: SyntaxCheckOptions): SyntaxCheckResult {
            val result = CommandLineUtils.execute("mvn validate -B", workingDirectory = devOpsConfiguration.projectDirectory())
            val passed = result.exitCode == 0
            return SyntaxCheckResult(
                passed = passed,
                errors = if (passed) {
                    emptyList()
                } else {
                    listOf(SyntaxError("<maven>", 0, 0, result.output ?: "mvn validate failed"))
                },
                filesChecked = 0,
                duration = Duration.ZERO,
            )
        }
    }

    override val tester: Tester = object : Tester {
        override val supportedTypes: Set<TestType> = setOf(TestType.UNIT, TestType.INTEGRATION)

        override suspend fun test(ctx: PipelineContext, options: TestOptions): TestReport {
            val command = when (options.type) {
                TestType.UNIT -> "mvn test -B"
                TestType.INTEGRATION -> "mvn verify -B"
                else -> throw Errors.unsupported(Stage.TEST, "test type ${options.type}")
            }
            val result = CommandLineUtils.execute(command, workingDirectory = devOpsConfiguration.projectDirectory())
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
            val result = CommandLineUtils.execute("mvn clean compile -B", workingDirectory = devOpsConfiguration.projectDirectory())
            if (result.exitCode != 0) {
                throw DevOpsError.recoverable(ErrorCode.COMPILE_FAILED, Stage.COMPILE, result.output ?: "mvn compile failed")
            }
            return CompileResult(outputs = emptyList(), warnings = emptyList(), duration = Duration.ZERO)
        }
    }

    override val builder: Builder = object : Builder {
        override val supportedTools: Set<BuildTool> = setOf(BuildTool.MAVEN)

        override suspend fun build(ctx: PipelineContext, options: BuildOptions): BuildResult {
            val result = CommandLineUtils.execute("mvn package -B -e", workingDirectory = devOpsConfiguration.projectDirectory())
            if (result.exitCode != 0) {
                throw DevOpsError.recoverable(ErrorCode.BUILD_FAILED, Stage.BUILD, result.output ?: "mvn package failed")
            }
            val artifacts = captureProduct().map { file ->
                val path = file.toPath()
                Artifact(
                    name = file.name,
                    type = options.artifactType,
                    file = path,
                    checksum = DigestUtils.sha256Hex(Files.newInputStream(path)),
                    sizeBytes = Files.size(path),
                )
            }
            return BuildResult(artifacts = artifacts, duration = Duration.ZERO)
        }
    }
}
