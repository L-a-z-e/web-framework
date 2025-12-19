# Web Framework - 풀스택 웹 애플리케이션 프레임워크

모던 웹 애플리케이션 아키텍처를 구현하는 **풀스택 프레임워크**입니다. **Spring Boot 3.x** 백엔드, **Vue 3** 프론트엔드, **Batch Processing** 서비스로 구성된 마이크로서비스 기반 플랫폼입니다.

---

## 🎯 프로젝트 개요

### 핵심 기술 스택

| 영역 | 기술 | 버전 |
|------|------|------|
| **Backend** | Spring Boot | 3.4.4 |
| **Frontend** | Vue.js | 3.5.13 |
| **Build Tool** | Gradle | 8.x |
| **Database** | MariaDB | Latest |
| **Container** | Docker & Docker Compose | Latest |
| **Node Package Manager** | Yarn | 1.22.22 |
| **Frontend Build** | Vite | 6.2.0 |

### 아키텍처 구성

```
┌─────────────────────────────────────────────────────────────┐
│                      Docker Network                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐│
│  │   Frontend   │     │   Backend    │     │    Batch     ││
│  │  (Vue 3)     │     │ (Spring Boot)│     │  (Spring Job)││
│  │  Port: 9000  │     │  Port: 9001  │     │   Scheduled  ││
│  └──────┬───────┘     └──────┬───────┘     └──────┬───────┘│
│         │                    │                    │         │
│         │                    ├────────────────────┤         │
│         │                    ▼                    │         │
│         │            ┌──────────────┐            │         │
│         │            │   MariaDB    │            │         │
│         │            │  (Port:3307) │            │         │
│         │            └──────────────┘            │         │
│         │                                        │         │
└─────────┼────────────────────────────────────────┼─────────┘
          │            API Communication           │
          └────────────────────────────────────────┘
```

### 주요 특징

- ✅ **마이크로서비스 아키텍처** - 독립적인 서비스 배포 및 확장
- ✅ **컨테이너화** - Docker 기반 환경 통일
- ✅ **모듈식 구조** - Backend, Frontend, Batch 분리
- ✅ **배치 처리** - 정기적인 작업 자동화
- ✅ **데이터베이스 추상화** - MariaDB 기반 데이터 관리
- ✅ **현대적 프론트엔드** - Vue 3 + TypeScript + Vite

---

## 📂 프로젝트 구조

```
web-framework/
│
├── backend/                    # Spring Boot 백엔드 서비스
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java 소스코드
│   │   │   └── resources/     # 설정 파일 (application.yml 등)
│   │   └── test/              # 테스트 코드
│   ├── Dockerfile             # 백엔드 Docker 이미지
│   └── build.gradle           # 백엔드 의존성 및 빌드 설정
│
├── frontend/                   # Vue 3 프론트엔드
│   ├── src/
│   │   ├── components/        # Vue 컴포넌트
│   │   ├── views/             # 페이지 뷰
│   │   ├── stores/            # Pinia 상태관리
│   │   ├── router/            # Vue Router 설정
│   │   └── main.ts            # 진입점
│   ├── public/                # 정적 자산
│   ├── package.json           # NPM 의존성
│   ├── vite.config.ts         # Vite 빌드 설정
│   ├── tsconfig.json          # TypeScript 설정
│   ├── Dockerfile             # 프론트엔드 Docker 이미지
│   └── nginx.conf             # Nginx 웹서버 설정
│
├── batch/                      # 배치 처리 서비스
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java 배치 작업
│   │   │   └── resources/     # 배치 설정
│   │   └── test/
│   ├── Dockerfile             # 배치 Docker 이미지
│   └── build.gradle           # 배치 의존성
│
├── docker-compose.yml         # 프로덕션 환경 설정
├── docker-compose.local.yml   # 로컬 개발 환경 설정
├── docker-compose.dev.yml     # 개발 환경 설정
├── docker-compose-external.yml # 외부 DB 연결 설정
│
├── build.gradle               # Root Gradle 설정 (버전관리)
├── settings.gradle            # Gradle 프로젝트 설정
├── .editorconfig              # 에디터 통일 설정
└── .dockerignore              # Docker 빌드 시 제외 파일
```

