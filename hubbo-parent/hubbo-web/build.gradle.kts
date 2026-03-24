plugins {
    id("common-dependencies")
    kotlin("kapt")
}

dependencies {
    api(project(":hubbo-configure"))
    api(project(":hubbo-service-facade"))
    api("org.springframework.boot:spring-boot-starter-webflux")
    kapt(libs.spring.context.indexer)
}
