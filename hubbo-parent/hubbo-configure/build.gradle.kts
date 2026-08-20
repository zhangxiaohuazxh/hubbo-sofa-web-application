import java.nio.file.Paths
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectory
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
val env = tasks.register<EnvTask>("env") {
    moduleName.set(project.parent?.project?.name ?: project.name)
    // 在配置期捕获待执行任务集合，避免执行期访问 gradle.startParameter（不兼容 configuration cache）
    val requestedTasks = gradle.startParameter.taskNames.toSet()
    val packagingTasks = setOf("jar", "bootJar", "flatJar", "zip")
    logger.lifecycle("tasknames {}", requestedTasks)
    onlyIf {
        !requestedTasks.any { taskName -> packagingTasks.contains(taskName) }
    }
}

tasks.named("build") {
    dependsOn(env)
}

tasks.named("classes") {
    dependsOn(env)
}