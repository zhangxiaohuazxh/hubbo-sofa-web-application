import gradle.kotlin.dsl.accessors._beddff3d7d050b6e161a3a7cbd15896e.distributions
import gradle.kotlin.dsl.accessors._beddff3d7d050b6e161a3a7cbd15896e.main
import gradle.kotlin.dsl.accessors._beddff3d7d050b6e161a3a7cbd15896e.runtimeClasspath
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("application")
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("io.spring.dependency-management")
    id("org.jetbrains.kotlin.plugin.spring")
    id("org.springframework.boot")
}

repositories {
    mavenLocal()
    gradlePluginPortal()
}

val libs = rootProject.extensions.getByType<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

dependencies {
    val springBom = libs.findLibrary("spring-boot-dependencies").get()
    api(platform(springBom))
    add("implementation", libs.findLibrary("kotlin-stdlib").get())
    add("implementation", libs.findLibrary("kotlin-reflect").get())
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}


application {
    mainClass.set("cn.hubbo.SofaWebApplication")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<BootJar> {
    archiveFileName.set("${project.name}-${project.version}.jar")
    archiveClassifier.set("executable")
    layered {
        enabled.set(true)
    }
    springBoot {
        mainClass.set("cn.hubbo.SofaWebApplication")
    }
    exclude("**/rebel.xml", "**/test/*.class")
}

// flat jar 扁平化包，在需要进行字节码优化时使用，也只能这个优化后的产物jar包去运行
plugins.withId("java") {
    if (project.name == "hubbo-boot") {
        tasks.register<Jar>("flatJar") {
            archiveFileName = "${project.name}-${project.version}-flat-with-all-dependencies.jar" // 自定义jar文件名
            // 包含主代码的类文件
            from(sourceSets.main.get().output)
            // 包含所有runtime依赖（解压后合并）
            dependsOn(configurations.runtimeClasspath)
            from({
                configurations.runtimeClasspath.get().filter {
                    it.name.endsWith(".jar")
                }.map {
                    zipTree(it)
                }
            })
            // 解决重复文件问题
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            // 设置manifest
            manifest {
                attributes(
                    "Main-Class" to "cn.hubbo.SofaWebApplication",
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version
                )
            }
            // 排除特定文件以避免冲突
            // 排除签名文件和元数据文件，避免JAR打包时出现冲突
            exclude("META-INF/*.SF")
            exclude("META-INF/*.DSA")
            exclude("META-INF/*.RSA")
            // 排除Spring相关元数据，避免启动时出现问题
            exclude("META-INF/spring.*")
            // 排除服务发现配置文件，防止依赖冲突
            exclude("META-INF/services/*")
            // 排除版本特定资源
            exclude("META-INF/versions/**")
            // 排除索引和清单文件
            exclude("META-INF/INDEX.LIST")
            exclude("META-INF/MANIFEST.MF")
        }
    }
}

distributions {
    main {
        contents {
            from(configurations.runtimeClasspath) {
                into("lib")
            }
            from("src/main/resources/config") {
                into("config")
                include("**/*.properties", "**/*.yml", "**/*.yaml", "**/*.xml")
            }
            from("src/main/resources/logging") {
                into("logging")
            }
            from("../../../script/startup.sh") {
                into("script")
            }
            from(rootProject.file("LICENSE"))
            from(rootProject.file("README.md"))
        }
    }
}

// zip包插件
plugins.withId("distribution") {
    val distContainer = extensions.getByType<org.gradle.api.distribution.DistributionContainer>()
    if (project.name == "hubbo-boot") {
        tasks.register<Zip>("zip") {
            archiveFileName.set("${project.name}-${project.version}.zip")
            destinationDirectory.set(layout.buildDirectory.dir("dist"))
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            val mainDist = distContainer.named("main")
            with(mainDist.get().contents)
            val configProject = project.parent?.findProject(":hubbo-configure")
            if (configProject != null) {
                from(configProject.projectDir.resolve("src/main/resources/config")) {
                    into("config")
                    include("**/*.properties", "**/*.yml", "**/*.yaml", "**/*.xml")
                }
                from(configProject.projectDir.resolve("src/main/resources/logging")) {
                    into("logging")
                }
            }
            from(rootProject.projectDir.resolve("../script")) {
                into("script")
            }
        }
    }
}

// todo 构建支持增强 启动脚本 编译优化 字节码优化工具引入。。。


