package cn.hubbo.utils.devops.core.error

/**
 * 是否可恢复。
 *
 * 决定上层编排器应该如何应对失败：
 * - [RECOVERABLE]：可重试（网络抖动、临时资源不足、超时）；
 * - [FATAL]：重试无意义（配置错误、认证失败、代码错误）。
 */
enum class Recoverability {
    RECOVERABLE,
    FATAL,
}

/**
 * 统一错误码。
 *
 * 每个错误码对应一类失败，便于告警聚合、日志检索与用户排障。
 * 错误码与 [DevOpsError] 一起组成标准错误模型。
 */
enum class ErrorCode {
    CONFIGURATION_INVALID,
    AUTH_FAILED,
    NETWORK_ERROR,
    CLONE_FAILED,
    REVISION_NOT_FOUND,
    SYNTAX_CHECK_FAILED,
    LINT_FAILED,
    TEST_FAILED,
    COVERAGE_BELOW_THRESHOLD,
    GATE_FAILED,
    COMPILE_FAILED,
    BUILD_FAILED,
    DOCKER_BUILD_FAILED,
    STATIC_ANALYSIS_FAILED,
    DEPENDENCY_SCAN_FAILED,
    ARTIFACT_UPLOAD_FAILED,
    ARTIFACT_NOT_FOUND,
    DEPLOY_FAILED,
    ROLLBACK_FAILED,
    HEALTH_CHECK_FAILED,
    TIMEOUT,
    CANCELLED,
    SKIPPED,
    TOOL_NOT_FOUND,
    HOOK_ERROR,
    PLUGIN_ERROR,
    UNSUPPORTED_FEATURE,
    UNKNOWN,
}
