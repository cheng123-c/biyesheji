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

  // 受保护路由 - 主页（数据看板）
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },

  // 健康数据管理
  {
    path: '/health-data',
    name: 'HealthData',
    component: () => import('../views/HealthData.vue'),
    meta: { requiresAuth: true }
  },

  // AI 健康评测
  {
    path: '/assessment',
    name: 'Assessment',
    component: () => import('../views/Assessment.vue'),
    meta: { requiresAuth: true }
  },

  // 通知
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('../views/Notifications.vue'),
    meta: { requiresAuth: true }
  },

  // 医疗记录
  {
    path: '/medical-records',
    name: 'MedicalRecords',
    component: () => import('../views/MedicalRecords.vue'),
    meta: { requiresAuth: true }
  },

  // 健康建议
  {
    path: '/suggestions',
    name: 'Suggestions',
    component: () => import('../views/Suggestions.vue'),
    meta: { requiresAuth: true }
  },

  // 干预方案
  {
    path: '/interventions',
    name: 'Interventions',
    component: () => import('../views/Interventions.vue'),
    meta: { requiresAuth: true }
  },

  // 健康问卷
  {
    path: '/questionnaires',
    name: 'Questionnaire',
    component: () => import('../views/Questionnaire.vue'),
    meta: { requiresAuth: true }
  },

  // 健康知识库
  {
    path: '/health-content',
    name: 'HealthContent',
    component: () => import('../views/HealthContent.vue'),
    meta: { requiresAuth: true }
  },

  // 症状分析（知识图谱）
  {
    path: '/symptom-analysis',
    name: 'SymptomAnalysis',
    component: () => import('../views/SymptomAnalysis.vue'),
    meta: { requiresAuth: true }
  },

  // 意见反馈
  {
    path: '/feedback',
    name: 'Feedback',
    component: () => import('../views/Feedback.vue'),
    meta: { requiresAuth: true }
  },

  // 个人信息
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    meta: { requiresAuth: true }
  },

  // 关于
  {
    path: '/about',
    name: 'About',
    component: () => import('../views/About.vue'),
    meta: { requiresAuth: true }
  },

  // 管理员后台
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/AdminDashboard.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },

  // 旧首页重定向
  {
    path: '/home',
    redirect: '/'
  },

  // 404 页面
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫 - 检查认证
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()

  // 页面加载时恢复认证状态：
  // - 有 accessToken 但 user 未加载时（如页面刷新），需要重新获取用户信息
  // - 没有 accessToken 时直接不恢复
  if (authStore.accessToken && !authStore.user) {
    await authStore.restoreAuthState()
  }

  const requiresAuth = to.meta.requiresAuth !== false

  // 如果需要认证但用户未登录
  if (requiresAuth && !authStore.isAuthenticated) {
    next({
      name: 'Login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 如果已登录，尝试访问登录/注册页，重定向到首页
  if (!requiresAuth && authStore.isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
    next({ name: 'Dashboard' })
    return
  }

  // 检查管理员权限
  const requiresAdmin = to.meta.requiresAdmin === true
  if (requiresAdmin && authStore.user) {
    const userRole = authStore.user.role
    if (userRole !== 'ADMIN' && userRole !== 'admin') {
      // 非管理员尝试访问管理员页面，重定向到首页
      next({ name: 'Dashboard' })
      return
    }
  }

  next()
})

export default router

