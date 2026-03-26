plugins {
    kotlin("kapt")
}

dependencies {
    api(project(":hubbo-dal"))
    api(libs.mapstruct)
    api("org.springframework.boot:spring-boot-starter-log4j2")
    kapt(libs.spring.context.indexer)
    api("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    api(libs.rocketmq)
    api(libs.redisson)
}
