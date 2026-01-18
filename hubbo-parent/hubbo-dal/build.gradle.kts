plugins {

}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")
    api(project(":hubbo-common"))
    api("org.springframework.boot:spring-boot-starter-jooq:4.0.1")
    api(libs.postgresql.r2dbc)
    api(libs.postgresql.driver)
    implementation("org.jooq:jooq-kotlin:3.20.10")
    implementation("org.jooq:jooq-meta-kotlin:3.20.10")
}
