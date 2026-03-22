import apiClient from './api'

/**
 * 健康数据相关的 API 调用
 */

// 上传单条健康数据
export const uploadHealthData = (data) => {
  return apiClient.post('/v1/health-data', data)
}

// 批量上传健康数据
export const batchUploadHealthData = (items) => {
  return apiClient.post('/v1/health-data/batch', { items })
}

// 获取各类型最新健康数据
export const getLatestHealthData = () => {
  return apiClient.get('/v1/health-data/latest')
}

// 分页获取健康数据列表
export const getHealthDataList = (params = {}) => {
  return apiClient.get('/v1/health-data', { params })
}

// 获取单条健康数据
export const getHealthDataById = (id) => {
  return apiClient.get(`/v1/health-data/${id}`)
}

// 获取健康数据趋势（指定类型+时间段）
export const getHealthDataTrend = (dataType, startTime, endTime) => {
  return apiClient.get('/v1/health-data/trend', {
    params: { dataType, startTime, endTime }
  })
}

// 获取健康数据统计
export const getHealthDataStatistics = () => {
  return apiClient.get('/v1/health-data/statistics')
}

// 更新健康数据
export const updateHealthData = (id, data) => {
  return apiClient.put(`/v1/health-data/${id}`, data)
}

// 删除健康数据
export const deleteHealthData = (id) => {
  return apiClient.delete(`/v1/health-data/${id}`)
}

