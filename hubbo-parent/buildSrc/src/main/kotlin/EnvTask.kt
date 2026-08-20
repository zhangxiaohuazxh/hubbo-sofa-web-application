import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.nio.file.Paths
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

/**
 * 拷贝用户目录 ~/.config/<module> 下的配置文件到 build/resources/main/config。
 * 不递归处理，文件必须小于 1M。
 */
abstract class EnvTask : DefaultTask() {

    @get:Input
    abstract val moduleName: Property<String>

    @TaskAction
    fun copyLocalConfig() {
        logger.lifecycle("=======================env task=======================")
        val localConfigHome = Paths.get(System.getProperty("user.home"), ".config", moduleName.get().replace("-parent", ""))
        if (localConfigHome.exists()) {
            val targetClassPath = project.layout.buildDirectory.dir("resources/main/config").get().asFile.toPath()
            if (!targetClassPath.exists()) {
                targetClassPath.createDirectory()
            }
            logger.lifecycle("找到本地的配置文件目录 {}", localConfigHome.toAbsolutePath())
            val maxSize = 1024 * 1024
            val files = localConfigHome.listDirectoryEntries().filter { it.isRegularFile() }
            files.filter { it.fileSize() > maxSize }
                .forEach { logger.warn("忽略大于1M的配置文件 {}", it.toAbsolutePath()) }
            files.filter { it.fileSize() <= maxSize }.forEach {
                runCatching {
                    val newFile = targetClassPath.resolve(it.fileName)
                    it.copyTo(newFile, overwrite = true)
                    logger.lifecycle("拷贝配置文件 {} 到 {}", it.toAbsolutePath(), newFile.toAbsolutePath())
                }
            }
        } else {
            logger.lifecycle("未找到本地配置文件目录 {}", localConfigHome.toAbsolutePath())
        }
        logger.lifecycle("=======================env task=======================")
    }
}