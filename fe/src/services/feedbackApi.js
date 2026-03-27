import apiClient from './api'

// 提交反馈
export const submitFeedback = (data) => {
  return apiClient.post('/v1/feedback', data)
}

// 获取我的反馈
export const getMyFeedbacks = () => {
  return apiClient.get('/v1/feedback/my')
}

// 获取反馈详情
export const getFeedbackById = (id) => {
  return apiClient.get(`/v1/feedback/my/${id}`)
}

// 管理员接口
export const adminGetAllFeedbacks = (pageNum = 1, pageSize = 15, status = null) => {
  return apiClient.get('/v1/feedback/admin/all', { params: { pageNum, pageSize, status } })
}

export const adminReplyFeedback = (id, reply, status = 'RESOLVED') => {
  return apiClient.post(`/v1/feedback/admin/${id}/reply`, { reply, status })
}

export const adminUpdateFeedbackStatus = (id, status) => {
  return apiClient.patch(`/v1/feedback/admin/${id}/status`, { status })
}

export const adminGetPendingCount = () => {
  return apiClient.get('/v1/feedback/admin/pending-count')
}

