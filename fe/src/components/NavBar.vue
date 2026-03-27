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
        <!-- 未登录 -->
        <li v-if="!isAuthenticated">
          <router-link to="/login" class="nav-link">登录</router-link>
        </li>
        <li v-if="!isAuthenticated">
          <router-link to="/register" class="nav-link btn btn-outline">注册</router-link>
        </li>

        <!-- 已登录导航 -->
        <li v-if="isAuthenticated">
          <router-link to="/" class="nav-link" active-class="nav-link-active" exact>
            🏠 首页
          </router-link>
        </li>
        <li v-if="isAuthenticated">
          <router-link to="/health-data" class="nav-link" active-class="nav-link-active">
            📊 健康数据
          </router-link>
        </li>
        <li v-if="isAuthenticated">
          <router-link to="/assessment" class="nav-link" active-class="nav-link-active">
            🧠 AI 评测
          </router-link>
        </li>
        <li v-if="isAuthenticated">
          <router-link to="/medical-records" class="nav-link" active-class="nav-link-active">
            🏥 医疗记录
          </router-link>
        </li>
        <li v-if="isAuthenticated">
          <router-link to="/suggestions" class="nav-link" active-class="nav-link-active">
            💡 健康建议
          </router-link>
        </li>
        <li v-if="isAuthenticated">
          <router-link to="/interventions" class="nav-link" active-class="nav-link-active">
            🏃 干预方案
          </router-link>
        </li>

        <!-- 通知图标（带未读数） -->
        <li v-if="isAuthenticated" class="notif-item">
          <router-link to="/notifications" class="nav-link notif-link" active-class="nav-link-active">
            🔔
            <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </router-link>
        </li>

        <!-- 用户菜单 -->
        <li v-if="isAuthenticated" class="user-menu">
          <button @click="toggleUserMenu" class="user-button">
            <span class="user-avatar">{{ userInitial }}</span>
            <span class="username">{{ displayName }}</span>
            <span class="dropdown-icon" :class="{ rotated: userMenuOpen }">▼</span>
          </button>

          <div v-if="userMenuOpen" class="dropdown-menu">
            <router-link to="/questionnaires" class="dropdown-item" @click="closeMenus">
              <span class="icon">📋</span>
              健康问卷
            </router-link>
            <router-link to="/health-content" class="dropdown-item" @click="closeMenus">
              <span class="icon">📚</span>
              健康知识库
            </router-link>
            <router-link to="/symptom-analysis" class="dropdown-item" @click="closeMenus">
              <span class="icon">🔬</span>
              症状分析
            </router-link>
            <router-link to="/feedback" class="dropdown-item" @click="closeMenus">
              <span class="icon">💬</span>
              意见反馈
            </router-link>
            <div class="dropdown-divider"></div>
            <router-link to="/profile" class="dropdown-item" @click="closeMenus">
              <span class="icon">👤</span>
              个人信息
            </router-link>
            <router-link v-if="isAdmin" to="/admin" class="dropdown-item" @click="closeMenus">
              <span class="icon">🛠️</span>
              管理后台
            </router-link>
            <router-link to="/about" class="dropdown-item" @click="closeMenus">
              <span class="icon">ℹ️</span>
              关于
            </router-link>
            <div class="dropdown-divider"></div>
            <button @click="handleLogout" class="dropdown-item btn-logout">
              <span class="icon">🚪</span>
              退出登录
            </button>
          </div>
        </li>
      </ul>

      <!-- 移动菜单按钮 -->
      <button class="menu-toggle" @click="menuOpen = !menuOpen">
        <span :class="{ open: menuOpen }"></span>
        <span :class="{ open: menuOpen }"></span>
        <span :class="{ open: menuOpen }"></span>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getUnreadCount } from '@/services/assessmentApi'

const router = useRouter()
const authStore = useAuthStore()

const menuOpen = ref(false)
const userMenuOpen = ref(false)
const unreadCount = ref(0)

const isAuthenticated = computed(() => authStore.isAuthenticated)
const username = computed(() => authStore.username)

const isAdmin = computed(() => {
  const user = authStore.user
  if (!user) return false
  return user.role === 'ADMIN' || user.role === 'admin'
})

const displayName = computed(() => {
  const user = authStore.user
  return user?.realName || user?.username || '用户'
})

