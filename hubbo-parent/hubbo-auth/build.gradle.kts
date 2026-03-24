plugins {
    kotlin("kapt")
}

dependencies {
    api(project(":hubbo-entity"))
    api(project(":hubbo-annotations"))
    api(libs.sl4j)
    kapt(libs.spring.context.indexer)
}
