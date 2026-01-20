description = "描述信息"

plugins {
    alias(libs.plugins.ksp) apply false
}

allprojects {
    tasks.withType<Copy>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    group = "com.hubbo"
    version = "0.0.1"
    repositories {
        mavenLocal()
        maven("https://maven.aliyun.com/repository/public")
    }
}

subprojects {
    apply(plugin = "common-dependencies")
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