---

## 🚀 빠른 시작

### 사전 요구사항

- **Java**: JDK 17+
- **Node.js**: 18+ (프론트엔드)
- **Docker**: 24.0+ (컨테이너 기반 개발)
- **Docker Compose**: 2.20+
- **Gradle**: 8.0+ (또는 gradlew 사용)

### 1. 저장소 클론

```bash
git clone https://github.com/L-a-z-e/web-framework.git
cd web-framework
```

### 2. 환경 설정

**로컬 개발 환경 (`.env` 파일 생성)**

```bash
# 데이터베이스 설정
MARIADB_ROOT_PASSWORD=root_password
MARIADB_DATABASE=web_framework_db
MARIADB_USER=app_user
MARIADB_PASSWORD=app_password

# Spring Boot 설정
SPRING_PROFILES_ACTIVE=local
SPRING_DATASOURCE_URL=jdbc:mariadb://mariadb-local:3306/web_framework_db
SPRING_DATASOURCE_USERNAME=app_user
SPRING_DATASOURCE_PASSWORD=app_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.mariadb.jdbc.Driver

# Vue 설정
VUE_APP_API_URL=http://localhost:9001

# 배치 설정
JOB_NAME=sampleJob
```

### 3. 로컬 개발 환경 실행 (Docker Compose)

```bash
# 전체 스택 실행 (가장 권장)
docker-compose -f docker-compose.local.yml up -d

# 확인
docker-compose -f docker-compose.local.yml ps

# 로그 확인
docker-compose -f docker-compose.local.yml logs -f backend
docker-compose -f docker-compose.local.yml logs -f frontend
docker-compose -f docker-compose.local.yml logs -f mariadb-local
```

**접근 URL**:
- 🌐 **Frontend**: http://localhost:9000
- 🔗 **Backend API**: http://localhost:9001
- 📊 **Swagger UI**: http://localhost:9001/swagger-ui.html
- 🗄️ **MariaDB**: localhost:3307

### 4. 수동 실행 (개발 모드)

#### Backend 실행

```bash
# Gradle 빌드 및 실행
./gradlew :backend:bootRun

# 또는 IDE에서 실행
# backend/src/main/java/com/laze/framework/.../Application.java 우클릭 > Run
```

**Backend 실행 시 필요한 환경 변수**:
```bash
export SPRING_PROFILES_ACTIVE=local
export SPRING_DATASOURCE_URL=jdbc:mariadb://localhost:3307/web_framework_db
export SPRING_DATASOURCE_USERNAME=app_user
export SPRING_DATASOURCE_PASSWORD=app_password
```

#### Frontend 개발 서버

```bash
cd frontend

# 의존성 설치
yarn install

# 개발 서버 실행 (Hot Reload)
yarn dev

# 프로덕션 빌드
yarn build

# 빌드 결과 미리보기
yarn preview
```

#### Batch 작업 실행

```bash
./gradlew :batch:bootRun --args='--spring.batch.job.name=sampleJob'
```

---

## 🛠 주요 기술 설명

### Backend (Spring Boot 3.4.4)

#### 핵심 의존성

```groovy
// Spring Boot Starters
- spring-boot-starter-web           // REST API 개발
- spring-boot-starter-security      // 보안 및 인증
- spring-boot-starter-actuator      // 애플리케이션 모니터링
- spring-boot-starter-aop           // AOP 기반 횡단 관심사

// 데이터 접근
- mybatis-spring-boot-starter       // MyBatis ORM
- mariadb-java-client              // MariaDB 드라이버

// 문서화 및 로깅
- springdoc-openapi-starter-webmvc-ui  // Swagger/OpenAPI
- logstash-logback-encoder          // JSON 로그 포맷

// 개발 도구
- spring-boot-devtools             // Hot Reload
```

#### 주요 기능

```java
// REST API 예제
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {
    
    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable Long id) {
        // 로직 구현
    }
    
    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@RequestBody CreateItemRequest request) {
        // 로직 구현
    }
}

// MyBatis Mapper
@Mapper
public interface ItemMapper {
    Item selectById(Long id);
    List<Item> selectAll();
    int insert(Item item);
}

// AOP 기반 로깅
@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.laze.framework..*.*(..))")
    public void logBefore(JoinPoint jp) {
        // 메서드 실행 전 로깅
    }
}
```

