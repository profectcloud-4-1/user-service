# ---------------- Build ----------------
FROM bellsoft/liberica-runtime-container:jdk-17-musl AS builder
WORKDIR /workspace/app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew
RUN ./gradlew --no-daemon dependencies || true

COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---------------- Runtime ----------------
FROM bellsoft/liberica-runtime-container:jre-17-musl

WORKDIR /app

COPY --from=builder /workspace/app/build/libs/*.jar /app/app.jar

# Java Agent 다운로드
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.9.0/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar

ENV SPRING_PROFILES_ACTIVE=dev,secret
ENV TZ=Asia/Seoul
ENV OTEL_SERVICE_NAME=user-service
ENV OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4317
ENV OTEL_EXPORTER_OTLP_PROTOCOL=grpc

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java \
 -javaagent:/otel/opentelemetry-javaagent.jar \
 -Dotel.service.name=$OTEL_SERVICE_NAME \
 -Dotel.exporter.otlp.endpoint=$OTEL_EXPORTER_OTLP_ENDPOINT \
 -Dotel.exporter.otlp.protocol=$OTEL_EXPORTER_OTLP_PROTOCOL \
 -Duser.timezone=$TZ \
 -jar /app/app.jar"]