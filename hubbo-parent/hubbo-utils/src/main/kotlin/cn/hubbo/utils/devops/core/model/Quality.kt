package cn.hubbo.utils.devops.core.model

import java.nio.file.Path
import java.time.Duration
import java.time.Instant

// ==================== 语法检查 ====================

/** 语法错误。 */
data class SyntaxError(
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val rule: String? = null,
)

/** 语法检查结果。 */
data class SyntaxCheckResult(
    val passed: Boolean,
    val errors: List<SyntaxError>,
    val filesChecked: Int,
    val duration: Duration,
)

// ==================== 测试 ====================

enum class TestType { UNIT, INTEGRATION, E2E, CONTRACT, PERFORMANCE }

enum class TestStatus { PASSED, FAILED, SKIPPED, FLAKY }

/** 单个测试用例。 */
data class TestCase(
    val name: String,
    val className: String? = null,
    val status: TestStatus,
    val durationMillis: Long,
    val failureMessage: String? = null,
    val errorType: String? = null,
    val stackTrace: String? = null,
)

/** 覆盖率结果（行/分支/语句/方法，百分比，0~1）。 */
data class CoverageResult(
    val lineCoverage: Double,
    val branchCoverage: Double? = null,
    val statementCoverage: Double? = null,
    val methodCoverage: Double? = null,
    val fileCoverage: Map<String, Double> = emptyMap(),
    /** 来源格式：jacoco / cobertura / lcov / go test coverage ... */
    val format: String = "jacoco",
)

/** 测试报告。 */
data class TestReport(
    val type: TestType,
    val total: Int,
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    val flaky: Int,
    val duration: Duration,
    val coverage: CoverageResult? = null,
    val testCases: List<TestCase> = emptyList(),
    val junitXmlPath: Path? = null,
    val htmlReportPath: Path? = null,
) {
    /** 通过率 = passed / (passed + failed)，无失败用例时为 1.0。 */
    val passRate: Double
        get() = if (passed + failed == 0) 1.0 else passed.toDouble() / (passed + failed)
}

// ==================== 代码风格检查 ====================

/** 问题严重等级。 */
enum class Severity { INFO, LOW, MEDIUM, HIGH, CRITICAL, BLOCKER }

/** 单条 Lint 违规。 */
data class LintViolation(
    val rule: String,
    val severity: Severity,
    val file: String,
    val line: Int,
    val column: Int? = null,
    val message: String,
    val fixAvailable: Boolean = false,
)

/** Lint 报告。 */
data class LintReport(
    val tool: String,
    val violations: List<LintViolation>,
    val filesScanned: Int,
    val duration: Duration,
) {
    /** 按严重等级聚合的违规数。 */
    val summary: Map<Severity, Int>
        get() = violations.groupingBy { it.severity }.eachCount()
}

// ==================== 静态代码分析 ====================

enum class IssueCategory { SAST, DEPENDENCY, COMPLEXITY, DUPLICATION, SECURITY, PERFORMANCE }

/** 复杂度度量。 */
data class ComplexityMetrics(
    val cyclomaticComplexity: Double,
    val cognitiveComplexity: Double,
    val linesOfCode: Long,
    val commentLines: Long,
    val maintainabilityIndex: Double? = null,
)

/** 单条分析问题（SAST / CVE / 复杂度 / 重复代码）。 */
data class AnalysisIssue(
    val id: String,
    val category: IssueCategory,
    val severity: Severity,
    val file: String? = null,
    val line: Int? = null,
    val message: String,
    val cve: String? = null,
    val cwe: String? = null,
    val remediation: String? = null,
)

/** 静态分析报告。 */
data class AnalysisReport(
    val tool: String,
    val issues: List<AnalysisIssue>,
    val complexityMetrics: ComplexityMetrics? = null,
    val duration: Duration,
) {
    val summary: Map<Severity, Int>
        get() = issues.groupingBy { it.severity }.eachCount()
}

/** 依赖漏洞（已知 CVE）。 */
data class DependencyIssue(
    val coordinates: Coordinates,
    val cve: String,
    val severity: Severity,
    val cvssScore: Double,
    val description: String,
    val fixedVersion: String? = null,
)

/** 依赖扫描报告。 */
data class DependencyScanReport(
    val dependencies: List<DependencyIssue>,
    val scannedCount: Int,
    val duration: Duration,
)

// ==================== 质量门禁 ====================

/** 门禁维度。 */
enum class GateDimension {
    COVERAGE,
    LINT_ERRORS,
    TEST_FAILURES,
    CRITICAL_VULNS,
    HIGH_VULNS,
    DUPLICATION_RATE,
    COMPLEXITY,
    SECURITY_RATING,
    BUILD_AGE,
}

enum class ComparisonOperator { GREATER_THAN, GREATER_OR_EQUAL, LESS_THAN, LESS_OR_EQUAL, EQUAL }

/** 门禁规则失败时的动作。 */
enum class GateAction { BLOCK, WARN, PASS }

/** 单条门禁规则。 */
data class GateRule(
    val dimension: GateDimension,
    val operator: ComparisonOperator,
    val threshold: Double,
    val action: GateAction = GateAction.BLOCK,
    val description: String = "",
)

/** 单条规则的校验结果。 */
data class GateCheckResult(
    val rule: GateRule,
    val actual: Double,
    val passed: Boolean,
    val message: String,
)

/** 门禁结果。 */
data class GateResult(
    val passed: Boolean,
    val checks: List<GateCheckResult>,
    val blockingFailures: List<GateCheckResult>,
    val evaluatedAt: Instant = Instant.now(),
)

/**
 * 门禁输入证据。
 *
 * 各质量维度从对应的阶段报告中提取，供 [cn.hubbo.utils.devops.capability.QualityGate]
 * 实现按 [GateRule] 计算实际值。
 */
data class GateEvidence(
    val coverage: CoverageResult? = null,
    val lintReport: LintReport? = null,
    val testReport: TestReport? = null,
    val analysisReport: AnalysisReport? = null,
    val dependencyReport: DependencyScanReport? = null,
    val extra: Map<GateDimension, Double> = emptyMap(),
)
