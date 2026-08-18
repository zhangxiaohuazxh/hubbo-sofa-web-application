package cn.hubbo.utils.devops

import cn.hubbo.utils.DevOpsUtils
import cn.hubbo.utils.devops.core.model.Language
import cn.hubbo.utils.devops.core.model.VcsType
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * DevOps 全局配置。
 *
 * 保留旧版字段（[url] / [basePath] / [projectName]）以兼容既有调用，
 * 新增 VCS 与语言提示，供旧版命令式方法使用。
 *
 * 新能力层建议使用各能力的 Options 配置对象（如 [cn.hubbo.utils.devops.config.CloneOptions]），
 * 本配置仅作为门面级默认值。
 */
data class DevOpsConfiguration(
    val url: String,
    val basePath: File = FileUtils.getTempDirectory(),
    val projectName: String = DevOpsUtils.parseRepositoryName(url),
    val vcs: VcsType = VcsType.GIT,
    val defaultBranch: String = "main",
    val language: Language = Language.NONE,
) {

    fun projectDirectory(): File = File("$basePath${File.separator}$projectName")

    companion object {
        val DEFAULT: DevOpsConfiguration = DevOpsConfiguration(url = "https://example.invalid/empty.git")
    }
}
