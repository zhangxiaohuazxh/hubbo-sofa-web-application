package cn.hubbo.utils.devops

import cn.hubbo.utils.CommandLineUtils
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
import cn.hubbo.utils.devops.config.AnalysisOptions
import cn.hubbo.utils.devops.config.BuildOptions
import cn.hubbo.utils.devops.config.CloneOptions
import cn.hubbo.utils.devops.config.CompileOptions
import cn.hubbo.utils.devops.config.DeployOptions
import cn.hubbo.utils.devops.config.GateConfig
import cn.hubbo.utils.devops.config.LintOptions
import cn.hubbo.utils.devops.config.SyntaxCheckOptions
import cn.hubbo.utils.devops.config.TestOptions
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.model.AnalysisReport
import cn.hubbo.utils.devops.core.model.Artifact
import cn.hubbo.utils.devops.core.model.ArtifactReference
import cn.hubbo.utils.devops.core.model.ArtifactRepository
import cn.hubbo.utils.devops.core.model.BuildResult
import cn.hubbo.utils.devops.core.model.CheckoutResult
import cn.hubbo.utils.devops.core.model.CompileResult
import cn.hubbo.utils.devops.core.model.DeployResult
import cn.hubbo.utils.devops.core.model.DependencyScanReport
import cn.hubbo.utils.devops.core.model.GateEvidence
import cn.hubbo.utils.devops.core.model.GateResult
import cn.hubbo.utils.devops.core.model.LintReport
import cn.hubbo.utils.devops.core.model.NotificationEvent
import cn.hubbo.utils.devops.core.model.PipelineDefinition
import cn.hubbo.utils.devops.core.model.PipelineRun
import cn.hubbo.utils.devops.core.model.Report
import cn.hubbo.utils.devops.core.model.ReportData
import cn.hubbo.utils.devops.core.model.ReportFormat
import cn.hubbo.utils.devops.core.model.SyntaxCheckResult
import cn.hubbo.utils.devops.core.model.TestReport
import cn.hubbo.utils.devops.extension.HookRegistry
import cn.hubbo.utils.devops.impl.DevOpsBuilder
import cn.hubbo.utils.devops.mock.NoopCapabilities
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

/**
 * DevOps 能力门面。
 *
 * 设计决策：
 * - **组合而非继承**：将全生命周期拆分为若干细粒度子接口
 *   （[SourceManager]、[SyntaxChecker]、[Tester]、[Linter]、[Compiler]、[Builder]、
 *   [StaticAnalyzer]、[QualityGate]、[ArtifactManager]、[Deployer]、
 *   [PipelineOrchestrator]、[Notifier]、[Reporter]、[EnvironmentManager]），
 *   [DevOps] 仅负责聚合与默认委托，遵循接口隔离原则，避免「胖接口」；
 * - **版本演进**：保留旧版命令式方法并标记 [Deprecated] 作为过渡，新代码一律走能力接口；
 *   可选能力通过 `supportedXxx` 集合与 `is` 智能转换探测，不破坏向后兼容；
 * - **可配置性**：所有行为由 [DevOpsConfiguration] 与各 Options 配置对象参数化，而非硬编码；
 * - **上下文传递**：每个能力方法均接收 [PipelineContext]（等价 Go context.Context），
 *   携带取消 / 超时、日志、指标与阶段信息；
 * - **并发安全**：实现必须线程安全；[PipelineContext.attributes] 为并发安全共享状态，
 *   支持并行执行多个流水线。
 */
interface DevOps {

    // ==================== 能力接口（核心入口） ====================

    val sourceManager: SourceManager get() = NoopCapabilities.sourceManager
    val syntaxChecker: SyntaxChecker get() = NoopCapabilities.syntaxChecker
    val tester: Tester get() = NoopCapabilities.tester
    val linter: Linter get() = NoopCapabilities.linter
    val qualityGate: QualityGate get() = NoopCapabilities.qualityGate
    val compiler: Compiler get() = NoopCapabilities.compiler
    val builder: Builder get() = NoopCapabilities.builder
    val staticAnalyzer: StaticAnalyzer get() = NoopCapabilities.staticAnalyzer
    val artifactManager: ArtifactManager get() = NoopCapabilities.artifactManager
    val deployer: Deployer get() = NoopCapabilities.deployer
    val orchestrator: PipelineOrchestrator get() = NoopCapabilities.orchestrator
    val notifier: Notifier get() = NoopCapabilities.notifier
    val reporter: Reporter get() = NoopCapabilities.reporter
    val environmentManager: EnvironmentManager get() = NoopCapabilities.environmentManager
    val metrics: MetricsSink get() = NoopCapabilities.metrics
    val hooks: HookRegistry get() = NoopCapabilities.hookRegistry

    // ==================== 新能力便捷方法（默认委托给能力接口） ====================

