plugins {
    kotlin("kapt")
}


dependencies {
    api(project(":hubbo-web"))
    kapt(libs.spring.context.indexer)
}
