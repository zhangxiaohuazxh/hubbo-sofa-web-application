package cn.hubbo.utils.oss

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.*
import aws.smithy.kotlin.runtime.content.ByteStream
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
 * 分片上传配置
 */
data class MultipartUploadConfig(
    val partSize: Long = 8 * 1024 * 1024,  // 8MB 默认分片大小
    val minPartSize: Long = 5 * 1024 * 1024, // 5MB 最小分片 (S3 限制，最后一片除外)
    val maxParts: Int = 10000,              // S3 最大分片数
    val concurrency: Int = 4,               // 并发上传数
    val threshold: Long = 16 * 1024 * 1024, // 超过此大小自动分片 (16MB)
)

/**
 * 分片上传结果
 */
data class MultipartUploadResult(
    val bucket: String,
    val key: String,
    val uploadId: String,
    val etag: String,
    val parts: List<CompletedPart>,
    val totalSize: Long,
)

/**
 * 分片范围（重命名为 UploadPartRange 避免与 AWS SDK 的 CompletedPart 冲突）
 */
data class UploadPartRange(
    val partNumber: Int,
    val offset: Long,
    val size: Long,
)

/**
 * 判断是否需要分片上传
 */
fun shouldUseMultipart(fileSize: Long, config: MultipartUploadConfig = MultipartUploadConfig()): Boolean =
    fileSize >= config.threshold

/**
 * 计算分片信息
 */
fun calculateParts(fileSize: Long, config: MultipartUploadConfig = MultipartUploadConfig()): List<UploadPartRange> {
    val partSize = config.partSize
    val parts = mutableListOf<UploadPartRange>()
    var offset = 0L
    var partNumber = 1

    while (offset < fileSize) {
        val remaining = fileSize - offset
        val size = minOf(partSize, remaining)
        parts.add(UploadPartRange(partNumber, offset, size))
        offset += size
        partNumber++
    }

    if (parts.size > 1) {
        val lastPart = parts.last()
        if (lastPart.size < config.minPartSize) {
            val secondLast = parts[parts.size - 2]
            parts[parts.size - 2] = UploadPartRange(
                secondLast.partNumber,
                secondLast.offset,
                secondLast.size + lastPart.size
            )
            parts.removeLast()
        }
    }

    require(parts.size <= config.maxParts) {
        "分片数 ${parts.size} 超过最大限制 ${config.maxParts}，请增大 partSize"
    }

    return parts
}
