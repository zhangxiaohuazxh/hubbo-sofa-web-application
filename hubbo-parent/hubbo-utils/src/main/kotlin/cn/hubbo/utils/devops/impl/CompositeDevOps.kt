package cn.hubbo.utils.devops.impl

import cn.hubbo.utils.devops.DevOps
import cn.hubbo.utils.devops.DevOpsConfiguration
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
import cn.hubbo.utils.devops.extension.HookRegistry
import cn.hubbo.utils.devops.extension.NoopHookRegistry
import cn.hubbo.utils.devops.extension.Plugin
import cn.hubbo.utils.devops.extension.PluginRegistry
import cn.hubbo.utils.devops.mock.NoopCapabilities
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * 组合装配的 [DevOps] 实现。
 *
 * 将各能力接口注入并统一暴露，是「如何组合子接口」的参考实现：
 * - 未注入的能力回落到 [NoopCapabilities]（核心能力 fail-fast，副作用能力空实现）；
 * - 支持通过 [Plugin] 在构造期绑定 / 覆盖能力实现（插件最后绑定者胜出）。
 *
 * 生产环境可用 DI 框架或 [DevOpsBuilder] 构建本类。
 */
class CompositeDevOps(
    private val configuration: DevOpsConfiguration,
    private val delegateSourceManager: SourceManager? = null,
    private val delegateSyntaxChecker: SyntaxChecker? = null,
    private val delegateTester: Tester? = null,
    private val delegateLinter: Linter? = null,
    private val delegateQualityGate: QualityGate? = null,
    private val delegateCompiler: Compiler? = null,
    private val delegateBuilder: Builder? = null,
    private val delegateStaticAnalyzer: StaticAnalyzer? = null,
    private val delegateArtifactManager: ArtifactManager? = null,
    private val delegateDeployer: Deployer? = null,
    private val delegateOrchestrator: PipelineOrchestrator? = null,
    private val delegateNotifier: Notifier? = null,
    private val delegateReporter: Reporter? = null,
    private val delegateEnvironmentManager: EnvironmentManager? = null,
    private val metricsSink: MetricsSink = NoopMetrics,
    private val hookRegistry: HookRegistry = NoopHookRegistry,
    private val logger: Logger = LoggerFactory.getLogger(DevOps::class.java),
    private val plugins: List<Plugin> = emptyList(),
) : DevOps {

    private val pluginRegistry = object : PluginRegistry {
        override val hooks: HookRegistry get() = this@CompositeDevOps.hooks

        private val bindings = ConcurrentHashMap<Class<*>, Any>()

        override fun <T : Any> bind(type: Class<T>, instance: T) {
            bindings[type] = instance
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : Any> resolve(type: Class<T>): T? = bindings[type] as T?
    }

    init {
        plugins.forEach { it.apply(pluginRegistry) }
    }

    override fun getDevOpsConfiguration(): DevOpsConfiguration = configuration
    override fun getLogger(): Logger = logger

    override val metrics: MetricsSink get() = metricsSink
    override val hooks: HookRegistry get() = hookRegistry

    override val sourceManager: SourceManager get() = boundOr(SourceManager::class.java, delegateSourceManager)
    override val syntaxChecker: SyntaxChecker get() = boundOr(SyntaxChecker::class.java, delegateSyntaxChecker)
    override val tester: Tester get() = boundOr(Tester::class.java, delegateTester)
    override val linter: Linter get() = boundOr(Linter::class.java, delegateLinter)
    override val qualityGate: QualityGate get() = boundOr(QualityGate::class.java, delegateQualityGate)
    override val compiler: Compiler get() = boundOr(Compiler::class.java, delegateCompiler)
    override val builder: Builder get() = boundOr(Builder::class.java, delegateBuilder)
    override val staticAnalyzer: StaticAnalyzer get() = boundOr(StaticAnalyzer::class.java, delegateStaticAnalyzer)
    override val artifactManager: ArtifactManager get() = boundOr(ArtifactManager::class.java, delegateArtifactManager)
    override val deployer: Deployer get() = boundOr(Deployer::class.java, delegateDeployer)
    override val orchestrator: PipelineOrchestrator get() = boundOr(PipelineOrchestrator::class.java, delegateOrchestrator)
    override val notifier: Notifier get() = boundOr(Notifier::class.java, delegateNotifier)
    override val reporter: Reporter get() = boundOr(Reporter::class.java, delegateReporter)
    override val environmentManager: EnvironmentManager get() = boundOr(EnvironmentManager::class.java, delegateEnvironmentManager)

    /** 解析顺序：插件绑定 > 构造注入 > Noop（fail-fast）。 */
    private fun <T : Any> boundOr(type: Class<T>, delegate: T?): T =
        pluginRegistry.resolve(type) ?: delegate ?: NoopCapabilities.required(type)
}

/**
 * [DevOps] 组合装配器。
 *
 * 用法：
 * ```
 * val ops = DevOps.builder()
 *     .configuration(DevOpsConfiguration("https://git.example.com/repo.git"))
 *     .sourceManager(GitSourceManager())
 *     .builder(MavenBuilder())
 *     .plugin(MyLintPlugin())
 *     .build()
 * ```
 */
class DevOpsBuilder {
    private var configuration: DevOpsConfiguration = DevOpsConfiguration.DEFAULT
    private var sourceManager: SourceManager? = null
    private var syntaxChecker: SyntaxChecker? = null
    private var tester: Tester? = null
    private var linter: Linter? = null
    private var qualityGate: QualityGate? = null
    private var compiler: Compiler? = null
    private var builder: Builder? = null
    private var staticAnalyzer: StaticAnalyzer? = null
    private var artifactManager: ArtifactManager? = null
    private var deployer: Deployer? = null
    private var orchestrator: PipelineOrchestrator? = null
    private var notifier: Notifier? = null
    private var reporter: Reporter? = null
    private var environmentManager: EnvironmentManager? = null
    private var metricsSink: MetricsSink = NoopMetrics
    private var hookRegistry: HookRegistry = NoopHookRegistry
    private var logger: Logger = LoggerFactory.getLogger(DevOps::class.java)
    private val plugins = mutableListOf<Plugin>()

    fun configuration(config: DevOpsConfiguration): DevOpsBuilder = apply { this.configuration = config }
    fun sourceManager(sm: SourceManager): DevOpsBuilder = apply { this.sourceManager = sm }
    fun syntaxChecker(sc: SyntaxChecker): DevOpsBuilder = apply { this.syntaxChecker = sc }
    fun tester(t: Tester): DevOpsBuilder = apply { this.tester = t }
    fun linter(l: Linter): DevOpsBuilder = apply { this.linter = l }
    fun qualityGate(g: QualityGate): DevOpsBuilder = apply { this.qualityGate = g }
    fun compiler(c: Compiler): DevOpsBuilder = apply { this.compiler = c }
    fun builder(b: Builder): DevOpsBuilder = apply { this.builder = b }
    fun staticAnalyzer(sa: StaticAnalyzer): DevOpsBuilder = apply { this.staticAnalyzer = sa }
    fun artifactManager(am: ArtifactManager): DevOpsBuilder = apply { this.artifactManager = am }
    fun deployer(d: Deployer): DevOpsBuilder = apply { this.deployer = d }
    fun orchestrator(o: PipelineOrchestrator): DevOpsBuilder = apply { this.orchestrator = o }
    fun notifier(n: Notifier): DevOpsBuilder = apply { this.notifier = n }
    fun reporter(r: Reporter): DevOpsBuilder = apply { this.reporter = r }
    fun environmentManager(em: EnvironmentManager): DevOpsBuilder = apply { this.environmentManager = em }
    fun metrics(sink: MetricsSink): DevOpsBuilder = apply { this.metricsSink = sink }
    fun hooks(registry: HookRegistry): DevOpsBuilder = apply { this.hookRegistry = registry }
    fun logger(l: Logger): DevOpsBuilder = apply { this.logger = l }
    fun plugin(p: Plugin): DevOpsBuilder = apply { this.plugins += p }

    fun build(): DevOps = CompositeDevOps(
        configuration = configuration,
        delegateSourceManager = sourceManager,
        delegateSyntaxChecker = syntaxChecker,
        delegateTester = tester,
        delegateLinter = linter,
        delegateQualityGate = qualityGate,
        delegateCompiler = compiler,
        delegateBuilder = builder,
        delegateStaticAnalyzer = staticAnalyzer,
        delegateArtifactManager = artifactManager,
        delegateDeployer = deployer,
        delegateOrchestrator = orchestrator,
        delegateNotifier = notifier,
        delegateReporter = reporter,
        delegateEnvironmentManager = environmentManager,
        metricsSink = metricsSink,
        hookRegistry = hookRegistry,
        logger = logger,
        plugins = plugins,
    )
}
