# hubbo-sofa-web-application

~~基于SOFA Boot 4.5.0 构建的Web应用~~
Spring Boot 4.0.1
Spring Cloud 2025.1.0
JDK25
Kotlin 2.20.0
Rust edition 2024

1. 最小依赖原则
2. 模块拆分之后隔离，互不影响

## 注意事项

1. hubbo-native-invocation模块中禁止使用Kotlin，也不要使用JNI完成跨语言调用

## todo list

1. APT 代码生成
2. git2-rs和gitoxide库的评估，gitoxide push支持的不完善，实验性阶段，git2是通过FFI调用C库
3. 自动化单元测试
4. 项目启动之后再考虑逐步引入Sofa