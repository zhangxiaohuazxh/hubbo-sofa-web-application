package cn.hubbo.unit.devops

import cn.hubbo.utils.devops.DevOps
import cn.hubbo.utils.devops.DevOpsConfiguration
import cn.hubbo.utils.devops.LocalStorageInfo
import cn.hubbo.utils.devops.core.PipelineContexts
import cn.hubbo.utils.devops.core.Stage
import cn.hubbo.utils.devops.core.model.PipelineDefinition
import cn.hubbo.utils.devops.core.model.StageDefinition
import cn.hubbo.utils.devops.impl.GitSourceManager
import cn.hubbo.utils.devops.impl.JavaDevOpsImpl
import cn.hubbo.utils.devops.impl.MavenBuilder
import cn.hubbo.utils.devops.impl.RustDevOpsImpl
import cn.hubbo.utils.devops.mock.MockOrchestrator
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

object JavaDevOpsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(JavaDevOpsUnitTest::class.java) }

    @Test
    fun testClone(): Unit = runBlocking {
        val ops = JavaDevOpsImpl(DevOpsConfiguration("https://gitee.com/xuxueli0323/xxl-job.git"))
        val localStorageInfo = ops.clone()
        logger.info("项目存储信息 {}", localStorageInfo)
        ops.compile()
        ops.build()
    }

    @Test
    fun testCaptureFinalProduct(): Unit = runBlocking {
        val devOpsConfiguration = DevOpsConfiguration("https://gitee.com/xuxueli0323/xxl-job.git")
        val ops = JavaDevOpsImpl(devOpsConfiguration)
        val files = ops.captureProduct()
        for (file in files) {
            logger.info("构建的产物 {}", file)
        }
    }

    @Test
    fun testPipeline(): Unit = runBlocking {
        val repoUrl = "https://git.example.com/repo.git"
        val ctx = PipelineContexts.default(pipelineName = "ci", stage = Stage.CLONE)
        // 组合装配：GitSourceManager + MavenBuilder；此处用 MockOrchestrator 占位编排器，
        // 真实环境应注入生产编排实现（如 SequentialOrchestrator / 分布式调度器）。
        val ops = DevOps.builder()
            .configuration(DevOpsConfiguration(repoUrl))
            .sourceManager(GitSourceManager())
            .builder(MavenBuilder())
            .orchestrator(MockOrchestrator())
            .build()
        val run = ops.runPipeline(ctx, PipelineDefinition(
            name = "ci",
            stages = listOf(
                StageDefinition("clone", Stage.CLONE, options = mapOf("url" to repoUrl)),
                StageDefinition("test", Stage.TEST, dependsOn = setOf("clone")),
                StageDefinition("gate", Stage.QUALITY_GATE, dependsOn = setOf("test")),
                StageDefinition("deploy", Stage.DEPLOY, dependsOn = setOf("gate"), condition = "branch == 'main'"),
            ),
        ))
        logger.info("流水线运行状态 {}", run.status)
    }


}