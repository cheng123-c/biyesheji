import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // 公开路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },

  // 受保护路由
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('../views/About.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },

  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫 - 检查认证
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 页面加载时恢复认证状态
  if (!authStore.isAuthenticated && !authStore.accessToken) {
    await authStore.restoreAuthState()
  }

  const requiresAuth = to.meta.requiresAuth !== false

  // 如果需要认证但用户未登录
  if (requiresAuth && !authStore.isAuthenticated) {
    // 重定向到登录页
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 如果已登录，尝试访问登录/注册页，重定向到首页
  if (!requiresAuth && authStore.isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router

