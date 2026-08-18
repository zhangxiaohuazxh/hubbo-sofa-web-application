package cn.hubbo.utils.devops.core.model

import cn.hubbo.utils.devops.core.Stage
import java.nio.file.Path
import java.time.Instant

/** 报告格式。 */
enum class ReportFormat { JSON, HTML, JUNIT_XML, MARKDOWN, PLAIN_TEXT }

/** 生成的报告文件元信息。 */
data class Report(
    val format: ReportFormat,
    val path: Path,
    val sizeBytes: Long,
    val generatedAt: Instant = Instant.now(),
)

/** 报告输入数据：聚合各阶段结果，供 Reporter 渲染为结构化报告。 */
data class ReportData(
    val pipelineName: String,
    val runId: String? = null,
    val stageResults: List<StageRunResult> = emptyList(),
    val testReport: TestReport? = null,
    val lintReport: LintReport? = null,
    val gateResult: GateResult? = null,
    val analysisReport: AnalysisReport? = null,
    val artifacts: List<Artifact> = emptyList(),
    val extra: Map<String, Any?> = emptyMap(),
)

/** 通知渠道。 */
enum class NotificationChannel { EMAIL, SLACK, WEBHOOK, TEAMS, SMS }

/** 通知事件。 */
data class NotificationEvent(
    val channel: NotificationChannel,
    val subject: String,
    val body: String,
    val stage: Stage? = null,
    val status: PipelineStatus? = null,
    val recipients: List<String> = emptyList(),
    val attachments: List<Path> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)
