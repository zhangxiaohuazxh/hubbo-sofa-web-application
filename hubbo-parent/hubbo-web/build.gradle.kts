plugins {
    id("common-dependencies")
}

dependencies {
    implementation(project(":hubbo-configure"))
    api(project(":hubbo-service-facade"))
    api("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}
