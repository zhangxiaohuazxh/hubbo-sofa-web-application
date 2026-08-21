import java.nio.file.Paths
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

plugins {
    kotlin("kapt")
}

dependencies {
    implementation(libs.bundles.annotations)
    compileOnly(libs.bundles.r2dbc)
    compileOnly("org.springframework.boot:spring-boot-starter-jooq")
    kapt(libs.spring.context.indexer)
    implementation(libs.coroutines.micrometer.context.propagation)
    compileOnly(libs.rocketmq)
    compileOnly(project(":hubbo-common"))
    compileOnly("org.springframework.boot:spring-boot-starter-data-redis-reactive")
}

// 拷贝用户目录下的隐藏文件到build/classes/config下，不会递归处理，目录名称 ${user.home}/project.name,文件必须小于1M
// jar flatJar bootJar zip 任务执行时不会执行env任务
// 注意：配置缓存(org.gradle.configuration-cache=true)下，执行阶段禁止访问 project/gradle，
// 因此所有依赖 project/gradle/System 的值都必须在配置阶段捕获，doLast 里只使用捕获后的局部变量。
val env = tasks.register("env") {
    // —— 配置阶段捕获 startParameter，避免执行阶段访问 gradle ——
    val taskNames = gradle.startParameter.taskNames
    val excludeTasks = listOf("jar", "bootJar", "flatJar", "zip")
    logger.lifecycle("tasknames {}", taskNames)
    val skip = taskNames.any { taskName ->
        excludeTasks.any { exclude -> taskName.equals(exclude) }
    }
    if (skip) {
        logger.lifecycle("当前执行的任务 {},忽略env task", taskNames)
    }
    onlyIf { !skip }

    // —— 配置阶段捕获所有依赖 project/System 的值 ——
    val moduleName = project.parent?.name ?: project.name
    val localConfigHome = Paths.get(System.getProperty("user.home"), ".config", moduleName.replace("-parent", ""))
    val targetClassPath = project.layout.buildDirectory.dir("resources/main/config").get().asFile.toPath()

    doLast {
        logger.lifecycle("=======================env task=======================")
        if (localConfigHome.exists()) {
            if (!targetClassPath.exists()) {
                targetClassPath.createDirectories()
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

tasks.named("build") {
    dependsOn(env)
}

tasks.named("classes") {
    dependsOn(env)
}