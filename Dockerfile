# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q package -DskipTests -Djacoco.skip=true \
    && cp target/*.jar app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/logs && chown -R spring:spring /app

COPY --from=build --chown=spring:spring /workspace/extracted/dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=spring:spring /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/extracted/application/ ./

USER spring

ENV PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- "http://localhost:${PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
