plugins {

}

dependencies {
    api(libs.postgresql.r2dbc)
    api(libs.postgresql.driver)
    api(project(":hubbo-common"))
    implementation(libs.jooq.meta.kotlin)
    api("org.springframework.boot:spring-boot-starter-jooq")
}
