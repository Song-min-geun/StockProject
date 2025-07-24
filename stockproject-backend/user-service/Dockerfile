# 이 Dockerfile은 프로젝트의 최상위 폴더에서 실행되도록 설계되었습니다.

# --- STAGE 1: Gradle을 이용해 Jar 파일 빌드 ---
FROM gradle:8.5-jdk17-focal AS builder

# 컨테이너 안의 작업 폴더 설정
WORKDIR /app

# 프로젝트 전체 파일을 컨테이너 안으로 복사
# 이렇게 하면 Dockerfile이 필요한 모든 파일(gradlew, settings.gradle, 소스코드 등)에 접근할 수 있습니다.
COPY . .

# user-service 모듈만 특정해서 빌드 실행 (전체 빌드보다 빠름)
RUN ./gradlew :stockproject-backend:user-service:build --no-daemon


# --- STAGE 2: 빌드된 Jar 파일로 최종 실행 이미지 생성 ---
FROM openjdk:17-jdk-slim

# 컨테이너 안의 작업 폴더 설정
WORKDIR /app

# builder 스테이지에서 빌드된 결과물(.jar 파일)만 복사
COPY --from=builder /app/stockproject-backend/user-service/build/libs/*.jar app.jar

# 컨테이너가 시작될 때 이 명령어로 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]