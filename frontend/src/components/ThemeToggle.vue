<template>
  <el-tooltip effect="dark" :content="tooltipText" placement="bottom">
    <el-button @click="toggleTheme" size="small" circle>
      <el-icon><Moon v-if="currentTheme === 'light'" /><Sunny v-else /></el-icon>
    </el-button>
  </el-tooltip>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { themeManager } from '@/themeManager.ts';
import { Moon, Sunny } from '@element-plus/icons-vue';
import { ElButton, ElIcon, ElTooltip } from 'element-plus';

const currentTheme = ref<'light' | 'dark'>(themeManager.getCurrentTheme());

const tooltipText = computed(() => {
  return currentTheme.value === 'light' ? '다크 모드로 전환' : '라이트 모드로 전환';
});

function toggleTheme() {
  currentTheme.value = themeManager.toggleTheme();
}

onMounted(() => {
  currentTheme.value = themeManager.getCurrentTheme();
});
</script>

<style scoped>
.el-button {
  padding: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.el-icon {
  margin: 0;
  font-size: 16px;
}
</style>
