import { createApp } from 'vue'
import './style.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import pinia from "@/store";

// --- !!! AG-Grid 모듈 임포트 및 등록 !!! ---
import { AllCommunityModule, ModuleRegistry } from 'ag-grid-community';

// Register all Community features
ModuleRegistry.registerModules([AllCommunityModule]);

const app = createApp(App);

app.use(ElementPlus);
app.use(router);
app.use(pinia);

app.mount('#app');
