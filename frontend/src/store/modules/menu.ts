import { ref } from 'vue';
import { defineStore } from 'pinia';
import type { MenuInfo } from '@/types/menu';
import { fetchMyMenusApi } from '@/services/menu';

export const useMenuStore = defineStore('menu', () => {
  // === State ===
  const menuItems = ref<MenuInfo[]>([]); // 메뉴 트리 데이터
  const isLoading = ref<boolean>(false);
  const error = ref<string | null>(null); // 에러 상태 추가

  // === Getters ===

  // === Actions ===
  async function fetchMenus() {
    if (isLoading.value || menuItems.value.length > 0) return;

    isLoading.value = true;
    error.value = null; // 에러 초기화
    try {
      const response = await fetchMyMenusApi();
      menuItems.value = response.data || [];
      console.log('Menus loaded into store:', menuItems.value);

    } catch (err: any) {
      console.error('Error fetching menus:', err);
      error.value = err.message || '메뉴 정보를 불러오는 데 실패했습니다.';
      menuItems.value = []; // 실패 시 초기화
    } finally {
      isLoading.value = false;
    }
  }

  function clearMenus() {
    menuItems.value = [];
    isLoading.value = false;
    error.value = null;
  }

  return {
    menuItems,
    isLoading,
    error,
    fetchMenus,
    clearMenus,
  };
});
