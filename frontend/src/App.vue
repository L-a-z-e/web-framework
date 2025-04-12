<template>
  <router-view />
</template>

<script setup lang="ts">

import {useAuthStore} from "@/store";
import {onMounted} from "vue";

const authStore = useAuthStore();

onMounted(() => {
  authStore.checkLoginStatus();
})
</script>

<style>
/* 전역 스타일 */
body { margin: 0; }
#app { height: 100vh; } /* 전체 높이 설정 */
</style>
