import apiClient from './api'

/**
 * 认证相关的 API 调用
 */

// 用户注册
export const register = (data) => {
  return apiClient.post('/v1/auth/register', data)
}

// 用户登录
export const login = (data) => {
  return apiClient.post('/v1/auth/login', data)
}

// 刷新 Token
export const refreshToken = (refreshToken) => {
  return apiClient.post('/v1/auth/refresh', {}, {
    headers: {
      Authorization: `Bearer ${refreshToken}`
    }
  })
}

// 修改密码
export const changePassword = (data) => {
  return apiClient.put('/v1/auth/change-password', data)
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return apiClient.get('/v1/users/me')
}

// 获取用户信息（根据 ID）
export const getUserById = (userId) => {
  return apiClient.get(`/v1/users/${userId}`)
}

// 更新用户信息
export const updateUserProfile = (data) => {
  return apiClient.put('/v1/users/me', data)
}

// 获取用户列表
export const getUserList = (pageNum = 1, pageSize = 20) => {
  return apiClient.get('/v1/users', {
    params: {
      pageNum,
      pageSize
    }
  })
}

// 删除用户账户
export const deleteAccount = () => {
  return apiClient.delete('/v1/users/me')
}

