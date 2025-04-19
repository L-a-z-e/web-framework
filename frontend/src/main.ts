import { createApp } from 'vue'
import './style.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import pinia from "@/store";
import apiClient from "@/services/api.ts";

// --- !!! AG-Grid 모듈 임포트 및 등록 !!! ---
import { AllCommunityModule, ModuleRegistry } from 'ag-grid-community';

// Register all Community features
ModuleRegistry.registerModules([AllCommunityModule]);

// const app = createApp(App);
//
// app.use(ElementPlus);
// app.use(router);
// app.use(pinia);
//
// app.mount('#app');

// --- !!! 비동기 초기화 함수 정의 !!! ---
async function initializeAppAndMount() {
  console.log('Starting application initialization...'); // 초기화 시작 로그

  try {
    // 백엔드 초기화 API 호출 (CSRF 쿠키 생성을 위해)
    // URL은 백엔드에 실제 구현된 경로로 변경해야 합니다.
    await apiClient.get('/api/public/init');
    console.log('Backend session/CSRF initialized successfully.');
  } catch (error) {
    // 초기화 API 호출 실패 시 에러 로그 (앱 실행은 계속 진행)
    console.error('Backend initialization request failed. The application might not work correctly if CSRF protection is needed.', error);
    // 필요하다면 여기서 사용자에게 심각한 오류임을 알리거나 앱 로딩을 중단할 수도 있습니다.
  }

  // --- Vue 앱 생성 및 설정 (기존 로직) ---
  const app = createApp(App);

  app.use(ElementPlus);
  app.use(router);
  app.use(pinia);

  // --- Vue 앱 마운트 ---
  app.mount('#app');
  console.log('Application mounted.'); // 마운트 완료 로그
}

// --- !!! 정의된 비동기 초기화 함수 실행 !!! ---
initializeAppAndMount();
