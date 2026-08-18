package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.model.Environment
import cn.hubbo.utils.devops.core.model.ResolvedEnvironment
import cn.hubbo.utils.devops.core.model.Secret
import java.nio.file.Path

/**
 * 环境管理能力。
 *
 * 管理不同环境的基础设施配置（K8s 命名空间、数据库连接字符串等），
 * 解析密钥并将其注入构建 / 部署过程。
 */
interface EnvironmentManager {
    /** 解析目标环境的完整配置（基础设施 + 密钥引用）。 */
    suspend fun resolve(ctx: PipelineContext, environment: Environment): ResolvedEnvironment

    /** 按需读取密钥。 */
    suspend fun secrets(ctx: PipelineContext, environment: Environment, keys: Set<String>): Map<String, Secret>

    /** 对模板文件做占位符注入（如 application.yml.template），返回渲染后的文件路径。 */
    suspend fun inject(ctx: PipelineContext, template: Path, environment: Environment): Path
}