### Frontend (Vue 3 + TypeScript + Vite)

#### 핵심 라이브러리

```json
{
  "dependencies": {
    "vue": "3.5.13",              // 프론트엔드 프레임워크
    "vue-router": "4.5.0",        // 라우팅
    "pinia": "3.0.2",             // 상태관리
    "axios": "1.8.4",             // HTTP 클라이언트
    "element-plus": "2.9.7",      // UI 컴포넌트 라이브러리
    "ag-grid-vue3": "33.2.3"      // 고급 데이터 그리드
  }
}
```

#### 프로젝트 구조

```
frontend/src/
├── components/
│   ├── common/
│   │   ├── Header.vue            # 헤더 컴포넌트
│   │   ├── Sidebar.vue           # 사이드바
│   │   └── Footer.vue            # 푸터
│   ├── forms/
│   │   └── ItemForm.vue          # 아이템 폼
│   └── tables/
│       └── ItemTable.vue         # 아이템 테이블
│
├── views/
│   ├── HomeView.vue              # 메인 페이지
│   ├── ItemListView.vue          # 아이템 목록
│   └── ItemDetailView.vue        # 아이템 상세
│
├── stores/
│   ├── modules/
│   │   └── itemStore.ts          # 아이템 상태관리
│   └── index.ts
│
├── router/
│   └── index.ts                  # 라우트 정의
│
├── services/
│   └── api.ts                    # API 호출
│
├── types/
│   └── index.ts                  # TypeScript 타입 정의
│
├── App.vue                       # 루트 컴포넌트
└── main.ts                       # 진입점
```

#### Vue 3 Composition API 예제

```typescript
// itemStore.ts - Pinia 스토어
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getItems, createItem } from '@/services/api'

export const useItemStore = defineStore('item', () => {
  const items = ref<Item[]>([])
  const loading = ref(false)
  
  const total = computed(() => items.value.length)
  
  async function fetchItems() {
    loading.value = true
    try {
      items.value = await getItems()
    } finally {
      loading.value = false
    }
  }
  
  async function addItem(item: CreateItemRequest) {
    const newItem = await createItem(item)
    items.value.push(newItem)
  }
  
  return { items, loading, total, fetchItems, addItem }
})

// ItemListView.vue - 뷰 컴포넌트
<script setup lang="ts">
import { onMounted } from 'vue'
import { useItemStore } from '@/stores/itemStore'

const itemStore = useItemStore()

onMounted(() => {
  itemStore.fetchItems()
})
</script>

<template>
  <div class="item-list">
    <div v-if="itemStore.loading" class="loading">로딩 중...</div>
    <div v-else class="items">
      <div v-for="item in itemStore.items" :key="item.id" class="item">
        {{ item.name }}
      </div>
    </div>
  </div>
</template>
```

### Batch Processing

Spring Batch를 활용한 대용량 데이터 처리 및 정기 작업 자동화:

```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    
    @Bean
    public Job sampleJob(JobRepository jobRepository, 
                         PlatformTransactionManager transactionManager) {
        return new JobBuilder("sampleJob", jobRepository)
            .start(sampleStep(jobRepository, transactionManager))
            .build();
    }
    
    @Bean
    public Step sampleStep(JobRepository jobRepository, 
                           PlatformTransactionManager transactionManager) {
        return new StepBuilder("sampleStep", jobRepository)
            .<Item, Item>chunk(100)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .transactionManager(transactionManager)
            .build();
    }
}
```

---

## 📊 데이터베이스 구조

### MariaDB 설정

**자동 초기화** (선택사항):
```bash
# db/init 폴더에 .sql 파일을 배치하면 자동으로 실행됨
docker-compose -f docker-compose.local.yml up

# 또는 수동으로 초기화 스크립트 실행
docker exec web_framework_db mysql -uroot -p < db/init/schema.sql
```

**연결 정보**:
```
Host: mariadb-local
Port: 3307
Username: app_user
Password: app_password
Database: web_framework_db
Charset: utf8mb4
Timezone: Asia/Seoul
```

