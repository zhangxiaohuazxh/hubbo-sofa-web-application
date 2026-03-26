plugins {
    id("com.google.devtools.ksp")
    kotlin("kapt")
}

dependencies {
    api(project(":hubbo-utils"))
    api(project(":hubbo-entity"))
    api(project(":hubbo-annotations"))
    api(project(":hubbo-native-binding"))
    api(libs.bundles.logging)
    api(libs.jackson.databind)
    // 亲测仅支持kapt 不支持ksp
    kapt(libs.log4j.core)
    compileOnly("org.springframework.boot:spring-boot-starter-log4j2")
    compileOnly("org.springframework:spring-context")
}

kapt {
    // 1. 允许在编译报错时依然尝试生成代码（方便调试）
    showProcessorStats = true
    // 2. 隔离类路径，减少 Gradle 宿主环境的干扰
    includeCompileClasspath = false
    // 3. 修正错误类型，防止因为找不到某些类导致生成中断
    correctErrorTypes = true
    arguments {
        // 告诉处理器包含插件元数据
        arg("log4j.skip.plugin.processing", "false")
    }
    // 确保生成路径正确
    includeCompileClasspath = false
}