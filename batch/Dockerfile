# =========================================
# Build Stage: Gradle을 사용하여 애플리케이션 빌드
# =========================================
FROM gradle:8.5-jdk17 as builder
WORKDIR /web-framework

# --- Gradle 빌드 캐싱 최적화 ---
COPY gradlew gradlew.bat ./
COPY gradle ./gradle/
COPY settings.gradle build.gradle ./
# 관련 모듈들의 build.gradle 복사
COPY batch/build.gradle ./batch/
#COPY libs/common-dto/build.gradle ./libs/common-dto/
#COPY libs/common-util/build.gradle ./libs/common-util/
# backend 모듈의 build.gradle 도 복사 (의존성 트리 파악에 도움될 수 있음)
# COPY backend/build.gradle ./backend/

# --- 의존성 다운로드 (캐싱) ---
RUN ./gradlew dependencies --no-daemon || return 0

# --- 전체 소스 코드 복사 ---
COPY . .

# --- Batch 애플리케이션 빌드 ---
# !!! 빌드 대상 모듈을 :batch 로 지정 !!!
RUN ./gradlew :batch:build -x test --no-daemon

# =========================================
# Runtime Stage: 빌드된 JAR 파일 실행
# =========================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /web-framework

# --- Non-root 사용자 설정 ---
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

# --- 빌드 결과물 복사 ---
# !!! batch 모듈의 빌드 결과 경로 및 최종 JAR 이름 지정 !!!
ARG JAR_FILE=batch/build/libs/*.jar
COPY --from=builder /web-framework/${JAR_FILE} batch.jar

# --- 기본 환경 변수 ---
ENV SPRING_PROFILES_ACTIVE="local"

# --- 포트 노출 (보통 Batch는 웹 서버가 없으므로 불필요) ---
# EXPOSE 8081

# --- 애플리케이션 실행 ---
# !!! 실행할 JAR 이름 수정 !!!
# 기본 실행 (Job 이름 등은 외부에서 환경 변수나 인수로 전달)
ENTRYPOINT ["java", "-jar", "/web-framework/batch.jar"]

# 예시: 환경 변수로 Job 이름을 받아 실행
# ENTRYPOINT ["java", "-jar", "/web-framework/batch.jar", "--spring.batch.job.name=${JOB_NAME}"]
