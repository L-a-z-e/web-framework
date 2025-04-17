<template>
  <el-container class="main-layout">
    <Sidebar :is-collapsed="isCollapsed"/>
    <el-container class="main-container">
      <Header :is-collapsed="isCollapsed" @toggle-sidebar="toggleSidebar"/>
      <TagsView/>
      <!-- Main Content Area -->
      <el-main class="layout-main">
        <div class="main-content-wrapper">
          <router-view v-slot="{ Component, route }">
            <keep-alive :include="cachedViews">
              <component :is="Component" :key="route.fullPath" />
            </keep-alive>
          </router-view>
        </div>
      </el-main>
      <el-footer class="layout-footer">
        web-framework {{ new Date().getFullYear() }}
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
// 분리된 레이아웃 컴포넌트 임포트
import Header from '@/layouts/Header.vue';
import Sidebar from '@/layouts/Sidebar.vue';
// MainLayout 에서 직접 사용하는 Element Plus 컴포넌트
import { ElContainer, ElMain, ElFooter } from 'element-plus';
import TagsView from "@/layouts/TagsView.vue";

import { useTagsViewStore } from "@/store";

// 사이드바 접힘/펼침 상태 관리
const isCollapsed = ref(false);

// KeepAlive 로 캐시할 컴포넌트 이름 목록 (컴포넌트의 name 옵션과 일치해야 함)
// TODO: 실제로는 Pinia 스토어 등에서 동적으로 관리
const tagsViewStore = useTagsViewStore();
const cachedViews = computed(() => tagsViewStore.cachedViews);

// Header 컴포넌트에서 발생시킨 이벤트를 받아 상태 변경
function toggleSidebar() {
  isCollapsed.value = !isCollapsed.value;
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
  background-color: #f0f2f5;
}

.main-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex-grow: 1;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 15px;
  height: calc(100vh - 50px - 34px - 30px); /* 높이 계산 확인! */
  overflow-y: auto;
}

.main-content-wrapper {
  background-color: #ffffff;
}

.layout-footer {
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-size: 12px;
  color: #909399;
  background-color: #f0f2f5;
  border-top: 1px solid #e6e6e6;
}
</style>
