package cn.hubbo.unit.devops

import cn.hubbo.utils.devops.DevOps
import cn.hubbo.utils.devops.DevOpsConfiguration
import cn.hubbo.utils.devops.config.BuildOptions
import cn.hubbo.utils.devops.config.BuildTool
import cn.hubbo.utils.devops.config.CloneOptions
import cn.hubbo.utils.devops.config.RevisionSpec
import cn.hubbo.utils.devops.core.PipelineContexts
import cn.hubbo.utils.devops.core.model.ArtifactType
import cn.hubbo.utils.devops.impl.GitSourceManager
import cn.hubbo.utils.devops.impl.MavenBuilder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CompositeDevOpsUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(CompositeDevOpsUnitTest::class.java) }

    @Test
    fun testCloneAndBuild(): Unit = runBlocking {
        val configuration = DevOpsConfiguration.DEFAULT
        val pipelineContext = PipelineContexts.default()
        val buildOptions = BuildOptions(tool = BuildTool.MAVEN, artifactType = ArtifactType.JAR)

        // 1. 克隆阶段：GitSourceManager 将仓库检入 ctx.workingDirectory/xxl-job（默认 java.io.tmpdir）
        val devOps = DevOps.builder()
            .sourceManager(GitSourceManager())
            .configuration(configuration)
            .build()
        val checkoutResult = devOps.clone(
            pipelineContext,
            CloneOptions(
                repositoryUrl = "https://gitee.com/xuxueli0323/xxl-job.git",
                revision = RevisionSpec.Branch("master")
            )
        )
        logger.info("clone result {}", checkoutResult)
        // 2. 构建阶段：MavenBuilder 必须显式指向克隆出的工作区。
        //    默认 projectDirectory = File(".") 是 Gradle 测试进程的工作目录（hubbo-parent/hubbo-test），
        //    其中没有 pom.xml，`mvn clean package -B -e -U` 会报 MissingProjectException（exit code 1）。
        val buildDevOps = DevOps.builder()
            .sourceManager(GitSourceManager())
            .builder(MavenBuilder(checkoutResult.workspace.toFile()))
            .configuration(configuration)
            .build()
        val buildResult = buildDevOps.build(pipelineContext, buildOptions)
        logger.info("build result {}", buildResult)
    }


}
