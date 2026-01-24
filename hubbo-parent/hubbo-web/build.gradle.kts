plugins {
    id("common-dependencies")
}

dependencies {
    api(project(":hubbo-configure"))
    api(project(":hubbo-service-facade"))
    api("org.springframework.boot:spring-boot-starter-webflux")
}
