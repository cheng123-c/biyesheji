import apiClient from './api'

/**
 * 医疗记录相关 API
 */

// 获取医疗记录列表（分页）
export const getMedicalRecords = (pageNum = 1, pageSize = 10) => {
  return apiClient.get('/v1/medical-records', {
    params: { pageNum, pageSize }
  })
}

// 根据ID获取医疗记录
export const getMedicalRecordById = (id) => {
  return apiClient.get(`/v1/medical-records/${id}`)
}

// 创建医疗记录
export const createMedicalRecord = (data) => {
  return apiClient.post('/v1/medical-records', data)
}

// 更新医疗记录
export const updateMedicalRecord = (id, data) => {
  return apiClient.put(`/v1/medical-records/${id}`, data)
}

// 删除医疗记录
export const deleteMedicalRecord = (id) => {
  return apiClient.delete(`/v1/medical-records/${id}`)
}

