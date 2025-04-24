<template>
  <el-aside :width="isCollapsed ? '64px' : '200px'" class="layout-aside">
    <div class="sidebar-logo-container">
      <router-link to="/" class="sidebar-logo-link">
        <slot name="logo">
          <img src="@/assets/vue.svg" alt="Logo" class="sidebar-logo" v-if="!isCollapsed">
          <img src="@/assets/vue.svg" alt="Logo" class="sidebar-logo-small" v-else>
        </slot>
        <h1 class="sidebar-title" v-show="!isCollapsed">Framework</h1>
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
import { defineProps, defineEmits, computed } from 'vue';
import {useRoute, useRouter} from 'vue-router';
import { ElAside, ElScrollbar, ElMenu, ElSubMenu, ElMenuItem, ElIcon } from 'element-plus';
import { Menu as MenuIcon, Document, Setting } from '@element-plus/icons-vue';
import {useMenuStore} from "@/store";
import MenuItem from "@/components/MenuItem.vue";

defineProps({ isCollapsed: { type: Boolean, default: false } });
// const emit = defineEmits(['menu-select']);
const route = useRoute();
const router = useRouter(); // ★ router 인스턴스 가져오기
const menuStore = useMenuStore();
const activeMenu = computed(() => route.fullPath);
function handleMenuSelect(index: string) {

  // 네비게이션 대상이 아닌 ID 형식인지 확인
  if (!index || index.startsWith('#')) {
    console.log(`Navigation skipped for non-navigable menu index: ${index}`);
    return; // 네비게이션 수행 안 함
  }

  // 해당 메뉴 ID에 대한 라우트가 실제로 존재하는지 확인
  if (router.hasRoute(index)) {
    console.log(`Navigating to route name: ${index}`);
    router.push({ name: index });
  } else {
    console.warn(`Route with name '${index}' not found. Navigation skipped.`);
  }

}
</script>

<style scoped>
.layout-aside {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background-color: var(--sidebar-bg-color);
  transition: width 0.28s;
}

.sidebar-logo-container {
  /* 로고 컨테이너는 고유 높이를 가짐 (변경 필요 시 조정) */
  flex-shrink: 0;
  background-color: var(--sidebar-logo-bg);
  padding: 10px 0;
  text-align: center;
}

.el-scrollbar {
  flex-grow: 1;
  overflow: hidden;
}

.el-scrollbar__wrap {
  overflow-x: hidden !important;
}

.layout-menu {
  border-right: none !important;
  height: 100%;
}

.sidebar-logo {
  height: 32px;
  vertical-align: middle;
  margin-right: 12px;
}
.sidebar-logo-small {
  height: 24px;
  vertical-align: middle;
}

.sidebar-title {
  display: inline-block;
  margin: 0;
  color: var(--sidebar-logo-text);
  font-weight: 600;
  line-height: 50px;
  font-size: 14px;
  vertical-align: middle;
}
</style>
