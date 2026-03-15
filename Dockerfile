# Bước 1: Build ứng dụng
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

RUN mkdir -p src/main/resources/models
RUN apt-get update && apt-get install -y curl unzip
RUN curl -L "https://drive.google.com/file/d/1xd_pAioTWO2RIpCwNjDzvDYuSmpu2Qc9/view?usp=drive_link" -o models.zip
RUN unzip -o models.zip -d src/main/resources/models && rm models.zip

COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Bước 2: Chạy ứng dụng
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]