plugins {
    kotlin("kapt")
}

dependencies {
    api(libs.jooq)
    api(libs.jooq.meta)
    api(libs.jooq.kotlin)
    api(libs.bundles.r2dbc)
    api(libs.jooq.meta.kotlin)
    api(libs.postgresql.driver)
    api(project(":hubbo-common"))
    compileOnly("org.springframework.boot:spring-boot-starter")
    kapt(libs.spring.context.indexer)
}
