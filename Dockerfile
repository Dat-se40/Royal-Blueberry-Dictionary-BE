# syntax=docker/dockerfile:1

################################################################################
# Stage 1: Tải model AI từ HuggingFace
################################################################################
FROM alpine:latest as model-downloader
WORKDIR /models
RUN apk add --no-cache curl

# Tải 3 files cần thiết cho EmbeddingService
RUN curl -L "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/onnx/model.onnx" -o model.onnx
RUN curl -L "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer.json" -o tokenizer.json
RUN curl -L "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer_config.json" -o tokenizer_config.json

################################################################################
# Stage 2: Resolve dependencies
################################################################################
FROM eclipse-temurin:17-jdk-jammy as deps
WORKDIR /build
COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -DskipTests

################################################################################
# Stage 3: Build & Package 
################################################################################
FROM deps as package
WORKDIR /build
COPY ./src src/

# Copy model từ stage 1 vào thư mục resources của source code
COPY --from=model-downloader /models/model.onnx src/main/resources/models/
COPY --from=model-downloader /models/tokenizer.json src/main/resources/models/
COPY --from=model-downloader /models/tokenizer_config.json src/main/resources/models/

RUN --mount=type=bind,source=pom.xml,target=pom.xml \
    --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

################################################################################
# Stage 4: Extract layers
################################################################################
FROM package as extract
WORKDIR /build
RUN java -Djarmode=layertools -jar target/app.jar extract --destination target/extracted

################################################################################
# Stage 5: Final Runtime
################################################################################
FROM eclipse-temurin:17-jre-jammy AS final

# Cài đặt thêm thư viện C++ cần thiết cho ONNX Runtime chạy trên Linux
RUN apt-get update && apt-get install -y --no-install-recommends \
    libstdc++6 \
    libgomp1 \
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
USER appuser

WORKDIR /app

# Copy các layers đã extract
COPY --from=extract build/target/extracted/dependencies/ ./
COPY --from=extract build/target/extracted/spring-boot-loader/ ./
COPY --from=extract build/target/extracted/snapshot-dependencies/ ./
COPY --from=extract build/target/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT [ "java", "org.springframework.boot.loader.launch.JarLauncher" ]