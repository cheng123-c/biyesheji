import apiClient from './api'

/**
 * 干预方案相关 API
 */

// 获取干预方案列表（分页）
export const getInterventions = (pageNum = 1, pageSize = 10, status) => {
  return apiClient.get('/v1/interventions', {
    params: { pageNum, pageSize, status }
  })
}

// 获取进行中的干预方案
export const getActiveInterventions = () => {
  return apiClient.get('/v1/interventions/active')
}

// 获取进行中方案数量
export const getActiveInterventionCount = () => {
  return apiClient.get('/v1/interventions/active/count')
}

// 根据ID获取干预方案
export const getInterventionById = (id) => {
  return apiClient.get(`/v1/interventions/${id}`)
}

// 创建干预方案
export const createIntervention = (data) => {
  return apiClient.post('/v1/interventions', data)
}

// 更新干预方案
export const updateIntervention = (id, data) => {
  return apiClient.put(`/v1/interventions/${id}`, data)
}

// 更新干预方案状态
export const updateInterventionStatus = (id, status) => {
  return apiClient.patch(`/v1/interventions/${id}/status`, { status })
}

// 删除干预方案
export const deleteIntervention = (id) => {
  return apiClient.delete(`/v1/interventions/${id}`)
}

