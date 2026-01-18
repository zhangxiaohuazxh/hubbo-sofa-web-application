plugins {

}

dependencies {
    implementation(project(":hubbo-utils"))
}


java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}