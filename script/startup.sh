java -jar -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./dump.hprof ./hubbo-parent/hubbo-boot/target/hubbo-sofa-boot.jar --spring.profiles.active=dev

# 启用压缩对象头特性
# -XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders

# 启用堆转储
#-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./dump.hprof

# JDK AOT Cache JVM 会观察本次运行中加载的类、链接的方法以及 JEP 515（AOT Method Profiling）收集的热点方法分析数据，当你正常关闭程序（Ctrl+C 或程序自行结束）时，JVM 会将这些信息打包成 app.aot 文件。
#-XX:AOTCacheOutput=app.aot

#加载AOT缓存，JVM启动时直接加载缓存中的类布局和预编译的方法
#-XX:AOTCache=app.aot

# Spring Boot4与aot相关的配置 刷新容器后自动退出
#-Dspring.context.exit=onRefresh