# ==================================
# 1. 빌드(Build) 단계
# ==================================
# - Java 17 및 Gradle 환경을 사용하여 프로젝트를 빌드합니다.
# - 빌드에 필요한 의존성만 먼저 받아와서 레이어 캐싱을 활용, 빌드 속도를 높입니다.
FROM gradle:8.5.0-jdk17-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /build

# 빌드에 필요한 파일만 먼저 복사하여 Docker 레이어 캐싱을 극대화
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (이 단계는 의존성이 변경될 때만 다시 실행됩니다)
RUN gradle dependencies

# 전체 소스 코드 복사
COPY src ./src

# Gradle 빌드 실행 (테스트는 생략하여 빌드 시간 단축)
RUN gradle build --no-daemon -x test


# ==================================
# 2. 실행(Runtime) 단계
# ==================================
# - 실제 서비스 실행에 필요한 최소한의 환경만 포함하여 이미지 크기를 줄이고 보안을 강화합니다.
# - 빌드 단계에서 생성된 JAR 파일만 가져와서 사용합니다.
FROM eclipse-temurin:17-jre-jammy

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 실행에 필요한 최소한의 사용자 생성 (보안 강화)
# - 루트(root) 사용자로 실행하는 것을 방지합니다.
RUN groupadd --system --gid 1001 appgroup && \
    useradd --system --uid 1001 --gid appgroup appuser
USER appuser

# 빌드 단계(builder)에서 생성된 JAR 파일을 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# 애플리케이션이 사용할 포트를 명시적으로 문서화
EXPOSE 8080

# 컨테이너 시작 시 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]