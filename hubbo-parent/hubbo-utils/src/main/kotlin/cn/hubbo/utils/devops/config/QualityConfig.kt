package cn.hubbo.utils.devops.config

import cn.hubbo.utils.devops.core.model.ComparisonOperator
import cn.hubbo.utils.devops.core.model.GateAction
import cn.hubbo.utils.devops.core.model.GateDimension
import cn.hubbo.utils.devops.core.model.GateRule
import cn.hubbo.utils.devops.core.model.Language
import cn.hubbo.utils.devops.core.model.Severity
import cn.hubbo.utils.devops.core.model.TestType
import java.nio.file.Path
import java.time.Duration

/** 语法检查配置。 */
data class SyntaxCheckOptions(
    val language: Language,
    val paths: List<Path>,
    val includePatterns: List<String> = emptyList(),
    val excludePatterns: List<String> = emptyList(),
    val maxErrors: Int = 100,
    val timeout: Duration = Duration.ofMinutes(5),
)

/** 测试范围。 */
sealed interface TestScope {
    data object All : TestScope
    data class Packages(val packages: List<String>) : TestScope
    data class Directories(val directories: List<Path>) : TestScope
    data class Tags(val tags: List<String>) : TestScope
}

/** 测试配置。 */
data class TestOptions(
    val type: TestType = TestType.UNIT,
    val scope: TestScope = TestScope.All,
    val coverageEnabled: Boolean = false,
    val coverageThreshold: Double? = null,
    val parallelism: Int = 1,
    val failFast: Boolean = false,
    val env: Map<String, String> = emptyMap(),
    val additionalArgs: List<String> = emptyList(),
    val timeout: Duration = Duration.ofMinutes(10),
)

/** 代码风格检查配置。 */
data class LintOptions(
    /** 指定 linter 工具；为空时由实现按语言选择默认工具。 */
    val tool: String? = null,
    val ruleSet: Path? = null,
    val severityThreshold: Severity = Severity.INFO,
    val paths: List<Path> = emptyList(),
    val excludePatterns: List<String> = emptyList(),
    val fix: Boolean = false,
    val timeout: Duration = Duration.ofMinutes(5),
)

/** 静态代码分析配置。 */
data class AnalysisOptions(
    val sastEnabled: Boolean = true,
    val dependencyScanEnabled: Boolean = true,
    val complexityEnabled: Boolean = true,
    val severityThreshold: Severity = Severity.HIGH,
    val exclusions: List<String> = emptyList(),
    val timeout: Duration = Duration.ofMinutes(10),
)

/** 质量门禁配置（规则组合）。 */
data class GateConfig(
    val rules: List<GateRule> = emptyList(),
    /** 存在 WARN 级未通过时是否计为失败。 */
    val failOnWarnings: Boolean = true,
) {
    companion object {
        /** 常用默认门禁：覆盖率 >= 80%、Lint 违规 0、严重漏洞 0、测试失败 0。 */
        val DEFAULT: GateConfig = GateConfig(
            rules = listOf(
                GateRule(GateDimension.COVERAGE, ComparisonOperator.GREATER_OR_EQUAL, 0.80, description = "测试覆盖率 >= 80%"),
                GateRule(GateDimension.LINT_ERRORS, ComparisonOperator.EQUAL, 0.0, description = "Lint 违规数为 0"),
                GateRule(GateDimension.CRITICAL_VULNS, ComparisonOperator.LESS_OR_EQUAL, 0.0, description = "严重漏洞数为 0"),
                GateRule(GateDimension.TEST_FAILURES, ComparisonOperator.EQUAL, 0.0, description = "测试失败数为 0"),
            ),
        )
    }
}
