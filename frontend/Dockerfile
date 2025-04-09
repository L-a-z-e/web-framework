# =========================================
# Build Stage: Node.js 환경에서 Vue.js 빌드
# =========================================
# 베이스 이미지: Node 22 Alpine
FROM node:22-alpine as builder
# 작업 디렉토리 설정
WORKDIR /web-framework

# --- 캐싱 최적화: 1단계 - package.json 복사 및 의존성 설치 ---
# package.json/lock 파일이 변경되지 않으면 node_modules 설치 단계를 건너뛰어 캐시 활용.
COPY frontend/package.json frontend/package-lock.json* ./frontend/
# package.json이 위치한 frontend/ 디렉토리로 이동하여 npm install 실행
RUN cd frontend && npm install

# --- 2단계: Frontend 소스 코드 복사 ---
COPY frontend/ ./frontend/

# --- 3단계: 애플리케이션 빌드 ---
RUN cd frontend && npm run build

# =========================================
# Runtime Stage: Nginx로 정적 파일 서빙
# =========================================
# 베이스 이미지: Nginx Stable Alpine 사용
FROM nginx:stable-alpine
# Nginx 기본 설정을 사용하지 않으므로 삭제
RUN rm /etc/nginx/conf.d/default.conf

# --- 커스텀 Nginx 설정 복사 ---
# 호스트의 frontend/nginx.conf 파일을 이미지의 설정 디렉토리로 복사
COPY frontend/nginx.conf /etc/nginx/conf.d/

# --- 빌드 결과물 복사 ---
COPY --from=builder /web-framework/frontend/dist /usr/share/nginx/html

# --- 포트 노출 ---
EXPOSE 9001

# --- Nginx 실행 ---
# foreground에서 실행 (-g daemon off;)하여 Docker 컨테이너가 종료되지 않도록 함
CMD ["nginx", "-g", "daemon off;"]
