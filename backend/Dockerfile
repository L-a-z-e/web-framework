# backend/Dockerfile

# 1단계: 빌드 환경 설정
FROM gradle:8.5-jdk17 as builder
WORKDIR /web-framework

# 프로젝트 루트 기준 경로로 복사
COPY settings.gradle .
COPY build.gradle .
COPY gradlew .
COPY gradle/ ./gradle/
# 루트의 common 폴더를 이미지 내 /web-framework/common 으로 복사
COPY common/ ./common/
# 루트의 backend 폴더를 이미지 내 /web-framework/backend 으로 복사
COPY backend/ ./backend/

# 애플리케이션 빌드
RUN gradle :backend:bootJar --no-daemon

# 2단계: 실행 환경 설정
FROM eclipse-temurin:17-jre-jammy
WORKDIR /web-framework

# 빌드된 JAR 파일 경로 수정 (이미지 내 경로 기준)
COPY --from=builder /web-framework/backend/build/libs/*.jar backend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/web-framework/backend.jar"]
