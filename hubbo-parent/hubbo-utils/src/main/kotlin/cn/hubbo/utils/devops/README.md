# DevOps 能力抽象层

覆盖从代码提交到交付部署的全生命周期 CI/CD 能力抽象，基于 Kotlin 协程设计，
接口隔离、配置驱动、统一错误模型、可观测、可扩展。

## 包结构

```
cn.hubbo.utils.devops
├── DevOps.kt                    # 门面：聚合全部能力 + 便捷方法（含旧版过渡方法）
├── DevOpsConfiguration.kt       # 门面级全局配置
├── core/
│   ├── Stage.kt                 # 阶段枚举 + 执行状态
│   ├── StepResult.kt            # 结构化执行结果（Success / Failure / Skipped）
│   ├── PipelineContext.kt       # 上下文（等价 Go context.Context）
│   ├── error/                   # 统一错误模型（ErrorCode / DevOpsError / Errors）
│   └── model/                   # 核心数据结构（Artifact、TestReport、GateResult、PipelineRun …）
├── config/                      # 各能力的配置对象（CloneOptions、TestOptions、GateConfig …）
├── capability/                  # 14 个子接口（接口隔离，避免胖接口）
├── extension/                   # 扩展点（Hook / Plugin / StageExecutor / CacheStore）
├── impl/                        # 组合装配 + 语言实现（Java/Maven、Rust/Cargo）
└── mock/                        # 测试辅助（内存 Mock / Noop 兜底）
```

## 设计要点

### 1. 接口隔离
`DevOps` 不再承载所有行为，而是聚合 14 个细粒度子接口：

| 子接口 | 阶段 | 说明 |
| --- | --- | --- |
| `SourceManager` | CLONE | Git/SVN 拉取，认证、代理、修订版本 |
| `SyntaxChecker` | SYNTAX_CHECK | 多语言语法验证 |
| `Tester` | TEST | 单测/集成/E2E，覆盖率 |
| `Linter` | LINT | golangci-lint / Checkstyle / ESLint / Pylint |
| `StaticAnalyzer` | STATIC_ANALYSIS | SAST / CVE / 复杂度 |
| `QualityGate` | QUALITY_GATE | 多维度阈值门禁 |
| `Compiler` | COMPILE | 编译、交叉编译 |
| `Builder` | BUILD | JAR/WAR/Docker 镜像等打包 |
| `ArtifactManager` | ARTIFACT_UPLOAD | Nexus/JFrog/容器仓库 |
| `Deployer` | DEPLOY | 滚动/蓝绿/金丝雀 + 回滚 |
| `PipelineOrchestrator` | — | 编排：顺序/并行/条件/暂停/重试/超时 |
| `Notifier` | — | 邮件/Slack/Webhook |
| `Reporter` | REPORT | JSON/HTML/JUnit XML |
| `EnvironmentManager` | — | 环境配置与密钥注入 |

### 2. 上下文传递
每个能力方法第一个参数均为 `PipelineContext`：
- 携带 `CoroutineContext`（取消/超时）、日志、指标、阶段、工作区；
- `attributes` 为并发安全共享状态，支持并行阶段间传值；
- 内置 `trace` 统一计时与指标埋点。

### 3. 错误模型
- `DevOpsError`：错误码 + 阶段 + 可恢复性（`RECOVERABLE`/`FATAL`）+ 结构化上下文；
- 编排器依据 `recoverability` 决定重试或终止；
- `Errors` 提供统一模板（`toolNotFound`、`timedOut`、`invalidConfig` …）。

### 4. 可配置性
所有行为通过 Options 参数化，不硬编码：
- 代码拉取：`CloneOptions`（修订版本用 sealed `RevisionSpec`，认证用 sealed `AuthSpec`）；
- 门禁：`GateConfig` 声明式规则（维度 × 算子 × 阈值 × 动作）。

### 5. 版本演进
- 旧版命令式方法保留并标记 `@Deprecated`，过渡期间不破坏既有调用；
- 可选特性探测：`supportedVcs` / `supportedTools` / `supportedLanguages` 暴露能力范围，
  调用方可用 `is` 智能转换访问更细粒度接口。

### 6. 并发安全
- 能力实现必须线程安全；
- `PipelineContext.attributes` 使用并发容器；
- 所有长耗时操作为 `suspend`，支持结构化并发取消。

### 7. 扩展机制
- **Hook**：`HookRegistry` 在 26 个触发点前后执行自定义逻辑（编译前脚本、部署后健康检查）；
- **Plugin**：`Plugin` 通过 `PluginRegistry` 绑定/覆盖能力实现（工具可插拔）；
- **分布式执行**：`StageExecutor` 抽象阶段执行，可序列化 `StageDefinition` 调度到远程节点；
- **缓存**：`CacheStore` 提供依赖/构建缓存抽象（默认 `NoopCacheStore`）。

## 实现指南

### 组合子接口
推荐用门面 Builder 装配（生产环境也可用 DI 框架）：

```kotlin
val ops = DevOps.builder()
    .configuration(DevOpsConfiguration("https://git.example.com/repo.git"))
    .sourceManager(GitSourceManager())
    .builder(MavenBuilder())
    .hooks(myHookRegistry)
    .plugin(MyLintPlugin())
    .build()
```

### 编排一条流水线
```kotlin
val ctx = PipelineContexts.default(pipelineName = "ci", stage = Stage.CLONE)

ops.runPipeline(ctx, PipelineDefinition(
    name = "ci",
    stages = listOf(
        StageDefinition("clone", Stage.CLONE, options = mapOf("url" to repoUrl)),
        StageDefinition("test", Stage.TEST, dependsOn = setOf("clone")),
        StageDefinition("gate", Stage.QUALITY_GATE, dependsOn = setOf("test")),
        StageDefinition("deploy", Stage.DEPLOY, dependsOn = setOf("gate"), condition = "branch == 'main'"),
    ),
))
```

### 新增一种 Linter / 测试框架 / 部署策略
1. 实现对应子接口（`Linter` / `Tester` / `Deployer`）；
2. 实现 `Plugin`，在 `apply(registry)` 中 `registry.bind(Linter::class, MyLinter())`；
3. 将插件加入 `DevOps.builder().plugin(myPlugin).build()`；
4. 在配置中通过 `tool` / `strategy` 等字段路由。

### 单元测试
`MockDevOps` 全部能力为内存 Mock，可直接驱动调用方逻辑：

```kotlin
val ops = MockDevOps()
val ctx = PipelineContexts.default()
val report = ops.test(ctx, TestOptions(type = TestType.UNIT, coverageEnabled = true))
assertEquals(1.0, report.passRate)
```
