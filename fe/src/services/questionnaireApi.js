import apiClient from './api'

// 获取问卷列表
export const getQuestionnaires = (type) => {
  return apiClient.get('/v1/questionnaires', { params: type ? { type } : {} })
}

// 获取问卷详情
export const getQuestionnaireById = (id) => {
  return apiClient.get(`/v1/questionnaires/${id}`)
}

// 提交问卷回答
export const submitQuestionnaire = (data) => {
  return apiClient.post('/v1/questionnaires/submit', data)
}

// 获取我的回答历史
export const getMyResponses = (pageNum = 1, pageSize = 10) => {
  return apiClient.get('/v1/questionnaires/my-responses', { params: { pageNum, pageSize } })
}

// 获取某次回答详情
export const getResponseById = (id) => {
  return apiClient.get(`/v1/questionnaires/my-responses/${id}`)
}

// 获取对某问卷的最新回答
export const getLatestResponse = (questionnaireId) => {
  return apiClient.get(`/v1/questionnaires/${questionnaireId}/my-latest`)
}

// 我已完成问卷数量
export const getMyCount = () => {
  return apiClient.get('/v1/questionnaires/my-count')
}

// 管理员接口
export const adminGetAll = () => {
  return apiClient.get('/v1/questionnaires/admin/all')
}

export const adminCreate = (data) => {
  return apiClient.post('/v1/questionnaires/admin', data)
}

export const adminUpdate = (id, data) => {
  return apiClient.put(`/v1/questionnaires/admin/${id}`, data)
}

export const adminDelete = (id) => {
  return apiClient.delete(`/v1/questionnaires/admin/${id}`)
}

