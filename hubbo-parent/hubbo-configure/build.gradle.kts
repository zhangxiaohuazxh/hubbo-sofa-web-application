plugins {

}

dependencies {
    implementation(libs.bundles.annotations)
    compileOnly(libs.bundles.r2dbc)
    compileOnly("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
