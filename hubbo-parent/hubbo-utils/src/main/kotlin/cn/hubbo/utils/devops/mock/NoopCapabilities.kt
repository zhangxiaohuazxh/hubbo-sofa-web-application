package cn.hubbo.utils.devops.mock

import cn.hubbo.utils.devops.capability.ArtifactManager
import cn.hubbo.utils.devops.capability.Builder
import cn.hubbo.utils.devops.capability.Compiler
import cn.hubbo.utils.devops.capability.Deployer
import cn.hubbo.utils.devops.capability.EnvironmentManager
import cn.hubbo.utils.devops.capability.Linter
import cn.hubbo.utils.devops.capability.MetricsSink
import cn.hubbo.utils.devops.capability.NoopMetrics
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
import cn.hubbo.utils.devops.config.RevisionSpec
import cn.hubbo.utils.devops.config.SyntaxCheckOptions
import cn.hubbo.utils.devops.config.TestOptions
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.error.DevOpsError
import cn.hubbo.utils.devops.core.error.ErrorCode
import cn.hubbo.utils.devops.core.model.AnalysisReport
import cn.hubbo.utils.devops.core.model.Artifact
import cn.hubbo.utils.devops.core.model.ArtifactReference
import cn.hubbo.utils.devops.core.model.ArtifactRepository
import cn.hubbo.utils.devops.core.model.BuildResult
import cn.hubbo.utils.devops.core.model.CheckoutResult
import cn.hubbo.utils.devops.core.model.CompileResult
import cn.hubbo.utils.devops.core.model.Coordinates
import cn.hubbo.utils.devops.core.model.DependencyScanReport
import cn.hubbo.utils.devops.core.model.DeployResult
import cn.hubbo.utils.devops.core.model.Environment
import cn.hubbo.utils.devops.core.model.GateEvidence
import cn.hubbo.utils.devops.core.model.GateResult
import cn.hubbo.utils.devops.core.model.HealthStatus
import cn.hubbo.utils.devops.core.model.LintReport
import cn.hubbo.utils.devops.core.model.NotificationEvent
import cn.hubbo.utils.devops.core.model.PipelineDefinition
import cn.hubbo.utils.devops.core.model.PipelineRun
import cn.hubbo.utils.devops.core.model.Report
import cn.hubbo.utils.devops.core.model.ReportData
import cn.hubbo.utils.devops.core.model.ReportFormat
import cn.hubbo.utils.devops.core.model.ResolvedEnvironment
import cn.hubbo.utils.devops.core.model.Revision
import cn.hubbo.utils.devops.core.model.Secret
import cn.hubbo.utils.devops.core.model.SyntaxCheckResult
import cn.hubbo.utils.devops.core.model.TestReport
import cn.hubbo.utils.devops.core.model.VcsType
import cn.hubbo.utils.devops.extension.HookPayload
import cn.hubbo.utils.devops.extension.HookRegistry
import cn.hubbo.utils.devops.extension.HookPoint
import cn.hubbo.utils.devops.extension.Hook
import cn.hubbo.utils.devops.extension.NoopHookRegistry
import java.nio.file.Path

/**
 * 各能力接口的空实现（No-op）。
 *
 * 用途：
 * - 作为 [cn.hubbo.utils.devops.DevOps] 接口的默认能力兜底，保证「只实现部分能力的调用方
 *   无需为其余能力提供实现」；
 * - 对核心能力采用 fail-fast（调用未装配的能力时抛 [DevOpsError]，UNSUPPORTED_FEATURE），
 *   避免静默吞掉错误导致流水线「看起来成功」；
 * - 对副作用型能力（[MetricsSink]、[HookRegistry]）使用真正的空实现，避免打断调用方。
 */
object NoopCapabilities {

