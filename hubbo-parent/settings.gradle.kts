pluginManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        mavenCentral()
    }
}

rootProject.name = "hubbo-parent"
// https://gradle.github.net.cn/current/userguide/java_library_plugin.html
// Learn more about structuring projects with Gradle - https://docs.gradle.org/8.7/userguide/multi_project_builds.html
include(":hubbo-annotations")
include(":hubbo-utils")
include(":hubbo-common")
include(":hubbo-entity")
include(":hubbo-dal")
include(":hubbo-service")
include(":hubbo-native-invocation")
include(":hubbo-integration")
include(":hubbo-service-facade")
include(":hubbo-configure")
include(":hubbo-auth")
include(":hubbo-scheduler")
include(":hubbo-web")
include(":hubbo-boot")
include(":hubbo-test")
include(":hubbo-native-binding")


// https://blog.csdn.net/k316378085/article/details/134359688