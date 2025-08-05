# --- STAGE 1: Gradle을 이용해 Jar 파일 빌드 ---
FROM gradle:8.5.0-jdk17-focal AS builder

WORKDIR /app
COPY . .
RUN ./gradlew :stockproject-backend:media-service:build -x test --no-daemon

# --- STAGE 2: 빌드된 Jar 파일로 최종 실행 이미지 생성 ---
FROM openjdk:17-jdk-slim

# ⭐️ FFmpeg 설치 과정 추가
RUN apt-get update && apt-get install -y ffmpeg

WORKDIR /app
COPY --from=builder /app/stockproject-backend/media-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]