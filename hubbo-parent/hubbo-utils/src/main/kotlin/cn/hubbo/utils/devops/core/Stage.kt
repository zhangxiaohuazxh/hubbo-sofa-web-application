package cn.hubbo.utils.devops.core

/**
 * 流水线的阶段枚举。
 *
 * [order] 表示执行优先级（数值越小越先执行），用于默认编排顺序与门禁排序。
 * 阶段覆盖从代码提交到交付部署的全生命周期。
 */
enum class Stage(val order: Int) {
    CLONE(10),
    SYNTAX_CHECK(20),
    LINT(30),
    TEST(40),
    STATIC_ANALYSIS(50),
    COMPILE(60),
    BUILD(70),
    QUALITY_GATE(80),
    ARTIFACT_UPLOAD(85),
    DEPLOY(90),
    VERIFY(95),
    REPORT(100),
    ;

    companion object {
        /** 默认阶段执行顺序。 */
        fun defaultOrder(): List<Stage> = entries.sortedBy { it.order }
    }
}

/** 单步执行状态。 */
enum class StepStatus {
    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED,
    SKIPPED,
    CANCELLED,
    TIMED_OUT,
}
