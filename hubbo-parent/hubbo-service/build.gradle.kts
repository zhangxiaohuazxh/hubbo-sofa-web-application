plugins {
    kotlin("kapt")
}

dependencies {
    api(project(":hubbo-dal"))
    api(libs.mapstruct)
    api("org.springframework.boot:spring-boot-starter-log4j2")
    kapt(libs.spring.context.indexer)
}
