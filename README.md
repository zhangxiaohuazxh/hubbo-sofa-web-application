# hubbo-sofa-web-application

开发环境：<br/>
Spring Boot 4.0.1 <br/>
Spring Cloud 2025.1.0 <br/>
JDK25 <br/>
Kotlin 2.3.0 <br/>
Rust edition 2024 <br/>

## 技术选型

## todo list

1.[ ] 引入协程库支持
2.[ ] APT 代码生成
3.[ ] 构建工具切换，maven -> gradle
4.[ ] git2-rs和gitoxide库的评估，gitoxide push支持的不完善，实验性阶段，git2是通过FFI调用C库
5.[ ] 自动化单元测试
6. [ ] 渐进式引入Sofa
7. [ ] 引入流水线，自举

分支：<br/>
master 主线 <br/>
dev 开发分支 <br/>
feature 模块开发分支 <br/>
bugfix 错误修复分支 <br/>

feature -> dev -> master <br/>
dev到master需要过渡两个版本之后再合入主线 <br/>

dev：

1. 减少运行期的损耗，编译期 > 运行期，优先编译期解决
2. 减少反射的使用
3. 最小化依赖
4. 跨平台兼容，native选用支持多平台的lib