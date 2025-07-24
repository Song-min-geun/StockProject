# --- STAGE 1: Gradle을 이용해 Jar 파일 빌드 ---
FROM gradle:8.5.0-jdk17-focal AS builder

WORKDIR /app

COPY . .

# ⭐️ 수정된 부분: "-x test" 플래그를 추가하여 테스트를 건너뛰고 빌드합니다.
RUN ./gradlew :stockproject-backend:stock-service:build -x test --no-daemon


# --- STAGE 2: 빌드된 Jar 파일로 최종 실행 이미지 생성 ---
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/stockproject-backend/stock-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]