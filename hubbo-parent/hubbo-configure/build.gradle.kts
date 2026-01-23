plugins {

}

dependencies {
    implementation(libs.bundles.annotations)
    compileOnly(libs.bundles.r2dbc)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
