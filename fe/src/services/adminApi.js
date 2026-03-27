import apiClient from './api'

/**
 * 管理员后台 API 调用
 */

// ======= 统计数据 =======

// 获取系统统计总览
export const getStatisticsOverview = () => {
  return apiClient.get('/v1/admin/statistics/overview')
}

// 获取风险分布统计
export const getRiskDistribution = (days = 30) => {
  return apiClient.get('/v1/admin/statistics/risk-distribution', {
    params: { days }
  })
}

// ======= 用户管理 =======

// 获取用户列表（分页）
export const getAdminUserList = (pageNum = 1, pageSize = 10) => {
  return apiClient.get('/v1/admin/users', {
    params: { pageNum, pageSize }
  })
}

// 获取指定用户详情
export const getAdminUserDetail = (userId) => {
  return apiClient.get(`/v1/admin/users/${userId}`)
}

// 更新用户状态（启用/禁用）
export const updateUserStatus = (userId, status) => {
  return apiClient.put(`/v1/admin/users/${userId}/status`, { status })
}

// 删除用户（软删除）
export const deleteAdminUser = (userId) => {
  return apiClient.delete(`/v1/admin/users/${userId}`)
}

// ======= 高风险评测 =======

// 获取高风险评测报告列表
export const getHighRiskAssessments = (days = 30) => {
  return apiClient.get('/v1/admin/assessments/high-risk', {
    params: { days }
  })
}

// ======= 系统健康 =======

// 获取系统健康状态
export const getSystemHealth = () => {
  return apiClient.get('/v1/admin/system/health')
}

