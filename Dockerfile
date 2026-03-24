# syntax=docker/dockerfile:1

################################################################################
# Stage 1: Download AI models from HuggingFace
# (models are gitignored, so they are fetched during image build)
################################################################################
FROM alpine:3.20 AS model-downloader
WORKDIR /models
RUN apk add --no-cache curl && \
    curl -fSL "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/onnx/model.onnx" -o model.onnx && \
    curl -fSL "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer.json" -o tokenizer.json && \
    curl -fSL "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer_config.json" -o tokenizer_config.json

################################################################################
# Stage 2: Resolve dependencies + Build JAR
################################################################################
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /build

COPY mvnw mvnw
COPY .mvn/ .mvn/
COPY pom.xml pom.xml
RUN chmod +x mvnw && ./mvnw dependency:go-offline -DskipTests

COPY ./src src/

RUN ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

################################################################################
# Stage 3: Lightweight Runtime
################################################################################
FROM eclipse-temurin:17-jre-jammy AS runtime

LABEL maintainer="Royal Blueberry Team" \
      org.opencontainers.image.title="Royal Blueberry API" \
      org.opencontainers.image.description="Spring Boot Dictionary API with AI Embedding"

RUN apt-get update && apt-get install -y --no-install-recommends \
    libstdc++6 \
    libgomp1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser

WORKDIR /app
ENV EMBEDDING_MODEL_DIR=/app/models

COPY --from=build /build/target/extracted/dependencies/ ./
COPY --from=build /build/target/extracted/spring-boot-loader/ ./
COPY --from=build /build/target/extracted/snapshot-dependencies/ ./
COPY --from=build /build/target/extracted/application/ ./
COPY --from=model-downloader /models/ /app/models/

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/v3/api-docs || curl -f http://localhost:8080/swagger-ui/index.html || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
