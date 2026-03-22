import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/services/authApi'

/**
 * 认证状态管理 Store
 *
 * 功能：
 * - 管理用户认证状态
 * - 管理 Token
 * - 管理当前用户信息
 */
export const useAuthStore = defineStore('auth', () => {
  // ============ State ============

  // 访问 Token
  const accessToken = ref(localStorage.getItem('accessToken') || null)

  // 刷新 Token
  const refreshToken = ref(localStorage.getItem('refreshToken') || null)

  // 当前用户信息
  const user = ref(null)

  // 加载状态
  const isLoading = ref(false)

  // 错误信息
  const error = ref(null)

  // ============ Computed ============

  // 是否已登录
  const isAuthenticated = computed(() => {
    return accessToken.value !== null && user.value !== null
  })

  // 用户名
  const username = computed(() => {
    return user.value?.username || null
  })

  // 用户 ID
  const userId = computed(() => {
    return user.value?.id || null
  })

  // ============ Actions ============

  /**
   * 注册用户
   */
  const register = async (formData) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.register(formData)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || '注册失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 登录
   */
  const login = async (credentials) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.login(credentials)
      const { access_token, refresh_token } = response.data

      // 保存 Token
      accessToken.value = access_token
      refreshToken.value = refresh_token

      // 保存到 localStorage
      localStorage.setItem('accessToken', access_token)
      localStorage.setItem('refreshToken', refresh_token)

      // 获取完整用户信息
      await fetchCurrentUser()

      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || '登录失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 获取当前用户信息
   */
  const fetchCurrentUser = async () => {
    if (!accessToken.value) {
      return
    }

    try {
      const response = await authApi.getCurrentUser()
      user.value = response.data
      return response.data
    } catch (err) {
      console.error('获取用户信息失败:', err)
      // 如果获取失败，保持现有状态
    }
  }

  /**
   * 更新用户信息
   */
  const updateProfile = async (profileData) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.updateUserProfile(profileData)
      user.value = response.data
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || '更新失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 修改密码
   */
  const changePassword = async (passwordData) => {
    isLoading.value = true
    error.value = null

    try {
      const response = await authApi.changePassword(passwordData)
      return response.data
    } catch (err) {
      error.value = err.response?.data?.message || '密码修改失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 刷新 Token
   */
  const refreshAccessToken = async () => {
    if (!refreshToken.value) {
      logout()
      return false
    }

    try {
      const response = await authApi.refreshToken(refreshToken.value)
      const { access_token, refresh_token } = response.data

      // 更新 Token
      accessToken.value = access_token
      refreshToken.value = refresh_token

      // 保存到 localStorage
      localStorage.setItem('accessToken', access_token)
      localStorage.setItem('refreshToken', refresh_token)

      return true
    } catch (err) {
      console.error('Token 刷新失败:', err)
      logout()
      return false
    }
  }

  /**
   * 登出
   */
  const logout = () => {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
    error.value = null

    // 清除 localStorage
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  /**
   * 删除账户
   */
  const deleteAccount = async () => {
    isLoading.value = true
    error.value = null

    try {
      await authApi.deleteAccount()
      logout()
      return true
    } catch (err) {
      error.value = err.response?.data?.message || '删除账户失败'
      throw err
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 恢复认证状态（页面刷新时调用）
   */
  const restoreAuthState = async () => {
    const savedAccessToken = localStorage.getItem('accessToken')
    const savedRefreshToken = localStorage.getItem('refreshToken')

    if (savedAccessToken && savedRefreshToken) {
      accessToken.value = savedAccessToken
      refreshToken.value = savedRefreshToken

      try {
        await fetchCurrentUser()
      } catch (err) {
        console.error('恢复认证状态失败:', err)
        logout()
      }
    }
  }

  return {
    // State
    accessToken,
    refreshToken,
    user,
    isLoading,
    error,

    // Computed
    isAuthenticated,
    username,
    userId,

    // Actions
    register,
    login,
    fetchCurrentUser,
    updateProfile,
    changePassword,
    refreshAccessToken,
    logout,
    deleteAccount,
    restoreAuthState
  }
})

