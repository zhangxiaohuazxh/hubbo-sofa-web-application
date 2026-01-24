plugins {

}

dependencies {
    testImplementation(project(":hubbo-boot"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    add("kspTest", libs.moshi.kotlin.codegen)
    testImplementation(libs.jooq.codegen)
    //    kapt("com.squareup.moshi:moshi-kotlin-codegen")
}
