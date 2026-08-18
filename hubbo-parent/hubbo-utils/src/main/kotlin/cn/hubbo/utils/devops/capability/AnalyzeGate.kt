package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.config.AnalysisOptions
import cn.hubbo.utils.devops.config.GateConfig
import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.model.AnalysisReport
import cn.hubbo.utils.devops.core.model.DependencyScanReport
import cn.hubbo.utils.devops.core.model.GateEvidence
import cn.hubbo.utils.devops.core.model.GateResult

/**
 * 静态代码分析能力。
 *
 * 除语法与风格检查外，进行漏洞扫描（SAST）、依赖项检查（已知 CVE）与复杂度度量。
 */
interface StaticAnalyzer {
    /** SAST 漏洞扫描 + 复杂度度量。 */
    suspend fun analyze(ctx: PipelineContext, options: AnalysisOptions): AnalysisReport

    /** 依赖项 CVE 检查。 */
    suspend fun scanDependencies(ctx: PipelineContext, options: AnalysisOptions): DependencyScanReport
}

/**
 * 质量门禁能力。
 *
 * 基于质量阈值（覆盖率、Lint 违规数、安全漏洞等级等）多维度组合判断，
 * 输出门禁报告，决定是否允许进入下一阶段。
 */
interface QualityGate {
    suspend fun evaluate(ctx: PipelineContext, gate: GateConfig, evidence: GateEvidence): GateResult
}
