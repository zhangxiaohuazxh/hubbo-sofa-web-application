package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.config.DeployOptions
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.model.Artifact
import cn.hubbo.utils.devops.core.model.ArtifactReference
import cn.hubbo.utils.devops.core.model.ArtifactRepository
import cn.hubbo.utils.devops.core.model.Coordinates
import cn.hubbo.utils.devops.core.model.DeployResult
import cn.hubbo.utils.devops.core.model.Environment
import cn.hubbo.utils.devops.core.model.HealthStatus
import java.nio.file.Path

/**
 * 制品管理能力。
 *
 * 将构建产物上传至制品库（Nexus / JFrog / 容器仓库），
 * 支持版本标签、元数据记录、下载、晋升与删除。
 */
interface ArtifactManager {
    suspend fun upload(ctx: PipelineContext, artifact: Artifact, repository: ArtifactRepository): ArtifactReference

    suspend fun download(ctx: PipelineContext, reference: ArtifactReference, targetDirectory: Path): Artifact

    suspend fun listVersions(ctx: PipelineContext, coordinates: Coordinates, repository: String): List<String>

    /** 晋升到更高级别的仓库（如从 snapshot 到 release）。 */
    suspend fun promote(ctx: PipelineContext, reference: ArtifactReference, targetRepository: String): ArtifactReference

    suspend fun delete(ctx: PipelineContext, reference: ArtifactReference)
}

/**
 * 部署能力。
 *
 * 支持多环境（开发 / 测试 / 预发布 / 生产）与多策略（滚动 / 蓝绿 / 金丝雀），
 * 可回滚到历史版本并执行健康检查。
 */
interface Deployer {
    suspend fun deploy(ctx: PipelineContext, options: DeployOptions): DeployResult

    /** 回滚到某次历史部署。 */
    suspend fun rollback(ctx: PipelineContext, deploymentId: String, environment: Environment): DeployResult

    /** 查询部署状态。 */
    suspend fun status(ctx: PipelineContext, deploymentId: String): DeployResult

    /** 健康检查。 */
    suspend fun healthCheck(ctx: PipelineContext, deploymentId: String): HealthStatus
}
