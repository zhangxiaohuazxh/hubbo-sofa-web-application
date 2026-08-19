package cn.hubbo.unit.devops

import cn.hubbo.common.cicd.Builder
import cn.hubbo.common.cicd.Iteration
import cn.hubbo.common.cicd.IterationStatus
import cn.hubbo.common.cicd.Project
import cn.hubbo.utils.devops.config.BuildOptions
import cn.hubbo.utils.devops.config.BuildTool
import cn.hubbo.utils.devops.core.PipelineContexts
import cn.hubbo.utils.devops.core.model.ArtifactType
import cn.hubbo.utils.devops.impl.MavenBuilder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime

class BuilderUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(BuilderUnitTest::class.java) }

    class SimpleBuilder(val iteration: Iteration) : Builder {

        private val logger: Logger by lazy { LoggerFactory.getLogger(SimpleBuilder::class.java) }
        override fun run() {
            logger.info("run")
        }

        override fun abort() {
            logger.info("abort")
        }

        override fun currentIteration(): Iteration {
            return iteration
        }
    }

    class EnhanceSimpleBuilder(val iteration: Iteration, val builder: Builder) : Builder {

        private val logger: Logger by lazy { LoggerFactory.getLogger(EnhanceSimpleBuilder::class.java) }

        override fun run() {
            builder.run()
            logger.info("enhance builder run")
        }

        override fun abort() {
            logger.info("enhance builder abort")
        }

        override fun currentIteration(): Iteration {
            return iteration
        }
    }

    @Test
    fun testVerifySimpleBuilderModel() {
        val iteration = Iteration(
            id = 0L,
            "迭代测试001",
            1L,
            "云姜",
            LocalDateTime.now(),
            IterationStatus.ACTIVE,
            "https://github.com/yunjiang-hubbo/hubbo-test.git",
            "master",
            Project(0L)
        )
        val simpleBuilder = SimpleBuilder(iteration)
        simpleBuilder.run()
        val enhanceSimpleBuilder = EnhanceSimpleBuilder(iteration, simpleBuilder)
        enhanceSimpleBuilder.run()
    }

    @Test
    fun testMavenBuilder(): Unit = runBlocking {
        val pipelineContext = PipelineContexts.default()
        val mavenBuilder = MavenBuilder(File("C:\\Users\\33233\\AppData\\Local\\Temp\\xxl-job"))
        val result =
            mavenBuilder.build(pipelineContext, BuildOptions(tool = BuildTool.MAVEN, artifactType = ArtifactType.JAR))
        logger.info("执行结果 {}", result)
    }


}