<template>
  <el-container class="main-layout">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '200px'" class="layout-aside">
      <div class="sidebar-logo-container">
        <slot name="logo">
<!--          <img src="@/assets/logo_placeholder.png" alt="Logo" class="sidebar-logo" v-if="!isCollapsed">-->
<!--          <img src="@/assets/logo_placeholder_small.png" alt="Logo" class="sidebar-logo-small" v-else>-->
        </slot>
      </div>
      <el-scrollbar>
        <el-menu
          :collapse="isCollapsed"
          @select="handleMenuSelect"
          class="layout-menu"
          background-color="#2b303b"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :collapse-transition="false"
        >
          <el-sub-menu index="menus-group">
            <template #title>
              <el-icon><MenuIcon /></el-icon>
              <span v-show="!isCollapsed">메뉴/권한</span>
            </template>
            <el-menu-item index="/menus">메뉴 관리</el-menu-item>
            <el-menu-item index="/screens">화면 관리</el-menu-item>
            <el-menu-item index="/roles">권한 그룹</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/sample">
            <el-icon><Document /></el-icon>
            <template #title>샘플</template>
          </el-menu-item>
          <el-menu-item index="/codes">
            <el-icon><Setting /></el-icon>
            <template #title>코드 관리</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <!-- Header -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="sidebar-toggle" @click="toggleSidebar">
            <Expand v-if="isCollapsed" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">HOME</el-breadcrumb-item>
            <el-breadcrumb-item>메뉴 관리</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <slot name="header-right">
            <el-input placeholder="Search..." class="header-search" :prefix-icon="Search" size="small"/>
            <el-badge :value="3" class="item" type="warning"> <el-icon size="18"><Bell /></el-icon> </el-badge>
            <el-dropdown>
               <span class="user-info">
                 <el-avatar size="small" :icon="UserFilled" /> Test User <el-icon><ArrowDown /></el-icon>
               </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>Profile</el-dropdown-item>
                  <el-dropdown-item divided @click="logout">Logout</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </slot>
        </div>
      </el-header>

      <!-- Main Content Area -->
      <el-main class="layout-main">
        <!-- TODO: TagsView 컴포넌트 위치 -->

        <!-- 라우터 뷰 -->
        <router-view v-slot="{ Component, route }">
          <!-- !!! 수정된 부분: keep-alive 내부에 오직 component 만 !!! -->
          <keep-alive :include="keepAliveComponents">
            <component :is="Component" :key="route.fullPath" />
          </keep-alive>
        </router-view>

      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  ElContainer, ElHeader, ElAside, ElMain, ElMenu, ElSubMenu, ElMenuItem, ElScrollbar, ElIcon,
  ElInput, ElBadge, ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar, ElBreadcrumb, ElBreadcrumbItem
} from 'element-plus';
import { Menu as MenuIcon, Document, Expand, Fold, Bell, UserFilled, ArrowDown, Search, Setting } from '@element-plus/icons-vue';
// TODO: Pinia 스토어 import
// import { useTagsViewStore } from '@/store/modules/tagsView';

const router = useRouter();
const isCollapsed = ref(false);

// --- 캐시할 컴포넌트 이름(name 옵션) 목록 ---
// TODO: 실제로는 Pinia 스토어에서 동적으로 관리해야 함
const keepAliveComponents = ref<string[]>(['Sample', 'Menu']); // 'Sample.vue', 'Menu.vue' 의 name 옵션 값

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value;
}

function handleMenuSelect(index: string) {
  if (index && index.startsWith('/')) {
    router.push(index);
  } else {
    console.warn('Invalid menu index (path) selected:', index);
  }
}

function logout() {
  console.log("Logout triggered");
  // TODO: 실제 로그아웃 로직
  router.push('/login');
}
</script>

<style scoped>
/* 스타일 내용은 이전과 동일하게 유지 */
.main-layout { height: 100vh; }
.layout-aside { background-color: #2b303b; transition: width 0.28s; width: 200px !important; overflow-x: hidden; }
.el-aside.el-aside--collapse { width: 64px !important; }
.sidebar-logo-container { height: 50px; background-color: #2b303b; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.sidebar-logo, .sidebar-logo-small { height: 32px; width: auto; vertical-align: middle; }
.sidebar-logo-small { height: 28px; }
.el-scrollbar { height: calc(100vh - 50px); }
.layout-menu { border-right: none; }
.layout-menu:not(.el-menu--collapse) { width: 200px; }
.layout-menu .el-menu-item,
.el-sub-menu :deep(.el-sub-menu__title) { color: #bfcbd9; }
.layout-menu .el-menu-item span,
.el-sub-menu :deep(.el-sub-menu__title span) { margin-left: 10px; }
.layout-menu .el-menu-item:hover,
.el-sub-menu :deep(.el-sub-menu__title:hover) { background-color: #001f3a !important; color: #fff !important; }
.layout-menu .el-menu-item.is-active { background-color: #409EFF !important; color: #fff !important; }
.layout-header { background-color: #fff; display: flex; justify-content: space-between; align-items: center; height: 50px; padding: 0 15px; border-bottom: 1px solid #e6e6e6; box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08); }
.header-left { display: flex; align-items: center; }
.sidebar-toggle { font-size: 18px; cursor: pointer; margin-right: 10px; }
.el-breadcrumb { margin-left: 10px; }
.header-right { display: flex; align-items: center; gap: 20px; }
.header-search { width: 180px; margin-right: 10px; }
.header-right .item { margin-top: 5px; cursor: pointer; }
.user-info { cursor: pointer; display: flex; align-items: center; gap: 8px; }
.layout-main { background-color: #f0f2f5; padding: 15px; overflow-y: auto; }
</style>
