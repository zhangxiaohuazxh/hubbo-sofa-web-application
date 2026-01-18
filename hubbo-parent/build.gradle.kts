description = "描述信息"

plugins {
    alias(libs.plugins.ksp) apply false
}

allprojects {
    group = "com.hubbo"
    version = "0.0.1"
    repositories {
        mavenLocal()
        maven("https://mirrors.huaweicloud.com/repository/maven/")
    }
}

subprojects {
    apply(plugin = "common-dependencies")
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
