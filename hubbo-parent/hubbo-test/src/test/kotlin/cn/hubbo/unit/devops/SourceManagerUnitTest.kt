package cn.hubbo.unit.devops

import cn.hubbo.utils.devops.config.CloneOptions
import cn.hubbo.utils.devops.core.PipelineContexts
import cn.hubbo.utils.devops.impl.GitSourceManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SourceManagerUnitTest {

    private val logger: Logger by lazy { LoggerFactory.getLogger(SourceManagerUnitTest::class.java) }


    @Test
    fun testGitSourceManagerClone(): Unit = runBlocking {
        val sourceManager = GitSourceManager()
        val pipelineContext = PipelineContexts.default()
        val result = sourceManager.clone(
            pipelineContext,
            CloneOptions("https://gitee.com/xuxueli0323/xxl-job.git", refspec = "master")
        )
        logger.info("clone result {}", result)
    }


}