package cn.hubbo.utils

object DevOpsUtils {


    fun parseRepositoryName(url: String): String {
        require(url.isNotBlank()) { "repository url must not be blank" }
        val lastSlash = url.lastIndexOf("/")
        val name = if (lastSlash >= 0) url.substring(lastSlash + 1) else url
        return name.removeSuffix(".git").ifBlank { "repository" }
    }


}