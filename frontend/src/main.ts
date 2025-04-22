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
