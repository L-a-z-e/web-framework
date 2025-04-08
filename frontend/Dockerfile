# frontend/Dockerfile

# 1단계: 빌드 환경 설정
FROM node:22-alpine as builder
WORKDIR /web-framework

# frontend 디렉토리의 package.json 등 COPY
COPY frontend/package*.json ./
# COPY frontend/yarn.lock ./ # yarn 사용 시

# 의존성 설치
RUN npm install

# frontend 디렉토리의 소스 코드 COPY
COPY frontend/ .

# application build
RUN npm run build

# 2단계: 정적 파일 서빙 환경 설정
FROM nginx:stable-alpine
RUN rm /etc/nginx/conf.d/default.conf

# frontend 디렉토리의 nginx.conf COPY
COPY frontend/nginx.conf /etc/nginx/conf.d/

# 빌드 결과물 COPY
COPY --from=builder /web-framework/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