    private fun unsupported(name: String, stage: Stage): Nothing =
        throw DevOpsError.fatal(
            ErrorCode.UNSUPPORTED_FEATURE,
            stage,
            "No $name implementation has been registered. " +
                "Provide one via DevOps.builder().${
                    name.replaceFirstChar { it.lowercase() }
                }(...) or bind a plugin.",
        )

    /** 供组合装配在未绑定实现时使用。 */
    fun <T : Any> required(type: Class<T>): T = unsupported(type.simpleName, Stage.REPORT)

    val sourceManager: SourceManager = object : SourceManager {
        override val supportedVcs: Set<VcsType> = emptySet()
        override suspend fun clone(ctx: PipelineContext, options: CloneOptions): CheckoutResult = unsupported("SourceManager", Stage.CLONE)
        override suspend fun checkout(ctx: PipelineContext, options: CloneOptions, revision: Revision): CheckoutResult = unsupported("SourceManager", Stage.CLONE)
        override suspend fun resolveRevision(ctx: PipelineContext, options: CloneOptions, spec: RevisionSpec): Revision = unsupported("SourceManager", Stage.CLONE)
        override suspend fun clean(ctx: PipelineContext, workspace: Path) = unsupported("SourceManager", Stage.CLONE)
    }

    val syntaxChecker: SyntaxChecker = object : SyntaxChecker {
        override val supportedLanguages: Set<cn.hubbo.utils.devops.core.model.Language> = emptySet()
        override suspend fun check(ctx: PipelineContext, options: SyntaxCheckOptions): SyntaxCheckResult = unsupported("SyntaxChecker", Stage.SYNTAX_CHECK)
    }

    val tester: Tester = object : Tester {
        override val supportedTypes: Set<cn.hubbo.utils.devops.core.model.TestType> = emptySet()
        override suspend fun test(ctx: PipelineContext, options: TestOptions): TestReport = unsupported("Tester", Stage.TEST)
    }

    val linter: Linter = object : Linter {
        override val supportedTools: Set<String> = emptySet()
        override suspend fun lint(ctx: PipelineContext, options: LintOptions): LintReport = unsupported("Linter", Stage.LINT)
    }

    val qualityGate: QualityGate = object : QualityGate {
        override suspend fun evaluate(ctx: PipelineContext, gate: GateConfig, evidence: GateEvidence): GateResult = unsupported("QualityGate", Stage.QUALITY_GATE)
    }

    val compiler: Compiler = object : Compiler {
        override suspend fun compile(ctx: PipelineContext, options: CompileOptions): CompileResult = unsupported("Compiler", Stage.COMPILE)
    }

    val builder: Builder = object : Builder {
        override val supportedTools: Set<cn.hubbo.utils.devops.config.BuildTool> = emptySet()
        override suspend fun build(ctx: PipelineContext, options: BuildOptions): BuildResult = unsupported("Builder", Stage.BUILD)
    }

    val staticAnalyzer: StaticAnalyzer = object : StaticAnalyzer {
        override suspend fun analyze(ctx: PipelineContext, options: AnalysisOptions): AnalysisReport = unsupported("StaticAnalyzer", Stage.STATIC_ANALYSIS)
        override suspend fun scanDependencies(ctx: PipelineContext, options: AnalysisOptions): DependencyScanReport = unsupported("StaticAnalyzer", Stage.STATIC_ANALYSIS)
    }

    val artifactManager: ArtifactManager = object : ArtifactManager {
        override suspend fun upload(ctx: PipelineContext, artifact: Artifact, repository: ArtifactRepository): ArtifactReference = unsupported("ArtifactManager", Stage.ARTIFACT_UPLOAD)
        override suspend fun download(ctx: PipelineContext, reference: ArtifactReference, targetDirectory: Path): Artifact = unsupported("ArtifactManager", Stage.ARTIFACT_UPLOAD)
        override suspend fun listVersions(ctx: PipelineContext, coordinates: Coordinates, repository: String): List<String> = unsupported("ArtifactManager", Stage.ARTIFACT_UPLOAD)
        override suspend fun promote(ctx: PipelineContext, reference: ArtifactReference, targetRepository: String): ArtifactReference = unsupported("ArtifactManager", Stage.ARTIFACT_UPLOAD)
        override suspend fun delete(ctx: PipelineContext, reference: ArtifactReference) = unsupported("ArtifactManager", Stage.ARTIFACT_UPLOAD)
    }

    val deployer: Deployer = object : Deployer {
        override suspend fun deploy(ctx: PipelineContext, options: DeployOptions): DeployResult = unsupported("Deployer", Stage.DEPLOY)
        override suspend fun rollback(ctx: PipelineContext, deploymentId: String, environment: Environment): DeployResult = unsupported("Deployer", Stage.DEPLOY)
        override suspend fun status(ctx: PipelineContext, deploymentId: String): DeployResult = unsupported("Deployer", Stage.DEPLOY)
        override suspend fun healthCheck(ctx: PipelineContext, deploymentId: String): HealthStatus = unsupported("Deployer", Stage.DEPLOY)
    }

    val orchestrator: PipelineOrchestrator = object : PipelineOrchestrator {
        override suspend fun run(ctx: PipelineContext, pipeline: PipelineDefinition): PipelineRun = unsupported("PipelineOrchestrator", Stage.CLONE)
        override suspend fun cancel(ctx: PipelineContext, runId: String) = unsupported("PipelineOrchestrator", Stage.CLONE)
        override suspend fun retry(ctx: PipelineContext, runId: String, fromStage: String?): PipelineRun = unsupported("PipelineOrchestrator", Stage.CLONE)
        override suspend fun pause(ctx: PipelineContext, runId: String) = unsupported("PipelineOrchestrator", Stage.CLONE)
        override suspend fun resume(ctx: PipelineContext, runId: String) = unsupported("PipelineOrchestrator", Stage.CLONE)
        override fun status(ctx: PipelineContext, runId: String): PipelineRun? = null
        override fun attach(observer: cn.hubbo.utils.devops.capability.PipelineObserver): cn.hubbo.utils.devops.capability.CancellableSubscription =
            cn.hubbo.utils.devops.capability.CancellableSubscription {}
    }

    val notifier: Notifier = object : Notifier {
        override suspend fun notify(ctx: PipelineContext, event: NotificationEvent) {
            ctx.logger.warn("Notifier not configured; dropping notification [{}] {}", event.channel, event.subject)
        }
    }

    val reporter: Reporter = object : Reporter {
        override suspend fun generate(ctx: PipelineContext, data: ReportData, format: ReportFormat, targetDirectory: Path): Report = unsupported("Reporter", Stage.REPORT)
    }

    val environmentManager: EnvironmentManager = object : EnvironmentManager {
        override suspend fun resolve(ctx: PipelineContext, environment: Environment): ResolvedEnvironment = unsupported("EnvironmentManager", Stage.DEPLOY)
        override suspend fun secrets(ctx: PipelineContext, environment: Environment, keys: Set<String>): Map<String, Secret> = unsupported("EnvironmentManager", Stage.DEPLOY)
        override suspend fun inject(ctx: PipelineContext, template: Path, environment: Environment): Path = unsupported("EnvironmentManager", Stage.DEPLOY)
    }

    val metrics: MetricsSink = NoopMetrics

    val hookRegistry: HookRegistry = NoopHookRegistry
}
