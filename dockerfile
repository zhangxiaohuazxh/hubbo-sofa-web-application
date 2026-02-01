# 阶段 1: 依赖下载（最大化利用 Docker 缓存）
FROM eclipse-temurin:25-jre-alpine as dependencies

# 设置工作目录
WORKDIR /app

# 复制所有子模块的构建文件（但不复制源代码）
COPY hubbo-parent/gradlew .
COPY hubbo-parent/gradle/ gradle/
COPY hubbo-parent/build.gradle.kts .
COPY hubbo-parent/gradle.properties* ./
COPY hubbo-parent/settings.gradle.kts .

# 复制所有子模块的构建文件（但不复制源代码）
COPY hubbo-parent/hubbo-annotations/build.gradle.kts hubbo-annotations/
COPY hubbo-parent/hubbo-utils/build.gradle.kts hubbo-utils/
COPY hubbo-parent/hubbo-common/build.gradle.kts hubbo-common/
COPY hubbo-parent/hubbo-entity/build.gradle.kts hubbo-entity/
COPY hubbo-parent/hubbo-dal/build.gradle.kts hubbo-dal/
COPY hubbo-parent/hubbo-service/build.gradle.kts hubbo-service/
COPY hubbo-parent/hubbo-native-invocation/build.gradle.kts hubbo-native-invocation/
COPY hubbo-parent/hubbo-integration/build.gradle.kts hubbo-integration/
COPY hubbo-parent/hubbo-service-facade/build.gradle.kts hubbo-service-facade/
COPY hubbo-parent/hubbo-configure/build.gradle.kts hubbo-configure/
COPY hubbo-parent/hubbo-auth/build.gradle.kts hubbo-auth/
COPY hubbo-parent/hubbo-scheduler/build.gradle.kts hubbo-scheduler/
COPY hubbo-parent/hubbo-web/build.gradle.kts hubbo-web/
COPY hubbo-parent/hubbo-boot/build.gradle.kts hubbo-boot/
COPY hubbo-parent/hubbo-hubbo-native-binding/build.gradle.kts hubbo-hubbo-native-binding/

# 赋予执行权限并下载依赖
# 这一层只要构建文件不变就会被 Docker 缓存
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon || true

# 阶段 2: 编译阶段
FROM eclipse-temurin:25-jre-alpine as builder
# 设置工作目录
WORKDIR /app

# 复制依赖缓存（重要！避免重新下载）
COPY --from=dependencies /root/.gradle /root/.gradle
COPY --from=dependencies /app/ /app/

# 复制所有源代码
COPY . .

# 构建项目
RUN ./gradlew :app:bootJar -x test --no-daemon --parallel

# 阶段3 运行时环境
FROM eclipse-temurin:25-jre-alpine

# 暴露端口（根据您的应用调整）
EXPOSE 8080

# 健康检查（可选）
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 设置 JVM 参数（可根据需要调整）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]