    suspend fun clone(ctx: PipelineContext, options: CloneOptions): CheckoutResult = sourceManager.clone(ctx, options)

    suspend fun checkSyntax(ctx: PipelineContext, options: SyntaxCheckOptions): SyntaxCheckResult = syntaxChecker.check(ctx, options)

    suspend fun lint(ctx: PipelineContext, options: LintOptions): LintReport = linter.lint(ctx, options)

    suspend fun test(ctx: PipelineContext, options: TestOptions): TestReport = tester.test(ctx, options)

    suspend fun analyze(ctx: PipelineContext, options: AnalysisOptions): AnalysisReport = staticAnalyzer.analyze(ctx, options)

    suspend fun scanDependencies(ctx: PipelineContext, options: AnalysisOptions): DependencyScanReport =
        staticAnalyzer.scanDependencies(ctx, options)

    suspend fun compile(ctx: PipelineContext, options: CompileOptions): CompileResult = compiler.compile(ctx, options)

    suspend fun build(ctx: PipelineContext, options: BuildOptions): BuildResult = builder.build(ctx, options)

    suspend fun evaluateGate(ctx: PipelineContext, gate: GateConfig, evidence: GateEvidence): GateResult =
        qualityGate.evaluate(ctx, gate, evidence)

    suspend fun uploadArtifact(ctx: PipelineContext, artifact: Artifact, repository: ArtifactRepository): ArtifactReference =
        artifactManager.upload(ctx, artifact, repository)

    suspend fun deploy(ctx: PipelineContext, options: DeployOptions): DeployResult = deployer.deploy(ctx, options)

    suspend fun runPipeline(ctx: PipelineContext, pipeline: PipelineDefinition): PipelineRun = orchestrator.run(ctx, pipeline)

    suspend fun notify(ctx: PipelineContext, event: NotificationEvent) = notifier.notify(ctx, event)

    suspend fun generateReport(ctx: PipelineContext, data: ReportData, format: ReportFormat, targetDirectory: Path): Report =
        reporter.generate(ctx, data, format, targetDirectory)

    // ==================== 旧版兼容方法（已弃用，仅作过渡） ====================

    @Deprecated("请改用能力接口：sourceManager.clone(ctx, CloneOptions)")
    suspend fun clone(): LocalStorageInfo {
        val configuration = getDevOpsConfiguration()
        val result = CommandLineUtils.execute("rm -rf ${configuration.projectDirectory()} && git clone ${configuration.url}")
        getLogger().info("执行结果 {} {}", result.exitCode, result.output)
        return LocalStorageInfo(configuration.url, configuration.projectDirectory())
    }

    @Deprecated("请改用能力接口：syntaxChecker.check(ctx, SyntaxCheckOptions)")
    suspend fun check() {
        // 保留为 no-op 以兼容既有实现
    }

    @Deprecated("请改用能力接口：tester.test(ctx, TestOptions)")
    suspend fun test() {
        // 保留为 no-op 以兼容既有实现
    }

    @Deprecated("请改用能力接口：compiler.compile(ctx, CompileOptions)")
    suspend fun compileCommand(): String = ""

    @Deprecated("请改用能力接口：compiler.compile(ctx, CompileOptions)")
    suspend fun compile() {
        val logger = getLogger()
        logger.info("==========================开始编译==========================")
        clean()
        CommandLineUtils.execute(compileCommand(), workingDirectory = getDevOpsConfiguration().projectDirectory())
        logger.info("==========================编译完成==========================")
    }

    @Deprecated("请改用能力接口：builder.build(ctx, BuildOptions)")
    suspend fun build() {
        // 保留为 no-op 以兼容既有实现
    }

    @Deprecated("请改用能力接口与 PipelineContext 的显式清理语义")
    suspend fun clean() {
        getLogger().info("清理旧的编译产物")
    }

    @Deprecated("请改用 builder.build 返回的 BuildResult.artifacts")
    fun isFinalProduct(file: File): Boolean = false

    @Deprecated("请改用 builder.build 返回的 BuildResult.artifacts")
    suspend fun captureProduct(): List<File> {
        val files = FileUtils.listFiles(
            getDevOpsConfiguration().projectDirectory(),
            FileFileFilter.INSTANCE,
            TrueFileFilter.INSTANCE,
        )
        return files.filter { isFinalProduct(it) }
    }

    /** 旧版全局配置；新代码建议直接为各能力传入 Options。 */
    fun getDevOpsConfiguration(): DevOpsConfiguration = DevOpsConfiguration.DEFAULT

    fun getLogger(): Logger = LoggerFactory.getLogger(DevOps::class.java)

    companion object {
        /** 组合装配入口：DevOps.builder().sourceManager(...).build()。 */
        fun builder(): DevOpsBuilder = DevOpsBuilder()
    }
}
