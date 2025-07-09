# 베이스 이미지로 Java 17 버전을 사용합니다.
FROM eclipse-temurin:17-jdk-jammy

# JAR 파일이 생성될 경로를 변수로 지정합니다.
ARG JAR_FILE_PATH=build/libs/*.jar

# 빌드된 JAR 파일을 이미지 안으로 복사합니다.
COPY ${JAR_FILE_PATH} app.jar

# 애플리케이션이 실행될 포트를 지정합니다. (각 서비스에 맞게 변경)
EXPOSE 8080

# 애플리케이션을 실행하는 명령어를 지정합니다.
ENTRYPOINT ["java", "-jar", "/app.jar"]