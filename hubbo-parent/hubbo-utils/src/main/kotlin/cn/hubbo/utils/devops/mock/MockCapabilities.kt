package cn.hubbo.utils.devops.mock

import cn.hubbo.utils.devops.capability.ArtifactManager
import cn.hubbo.utils.devops.capability.Builder
import cn.hubbo.utils.devops.capability.CancellableSubscription
import cn.hubbo.utils.devops.capability.Compiler
import cn.hubbo.utils.devops.capability.Deployer
import cn.hubbo.utils.devops.capability.EnvironmentManager
import cn.hubbo.utils.devops.capability.Linter
import cn.hubbo.utils.devops.capability.MetricsSink
import cn.hubbo.utils.devops.capability.Notifier
import cn.hubbo.utils.devops.capability.PipelineObserver
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
import cn.hubbo.utils.devops.core.model.AnalysisIssue
import cn.hubbo.utils.devops.core.model.AnalysisReport
import cn.hubbo.utils.devops.core.model.Artifact
import cn.hubbo.utils.devops.config.BuildTool
import cn.hubbo.utils.devops.core.model.ArtifactReference
import cn.hubbo.utils.devops.core.model.ArtifactRepository
import cn.hubbo.utils.devops.core.model.BuildResult
import cn.hubbo.utils.devops.core.model.CheckoutResult
import cn.hubbo.utils.devops.core.model.ComparisonOperator
import cn.hubbo.utils.devops.core.model.ComplexityMetrics
import cn.hubbo.utils.devops.core.model.CompileResult
import cn.hubbo.utils.devops.core.model.Coordinates
import cn.hubbo.utils.devops.core.model.CoverageResult
import cn.hubbo.utils.devops.core.model.DependencyScanReport
import cn.hubbo.utils.devops.core.model.DeployResult
import cn.hubbo.utils.devops.core.model.DeployStatus
import cn.hubbo.utils.devops.core.model.DeployStrategy
import cn.hubbo.utils.devops.core.model.Environment
import cn.hubbo.utils.devops.core.model.GateAction
import cn.hubbo.utils.devops.core.model.GateCheckResult
import cn.hubbo.utils.devops.core.model.GateDimension
import cn.hubbo.utils.devops.core.model.GateEvidence
import cn.hubbo.utils.devops.core.model.GateResult
import cn.hubbo.utils.devops.core.model.HealthStatus
import cn.hubbo.utils.devops.core.model.InfrastructureSpec
import cn.hubbo.utils.devops.core.model.Language
import cn.hubbo.utils.devops.core.model.LintReport
import cn.hubbo.utils.devops.core.model.NotificationEvent
import cn.hubbo.utils.devops.core.model.PipelineDefinition
import cn.hubbo.utils.devops.core.model.PipelineRun
import cn.hubbo.utils.devops.core.model.PipelineStatus
import cn.hubbo.utils.devops.core.model.Report
import cn.hubbo.utils.devops.core.model.ReportData
import cn.hubbo.utils.devops.core.model.ReportFormat
import cn.hubbo.utils.devops.core.model.ResolvedEnvironment
import cn.hubbo.utils.devops.core.model.Revision
import cn.hubbo.utils.devops.core.model.RevisionType
import cn.hubbo.utils.devops.core.model.Secret
import cn.hubbo.utils.devops.core.model.SyntaxCheckResult
import cn.hubbo.utils.devops.core.model.TestCase
import cn.hubbo.utils.devops.core.model.TestReport
import cn.hubbo.utils.devops.core.model.TestStatus
import cn.hubbo.utils.devops.core.model.TestType
import cn.hubbo.utils.devops.core.model.VcsType
import cn.hubbo.utils.devops.extension.Hook
import cn.hubbo.utils.devops.extension.HookPayload
import cn.hubbo.utils.devops.extension.HookPoint
import cn.hubbo.utils.devops.extension.HookRegistry
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 内存 Mock 能力实现。
 *
 * 用于单元测试调用方逻辑：无需真实 Git / Maven / K8s，行为确定、状态可观测。
 * 每个 Mock 都是普通类，可按需注入共享状态（如记录已发送的通知）。
 */

