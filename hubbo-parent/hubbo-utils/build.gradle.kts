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
    api("jakarta.validation:jakarta.validation-api")
    api(libs.sl4j)
    api(libs.disruptor)
    //    ksp("com.squareup.moshi:moshi-kotlin-codegen")
    api(project(":hubbo-annotations"))
    api(libs.awssdk)
    api(libs.s3.kotlin)
    // AWS SDK for Kotlin 的 HTTP 引擎：必须在运行期类路径上，否则 S3Client 构建时找不到引擎
    implementation(libs.smithy.okhttp.engine)
    api(libs.snakeyaml)
    api(libs.commons.exec)
    api(libs.jbang)
    api(libs.jgit)
    api(libs.jgit.ssh.apache)
    api(libs.netty.incubator.codec.http3)
}
