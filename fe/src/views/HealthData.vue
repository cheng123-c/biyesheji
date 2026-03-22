<template>
  <div class="health-data-container">
    <div class="page-header">
      <h1>健康数据管理</h1>
      <button @click="showUploadModal = true" class="btn btn-primary">
        + 上传数据
      </button>
    </div>

    <!-- 提示信息 -->
    <div v-if="error" class="alert alert-error">
      {{ error }}
      <button @click="error = null" class="close">&times;</button>
    </div>
    <div v-if="success" class="alert alert-success">
      {{ success }}
      <button @click="success = null" class="close">&times;</button>
    </div>

    <!-- 最新数据卡片区 -->
    <section class="latest-data-section">
      <h2>最新健康指标</h2>
      <div v-if="latestLoading" class="loading-spinner">加载中...</div>
      <div v-else-if="latestData.length === 0" class="empty-state">
        <p>暂无健康数据，请上传您的第一条健康数据</p>
      </div>
      <div v-else class="data-cards">
        <div
          v-for="item in latestData"
          :key="item.id"
          class="data-card"
          :class="getDataTypeClass(item.dataType)"
        >
          <div class="card-icon">{{ getDataTypeIcon(item.dataType) }}</div>
          <div class="card-info">
            <div class="card-title">{{ getDataTypeName(item.dataType) }}</div>
            <div class="card-value">
              {{ item.dataValue }}
              <span class="card-unit">{{ item.unit }}</span>
            </div>
            <div class="card-status" :class="getValueStatus(item)">
              {{ getValueStatusText(item) }}
            </div>
            <div class="card-time">{{ formatTime(item.collectedAt) }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 趋势图区域 -->
    <section class="trend-section">
      <div class="section-header">
        <h2>数据趋势</h2>
        <div class="trend-controls">
          <select v-model="selectedType" @change="loadTrendData" class="form-select">
            <option value="heart_rate">心率</option>
            <option value="blood_pressure_systolic">收缩压</option>
            <option value="blood_pressure_diastolic">舒张压</option>
            <option value="blood_glucose">血糖</option>
            <option value="blood_oxygen">血氧</option>
            <option value="body_temperature">体温</option>
            <option value="weight">体重</option>
          </select>
          <select v-model="selectedDays" @change="loadTrendData" class="form-select">
            <option :value="7">近7天</option>
            <option :value="14">近14天</option>
            <option :value="30">近30天</option>
          </select>
        </div>
      </div>

      <div v-if="trendLoading" class="loading-spinner">加载趋势数据...</div>
      <div v-else-if="trendData.length === 0" class="empty-state">
        <p>暂无 {{ getDataTypeName(selectedType) }} 的历史数据</p>
      </div>
      <div v-else class="trend-chart-container">
        <div class="simple-chart">
          <div class="chart-y-axis">
            <span>{{ trendMax }}</span>
            <span>{{ Math.round((trendMax + trendMin) / 2) }}</span>
            <span>{{ trendMin }}</span>
          </div>
          <div class="chart-bars">
            <div
              v-for="(item, index) in trendData.slice(-14)"
              :key="index"
              class="chart-bar-wrapper"
            >
              <div
                class="chart-bar"
                :style="{ height: getBarHeight(item.dataValue) + '%' }"
                :title="`${item.dataValue} ${getUnit(selectedType)} - ${formatDate(item.collectedAt)}`"
              ></div>
              <span class="chart-date">{{ formatShortDate(item.collectedAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据列表 -->
    <section class="data-list-section">
      <div class="section-header">
        <h2>历史记录</h2>
        <div class="filter-controls">
          <input
            v-model="searchType"
            type="text"
            placeholder="筛选类型..."
            class="form-input"
          />
        </div>
      </div>

      <div v-if="listLoading" class="loading-spinner">加载中...</div>
      <div v-else-if="filteredList.length === 0" class="empty-state">
        <p>暂无历史记录</p>
      </div>
      <div v-else class="data-table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>类型</th>
              <th>数值</th>
              <th>单位</th>
              <th>来源</th>
              <th>采集时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredList" :key="item.id">
              <td>
                <span class="type-badge">{{ getDataTypeName(item.dataType) }}</span>
              </td>
              <td class="value-cell">{{ item.dataValue }}</td>
              <td>{{ item.unit || '-' }}</td>
              <td>{{ item.dataSource || 'manual' }}</td>
              <td>{{ formatTime(item.collectedAt) }}</td>
              <td>
                <button @click="confirmDelete(item.id)" class="btn-icon btn-delete">
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 分页 -->
        <div class="pagination">
          <button
            :disabled="currentPage <= 1"
            @click="changePage(currentPage - 1)"
            class="btn btn-page"
          >上一页</button>
          <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
          <button
            :disabled="currentPage >= totalPages"
            @click="changePage(currentPage + 1)"
            class="btn btn-page"
          >下一页</button>
        </div>
      </div>
    </section>

    <!-- 上传数据 Modal -->
    <div v-if="showUploadModal" class="modal-overlay" @click.self="showUploadModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>上传健康数据</h3>
          <button @click="showUploadModal = false" class="close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>数据类型 *</label>
            <select v-model="uploadForm.dataType" class="form-select">
              <option value="">请选择数据类型</option>
              <option value="heart_rate">心率 (bpm)</option>
              <option value="blood_pressure_systolic">收缩压 (mmHg)</option>
              <option value="blood_pressure_diastolic">舒张压 (mmHg)</option>
              <option value="blood_glucose">血糖 (mg/dL)</option>
              <option value="blood_oxygen">血氧 (%)</option>
              <option value="body_temperature">体温 (°C)</option>
              <option value="weight">体重 (kg)</option>
              <option value="height">身高 (cm)</option>
            </select>
          </div>
          <div class="form-group">
            <label>数值 *</label>
            <input
              v-model.number="uploadForm.dataValue"
              type="number"
              step="0.01"
              placeholder="请输入数值"
              class="form-input"
            />
          </div>
          <div class="form-group">
            <label>单位</label>
            <input
              v-model="uploadForm.unit"
              type="text"
              placeholder="自动填充"
              class="form-input"
              :value="getUnit(uploadForm.dataType)"
              disabled
            />
          </div>
          <div class="form-group">
            <label>数据来源</label>
            <select v-model="uploadForm.dataSource" class="form-select">
              <option value="manual">手动输入</option>
              <option value="device">设备采集</option>
              <option value="import">导入</option>
            </select>
          </div>
          <div class="form-group">
            <label>采集时间</label>
            <input
              v-model="uploadForm.collectedAt"
              type="datetime-local"
              class="form-input"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="handleUpload" :disabled="uploading" class="btn btn-primary">
            {{ uploading ? '上传中...' : '确认上传' }}
          </button>
          <button @click="showUploadModal = false" class="btn btn-cancel">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import {
  uploadHealthData,
  getLatestHealthData,
  getHealthDataList,
  getHealthDataTrend,
  deleteHealthData
} from '@/services/healthDataApi'

// 状态
const error = ref(null)
const success = ref(null)
const latestLoading = ref(true)
const trendLoading = ref(false)
const listLoading = ref(true)
const uploading = ref(false)
const showUploadModal = ref(false)

// 数据
const latestData = ref([])
const trendData = ref([])
const dataList = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 20

// 趋势图控制
const selectedType = ref('heart_rate')
const selectedDays = ref(30)

// 搜索过滤
const searchType = ref('')

// 上传表单
const uploadForm = ref({
  dataType: '',
  dataValue: null,
  dataSource: 'manual',
  collectedAt: ''
})

// 计算属性
const filteredList = computed(() => {
  if (!searchType.value) return dataList.value
  return dataList.value.filter(item =>
    getDataTypeName(item.dataType).includes(searchType.value) ||
    item.dataType.includes(searchType.value)
  )
})

const trendMax = computed(() => {
  if (!trendData.value.length) return 100
  return Math.ceil(Math.max(...trendData.value.map(d => Number(d.dataValue))))
})

const trendMin = computed(() => {
  if (!trendData.value.length) return 0
  return Math.floor(Math.min(...trendData.value.map(d => Number(d.dataValue))))
})

// 页面加载
onMounted(async () => {
  await Promise.all([
    loadLatestData(),
    loadDataList(),
    loadTrendData()
  ])
})

// 加载最新数据
const loadLatestData = async () => {
  latestLoading.value = true
  try {
    const response = await getLatestHealthData()
    latestData.value = response.data || []
  } catch (err) {
    console.error('加载最新数据失败:', err)
  } finally {
    latestLoading.value = false
  }
}

// 加载数据列表
const loadDataList = async (page = 1) => {
  listLoading.value = true
  try {
    const response = await getHealthDataList({ pageNum: page, pageSize })
    dataList.value = response.data?.list || []
    totalPages.value = response.data?.pages || 1
    currentPage.value = page
  } catch (err) {
    console.error('加载数据列表失败:', err)
  } finally {
    listLoading.value = false
  }
}

// 加载趋势数据
// 将 Date 转换为后端 LocalDateTime 接受的格式 yyyy-MM-ddTHH:mm:ss
const toLocalDateTimeStr = (date) => {
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const loadTrendData = async () => {
  trendLoading.value = true
  try {
    const endTime = toLocalDateTimeStr(new Date())
    const startTime = toLocalDateTimeStr(new Date(Date.now() - selectedDays.value * 24 * 60 * 60 * 1000))
    const response = await getHealthDataTrend(selectedType.value, startTime, endTime)
    trendData.value = (response.data || []).reverse()
  } catch (err) {
    console.error('加载趋势数据失败:', err)
    trendData.value = []
  } finally {
    trendLoading.value = false
  }
}

// 将 datetime-local 格式 "2024-03-22T12:00:00" 转为后端接受的 "2024-03-22 12:00:00"
const formatForBackend = (datetimeLocalStr) => {
  if (!datetimeLocalStr) return null
  // datetime-local 格式为 "2024-03-22T12:00:00"，替换 T 为空格
  return datetimeLocalStr.replace('T', ' ')
}

// 上传健康数据
const handleUpload = async () => {
  if (!uploadForm.value.dataType) {
    error.value = '请选择数据类型'
    return
  }
  if (uploadForm.value.dataValue === null || uploadForm.value.dataValue === undefined || uploadForm.value.dataValue === '') {
    error.value = '请输入数值'
    return
  }

  uploading.value = true
  error.value = null

  try {
    const payload = {
      dataType: uploadForm.value.dataType,
      dataValue: uploadForm.value.dataValue,
      unit: getUnit(uploadForm.value.dataType),
      dataSource: uploadForm.value.dataSource,
      collectedAt: formatForBackend(uploadForm.value.collectedAt)
    }

    await uploadHealthData(payload)
    success.value = '数据上传成功！'
    showUploadModal.value = false
    uploadForm.value = { dataType: '', dataValue: null, dataSource: 'manual', collectedAt: '' }

    // 刷新数据
    await Promise.all([loadLatestData(), loadDataList(), loadTrendData()])
  } catch (err) {
    error.value = err.response?.data?.message || '上传失败，请稍后重试'
  } finally {
    uploading.value = false
  }
}

// 确认删除
const confirmDelete = async (id) => {
  if (confirm('确定要删除这条健康数据吗？')) {
    try {
      await deleteHealthData(id)
      success.value = '删除成功'
      await Promise.all([loadLatestData(), loadDataList(currentPage.value)])
    } catch (err) {
      error.value = '删除失败'
    }
  }
}

// 分页
const changePage = async (page) => {
  await loadDataList(page)
}

// 工具函数
const getDataTypeName = (type) => {
  const names = {
    heart_rate: '心率',
    blood_pressure_systolic: '收缩压',
    blood_pressure_diastolic: '舒张压',
    blood_glucose: '血糖',
    blood_oxygen: '血氧',
    body_temperature: '体温',
    weight: '体重',
    height: '身高'
  }
  return names[type] || type
}

const getDataTypeIcon = (type) => {
  const icons = {
    heart_rate: '❤️',
    blood_pressure_systolic: '🩺',
    blood_pressure_diastolic: '🩺',
    blood_glucose: '🩸',
    blood_oxygen: '💨',
    body_temperature: '🌡️',
    weight: '⚖️',
    height: '📏'
  }
  return icons[type] || '📊'
}

const getDataTypeClass = (type) => {
  const classes = {
    heart_rate: 'card-heart',
    blood_pressure_systolic: 'card-bp',
    blood_pressure_diastolic: 'card-bp',
    blood_glucose: 'card-glucose',
    blood_oxygen: 'card-oxygen',
    body_temperature: 'card-temp',
    weight: 'card-weight'
  }
  return classes[type] || ''
}

const getUnit = (type) => {
  const units = {
    heart_rate: 'bpm',
    blood_pressure_systolic: 'mmHg',
    blood_pressure_diastolic: 'mmHg',
    blood_glucose: 'mg/dL',
    blood_oxygen: '%',
    body_temperature: '°C',
    weight: 'kg',
    height: 'cm'
  }
  return units[type] || ''
}

const normalRanges = {
  heart_rate: { min: 60, max: 100 },
  blood_pressure_systolic: { min: 90, max: 120 },
  blood_pressure_diastolic: { min: 60, max: 80 },
  blood_glucose: { min: 70, max: 100 },
  blood_oxygen: { min: 95, max: 100 },
  body_temperature: { min: 36.0, max: 37.5 },
}

const getValueStatus = (item) => {
  const range = normalRanges[item.dataType]
  if (!range) return 'status-normal'
  const val = Number(item.dataValue)
  if (val < range.min || val > range.max) return 'status-abnormal'
  return 'status-normal'
}

const getValueStatusText = (item) => {
  const range = normalRanges[item.dataType]
  if (!range) return '参考范围未知'
  const val = Number(item.dataValue)
  if (val < range.min) return '偏低'
  if (val > range.max) return '偏高'
  return '正常'
}

const getBarHeight = (value) => {
  const range = trendMax.value - trendMin.value
  if (range === 0) return 50
  return Math.max(5, ((Number(value) - trendMin.value) / range) * 90)
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const formatDate = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN')
}

const formatShortDate = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<style scoped>
.health-data-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

section h2 {
  font-size: 20px;
  color: #333;
  margin: 0 0 20px 0;
}

/* 最新数据卡片 */
.data-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.data-card {
  background: #f8f9ff;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  border: 1px solid #e8ecff;
  transition: transform 0.2s, box-shadow 0.2s;
}

.data-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.card-heart { border-left: 4px solid #ff6b6b; }
.card-bp { border-left: 4px solid #4ecdc4; }
.card-glucose { border-left: 4px solid #ffe66d; }
.card-oxygen { border-left: 4px solid #a29bfe; }
.card-temp { border-left: 4px solid #fd79a8; }
.card-weight { border-left: 4px solid #6c5ce7; }

.card-icon { font-size: 28px; }

.card-title {
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.card-unit {
  font-size: 12px;
  color: #888;
  font-weight: normal;
}

.card-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  margin-top: 4px;
  display: inline-block;
}

.status-normal { background: #d4edda; color: #155724; }
.status-abnormal { background: #f8d7da; color: #721c24; }

.card-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}

/* 趋势图 */
.trend-controls {
  display: flex;
  gap: 12px;
}

.simple-chart {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 200px;
  padding: 10px 0;
}

.chart-y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  text-align: right;
  font-size: 12px;
  color: #888;
  min-width: 50px;
  padding-right: 8px;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  flex: 1;
  height: 100%;
}

.chart-bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  height: 100%;
  justify-content: flex-end;
}

.chart-bar {
  width: 100%;
  background: linear-gradient(to top, #667eea, #764ba2);
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
  cursor: pointer;
  min-height: 4px;
}

.chart-bar:hover {
  opacity: 0.8;
}

.chart-date {
  font-size: 10px;
  color: #999;
  margin-top: 4px;
  white-space: nowrap;
}

/* 数据列表 */
.data-table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  text-align: left;
  padding: 12px 16px;
  background: #f8f9fa;
  color: #666;
  font-size: 13px;
  border-bottom: 2px solid #eee;
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  color: #333;
}

.data-table tr:hover td {
  background: #fafafa;
}

.type-badge {
  background: #e8f4f8;
  color: #2980b9;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.value-cell {
  font-weight: 600;
  color: #667eea;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}

.page-info {
  color: #666;
  font-size: 14px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
}

/* 通用表单 */
.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #555;
  font-weight: 500;
}

.form-input, .form-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus, .form-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102,126,234,0.1);
}

/* 按钮 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  font-size: 14px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102,126,234,0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancel {
  background: #f0f0f0;
  color: #555;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-page {
  background: #f8f9fa;
  color: #333;
  padding: 8px 16px;
}

.btn-page:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-icon {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-delete {
  background: #fee;
  color: #c33;
}

.btn-delete:hover {
  background: #fcc;
}

/* 加载/空状态 */
.loading-spinner {
  text-align: center;
  padding: 40px;
  color: #888;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #aaa;
}

/* 提示 */
.alert {
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-error { background: #fee; color: #c33; border: 1px solid #fcc; }
.alert-success { background: #efe; color: #393; border: 1px solid #cfc; }

.close {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  color: inherit;
  padding: 0;
  margin-left: 10px;
}

/* 响应式 */
@media (max-width: 768px) {
  .health-data-container {
    padding: 12px;
  }

  .data-cards {
    grid-template-columns: repeat(2, 1fr);
  }

  .trend-controls {
    flex-direction: column;
    gap: 8px;
  }
}
</style>

