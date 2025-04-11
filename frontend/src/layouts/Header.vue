<template>
  <el-header class="layout-header">
    <div class="header-left">
      <el-icon class="sidebar-toggle" @click="emit('toggle-sidebar')">
        <Expand v-if="isCollapsed" />
        <Fold v-else />
      </el-icon>
      <!-- 브레드크럼 -->
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">HOME</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <slot name="header-right">
        <el-input placeholder="Search..." class="header-search" :prefix-icon="Search" size="small"/>
        <el-tooltip effect="dark" content="알림" placement="bottom">
          <el-badge :value="3" class="item" type="warning">
            <el-icon size="18"><Bell /></el-icon>
          </el-badge>
        </el-tooltip>
        <el-dropdown>
           <span class="user-info">
             <el-avatar size="small" :icon="UserFilled" class="user-avatar"/> Test User <el-icon><ArrowDown /></el-icon>
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
</template>

<script setup lang="ts">
// --- Script 부분은 이전 답변과 동일하게 유지 ---
import { defineProps, defineEmits, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import {
  ElHeader, ElIcon, ElInput, ElBadge, ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar, ElBreadcrumb, ElBreadcrumbItem, ElTooltip // Tooltip 추가
} from 'element-plus';
import { Expand, Fold, Bell, UserFilled, ArrowDown, Search } from '@element-plus/icons-vue';

defineProps({ isCollapsed: { type: Boolean, default: false } });
const emit = defineEmits(['toggle-sidebar']);
const router = useRouter();
const route = useRoute();
const currentRouteTitle = computed(() => route.meta.title || 'Page');
function logout() { router.push('/login'); }
</script>

<style scoped>
.layout-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px; /* 헤더 높이 50px */
  padding: 0 20px; /* 좌우 패딩 */
  border-bottom: 1px solid #f0f2f5; /* 헤더 하단 구분선 */
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08); /* 그림자 */
}

.header-left {
  display: flex;
  align-items: center;
}

.sidebar-toggle {
  font-size: 20px; /* 아이콘 크기 */
  cursor: pointer;
  margin-right: 15px; /* 브레드크럼과의 간격 */
  color: #515a6e; /* 아이콘 색상 */
}
.sidebar-toggle:hover {
  color: #409EFF; /* 호버 색상 */
}

.el-breadcrumb {
  margin-left: 0; /* 토글 아이콘과의 간격으로 조정 */
}
/* 브레드크럼 내부 스타일 조정 */
.el-breadcrumb :deep(.el-breadcrumb__inner),
.el-breadcrumb :deep(.el-breadcrumb__separator) {
  color: #515a6e; /* 기본 텍스트 색상 */
  font-weight: normal;
}
.el-breadcrumb :deep(.el-breadcrumb__inner a:hover),
.el-breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: #409EFF; /* 링크 호버 색상 */
  cursor: pointer;
}
/* 현재 페이지 브레드크럼 색상 */
.el-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #303133; /* 마지막 항목은 조금 더 진하게 */
  font-weight: 600;
}


.header-right {
  display: flex;
  align-items: center;
  gap: 20px; /* 요소들 사이 간격 */
}

.header-search {
  width: 180px;
}
.header-search :deep(.el-input__wrapper) {
  border-radius: 15px; /* 검색창 둥글게 */
}

.header-right .item .el-icon {
  color: #515a6e; /* 알림 아이콘 색상 */
  cursor: pointer;
}
.header-right .item .el-badge__content {
  transform: translateY(-2px) translateX(5px); /* 뱃지 위치 미세 조정 */
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #515a6e;
  font-size: 14px;
}
.user-avatar {
  background-color: #e4e7ed; /* 기본 아바타 배경 */
  color: #909399; /* 기본 아바타 아이콘 색상 */
}
</style>
