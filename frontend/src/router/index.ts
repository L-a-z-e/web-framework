import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

const MainLayout = () => import('@/layouts/MainLayout.vue');
const Login = () => import('@/views/auth/Login.vue');
const Dashboard = () => import('@/views/dashboard/Dashboard.vue');
const Menu = () => import('@/views/menu/Menu.vue');
const Sample = () => import('@/views/sample/Sample.vue');

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
        path: '/menus',
        name: 'MenuManagement',
        component: Menu,
        meta: { title: '메뉴 관리', requiresAuth: true }
      },
      {
        path: '/sample',
        name: 'Sample',
        component: Sample,
        meta: { title: '샘플 페이지', requiresAuth: true }
      }
    ]
  },
  // { path: '/:pathMatch(.*)*', name: 'NotFound', component: NotFound },
];

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
