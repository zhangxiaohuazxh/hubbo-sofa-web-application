plugins {

}

dependencies {
    api(project(":hubbo-entity"))
    api(project(":hubbo-annotations"))
    api(libs.sl4j)
    compileOnly("org.projectlombok:lombok:1.18.42")

}
