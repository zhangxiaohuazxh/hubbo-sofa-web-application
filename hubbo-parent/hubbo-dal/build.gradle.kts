plugins {
    //    id("nu.studer.jooq")
    //    id("org.jooq.jooq-codegen-gradle")
}

dependencies {
    api(libs.jooq.meta)
    api(libs.jooq.kotlin)
    api(libs.bundles.r2dbc)
    api(libs.jooq.meta.kotlin)
    api(libs.postgresql.driver)
    api(project(":hubbo-common"))
    api("org.springframework.boot:spring-boot-starter-jooq")
}
