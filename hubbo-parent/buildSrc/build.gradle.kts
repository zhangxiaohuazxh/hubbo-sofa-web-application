plugins {
    `kotlin-dsl`
    java
}


repositories {
    mavenLocal()
    gradlePluginPortal()
}

dependencies {
    api(libs.bundles.kotlin)
    implementation(libs.kotlin.allopen)
    implementation(libs.kotlin.noarg)
    implementation(libs.spring.boot.plugin)
    implementation(libs.kotlin.spring.plugin)
    implementation(libs.kotlin.jvm.plugin)
    implementation(libs.spring.dependency.management.plugin)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
