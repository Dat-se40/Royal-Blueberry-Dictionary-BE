# Bước 1: Build ứng dụng
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Tạo folder models
RUN mkdir -p src/main/resources/models

# Cài curl
RUN apt-get update && apt-get install -y curl

# Tải 3 files từ HuggingFace
RUN curl -L "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/onnx/model.onnx" -o src/main/resources/models/model.onnx
RUN curl -L "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer.json" -o src/main/resources/models/tokenizer.json
RUN curl -L "https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2/resolve/main/tokenizer_config.json" -o src/main/resources/models/tokenizer_config.json

# Verify files tải được
RUN ls -lh src/main/resources/models/

# Build app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Bước 2: Chạy ứng dụng (dùng Ubuntu thay vì Alpine để có C++ libraries)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    libstdc++6 \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar


EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]