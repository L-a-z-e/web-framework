<template>
  <el-container class="main-layout">
    <Sidebar :is-collapsed="isCollapsed"/>
    <el-container class="main-container">
      <Header :is-collapsed="isCollapsed" @toggle-sidebar="toggleSidebar"/>
      <TagsView/>
      <!-- Main Content Area -->
      <el-main class="layout-main">
<!--        <div class="main-content-wrapper">-->
          <router-view v-slot="{ Component, route }">
            <keep-alive :include="cachedViews">
              <component :is="Component" :key="route.fullPath" />
            </keep-alive>
          </router-view>
<!--        </div>-->
      </el-main>
      <el-footer class="layout-footer">
        web-framework {{ new Date().getFullYear() }}
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import {ref, computed, onMounted} from 'vue';
import Header from '@/layouts/Header.vue';
import Sidebar from '@/layouts/Sidebar.vue';
import { ElContainer, ElMain, ElFooter } from 'element-plus';
import TagsView from "@/layouts/TagsView.vue";

import { useTagsViewStore } from "@/store";
import {themeManager} from "@/themeManager.ts";

// 사이드바 collapsed
const isCollapsed = ref(false);

const tagsViewStore = useTagsViewStore();
const cachedViews = computed(() => tagsViewStore.cachedViews);

function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value;
}

onMounted(() => {
  themeManager.initTheme();
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.main-container {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background-color: var(--app-bg-color, #f0f2f5);
}

.layout-main {
  padding: 0; /* 패딩 없음 */
  overflow-x: hidden; /* 가로 스크롤 방지 */
  overflow-y: auto; /* 세로 스크롤 */
  height: calc(100vh - var(--header-height) - var(--tagview-height) - var(--footer-height)); /* 높이 계산 유지 */
  background-color: transparent; /* 배경색 제거 (app-page 에서 관리) */
}
/*.main-content-wrapper {
//  height: 100%;
//  width: 100%;
//} */

.layout-footer {
  text-align: center;
  font-size: 12px;
  color: var(--footer-text-color, #909399);
  border-top: 1px solid var(--app-border-color, #e4e7ed);
  background-color: var(--footer-bg-color, #ffffff);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  height: 30px;
}
</style>
