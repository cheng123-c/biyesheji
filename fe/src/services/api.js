import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

// Create axios instance
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// Request interceptor - 添加认证 Token
apiClient.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    const token = authStore.accessToken

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor - 处理响应和错误
apiClient.interceptors.response.use(
  response => {
    // 返回数据（ApiResponse 对象）
    return response.data
  },
  error => {
    const authStore = useAuthStore()

    // 处理 401 Unauthorized - Token 过期或无效
    if (error.response && error.response.status === 401) {
      console.warn('Token 过期，跳转到登录页...')

      // 清除认证信息
      authStore.logout()

      // 重定向到登录页
      window.location.href = '/login'
    }

    // 处理 403 Forbidden - 权限不足
    if (error.response && error.response.status === 403) {
      console.warn('权限不足，拒绝访问')
      // 不做重定向，让具体页面处理 403 错误
    }

    if (error.response) {
      console.error('Response error:', error.response.status, error.response.data)
    } else if (error.request) {
      console.error('Request error:', error.request)
    } else {
      console.error('Error:', error.message)
    }

    return Promise.reject(error)
  }
)

export default apiClient

