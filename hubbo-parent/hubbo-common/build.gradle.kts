plugins {

}

dependencies {
    api(project(":hubbo-utils"))
    api(project(":hubbo-entity"))
    api(project(":hubbo-annotations"))
    api(project(":hubbo-native-binding"))
    api(libs.bundles.logging)
}
