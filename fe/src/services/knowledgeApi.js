import apiClient from './api'

// 获取症状列表
export const getSymptoms = () => {
  return apiClient.get('/v1/knowledge/symptoms')
}

// 获取疾病列表
export const getDiseases = () => {
  return apiClient.get('/v1/knowledge/diseases')
}

// 搜索医学概念
export const searchConcepts = (keyword) => {
  return apiClient.get('/v1/knowledge/concepts/search', { params: { keyword } })
}

// 获取概念的知识图谱关系
export const getConceptRelations = (id) => {
  return apiClient.get(`/v1/knowledge/concepts/${id}/relations`)
}

// 症状推理分析
export const inferFromSymptoms = (symptoms) => {
  return apiClient.post('/v1/knowledge/infer', { symptoms })
}

// 获取历史推理记录
export const getInferHistory = (limit = 10) => {
  return apiClient.get('/v1/knowledge/infer/history', { params: { limit } })
}

// 管理员接口
export const adminGetAllConcepts = () => {
  return apiClient.get('/v1/knowledge/admin/concepts')
}

export const adminCreateConcept = (data) => {
  return apiClient.post('/v1/knowledge/admin/concepts', data)
}

export const adminUpdateConcept = (id, data) => {
  return apiClient.put(`/v1/knowledge/admin/concepts/${id}`, data)
}

export const adminDeleteConcept = (id) => {
  return apiClient.delete(`/v1/knowledge/admin/concepts/${id}`)
}

export const adminCreateRelation = (data) => {
  return apiClient.post('/v1/knowledge/admin/relations', data)
}

export const adminDeleteRelation = (id) => {
  return apiClient.delete(`/v1/knowledge/admin/relations/${id}`)
}

