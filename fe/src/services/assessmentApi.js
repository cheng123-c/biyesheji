import apiClient from './api'

/**
 * 健康评测相关的 API 调用
 */

// 发起健康评测
export const startEvaluation = () => {
  return apiClient.post('/v1/assessments/evaluate')
}

// 获取最新评测报告
export const getLatestAssessment = () => {
  return apiClient.get('/v1/assessments/latest')
}

// 根据ID获取评测报告
export const getAssessmentById = (id) => {
  return apiClient.get(`/v1/assessments/${id}`)
}

// 获取评测报告列表（分页）
export const getAssessmentReports = (pageNum = 1, pageSize = 10) => {
  return apiClient.get('/v1/assessments/reports', {
    params: { pageNum, pageSize }
  })
}

// 获取今年评测次数
export const getAssessmentCount = () => {
  return apiClient.get('/v1/assessments/count')
}

// 导出指定评测报告为 PDF（返回 blob）
export const exportAssessmentPdf = (id) => {
  return apiClient.get(`/v1/assessments/${id}/export/pdf`, {
    responseType: 'blob'
  })
}

// 导出最新评测报告为 PDF（返回 blob）
export const exportLatestAssessmentPdf = () => {
  return apiClient.get('/v1/assessments/latest/export/pdf', {
    responseType: 'blob'
  })
}

/**
 * 通知相关 API
 */

// 获取通知列表（分页）
export const getNotifications = (pageNum = 1, pageSize = 20) => {
  return apiClient.get('/v1/notifications', {
    params: { pageNum, pageSize }
  })
}

// 获取未读通知
export const getUnreadNotifications = () => {
  return apiClient.get('/v1/notifications/unread')
}

// 获取未读通知数量
export const getUnreadCount = () => {
  return apiClient.get('/v1/notifications/unread/count')
}

// 标记通知为已读
export const markNotificationRead = (id) => {
  return apiClient.put(`/v1/notifications/${id}/read`)
}

// 全部标记为已读
export const markAllNotificationsRead = () => {
  return apiClient.put('/v1/notifications/read-all')
}

// 删除通知
export const deleteNotification = (id) => {
  return apiClient.delete(`/v1/notifications/${id}`)
}

