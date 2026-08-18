package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.config.BuildOptions
import cn.hubbo.utils.devops.config.BuildTool
import cn.hubbo.utils.devops.config.CompileOptions
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.model.BuildResult
import cn.hubbo.utils.devops.core.model.CompileResult

/**
 * 编译能力。
 *
 * 将源代码编译为中间产物（.class / .o / 可执行文件），
 * 支持多平台交叉编译与编译参数（优化级别、宏定义等）。
 */
interface Compiler {
    suspend fun compile(ctx: PipelineContext, options: CompileOptions): CompileResult
}

/**
 * 构建能力。
 *
 * 将编译产物与依赖资源打包为交付物（JAR / WAR / Docker 镜像 / 压缩包），
 * 对 Maven、Gradle、Make、Dockerfile 等构建工具做抽象调用。
 */
interface Builder {
    /** 支持的构建工具集合。 */
    val supportedTools: Set<BuildTool>

    suspend fun build(ctx: PipelineContext, options: BuildOptions): BuildResult
}