**MyBatis 설정**:
```yaml
# backend/src/main/resources/application.yml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.laze.framework.domain
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
```

---

## 🐳 Docker 및 컨테이너

### 환경별 Docker Compose 파일

| 파일 | 용도 | 사용 상황 |
|------|------|---------|
| `docker-compose.yml` | 프로덕션 환경 | AWS, GCP 등 클라우드 배포 |
| `docker-compose.local.yml` | 로컬 개발 | 로컬 머신에서 전체 스택 실행 |
| `docker-compose.dev.yml` | 개발 환경 | 팀 개발 환경 |
| `docker-compose-external.yml` | 외부 DB | 기존 DB 서버와 연결 |

### 컨테이너 상태 확인

```bash
# 모든 컨테이너 상태 확인
docker-compose -f docker-compose.local.yml ps

# 특정 서비스 로그 확인
docker-compose -f docker-compose.local.yml logs -f backend
docker-compose -f docker-compose.local.yml logs -f frontend
docker-compose -f docker-compose.local.yml logs -f mariadb-local

# 컨테이너 접속
docker-compose -f docker-compose.local.yml exec backend bash
docker-compose -f docker-compose.local.yml exec mariadb-local mysql -u root -p
```

### Dockerfile 설명

#### Backend Dockerfile (Multi-stage)
```dockerfile
# Build stage
FROM gradle:latest AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Runtime stage
FROM openjdk:17-slim
COPY --from=builder /app/backend/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 9001
```

#### Frontend Dockerfile
```dockerfile
# Build stage
FROM node:18-alpine AS builder
WORKDIR /app
COPY frontend/package*.json ./
RUN yarn install
COPY frontend .
RUN yarn build

# Production stage
FROM nginx:alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 9000
```

---

## 🔐 보안 설정

### Spring Security 설정

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
        return http.build();
    }
}
```

### CORS 설정

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:9000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

---

## 📋 API 문서

### Swagger/OpenAPI

프로젝트는 자동으로 Swagger UI를 생성합니다.

```bash
# Backend 실행 후 접근
http://localhost:9001/swagger-ui.html
```

### API 엔드포인트 예제

```
GET  /api/v1/items              # 아이템 목록 조회
GET  /api/v1/items/{id}         # 아이템 상세 조회
POST /api/v1/items              # 아이템 생성
PUT  /api/v1/items/{id}         # 아이템 수정
DELETE /api/v1/items/{id}       # 아이템 삭제

# Actuator (모니터링)
GET  /actuator                  # 모든 엔드포인트
GET  /actuator/health          # 헬스체크
GET  /actuator/metrics         # 메트릭스
```

---

## 🧪 테스트

### Backend 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 클래스 테스트
./gradlew test --tests "com.laze.framework.service.ItemServiceTest"

# 테스트 리포트 생성
./gradlew test --info
```

### Frontend 테스트 (설정 예정)

```bash
cd frontend

# 단위 테스트 (Vue Test Utils)
yarn test:unit

# E2E 테스트 (Playwright)
yarn test:e2e
```

### JUnit 5 + Mockito 테스트 예제

```java
@SpringBootTest
class ItemServiceTest {
    
    @MockBean
    private ItemMapper itemMapper;
    
    @Autowired
    private ItemService itemService;
    
    @Test
    @DisplayName("아이템 조회 테스트")
    void testGetItem() {
        // Arrange
        Item expectedItem = new Item(1L, "Test Item");
        given(itemMapper.selectById(1L)).willReturn(expectedItem);
        
        // Act
        Item result = itemService.getItem(1L);
        
        // Assert
        assertEquals(expectedItem.getName(), result.getName());
        verify(itemMapper).selectById(1L);
    }
}
```

---

## 🚢 배포

### Docker 이미지 빌드 및 푸시

```bash
# 이미지 빌드
docker build -f backend/Dockerfile -t your-registry/web-framework-backend:latest .
docker build -f frontend/Dockerfile -t your-registry/web-framework-frontend:latest .
docker build -f batch/Dockerfile -t your-registry/web-framework-batch:latest .

# 레지스트리에 푸시
docker push your-registry/web-framework-backend:latest
docker push your-registry/web-framework-frontend:latest
docker push your-registry/web-framework-batch:latest
```

