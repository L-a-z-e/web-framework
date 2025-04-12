<template>
  <!-- 하위 메뉴가 있는 경우: ElSubMenu -->
  <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.menuId">
    <template #title>
      <el-icon v-if="item.menuIcon"><component :is="iconComponent" /></el-icon>
      <span>{{ item.menuNm }}</span>
    </template>
    <!-- 재귀 호출 -->
    <menu-item v-for="child in item.children" :key="child.menuId" :item="child" />
  </el-sub-menu>

  <!-- 하위 메뉴가 없는 경우: ElMenuItem -->
  <el-menu-item v-else :index="item.menuId"> <!-- menuId 를 경로(index)로 사용 -->
    <el-icon v-if="item.menuIcon"><component :is="iconComponent" /></el-icon>
    <template #title>{{ item.menuNm }}</template>
  </el-menu-item>
</template>

<script setup lang="ts">
import { defineProps, computed } from 'vue';
import { ElSubMenu, ElMenuItem, ElIcon } from 'element-plus';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import type { MenuInfo } from '@/types/menu';

const props = defineProps<{ item: MenuInfo }>();

// 아이콘 컴포넌트 동적 로딩
const iconComponent = computed(() => {
  const iconName = props.item.menuIcon || 'Menu';
  // @ts-ignore
  return ElementPlusIconsVue[iconName] || ElementPlusIconsVue['Menu'];
});

// 이 컴포넌트 자체의 이름 (KeepAlive 등에서 사용 가능)
defineOptions({ name: 'MenuItem' });
</script>

<style scoped>
</style>