class MockSourceManager(
    override val supportedVcs: Set<VcsType> = setOf(VcsType.GIT),
) : SourceManager {
    val clonedUrls: MutableList<String> = CopyOnWriteArrayList()

    override suspend fun clone(ctx: PipelineContext, options: CloneOptions): CheckoutResult {
        val workspace = options.targetDirectory
            ?: ctx.workingDirectory.resolve(options.repositoryUrl.substringAfterLast('/'))
        Files.createDirectories(workspace)
        clonedUrls += options.repositoryUrl
        val revision = when (val spec = options.revision) {
            is RevisionSpec.Branch -> Revision(spec.name, RevisionType.BRANCH, "mock-${spec.name}")
            is RevisionSpec.Tag -> Revision(spec.name, RevisionType.TAG, "mock-${spec.name}")
            is RevisionSpec.Commit -> Revision(spec.hash, RevisionType.COMMIT, spec.hash)
            RevisionSpec.Default -> Revision("default", RevisionType.BRANCH, "mock-head")
        }
        return CheckoutResult(workspace, revision, options.vcs)
    }

    override suspend fun checkout(ctx: PipelineContext, options: CloneOptions, revision: Revision): CheckoutResult =
        clone(ctx, options.copy(revision = RevisionSpec.Commit(revision.commitHash ?: revision.ref)))

    override suspend fun resolveRevision(ctx: PipelineContext, options: CloneOptions, spec: RevisionSpec): Revision = when (spec) {
        is RevisionSpec.Branch -> Revision(spec.name, RevisionType.BRANCH, "mock-${spec.name}")
        is RevisionSpec.Tag -> Revision(spec.name, RevisionType.TAG, "mock-${spec.name}")
        is RevisionSpec.Commit -> Revision(spec.hash, RevisionType.COMMIT, spec.hash)
        RevisionSpec.Default -> Revision("default", RevisionType.BRANCH, "mock-head")
    }

    override suspend fun clean(ctx: PipelineContext, workspace: Path) {
        Files.deleteIfExists(workspace)
    }
}

class MockSyntaxChecker : SyntaxChecker {
    override val supportedLanguages: Set<Language> = Language.entries.toSet()

    override suspend fun check(ctx: PipelineContext, options: SyntaxCheckOptions): SyntaxCheckResult =
        SyntaxCheckResult(
            passed = true,
            errors = emptyList(),
            filesChecked = options.paths.size,
            duration = Duration.ofMillis(5),
        )
}

class MockTester : Tester {
    override val supportedTypes: Set<TestType> = TestType.entries.toSet()

    override suspend fun test(ctx: PipelineContext, options: TestOptions): TestReport = TestReport(
        type = options.type,
        total = 10,
        passed = 10,
        failed = 0,
        skipped = 0,
        flaky = 0,
        duration = Duration.ofMillis(120),
        coverage = if (options.coverageEnabled) CoverageResult(lineCoverage = options.coverageThreshold ?: 0.85) else null,
        testCases = (1..10).map { TestCase("test$it", "com.example.MockTest", TestStatus.PASSED, 10) },
    )
}

class MockLinter(
    override val supportedTools: Set<String> = setOf("mock-lint"),
) : Linter {
    override suspend fun lint(ctx: PipelineContext, options: LintOptions): LintReport =
        LintReport(tool = options.tool ?: "mock-lint", violations = emptyList(), filesScanned = 0, duration = Duration.ZERO)
}

class MockCompiler : Compiler {
    override suspend fun compile(ctx: PipelineContext, options: CompileOptions): CompileResult {
        val out = options.outputDirectory ?: ctx.workingDirectory.resolve("build/classes")
        Files.createDirectories(out)
        val marker = out.resolve("mock.class")
        if (!Files.exists(marker)) Files.createFile(marker)
        return CompileResult(outputs = listOf(marker), warnings = emptyList(), duration = Duration.ofMillis(50), platform = options.targetPlatform)
    }
}

class MockBuilder(
    override val supportedTools: Set<BuildTool> = BuildTool.entries.toSet(),
) : Builder {
    override suspend fun build(ctx: PipelineContext, options: BuildOptions): BuildResult {
        val out = options.outputDirectory ?: ctx.workingDirectory.resolve("dist")
        Files.createDirectories(out)
        val file = out.resolve("app.jar")
        if (!Files.exists(file)) Files.writeString(file, "mock artifact")
        return BuildResult(
            artifacts = listOf(Artifact("app.jar", options.artifactType, file, checksum = "deadbeef", sizeBytes = Files.size(file))),
            duration = Duration.ofMillis(100),
        )
    }
}

class MockStaticAnalyzer : StaticAnalyzer {
    override suspend fun analyze(ctx: PipelineContext, options: AnalysisOptions): AnalysisReport = AnalysisReport(
        tool = "mock-sast",
        issues = emptyList(),
        complexityMetrics = ComplexityMetrics(cyclomaticComplexity = 3.0, cognitiveComplexity = 4.0, linesOfCode = 100, commentLines = 10),
        duration = Duration.ZERO,
    )

