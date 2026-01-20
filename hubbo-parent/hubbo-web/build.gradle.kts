plugins {
    id("common-dependencies")
}

dependencies {
    implementation(project(":hubbo-configure"))
    api(project(":hubbo-service-facade"))
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-log4j2")
}
