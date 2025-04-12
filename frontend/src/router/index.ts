import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import {useAuthStore, useMenuStore} from "@/store";

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
  // 404 Not Found 페이지 (가장 마지막에 정의)
  // {
  //   path: '/:pathMatch(.*)*', // 정의되지 않은 모든 경로
  //   name: 'NotFound',
  //   component: NotFound,
  //   meta: { title: '페이지를 찾을 수 없음' }
  // },
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

// --- Navigation Guard setting ---
let isInitialAuthCheckDone = false; // 앱 로딩 시 checkLoginStatus 중복 호출 방지 플래그
let hasFetchedMenus = false;

router.beforeEach(async (to, from, next) => {
  console.log(`Navigating from ${from.fullPath} to ${to.fullPath}`);

  const authStore = useAuthStore();
  const menuStore = useMenuStore();

  // --- 1. 앱 초기 로딩 시 또는 새로고침 시 로그인 상태 확인 ---
  // 아직 확인 전이고 & 로컬 스토리지 등에 상태가 남아있을 가능성이 있다면 서버에 확인
  if (!isInitialAuthCheckDone && !authStore.isLoggedIn && localStorage.getItem('auth') && !authStore.isLoading) {
    console.log('Initial auth check needed. Calling checkLoginStatus...');
    try {
      await authStore.checkLoginStatus(); // 서버에 세션 유효성 및 사용자 정보 요청
      console.log('Initial auth check completed. Logged in:', authStore.isLoggedIn);
    } catch (error) {
      console.error('Error during initial auth check:', error);
    } finally {
      isInitialAuthCheckDone = true;
    }
  } else if (!isInitialAuthCheckDone) {
    isInitialAuthCheckDone = true;
  }

  // --- 2. 라우트 접근 제어 ---
  const isLoggedIn = authStore.isLoggedIn;   // 현재 로그인 상태 확인

  if (isLoggedIn && menuStore.menuItems.length === 0 && !hasFetchedMenus && !menuStore.isLoading) {
    console.log('Fetching menus in navigation guard...');
    hasFetchedMenus = true; // 메뉴 로딩 시도 플래그 설정 (중복 방지)
    try {
      await menuStore.fetchMenus(); // 비동기로 메뉴 로드
      console.log('Menus fetched successfully.');
    } catch (error) {
      console.error('Failed to fetch menus:', error);
      hasFetchedMenus = false;
    }
  }

  const requiresAuth = to.meta.requiresAuth; // 이동할 페이지가 인증 필요한지 확인
  console.log(`Route requires auth: ${requiresAuth}, User logged in: ${isLoggedIn}`);

  if (requiresAuth && !isLoggedIn) {
    // 인증 필요한 페이지인데 로그인이 안 된 경우 -> 로그인 페이지로 이동
    console.log('Redirecting to login page.');
    menuStore.clearMenus();
    hasFetchedMenus = false; // 메뉴 로딩 플래그 리셋
    // 이동하려던 경로를 쿼리 파라미터(redirect)로 넘겨서 로그인 후 돌아올 수 있게 함 (선택적)
    next({ name: 'Login', query: { redirect: to.fullPath } });
  } else if (to.name === 'Login' && isLoggedIn) {
    // 이미 로그인했는데 로그인 페이지로 가려는 경우 -> 대시보드로 이동
    console.log('Already logged in. Redirecting to dashboard.');
    next({ path: '/' }); // 기본 경로로 이동
  } else {
    // 그 외 모든 경우 (인증 불필요 페이지, 인증 필요한데 로그인 됨) -> 정상 진행
    console.log('Proceeding with navigation.');
    next();
  }
});

export default router;