    override suspend fun scanDependencies(ctx: PipelineContext, options: AnalysisOptions): DependencyScanReport =
        DependencyScanReport(dependencies = emptyList(), scannedCount = 0, duration = Duration.ZERO)
}

class MockQualityGate : QualityGate {
    override suspend fun evaluate(ctx: PipelineContext, gate: GateConfig, evidence: GateEvidence): GateResult {
        val checks = gate.rules.map { rule ->
            val actual = when (rule.dimension) {
                GateDimension.COVERAGE -> evidence.coverage?.lineCoverage ?: 0.0
                GateDimension.LINT_ERRORS -> evidence.lintReport?.violations?.size?.toDouble() ?: 0.0
                GateDimension.TEST_FAILURES -> evidence.testReport?.failed?.toDouble() ?: 0.0
                GateDimension.CRITICAL_VULNS -> evidence.analysisReport?.issues?.count { it.severity.name == "CRITICAL" }?.toDouble() ?: 0.0
                GateDimension.HIGH_VULNS -> evidence.analysisReport?.issues?.count { it.severity.name == "HIGH" }?.toDouble() ?: 0.0
                else -> evidence.extra[rule.dimension] ?: 0.0
            }
            val passed = compare(rule.operator, actual, rule.threshold)
            GateCheckResult(rule, actual, passed, if (passed) "OK" else "threshold not met (actual=$actual, expected $rule.operator ${rule.threshold})")
        }
        val blocking = checks.filter { !it.passed && it.rule.action == GateAction.BLOCK }
        return GateResult(passed = blocking.isEmpty(), checks = checks, blockingFailures = blocking)
    }

    private fun compare(op: ComparisonOperator, a: Double, b: Double): Boolean = when (op) {
        ComparisonOperator.GREATER_THAN -> a > b
        ComparisonOperator.GREATER_OR_EQUAL -> a >= b
        ComparisonOperator.LESS_THAN -> a < b
        ComparisonOperator.LESS_OR_EQUAL -> a <= b
        ComparisonOperator.EQUAL -> a == b
    }
}

class MockArtifactManager : ArtifactManager {
    private val store = ConcurrentHashMap<String, Artifact>()

    override suspend fun upload(ctx: PipelineContext, artifact: Artifact, repository: ArtifactRepository): ArtifactReference {
        val key = "${repository.name}/${artifact.file.fileName}"
        store[key] = artifact
        return ArtifactReference(
            coordinates = Coordinates("mock", artifact.name.substringBeforeLast('.'), "1.0.0"),
            repository = repository.name,
            storageKey = key,
        )
    }

    override suspend fun download(ctx: PipelineContext, reference: ArtifactReference, targetDirectory: Path): Artifact =
        store[reference.storageKey]
            ?: throw DevOpsError.fatal(ErrorCode.ARTIFACT_NOT_FOUND, Stage.ARTIFACT_UPLOAD, "mock artifact missing: ${reference.storageKey}")

    override suspend fun listVersions(ctx: PipelineContext, coordinates: Coordinates, repository: String): List<String> = listOf(coordinates.version)

    override suspend fun promote(ctx: PipelineContext, reference: ArtifactReference, targetRepository: String): ArtifactReference =
        reference.copy(repository = targetRepository)

    override suspend fun delete(ctx: PipelineContext, reference: ArtifactReference) {
        store.remove(reference.storageKey)
    }
}

class MockDeployer : Deployer {
    override suspend fun deploy(ctx: PipelineContext, options: DeployOptions): DeployResult = DeployResult(
        deploymentId = "mock-deploy-${options.environment.name.lowercase()}",
        environment = options.environment,
        strategy = options.strategy,
        targetUrl = "http://mock.local",
        startedAt = Instant.now(),
        finishedAt = Instant.now(),
        status = DeployStatus.SUCCEEDED,
    )

    override suspend fun rollback(ctx: PipelineContext, deploymentId: String, environment: Environment): DeployResult = DeployResult(
        deploymentId = deploymentId,
        environment = environment,
        strategy = DeployStrategy.ROLLING,
        startedAt = Instant.now(),
        finishedAt = Instant.now(),
        status = DeployStatus.ROLLED_BACK,
    )

    override suspend fun status(ctx: PipelineContext, deploymentId: String): DeployResult = DeployResult(
        deploymentId = deploymentId,
        environment = Environment.DEV,
        strategy = DeployStrategy.ROLLING,
        startedAt = Instant.now(),
        status = DeployStatus.IN_PROGRESS,
    )

    override suspend fun healthCheck(ctx: PipelineContext, deploymentId: String): HealthStatus = HealthStatus.HEALTHY
}

class MockOrchestrator : PipelineOrchestrator {
    private val runs = ConcurrentHashMap<String, PipelineRun>()

