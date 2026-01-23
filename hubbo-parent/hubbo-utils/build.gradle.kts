plugins {

}

dependencies {

    api(libs.bundles.moshi)
    api(libs.bundles.google)
    api(libs.bundles.apache)
    api(libs.bundles.alipay)
    api(libs.bundles.alibaba)
    api(libs.bundles.reactive)
    api(libs.bundles.squareup)
    api(libs.bundles.coroutines)
    api(libs.bundles.annotations)
    api(libs.sl4j)
    api(libs.disruptor)
    //    ksp("com.squareup.moshi:moshi-kotlin-codegen")
    api(project(":hubbo-annotations"))

}
