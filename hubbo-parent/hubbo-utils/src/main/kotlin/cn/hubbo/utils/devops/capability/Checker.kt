package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.config.LintOptions
import cn.hubbo.utils.devops.config.SyntaxCheckOptions
import cn.hubbo.utils.devops.config.TestOptions
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.model.Language
import cn.hubbo.utils.devops.core.model.LintReport
import cn.hubbo.utils.devops.core.model.SyntaxCheckResult
import cn.hubbo.utils.devops.core.model.TestReport
import cn.hubbo.utils.devops.core.model.TestType

/**
 * 语法检查能力。
 *
 * 针对不同语言调用对应的语法解析器或编译器进行静态语法验证，
 * 返回通过 / 失败及详细错误信息。
 */
interface SyntaxChecker {
    /** 支持的源语言。 */
    val supportedLanguages: Set<Language>

    suspend fun check(ctx: PipelineContext, options: SyntaxCheckOptions): SyntaxCheckResult
}

/**
 * 测试能力。
 *
 * 支持单元测试、集成测试、端到端测试等类型；
 * 可指定测试范围（包 / 目录 / 标签），输出通过率、失败用例与覆盖率。
 */
interface Tester {
    /** 支持的测试类型。 */
    val supportedTypes: Set<TestType>

    suspend fun test(ctx: PipelineContext, options: TestOptions): TestReport
}

/**
 * 代码风格检查能力。
 *
 * 集成常见 Linter（golangci-lint / Checkstyle / ESLint / Pylint 等），
 * 支持自定义规则集，返回违规列表及严重等级。
 */
interface Linter {
    /** 支持的工具名集合。 */
    val supportedTools: Set<String>

    suspend fun lint(ctx: PipelineContext, options: LintOptions): LintReport
}
