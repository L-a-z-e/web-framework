<template>
  <el-aside :width="isCollapsed ? '64px' : '200px'" class="layout-aside">
    <div class="sidebar-logo-container">
      <router-link to="/" class="sidebar-logo-link">
        <slot name="logo">
          <img src="@/assets/vue.svg" alt="Logo" class="sidebar-logo" v-if="!isCollapsed"> <!-- Vue 로고 사용 (예시) -->
          <img src="@/assets/vue.svg" alt="Logo" class="sidebar-logo-small" v-else>
        </slot>
        <h1 class="sidebar-title" v-show="!isCollapsed">Framework</h1> <!-- 제목 추가 -->
      </router-link>
    </div>
    <el-scrollbar>
      <el-menu
        :collapse="isCollapsed"
        class="layout-menu"
        background-color="#2b303b"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :collapse-transition="false"
        :default-active="activeMenu"
        @select="handleMenuSelect"
        router
        >
        <MenuItem v-for="menu in menuStore.menuItems" :key="menu.menuId" :item="menu" />
      </el-menu>
    </el-scrollbar>
  </el-aside>
</template>

<script setup lang="ts">
// --- Script 부분은 이전 답변과 동일하게 유지 ---
import { defineProps, defineEmits, computed } from 'vue';
import {useRoute, useRouter} from 'vue-router';
import { ElAside, ElScrollbar, ElMenu, ElSubMenu, ElMenuItem, ElIcon } from 'element-plus';
import { Menu as MenuIcon, Document, Setting } from '@element-plus/icons-vue';
import {useMenuStore} from "@/store";
import MenuItem from "@/components/MenuItem.vue"; // 사용할 아이콘 임포트

defineProps({ isCollapsed: { type: Boolean, default: false } });
// const emit = defineEmits(['menu-select']);
const route = useRoute();
const router = useRouter(); // ★ router 인스턴스 가져오기
const menuStore = useMenuStore();
const activeMenu = computed(() => route.fullPath);
function handleMenuSelect(index: string) {
  // index MenuItem.vue 에서 :index="item.menuId" 로 설정된 값

  // 1. 네비게이션 대상이 아닌 ID 형식인지 확인 (예: '#'으로 시작)
  if (!index || index.startsWith('#')) {
    console.log(`Navigation skipped for non-navigable menu index: ${index}`);
    return; // 네비게이션 수행 안 함
  }

  // 2. (선택적 강화) 해당 메뉴 ID에 대한 라우트가 실제로 존재하는지 확인
  //    라우트 이름(name)이 메뉴 ID(index)와 동일하다고 가정합니다.
  if (router.hasRoute(index)) {
    console.log(`Navigating to route name: ${index}`);
    // 라우트 이름으로 이동하는 것이 더 안전할 수 있습니다.
    router.push({ name: index });
    // 또는 경로로 직접 이동해야 한다면:
    // router.push('/' + index); // 경로 구조에 맞게 조정
  } else {
    // 정의되지 않은 라우트로의 이동 시도 방지
    console.warn(`Route with name '${index}' not found. Navigation skipped.`);
  }

}
</script>

<style scoped>
.layout-aside {
  background-color: #2b303b; /* 어두운 배경 */
  color: #bfcbd9; /* 기본 텍스트 색상 */
  transition: width 0.28s;
  width: 200px !important;
  height: 100%;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35); /* 오른쪽에 그림자 */
  z-index: 10; /* 다른 요소 위에 오도록 */
}
/* Element Plus collapse 클래스 타겟팅 */
.el-aside.el-menu--collapse {
  width: 64px !important;
}
.el-aside.el-menu--collapse .sidebar-title {
  display: none; /* 접혔을 때 제목 숨김 */
}
.el-aside.el-menu--collapse .sidebar-logo-link {
  padding: 0; /* 접혔을 때 로고 패딩 제거 */
  justify-content: center;
}


.sidebar-logo-container {
  height: 50px; /* 헤더 높이와 일치 */
  background-color: #2b303b;
  flex-shrink: 0;
  overflow: hidden;
  padding-left: 14px; /* 로고 좌측 여백 */
}

.sidebar-logo-link {
  display: flex;
  align-items: center;
  height: 100%;
  text-decoration: none; /* 링크 밑줄 제거 */
}

.sidebar-logo, .sidebar-logo-small {
  height: 32px;
  width: 32px; /* 너비도 고정 (예시) */
  vertical-align: middle;
  margin-right: 12px; /* 로고와 제목 사이 간격 */
}
.sidebar-logo-small {
  margin-right: 0; /* 접혔을 때는 간격 없음 */
}

.sidebar-title {
  color: white;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  display: inline-block; /* 로고 옆에 표시 */
  vertical-align: middle;
  white-space: nowrap; /* 줄바꿈 방지 */
}


.el-scrollbar {
  flex-grow: 1;
  background-color: #2b303b;
}
/* 스크롤바 숨김 (미관상) - 필요시 제거 */
.el-scrollbar :deep(.el-scrollbar__bar) {
  display: none;
}

.layout-menu {
  border-right: none; /* 메뉴 오른쪽 테두리 제거 */
  background-color: #2b303b;
}
/* 메뉴가 접히지 않았을 때 너비 */
.layout-menu:not(.el-menu--collapse) {
  width: 200px;
}

/* 메뉴 아이템, 서브메뉴 제목 공통 스타일 */
.el-menu-item,
.el-sub-menu :deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  color: #bfcbd9; /* 기본 글자색 */
  background-color: #2b303b !important; /* 배경색 명시 (오버라이드) */
}
/* 아이콘 스타일 */
.el-menu-item .el-icon,
.el-sub-menu :deep(.el-sub-menu__title .el-icon) {
  color: #bfcbd9;
  margin-right: 5px; /* 아이콘과 텍스트 간격 */
  width: 24px; /* 아이콘 영역 너비 고정 */
  text-align: center;
  font-size: 18px; /* 아이콘 크기 */
  vertical-align: middle; /* 아이콘 세로 중앙 정렬 */
}
/* 텍스트 span */
.el-menu-item span,
.el-sub-menu :deep(.el-sub-menu__title span) {
  vertical-align: middle;
}

/* 호버 스타일 */
.el-menu-item:hover,
.el-sub-menu :deep(.el-sub-menu__title:hover) {
  background-color: #001f3a !important; /* 어두운 호버 배경 */
  color: #fff !important; /* 흰색 글자 */
}
.el-menu-item:hover .el-icon,
.el-sub-menu :deep(.el-sub-menu__title:hover .el-icon) {
  color: #fff; /* 호버 시 아이콘 색상 */
}

/* 활성 메뉴 아이템 스타일 */
.el-menu-item.is-active {
  background-color: #409EFF !important; /* Element Plus 파란색 */
  color: #fff !important;
}
.el-menu-item.is-active .el-icon {
  color: #fff !important;
}

/* 서브메뉴 아이템 들여쓰기 조정 */
.el-menu--collapse .el-sub-menu .el-menu-item,
.el-menu .el-sub-menu .el-menu-item {
  padding-left: 49px !important; /* 아이콘 영역 + 추가 들여쓰기 */
  background-color: #1f2d3d !important; /* 서브메뉴 배경색 */
  height: 45px; /* 서브메뉴 높이 약간 줄임 */
  line-height: 45px;
}
.el-menu .el-sub-menu .el-menu-item:hover {
  background-color: #001528 !important; /* 서브메뉴 호버 배경 */
}
.el-menu .el-sub-menu .el-menu-item.is-active {
  background-color: #409EFF !important; /* 서브메뉴 활성 배경 */
  color: #fff !important;
}

</style>
