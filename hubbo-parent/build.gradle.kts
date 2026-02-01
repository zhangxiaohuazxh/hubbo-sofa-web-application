description = "描述信息"

plugins {
}

allprojects {
    tasks.withType<Copy>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    tasks.withType<AbstractArchiveTask>().configureEach {
        // 设置重复文件处理策略为 EXCLUDE
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    tasks.withType<Zip>().configureEach {
        // 设置重复文件处理策略为 EXCLUDE
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    tasks.withType<Jar>().configureEach {
        // 设置重复文件处理策略为 EXCLUDE
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