const userInitial = computed(() => {
  const name = displayName.value
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const toggleUserMenu = () => {
  userMenuOpen.value = !userMenuOpen.value
}

const closeMenus = () => {
  userMenuOpen.value = false
  menuOpen.value = false
}

const handleLogout = () => {
  authStore.logout()
  closeMenus()
  router.push('/login')
}

// 加载未读通知数
const loadUnreadCount = async () => {
  if (!isAuthenticated.value) return
  try {
    const response = await getUnreadCount()
    unreadCount.value = response.data || 0
  } catch {
    unreadCount.value = 0
  }
}

// 点击外部关闭菜单
const handleOutsideClick = (e) => {
  if (!e.target.closest('.user-menu')) {
    userMenuOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
  if (isAuthenticated.value) {
    loadUnreadCount()
    // 每分钟刷新一次未读数
    const timer = setInterval(loadUnreadCount, 60000)
    onUnmounted(() => clearInterval(timer))
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick)
})
</script>

<style scoped>
.navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0;
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
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

.navbar-brand {}

.brand-link {
  display: flex;
  align-items: center;
  color: white;
  text-decoration: none;
  font-weight: bold;
  font-size: 18px;
  gap: 8px;
}

.brand-link:hover { opacity: 0.9; }

.logo-icon { font-size: 24px; }

.brand-text { display: none; }

@media (min-width: 768px) {
  .brand-text { display: inline; }
}

.nav-menu {
  display: flex;
  align-items: center;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 4px;
}

.nav-link {
  color: rgba(255,255,255,0.85);
  text-decoration: none;
  padding: 8px 14px;
  border-radius: 6px;
  transition: all 0.2s;
  display: block;
  font-size: 14px;
  font-weight: 500;
}

.nav-link:hover {
  background-color: rgba(255,255,255,0.15);
  color: white;
}

.nav-link-active {
  background-color: rgba(255,255,255,0.2);
  color: white;
}

.btn-outline {
  border: 1px solid rgba(255,255,255,0.5);
  color: white;
}

/* 通知图标 */
.notif-item { position: relative; }

.notif-link {
  position: relative;
  font-size: 18px;
  padding: 6px 12px;
}

.notif-badge {
  position: absolute;
  top: 0px;
  right: 0px;
  min-width: 16px;
  height: 16px;
  background: #e74c3c;
  color: white;
  font-size: 10px;
  font-weight: bold;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 3px;
}

/* 用户菜单 */
.user-menu { position: relative; }

.user-button {
  background: rgba(255,255,255,0.15);
  color: white;
  border: 1px solid rgba(255,255,255,0.25);
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.user-button:hover { background: rgba(255,255,255,0.25); }

.user-avatar {
  width: 26px;
  height: 26px;
  background: rgba(255,255,255,0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
}

.dropdown-icon {
  font-size: 9px;
  transition: transform 0.2s;
}

.dropdown-icon.rotated { transform: rotate(180deg); }

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  min-width: 180px;
  z-index: 1000;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  transition: background 0.15s;
  font-size: 14px;
}

.dropdown-item:hover { background: #f5f5f5; }

.dropdown-item .icon { font-size: 15px; }

.dropdown-divider {
  height: 1px;
  background: #eee;
  margin: 4px 0;
}

.btn-logout:hover { background: #fff5f5; }

/* 移动菜单按钮 */
.menu-toggle {
  display: none;
  flex-direction: column;
  background: none;
  border: none;
  cursor: pointer;
  gap: 5px;
  padding: 4px;
}

.menu-toggle span {
  width: 24px;
  height: 2px;
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
    background: linear-gradient(135deg, #667eea, #764ba2);
    padding: 10px 20px;
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.3s ease;
    gap: 0;
    align-items: stretch;
  }

  .nav-menu.active {
    max-height: 500px;
    border-bottom: 1px solid rgba(255,255,255,0.2);
    box-shadow: 0 6px 12px rgba(0,0,0,0.15);
  }

  .nav-menu.active > li {
    padding: 4px 0;
  }

  .menu-toggle { display: flex; }

  .dropdown-menu {
    position: static;
    background: rgba(255,255,255,0.1);
    box-shadow: none;
    border-radius: 6px;
    margin-top: 4px;
  }

  .dropdown-item {
    color: rgba(255,255,255,0.9);
    padding: 10px 14px;
  }

  .dropdown-item:hover { background: rgba(255,255,255,0.2); }

  .btn-logout:hover { background: rgba(255,0,0,0.15); }
}
</style>

