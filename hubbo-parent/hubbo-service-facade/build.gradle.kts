plugins {
    kotlin("kapt")
}

dependencies {
    api(project(":hubbo-auth"))
    api(project(":hubbo-service"))
    api(project(":hubbo-scheduler"))
    api(project(":hubbo-integration"))
    api(project(":hubbo-native-invocation"))
    kapt(libs.spring.context.indexer)
}

