import apiClient from './api'

/**
 * 健康建议相关 API
 */

// 获取建议列表（分页）
export const getSuggestions = (pageNum = 1, pageSize = 10, priority) => {
  return apiClient.get('/v1/suggestions', {
    params: { pageNum, pageSize, priority }
  })
}

// 获取未读建议
export const getUnreadSuggestions = () => {
  return apiClient.get('/v1/suggestions/unread')
}

// 获取未读建议数量
export const getSuggestionUnreadCount = () => {
  return apiClient.get('/v1/suggestions/unread/count')
}

// 根据ID获取建议详情
export const getSuggestionById = (id) => {
  return apiClient.get(`/v1/suggestions/${id}`)
}

// 创建健康建议
export const createSuggestion = (data) => {
  return apiClient.post('/v1/suggestions', data)
}

// 标记建议为已读
export const markSuggestionRead = (id) => {
  return apiClient.put(`/v1/suggestions/${id}/read`)
}

// 全部标记为已读
export const markAllSuggestionsRead = () => {
  return apiClient.put('/v1/suggestions/read-all')
}

// 删除建议
export const deleteSuggestion = (id) => {
  return apiClient.delete(`/v1/suggestions/${id}`)
}

