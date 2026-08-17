package cn.hubbo.utils

object DevOpsUtils {


    fun parseRepositoryName(url: String): String {
        return url.substring(url.lastIndexOf("/") + 1).replace(".git", "")
    }


}