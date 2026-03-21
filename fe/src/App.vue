<template>
  <div class="app">
    <!-- 导航栏 -->
    <NavBar />

    <!-- 主内容 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>&copy; 健康评测系统 - 毕业设计项目</p>
    </footer>
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f5f7fa;
}
</style>

<style scoped>
.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  padding: 0;
}

.footer {
  background-color: #f5f5f5;
  padding: 20px;
  text-align: center;
  color: #666;
  border-top: 1px solid #eee;
  margin-top: auto;
}

.footer p {
  margin: 0;
  font-size: 14px;
}

/* 登录/注册页面不显示footer和导航栏的额外margin */
</style>

