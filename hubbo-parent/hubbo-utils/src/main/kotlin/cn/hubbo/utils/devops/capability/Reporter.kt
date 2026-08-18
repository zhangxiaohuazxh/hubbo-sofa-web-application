package cn.hubbo.utils.devops.capability

import cn.hubbo.utils.devops.core.PipelineContext
import cn.hubbo.utils.devops.core.model.NotificationEvent
import cn.hubbo.utils.devops.core.model.Report
import cn.hubbo.utils.devops.core.model.ReportData
import cn.hubbo.utils.devops.core.model.ReportFormat
import java.nio.file.Path

/**
 * 通知能力。
 *
 * 在各阶段完成后发送通知（邮件 / Slack / Webhook 等）。
 * 通知是流水线的副作用通道，实现应当吞掉投递异常并记录日志，避免阻断流水线。
 */
interface Notifier {
    suspend fun notify(ctx: PipelineContext, event: NotificationEvent)
}

/**
 * 报告能力。
 *
 * 生成结构化报告（JSON / HTML / JUnit XML），供外部系统集成。
 */
interface Reporter {
    suspend fun generate(ctx: PipelineContext, data: ReportData, format: ReportFormat, targetDirectory: Path): Report
}
