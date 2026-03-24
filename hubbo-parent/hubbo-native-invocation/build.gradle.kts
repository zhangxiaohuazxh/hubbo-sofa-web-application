plugins {
    kotlin("kapt")
}

dependencies {
    implementation(project(":hubbo-utils"))
    implementation(libs.bundles.annotations)
    kapt(libs.spring.context.indexer)
}