    override suspend fun run(ctx: PipelineContext, pipeline: PipelineDefinition): PipelineRun {
        val run = PipelineRun(
            runId = "run-${runs.size + 1}",
            definition = pipeline,
            status = PipelineStatus.SUCCEEDED,
            startedAt = Instant.now(),
            finishedAt = Instant.now(),
        )
        runs[run.runId] = run
        return run
    }

    override suspend fun cancel(ctx: PipelineContext, runId: String) {
        runs.computeIfPresent(runId) { _, r -> r.copy(status = PipelineStatus.CANCELLED) }
    }

    override suspend fun retry(ctx: PipelineContext, runId: String, fromStage: String?): PipelineRun {
        val original = runs[runId] ?: throw DevOpsError.fatal(ErrorCode.UNKNOWN, Stage.CLONE, "unknown run $runId")
        return run(ctx, original.definition)
    }

    override suspend fun pause(ctx: PipelineContext, runId: String) {
        runs.computeIfPresent(runId) { _, r -> r.copy(status = PipelineStatus.PAUSED) }
    }

    override suspend fun resume(ctx: PipelineContext, runId: String) {
        runs.computeIfPresent(runId) { _, r -> r.copy(status = PipelineStatus.RUNNING) }
    }

    override fun status(ctx: PipelineContext, runId: String): PipelineRun? = runs[runId]

    override fun attach(observer: PipelineObserver): CancellableSubscription = CancellableSubscription {}
}

class MockNotifier : Notifier {
    val sent: MutableList<NotificationEvent> = CopyOnWriteArrayList()

    override suspend fun notify(ctx: PipelineContext, event: NotificationEvent) {
        sent += event
    }
}

class MockReporter : Reporter {
    override suspend fun generate(ctx: PipelineContext, data: ReportData, format: ReportFormat, targetDirectory: Path): Report {
        Files.createDirectories(targetDirectory)
        val file = targetDirectory.resolve("${data.pipelineName}.${format.name.lowercase()}")
        if (!Files.exists(file)) Files.writeString(file, """{"pipeline":"${data.pipelineName}"}""")
        return Report(format, file, Files.size(file))
    }
}

class MockEnvironmentManager : EnvironmentManager {
    override suspend fun resolve(ctx: PipelineContext, environment: Environment): ResolvedEnvironment =
        ResolvedEnvironment(
            environment = environment,
            config = mapOf("ENV" to environment.name),
            infra = InfrastructureSpec(),
        )

    override suspend fun secrets(ctx: PipelineContext, environment: Environment, keys: Set<String>): Map<String, Secret> =
        keys.associateWith { Secret("mock-secret-$it") }

    override suspend fun inject(ctx: PipelineContext, template: Path, environment: Environment): Path {
        val target = template.resolveSibling(template.fileName.toString() + ".rendered")
        Files.writeString(target, Files.readString(template).replace("\${env}", environment.name))
        return target
    }
}

/** 内存指标：记录计数 / 计量 / 直方图，供测试断言。 */
class InMemoryMetrics : MetricsSink {
    val counters = ConcurrentHashMap<String, Long>()
    val gauges = ConcurrentHashMap<String, Double>()
    val histograms = ConcurrentHashMap<String, MutableList<Double>>()

    override fun counter(name: String, delta: Long, tags: Map<String, String>) {
        counters.merge(name, delta, Long::plus)
    }

    override fun gauge(name: String, value: Double, tags: Map<String, String>) {
        gauges[name] = value
    }

    override fun histogram(name: String, value: Double, tags: Map<String, String>) {
        histograms.computeIfAbsent(name) { CopyOnWriteArrayList() }.add(value)
    }
}

/** 内存钩子注册表：按触发点注册并排序执行。 */
class InMemoryHookRegistry : HookRegistry {
    private data class Registered(val name: String?, val order: Int, val hook: Hook)

    private val hooks = ConcurrentHashMap<HookPoint, MutableList<Registered>>()

    override fun register(hookPoint: HookPoint, hook: Hook, name: String?, order: Int) {
        hooks.computeIfAbsent(hookPoint) { CopyOnWriteArrayList() }.add(Registered(name, order, hook))
        hooks[hookPoint]?.sortBy { it.order }
    }

    override fun unregister(name: String): Boolean {
        var removed = false
        hooks.values.forEach { list ->
            removed = list.removeAll { it.name == name } || removed
        }
        return removed
    }

    override suspend fun fire(ctx: PipelineContext, hookPoint: HookPoint, payload: HookPayload) {
        hooks[hookPoint]?.forEach { it.hook.execute(ctx, payload) }
    }
}
