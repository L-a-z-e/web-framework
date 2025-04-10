import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

const MainLayout = () => import('@/layouts/MainLayout.vue');
const Login = () => import('@/views/auth/Login.vue');
const Dashboard = () => import('@/views/dashboard/Dashboard.vue');
const Menu = () => import('@/views/menu/Menu.vue');     // <<< 경로 변경
const Sample = () => import('@/views/sample/Sample.vue');    // <<< 경로 변경

// --- 라우트 정의 (경로도 필요시 수정) ---
const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '대시보드', requiresAuth: true }
      },
      {
        // --- 경로 변경 예시 ---
        path: '/menus', // '/fw/menus' 에서 변경
        name: 'MenuManagement',
        component: Menu, // 변경된 컴포넌트 사용
        meta: { title: '메뉴 관리', requiresAuth: true }
      },
      {
        // --- 경로 변경 예시 ---
        path: '/sample', // '/fw/sample' 에서 변경
        name: 'Sample',
        component: Sample, // 변경된 컴포넌트 사용
        meta: { title: '샘플 페이지', requiresAuth: true }
      }
      // --- 앞으로 여기에 페이지 경로들을 계속 추가 ---
    ]
  },
  // { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFound },
];

// --- 라우터 인스턴스 생성 (이하 동일) ---
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

export default router;
