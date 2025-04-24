// themeManager.ts
export const themeManager = {
  // 현재 테마 가져오기 (로컬 스토리지에서)
  getCurrentTheme(): 'light' | 'dark' {
    return localStorage.getItem('theme') === 'dark' ? 'dark' : 'light';
  },

  // 테마 전환
  toggleTheme() {
    const currentTheme = this.getCurrentTheme();
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';

    if (newTheme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }

    localStorage.setItem('theme', newTheme);
    return newTheme;
  },

  // 초기 테마 설정
  initTheme() {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark' ||
      (!savedTheme && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
    } else {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
    }
  }
};
