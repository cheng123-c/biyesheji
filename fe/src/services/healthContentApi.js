import apiClient from './api'

// 获取已发布内容列表
export const getHealthContents = (pageNum = 1, pageSize = 10, contentType = null) => {
  return apiClient.get('/v1/health-content', { params: { pageNum, pageSize, contentType } })
}

// 搜索健康内容
export const searchHealthContent = (keyword) => {
  return apiClient.get('/v1/health-content/search', { params: { keyword } })
}

// 获取内容详情
export const getHealthContentById = (id) => {
  return apiClient.get(`/v1/health-content/${id}`)
}

// 管理员接口
export const adminGetAllContent = (pageNum = 1, pageSize = 10) => {
  return apiClient.get('/v1/health-content/admin/all', { params: { pageNum, pageSize } })
}

export const adminCreateContent = (data) => {
  return apiClient.post('/v1/health-content/admin', data)
}

export const adminUpdateContent = (id, data) => {
  return apiClient.put(`/v1/health-content/admin/${id}`, data)
}

export const adminDeleteContent = (id) => {
  return apiClient.delete(`/v1/health-content/admin/${id}`)
}

export const adminTogglePublish = (id) => {
  return apiClient.patch(`/v1/health-content/admin/${id}/publish`)
}

