# backend/Dockerfile

# =========================================
# Build Stage: Gradle을 사용하여 애플리케이션 빌드
# =========================================
# 베이스 이미지: gradle 8.5 + JDK 17 사용 (사용자 원본 유지)
FROM gradle:8.5-jdk17 as builder
# 작업 디렉토리 설정 (사용자 선택 유지)
WORKDIR /web-framework

# --- 캐싱 최적화: 1단계 - 빌드 설정 파일 복사 ---
# 이유: 이 파일들이 변경되지 않으면 이후 의존성 다운로드 캐시를 재사용하기 위함.
COPY gradlew gradlew.bat ./
COPY gradle ./gradle/
COPY settings.gradle build.gradle ./
# 각 모듈의 build.gradle 복사 (libs는 backend가 의존하므로 캐싱에 중요)
COPY backend/build.gradle ./backend/
#COPY libs/common-dto/build.gradle ./libs/common-dto/
#COPY libs/common-util/build.gradle ./libs/common-util/

# 배치 추가시 수정
# COPY batch/build.gradle ./batch/

# --- 캐싱 최적화: 2단계 - 의존성 다운로드 ---
# `|| return 0` 은 혹시 의존성 다운로드 중 네트워크 오류 등이 발생해도 빌드를 중단하지 않도록 합니다 (Optional).
RUN ./gradlew dependencies --no-daemon || return 0

# --- 3단계: 전체 소스 코드 복사 ---
COPY . .

# --- 4단계: Backend 애플리케이션 빌드 ---
RUN ./gradlew :backend:build -x test --no-daemon

# =========================================
# Runtime Stage: 빌드된 JAR 파일 실행
# =========================================
FROM eclipse-temurin:17-jre-jammy
# 작업 디렉토리 설정 (Build Stage와 동일하게 유지)
WORKDIR /web-framework

# --- 보안 강화: Non-root 사용자 설정 ---
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

# --- 빌드 결과물 복사 ---
COPY --from=builder /web-framework/backend/build/libs/*.jar backend.jar

# --- 기본 환경 변수 설정 ---
# docker-compose.yml 등에서 오버라이드
ENV SPRING_PROFILES_ACTIVE="default"

# --- 포트 노출 ---
EXPOSE 9001

# --- 애플리케이션 실행 ---
# 변경된 JAR 파일 이름과 경로로 실행
ENTRYPOINT ["java", "-jar", "/web-framework/backend.jar"]
