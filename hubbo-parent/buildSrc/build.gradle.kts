plugins {
    `kotlin-dsl`
    java
    alias(libs.plugins.kapt)
}


repositories {
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.ksp)
    api(libs.bundles.kotlin)
    implementation(libs.kotlin.kapt)
    implementation(libs.kotlin.allopen)
    implementation(libs.kotlin.noarg)
    implementation(libs.bundles.moshi)
    implementation(libs.spring.boot.plugin)
    implementation(libs.kotlin.spring.plugin)
    implementation(libs.kotlin.jvm.plugin)
    implementation(libs.spring.dependency.management.plugin)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
