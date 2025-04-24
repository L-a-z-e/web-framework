<template>
  <el-header class="layout-header">
    <div class="header-left">
      <el-icon class="sidebar-toggle" @click="emit('toggle-sidebar')">
        <Expand v-if="isCollapsed" />
        <Fold v-else />
      </el-icon>

      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/' }">HOME</el-breadcrumb-item>
        <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <div class="header-right">
      <slot name="header-right">
        <el-input placeholder="Search..." class="header-search" :prefix-icon="Search" size="small"/>
        <ThemeToggle />
        <el-tooltip effect="dark" content="알림" placement="bottom">
          <el-badge :value="3" class="item" type="warning">
            <el-icon size="18"><Bell /></el-icon>
          </el-badge>
        </el-tooltip>
        <el-dropdown>
          <span class="user-info">
            <el-avatar size="small" :icon="UserFilled" class="user-avatar"/> {{ authStore.username }} <el-icon><ArrowDown /></el-icon>
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
import { defineProps, defineEmits, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/store';
import {
  ElHeader, ElIcon, ElInput, ElBadge, ElDropdown, ElDropdownMenu, ElDropdownItem, ElAvatar, ElBreadcrumb, ElBreadcrumbItem, ElTooltip // Tooltip 추가
} from 'element-plus';
import { Expand, Fold, Bell, UserFilled, ArrowDown, Search } from '@element-plus/icons-vue';
import ThemeToggle from '@/components/ThemeToggle.vue';

defineProps({ isCollapsed: { type: Boolean, default: false } });
const emit = defineEmits(['toggle-sidebar']);
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const currentRouteTitle = computed(() => route.meta.title || 'Page');

async function logout() {
  await authStore.logout();
  router.push('/login');
}
</script>

<style scoped>
.layout-header {
  background-color: var(--header-bg-color, #ffffff);
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: var(--header-height, 50px);
  padding: 0 20px;
  border-bottom: 1px solid var(--header-border-color, #f0f2f5);
  box-shadow: var(--header-shadow, 0 1px 4px rgba(0, 21, 41, 0.08));
}

.header-left {
  display: flex;
  align-items: center;
}

.sidebar-toggle {
  font-size: 20px;
  cursor: pointer;
  margin-right: 15px;
  color: var(--icon-color, #515a6e);
}
.sidebar-toggle:hover {
  color: var(--button-primary-color, #409EFF);
}

.el-breadcrumb {
  margin-left: 0;
}

.el-breadcrumb :deep(.el-breadcrumb__inner),
.el-breadcrumb :deep(.el-breadcrumb__separator) {
  color: var(--header-text-color, #515a6e);
  font-weight: normal;
}
.el-breadcrumb :deep(.el-breadcrumb__inner a:hover),
.el-breadcrumb :deep(.el-breadcrumb__inner.is-link:hover) {
  color: var(--button-primary-color, #409EFF);
  cursor: pointer;
}

.el-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: var(--header-text-color, #303133);
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
  border-radius: 15px;
  background-color: var(--form-bg-color, #f5f7fa);
}

.header-right .item .el-icon {
  color: var(--icon-color, #515a6e);
  cursor: pointer;
}
.header-right .item .el-badge__content {
  transform: translateY(-2px) translateX(5px);
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--header-text-color, #515a6e);
  font-size: 14px;
}
.user-avatar {
  background-color: var(--form-disabled-bg, #e4e7ed);
  color: var(--form-disabled-text, #909399);
}
</style>

