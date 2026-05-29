# ===== 构建阶段 =====
# 注：本环境镜像仓库可能需配置国内加速；Maven 走项目级阿里云镜像
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# 先拷贝 pom 与镜像配置，最大化利用依赖缓存层
COPY apps/api/pom.xml ./pom.xml
COPY deploy/maven-settings.xml /root/.m2/settings.xml
RUN mvn -s /root/.m2/settings.xml -B dependency:go-offline
# 再拷贝源码打包（测试在 CI 中单独跑）
COPY apps/api/src ./src
RUN mvn -s /root/.m2/settings.xml -B clean package -DskipTests

# ===== 运行阶段 =====
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# 健康检查需要 curl
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -fs http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
