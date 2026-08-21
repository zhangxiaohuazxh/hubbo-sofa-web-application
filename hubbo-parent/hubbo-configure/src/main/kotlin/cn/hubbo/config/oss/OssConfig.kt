package cn.hubbo.config.oss

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.validation.annotation.Validated
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import aws.sdk.kotlin.services.s3.S3Client
import cn.hubbo.utils.devops.impl.S3ArtifactManager
import cn.hubbo.utils.devops.impl.S3ArtifactManagerConfig
import cn.hubbo.utils.devops.impl.S3ClientOptions
import cn.hubbo.utils.devops.impl.S3RepositorySpec
import cn.hubbo.utils.devops.impl.createS3Client
import org.springframework.context.annotation.Bean

@Configuration
@PropertySource("classpath:config/application-oss.yaml")
@ConfigurationProperties(prefix = "hubbo.oss")
@Validated
class OssConfig {

    @Valid
    lateinit var client: S3ClientOptions

    @Valid
    lateinit var artifactManager: S3ArtifactManagerConfig

    /**
     * 创建 S3Client 实例
     */
    @Bean("s3Client")
    fun createS3Client(): S3Client = createS3Client(
        cn.hubbo.utils.devops.impl.S3ClientOptions(
            endpointUrl = client.endpointUrl,
            region = client.region,
            accessKeyId = client.accessKeyId,
            secretAccessKey = client.secretAccessKey,
            forcePathStyle = client.forcePathStyle,
            maxAttempts = client.maxAttempts,
            connectTimeoutSeconds = client.connectTimeoutSeconds,
            readTimeoutSeconds = client.readTimeoutSeconds,
            writeTimeoutSeconds = client.writeTimeoutSeconds,
            enableAwsChunked = client.enableAwsChunked,
            forceHttp11 = client.forceHttp11,
        )
    )

    /**
     * 创建 S3ArtifactManager 实例
     */
    fun createS3ArtifactManager(): S3ArtifactManager = S3ArtifactManager.create(
        createS3Client(),
        artifactManager,
    )
}