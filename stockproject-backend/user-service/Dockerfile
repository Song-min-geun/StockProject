# 1. 베이스 이미지 선택
# Eclipse Temurin 버전의 Java 17 이미지를 기반으로 시작합니다.
FROM eclipse-temurin:17-jdk-jammy

# 2. 작업 디렉토리 설정
# 컨테이너 내에서 명령어가 실행될 기본 경로를 /app으로 지정합니다.
WORKDIR /app

# 3. 빌드 결과물 복사
# Gradle 빌드를 통해 생성된 JAR 파일을 컨테이너 내부로 복사합니다.
# JAR 파일의 경로는 build/libs/user-service-0.0.1-SNAPSHOT.jar 입니다.
# 이 파일을 컨테이너 내 /app/app.jar 라는 이름으로 복사합니다.
COPY build/libs/*.jar app.jar

# 4. 애플리케이션 실행
# 컨테이너가 시작될 때 실행할 명령어를 정의합니다.
# "java -jar /app/app.jar" 명령어로 Spring Boot 애플리케이션을 실행합니다.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]