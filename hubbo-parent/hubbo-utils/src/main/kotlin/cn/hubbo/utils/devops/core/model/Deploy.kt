package cn.hubbo.utils.devops.core.model

import java.nio.file.Path
import java.time.Instant

/** 部署环境。 */
enum class Environment { DEV, TEST, STAGING, PROD, DR }

/** 部署策略。 */
enum class DeployStrategy { ROLLING, BLUE_GREEN, CANARY, RECREATE, IMMEDIATE }

/** 部署状态。 */
enum class DeployStatus { PENDING, IN_PROGRESS, SUCCEEDED, FAILED, ROLLED_BACK, CANCELLED }

/** 健康状态。 */
enum class HealthStatus { HEALTHY, DEGRADED, UNHEALTHY, UNKNOWN }

/** 部署结果。 */
data class DeployResult(
    val deploymentId: String,
    val environment: Environment,
    val strategy: DeployStrategy,
    val targetUrl: String? = null,
    val startedAt: Instant,
    val finishedAt: Instant? = null,
    val status: DeployStatus,
    val logs: Path? = null,
)

/** Kubernetes 基础设施规格。 */
data class KubernetesSpec(
    val namespace: String,
    val cluster: String? = null,
    val deploymentName: String? = null,
    val kubeconfig: String? = null,
    val labels: Map<String, String> = emptyMap(),
)

/** 数据库连接规格。 */
data class DatabaseSpec(
    val name: String,
    val connectionString: String,
    val username: String? = null,
    val poolMaxSize: Int? = null,
)

/** 容器仓库规格。 */
data class ContainerRegistrySpec(
    val registry: String,
    val repository: String,
    val tag: String? = null,
)

/** 基础设施配置聚合。 */
data class InfrastructureSpec(
    val kubernetes: KubernetesSpec? = null,
    val database: List<DatabaseSpec> = emptyList(),
    val registry: ContainerRegistrySpec? = null,
    val custom: Map<String, String> = emptyMap(),
)

/** 密钥（value 为明文，redacted 用于日志展示）。 */
data class Secret(
    val value: String,
    val redacted: String = "******",
)

/** 解析后的目标环境（配置 + 基础设施 + 密钥），注入构建/部署过程。 */
data class ResolvedEnvironment(
    val environment: Environment,
    val config: Map<String, String>,
    val infra: InfrastructureSpec,
    val secrets: Map<String, Secret> = emptyMap(),
)
