package cn.hubbo.utils.devops.mock

import cn.hubbo.utils.devops.DevOps
import cn.hubbo.utils.devops.DevOpsConfiguration
import cn.hubbo.utils.devops.LocalStorageInfo
import cn.hubbo.utils.devops.capability.ArtifactManager
import cn.hubbo.utils.devops.capability.Builder
import cn.hubbo.utils.devops.capability.Compiler
import cn.hubbo.utils.devops.capability.Deployer
import cn.hubbo.utils.devops.capability.EnvironmentManager
import cn.hubbo.utils.devops.capability.Linter
import cn.hubbo.utils.devops.capability.MetricsSink
import cn.hubbo.utils.devops.capability.Notifier
import cn.hubbo.utils.devops.capability.PipelineOrchestrator
import cn.hubbo.utils.devops.capability.QualityGate
import cn.hubbo.utils.devops.capability.Reporter
import cn.hubbo.utils.devops.capability.SourceManager
import cn.hubbo.utils.devops.capability.StaticAnalyzer
import cn.hubbo.utils.devops.capability.SyntaxChecker
import cn.hubbo.utils.devops.capability.Tester
import cn.hubbo.utils.devops.config.BuildOptions
import cn.hubbo.utils.devops.config.BuildTool
import cn.hubbo.utils.devops.config.CloneOptions
import cn.hubbo.utils.devops.config.CompileOptions
import cn.hubbo.utils.devops.config.SyntaxCheckOptions
import cn.hubbo.utils.devops.config.TestOptions
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.PipelineContexts
import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.model.ArtifactType
import cn.hubbo.utils.devops.core.model.Language
import cn.hubbo.utils.devops.extension.HookRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 测试用 [DevOps] 装配。
 *
 * 全部能力使用内存 Mock，便于单元测试调用方逻辑而不依赖外部工具。
 * 旧版命令式方法也被重定向到 Mock 能力，避免默认实现真正执行 git / mvn。
 */
class MockDevOps(
    private val configuration: DevOpsConfiguration = DevOpsConfiguration(url = "mock://repo.git"),
) : DevOps {

    override val sourceManager: SourceManager = MockSourceManager()
    override val syntaxChecker: SyntaxChecker = MockSyntaxChecker()
    override val tester: Tester = MockTester()
    override val linter: Linter = MockLinter()
    override val qualityGate: QualityGate = MockQualityGate()
    override val compiler: Compiler = MockCompiler()
    override val builder: Builder = MockBuilder()
    override val staticAnalyzer: StaticAnalyzer = MockStaticAnalyzer()
    override val artifactManager: ArtifactManager = MockArtifactManager()
    override val deployer: Deployer = MockDeployer()
    override val orchestrator: PipelineOrchestrator = MockOrchestrator()
    override val notifier: Notifier = MockNotifier()
    override val reporter: Reporter = MockReporter()
    override val environmentManager: EnvironmentManager = MockEnvironmentManager()
    override val metrics: MetricsSink = InMemoryMetrics()
    override val hooks: HookRegistry = InMemoryHookRegistry()

    override fun getDevOpsConfiguration(): DevOpsConfiguration = configuration

    override fun getLogger(): Logger = LoggerFactory.getLogger(MockDevOps::class.java)

    private fun newContext(stage: Stage = Stage.CLONE): PipelineContext = PipelineContexts.default(
        pipelineName = configuration.projectName,
        runId = "mock-run",
        stage = stage,
        metrics = metrics,
        workingDirectory = configuration.projectDirectory().toPath(),
    )

    // ---- 旧版命令式方法（重定向到 Mock 能力） ----

    @Deprecated("use capability methods")
    override suspend fun clone(): LocalStorageInfo {
        sourceManager.clone(newContext(), CloneOptions(configuration.url, targetDirectory = configuration.projectDirectory().toPath()))
        return LocalStorageInfo(configuration.url, configuration.projectDirectory())
    }

    @Deprecated("use capability methods")
    override suspend fun check() {
        syntaxChecker.check(
            newContext(Stage.SYNTAX_CHECK),
            SyntaxCheckOptions(Language.NONE, listOf(configuration.projectDirectory().toPath())),
        )
    }

    @Deprecated("use capability methods")
    override suspend fun compile() {
        compiler.compile(
            newContext(Stage.COMPILE),
            CompileOptions(outputDirectory = configuration.projectDirectory().toPath().resolve("build")),
        )
    }

    @Deprecated("use capability methods")
    override suspend fun build() {
        builder.build(newContext(Stage.BUILD), BuildOptions(BuildTool.MAVEN, ArtifactType.JAR))
    }

    @Deprecated("use capability methods")
    override suspend fun test() {
        tester.test(newContext(Stage.TEST), TestOptions())
    }
}
