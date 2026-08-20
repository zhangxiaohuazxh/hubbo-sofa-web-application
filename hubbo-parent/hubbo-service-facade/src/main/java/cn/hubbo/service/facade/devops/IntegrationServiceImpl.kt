package cn.hubbo.service.facade.devops

import cn.hubbo.dal.IntegrationDao
import cn.hubbo.entity.vo.IterationVO
import cn.hubbo.service.devops.IntegrationService
import cn.hubbo.utils.CommandLineUtils
import jakarta.annotation.Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class IntegrationServiceImpl : IntegrationService {

    private val logger: Logger by lazy { LoggerFactory.getLogger(IntegrationServiceImpl::class.java) }

    @Resource
    private lateinit var iterationDao: IntegrationDao


    override suspend fun continuousIntegration(iteration: IterationVO) {
        logger.info("===================开始执行部署任务===================")
        logger.info("项目信息 {}", iteration)
        val projectIteration = iterationDao.findProjectIntegrationInfoByIterationId(iteration.iterationId)
            ?: throw IllegalArgumentException("未找到迭代信息 iterationId=${iteration.iterationId}")
        logger.info("查询到的迭代信息 {}", projectIteration)
        val project = iterationDao.findProjectInfoByProjectId(projectIteration.projectId)
            ?: throw IllegalArgumentException("未找到项目信息 projectId=${projectIteration.projectId}")
        logger.info("查询到的项目信息  {}", project)

        // 用户可控字段一律通过进程环境变量传入，避免拼进 shell 命令导致命令注入；
        // 同时对 projectName / branch 做白名单校验，防止目录穿越（如 rm -rf /）风险。
        val projectName = validateSafeName(project.projectName, "projectName")
        val branch = validateSafeName(projectIteration.currentBranch, "currentBranch")
        val repositoryUrl = project.repositoryUrl.also { validateRepositoryUrl(it) }

        //  执行阶段任务
        //  clone
        //  checkout
        //   compile
        //   test
        //   build
        val command =
            "rm -rf \"\$PROJECT_NAME\" && git clone \"\$REPO_URL\" \"\$PROJECT_NAME\" && cd \"\$PROJECT_NAME\" && git checkout \"\$BRANCH\" && mvn clean compile package"
        val res = CommandLineUtils.exec(
            command,
            timeoutMillis = 600_000L,
            environment = mapOf(
                "PROJECT_NAME" to projectName,
                "REPO_URL" to repositoryUrl,
                "BRANCH" to branch
            )
        )
        logger.info("执行结果 {}", res)
        logger.info("===================部署任务执行完成===================")
    }

    override suspend fun continuousDelivery() {
        logger.warn("continuousDelivery 尚未实现，暂不执行")
        throw UnsupportedOperationException("continuousDelivery 尚未实现")
    }

    private fun validateSafeName(value: String, field: String): String {
        require(SAFE_NAME.matches(value)) {
            "非法的$field: $value 仅允许字母、数字、下划线、点和连字符"
        }
        return value
    }

    private fun validateRepositoryUrl(value: String) {
        require(REPOSITORY_URL.matches(value)) {
            "非法的仓库地址: $value"
        }
    }

    companion object {

        /** 目录/分支名白名单：禁止空白与 shell 元字符，杜绝命令注入与目录穿越 */
        private val SAFE_NAME = Regex("^[a-zA-Z0-9_.-]+$")

        /** 仓库地址仅允许标准协议形式，禁止空白字符 */
        private val REPOSITORY_URL = Regex("^(https?|ssh|git|file)://\\S+|^git@\\S+:\\S+$")

    }

}