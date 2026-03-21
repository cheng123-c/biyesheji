<template>
  <nav class="navbar">
    <div class="navbar-container">
      <!-- Logo -->
      <div class="navbar-brand">
        <router-link to="/" class="brand-link">
          <span class="logo-icon">🏥</span>
          <span class="brand-text">健康评测系统</span>
        </router-link>
      </div>

      <!-- 导航菜单 -->
      <ul class="nav-menu" :class="{ active: menuOpen }">
        <li v-if="!isAuthenticated">
          <router-link to="/login" class="nav-link">登录</router-link>
        </li>
        <li v-if="!isAuthenticated">
          <router-link to="/register" class="nav-link btn btn-primary">注册</router-link>
        </li>

        <li v-if="isAuthenticated">
          <router-link to="/" class="nav-link">首页</router-link>
        </li>
        <li v-if="isAuthenticated">
          <router-link to="/about" class="nav-link">关于</router-link>
        </li>

        <!-- 用户菜单 -->
        <li v-if="isAuthenticated" class="user-menu">
          <button @click="toggleUserMenu" class="user-button">
            <span class="username">{{ username }}</span>
            <span class="dropdown-icon">▼</span>
          </button>

          <div v-if="userMenuOpen" class="dropdown-menu">
            <router-link to="/profile" class="dropdown-item">
              <span class="icon">👤</span>
              个人信息
            </router-link>
            <button @click="handleLogout" class="dropdown-item btn-logout">
              <span class="icon">🚪</span>
              登出
            </button>
          </div>
        </li>
      </ul>

      <!-- 移动菜单按钮 -->
      <button class="menu-toggle" @click="menuOpen = !menuOpen">
        <span></span>
        <span></span>
        <span></span>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const menuOpen = ref(false)
const userMenuOpen = ref(false)

const isAuthenticated = computed(() => authStore.isAuthenticated)
const username = computed(() => authStore.username)

const toggleUserMenu = () => {
  userMenuOpen.value = !userMenuOpen.value
}

const handleLogout = () => {
  authStore.logout()
  userMenuOpen.value = false
  menuOpen.value = false
  router.push('/login')
}

// 点击其他地方关闭菜单
document.addEventListener('click', (e) => {
  if (!e.target.closest('.user-menu')) {
    userMenuOpen.value = false
  }
})
</script>

<style scoped>
.navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.navbar-brand {
  display: flex;
  align-items: center;
}

.brand-link {
  display: flex;
  align-items: center;
  color: white;
  text-decoration: none;
  font-weight: bold;
  font-size: 20px;
  transition: all 0.3s;
}

.brand-link:hover {
  opacity: 0.8;
}

.logo-icon {
  font-size: 24px;
  margin-right: 8px;
}

.brand-text {
  display: none;
}

@media (min-width: 768px) {
  .brand-text {
    display: inline;
  }
}

.nav-menu {
  display: flex;
  align-items: center;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 10px;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s;
  display: block;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.btn-primary {
  background-color: rgba(255, 255, 255, 0.3);
  border: 1px solid white;
}

.btn-primary:hover {
  background-color: rgba(255, 255, 255, 0.4);
}

.user-menu {
  position: relative;
}

.user-button {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  transition: all 0.3s;
}

.user-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.dropdown-icon {
  font-size: 10px;
  transition: transform 0.3s;
}

.user-menu.active .dropdown-icon {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 180px;
  margin-top: 8px;
  z-index: 1000;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}

.dropdown-item .icon {
  font-size: 16px;
}

.btn-logout:hover {
  background-color: #fee;
}

.menu-toggle {
  display: none;
  flex-direction: column;
  background: none;
  border: none;
  cursor: pointer;
  gap: 6px;
}

.menu-toggle span {
  width: 25px;
  height: 3px;
  background-color: white;
  border-radius: 2px;
  transition: all 0.3s;
}

/* 移动响应 */
@media (max-width: 768px) {
  .nav-menu {
    position: absolute;
    top: 60px;
    left: 0;
    right: 0;
    flex-direction: column;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 10px;
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.3s;
    gap: 0;
  }

  .nav-menu.active {
    max-height: 400px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  }

  .nav-menu.active li {
    padding: 10px 0;
  }

  .menu-toggle {
    display: flex;
  }

  .dropdown-menu {
    position: static;
    background: rgba(255, 255, 255, 0.1);
    box-shadow: none;
    margin-top: 0;
  }

  .dropdown-item {
    color: white;
    padding: 10px 16px;
  }

  .dropdown-item:hover {
    background-color: rgba(255, 255, 255, 0.2);
  }

  .btn-logout:hover {
    background-color: rgba(255, 255, 255, 0.2);
  }
}
</style>

