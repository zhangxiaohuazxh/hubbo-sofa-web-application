plugins {

}

dependencies {
    implementation(project(":hubbo-utils"))
    compileOnly("org.projectlombok:lombok:1.18.42")
    implementation(libs.bundles.annotations)
}