### Kubernetes 배포 (선택사항)

```bash
# ConfigMap 생성 (환경변수)
kubectl create configmap web-framework-config --from-file=application.yml

# Secret 생성 (민감한 정보)
kubectl create secret generic web-framework-secret \
  --from-literal=db-password=your-password

# 배포
kubectl apply -f k8s/
```

---

## 📈 모니터링 및 로깅

### Spring Boot Actuator

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

### 로그 포맷 (JSON)

```yaml
logging:
  level:
    root: INFO
    com.laze.framework: DEBUG
  pattern:
    console: "[%d{yyyy-MM-dd HH:mm:ss}] [%thread] %-5level %logger{36} - %msg%n"
```

### Prometheus 메트릭 수집

```bash
# 메트릭 엔드포인트
curl http://localhost:9001/actuator/prometheus
```

---

## 🤝 기여 가이드

### 개발 워크플로우

1. **브랜치 생성**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **코드 작성 및 테스트**
   ```bash
   # 테스트 작성
   ./gradlew test
   
   # 코드 포맷팅
   ./gradlew spotlessApply
   ```

3. **커밋 및 푸시**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   git push origin feature/your-feature-name
   ```

4. **Pull Request 생성**
   - GitHub PR 페이지에서 PR 생성
   - 상세한 설명 작성
   - 리뷰 요청

### 코드 컨벤션

- **Java**: Google Java Style Guide
- **TypeScript**: Airbnb TypeScript Style Guide
- **Commit Message**: Conventional Commits
  - `feat:` 새로운 기능
  - `fix:` 버그 수정
  - `docs:` 문서 변경
  - `style:` 코드 스타일 변경
  - `refactor:` 코드 리팩토링
  - `test:` 테스트 추가

---

## 🐛 문제 해결

### Backend 관련 문제

#### 포트 충돌
```bash
# 포트 확인
lsof -i :9001

# 프로세스 종료
kill -9 <PID>
```

#### 데이터베이스 연결 실패
```bash
# MariaDB 상태 확인
docker-compose logs mariadb-local

# 컨테이너 재시작
docker-compose restart mariadb-local
```

#### Gradle 빌드 실패
```bash
# 캐시 삭제 후 재빌드
./gradlew clean build --no-build-cache
```

### Frontend 관련 문제

#### 의존성 설치 실패
```bash
cd frontend

# 캐시 삭제
rm -rf node_modules yarn.lock

# 재설치
yarn install
```

#### Hot Reload 작동 안함
```bash
# 브라우저 캐시 초기화
# Ctrl + Shift + Delete (또는 Cmd + Shift + Delete)

# 개발 서버 재시작
yarn dev
```

### Docker 관련 문제

#### 이미지 빌드 실패
```bash
# 기존 이미지 제거
docker system prune -a

# 빌드 재시작
docker-compose -f docker-compose.local.yml build --no-cache
```

#### 컨테이너 상태 이상
```bash
# 전체 중지 및 제거
docker-compose -f docker-compose.local.yml down

# 다시 시작
docker-compose -f docker-compose.local.yml up -d
```

---

## 📚 참고 자료

### 공식 문서
- [Spring Boot 3.4](https://spring.io/projects/spring-boot)
- [Vue 3](https://vuejs.org/)
- [MyBatis](https://mybatis.org/)
- [Docker Documentation](https://docs.docker.com/)

---

## 📊 프로젝트 상태

| 항목 | 상태 | 진행률 |
|------|------|------|
| Backend 기초 | ✅ 완료 | 100% |
| Frontend 기초 | ✅ 완료 | 100% |
| 데이터베이스 | ✅ 완료 | 100% |
| Docker 통합 | ✅ 완료 | 100% |
| 배치 처리 | 🔄 진행중 | 70% |
| 테스트 코드 | 🔄 진행중 | 50% |
| 문서화 | ✅ 완료 | 100% |
| Kubernetes 배포 | ⏳ 예정 | 0% |

---

**마지막 업데이트**: 2025년 12월  
**유지보수자**: L-a-z-e  
**이슈 및 PR**: 언제든지 환영합니다! 🎉
