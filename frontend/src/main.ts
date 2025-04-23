import { createApp } from 'vue'
import './style.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import pinia from "@/store"
import { initializeCsrf } from "@/services/api"

// AG-Grid 모듈 임포트 및 등록
import { AllCommunityModule, ModuleRegistry } from 'ag-grid-community'
ModuleRegistry.registerModules([AllCommunityModule])

// 새로고침 감지 및 처리
if (window.performance && window.performance.navigation.type === 1) {
  // 새로고침 감지됨 (performance.navigation.type이 1인 경우)
  console.log('페이지가 새로고침됨');

  // 로컬 스토리지 체크로 로그인 상태 확인 (간단한 방법)
  const isLoggedIn = localStorage.getItem('auth') !== null;

  if (isLoggedIn) {
    // 로그인 상태일 때만 리다이렉트 (선택적)
    window.location.replace('/');
  }
}

// 앱 생성 및 설정
const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.use(pinia)

// 앱 마운트
app.mount('#app')

// CSRF 토큰 초기화 (앱 마운트 후)
initializeCsrf().catch(error => {
  console.error('CSRF 토큰 초기화 실패:', error)
})
