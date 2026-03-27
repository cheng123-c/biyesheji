<template>
  <div class="medical-records-container">
    <div class="page-header">
      <h1>🏥 医疗记录</h1>
      <button @click="openCreateModal" class="btn btn-primary">+ 添加记录</button>
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

    <!-- 记录列表 -->
    <section class="records-section">
      <div v-if="loading" class="loading-spinner">加载中...</div>
      <div v-else-if="records.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无医疗记录</p>
        <p class="empty-sub">记录您的诊断结果、用药信息、手术记录等医疗档案</p>
        <button @click="openCreateModal" class="btn btn-primary">添加第一条记录</button>
      </div>
      <div v-else>
        <div class="records-list">
          <div
            v-for="record in records"
            :key="record.id"
            class="record-card"
            :class="getRecordTypeClass(record.recordType)"
          >
            <div class="record-header">
              <div class="record-type-badge">
                <span class="type-icon">{{ getRecordTypeIcon(record.recordType) }}</span>
                <span class="type-name">{{ getRecordTypeName(record.recordType) }}</span>
              </div>
              <div class="record-date">{{ formatDate(record.recordDate) }}</div>
            </div>
            <div class="record-title">{{ record.recordTitle }}</div>
            <div class="record-meta">
              <span v-if="record.hospital" class="meta-item">
                🏨 {{ record.hospital }}
              </span>
              <span v-if="record.doctorName" class="meta-item">
                👨‍⚕️ {{ record.doctorName }}
              </span>
            </div>
            <div v-if="record.recordContent" class="record-content">
              {{ record.recordContent.length > 120 ? record.recordContent.substring(0, 120) + '...' : record.recordContent }}
            </div>
            <div class="record-actions">
              <button @click="openDetailModal(record)" class="btn-text btn-view">查看详情</button>
              <button @click="openEditModal(record)" class="btn-text btn-edit">编辑</button>
              <button @click="confirmDelete(record.id)" class="btn-text btn-delete">删除</button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <button :disabled="currentPage <= 1" @click="loadRecords(currentPage - 1)" class="btn btn-page">上一页</button>
          <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
          <button :disabled="currentPage >= totalPages" @click="loadRecords(currentPage + 1)" class="btn btn-page">下一页</button>
        </div>
      </div>
    </section>

    <!-- 创建/编辑弹窗 -->
    <div v-if="showFormModal" class="modal-overlay" @click.self="closeFormModal">
      <div class="modal modal-large">
        <div class="modal-header">
          <h3>{{ editingRecord ? '编辑医疗记录' : '添加医疗记录' }}</h3>
          <button @click="closeFormModal" class="close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>记录类型 *</label>
              <select v-model="form.recordType" class="form-select">
                <option value="">请选择类型</option>
                <option value="DIAGNOSIS">诊断结果</option>
                <option value="MEDICATION">用药记录</option>
                <option value="SURGERY">手术记录</option>
                <option value="EXAMINATION">检查报告</option>
                <option value="OTHER">其他</option>
              </select>
            </div>
            <div class="form-group">
              <label>记录日期 *</label>
              <input v-model="form.recordDate" type="date" class="form-input" />
            </div>
          </div>
          <div class="form-group">
            <label>记录标题 *</label>
            <input v-model="form.recordTitle" type="text" placeholder="请输入记录标题" class="form-input" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>医院名称</label>
              <input v-model="form.hospital" type="text" placeholder="就诊医院" class="form-input" />
            </div>
            <div class="form-group">
              <label>医生姓名</label>
              <input v-model="form.doctorName" type="text" placeholder="主治医生" class="form-input" />
            </div>
          </div>
          <div class="form-group">
            <label>记录内容</label>
            <textarea
              v-model="form.recordContent"
              rows="5"
              placeholder="详细描述诊断、用药、检查等内容..."
              class="form-textarea"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="handleSubmit" :disabled="submitting" class="btn btn-primary">
            {{ submitting ? '保存中...' : (editingRecord ? '保存修改' : '添加记录') }}
          </button>
          <button @click="closeFormModal" class="btn btn-cancel">取消</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal && detailRecord" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal modal-large">
        <div class="modal-header">
          <div class="detail-header-info">
            <span class="type-icon-lg">{{ getRecordTypeIcon(detailRecord.recordType) }}</span>
            <h3>{{ detailRecord.recordTitle }}</h3>
          </div>
          <button @click="showDetailModal = false" class="close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="detail-meta">
            <div class="meta-row">
              <span class="meta-label">类型</span>
              <span class="meta-value type-badge">{{ getRecordTypeName(detailRecord.recordType) }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">日期</span>
              <span class="meta-value">{{ formatDate(detailRecord.recordDate) }}</span>
            </div>
            <div v-if="detailRecord.hospital" class="meta-row">
              <span class="meta-label">医院</span>
              <span class="meta-value">{{ detailRecord.hospital }}</span>
            </div>
            <div v-if="detailRecord.doctorName" class="meta-row">
              <span class="meta-label">医生</span>
              <span class="meta-value">{{ detailRecord.doctorName }}</span>
            </div>
          </div>
          <div v-if="detailRecord.recordContent" class="detail-content">
            <h4>记录内容</h4>
            <p>{{ detailRecord.recordContent }}</p>
          </div>
          <div class="detail-footer-info">
            <span class="created-time">创建时间：{{ formatDateTime(detailRecord.createdAt) }}</span>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="openEditModal(detailRecord)" class="btn btn-primary">编辑</button>
          <button @click="showDetailModal = false" class="btn btn-cancel">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getMedicalRecords,
  createMedicalRecord,
  updateMedicalRecord,
  deleteMedicalRecord
} from '@/services/medicalRecordApi'

const loading = ref(true)
const submitting = ref(false)
const error = ref(null)
const success = ref(null)

const records = ref([])
const currentPage = ref(1)
const totalPages = ref(1)

const showFormModal = ref(false)
const showDetailModal = ref(false)
const editingRecord = ref(null)
const detailRecord = ref(null)

const form = ref({
  recordType: '',
  recordTitle: '',
  recordDate: '',
  hospital: '',
  doctorName: '',
  recordContent: ''
})

onMounted(() => {
  loadRecords()
})

const loadRecords = async (page = 1) => {
  loading.value = true
  try {
    const response = await getMedicalRecords(page, 10)
    records.value = response.data?.list || []
    totalPages.value = response.data?.pages || 1
    currentPage.value = page
  } catch (err) {
    error.value = '加载医疗记录失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  editingRecord.value = null
  form.value = {
    recordType: '',
    recordTitle: '',
    recordDate: new Date().toLocaleDateString('sv'),
    hospital: '',
    doctorName: '',
    recordContent: ''
  }
  showFormModal.value = true
}

const openEditModal = (record) => {
  editingRecord.value = record
  form.value = {
    recordType: record.recordType || '',
    recordTitle: record.recordTitle || '',
    recordDate: record.recordDate || '',
    hospital: record.hospital || '',
    doctorName: record.doctorName || '',
    recordContent: record.recordContent || ''
  }
  showDetailModal.value = false
  showFormModal.value = true
}

const openDetailModal = (record) => {
  detailRecord.value = record
  showDetailModal.value = true
}

const closeFormModal = () => {
  showFormModal.value = false
  editingRecord.value = null
}

const handleSubmit = async () => {
  if (!form.value.recordType) {
    error.value = '请选择记录类型'
    return
  }
  if (!form.value.recordTitle) {
    error.value = '请输入记录标题'
    return
  }
  if (!form.value.recordDate) {
    error.value = '请选择记录日期'
    return
  }

  submitting.value = true
  error.value = null

  try {
    if (editingRecord.value) {
      await updateMedicalRecord(editingRecord.value.id, form.value)
      success.value = '记录更新成功'
    } else {
      await createMedicalRecord(form.value)
      success.value = '记录添加成功'
    }
    closeFormModal()
    await loadRecords(currentPage.value)
  } catch (err) {
    error.value = err.response?.data?.message || '操作失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

const confirmDelete = async (id) => {
  if (confirm('确定要删除这条医疗记录吗？此操作不可撤销。')) {
    try {
      await deleteMedicalRecord(id)
      success.value = '删除成功'
      // 若当前页删除后已无数据且不是第一页，跳回上一页
      const targetPage = records.value.length === 1 && currentPage.value > 1
        ? currentPage.value - 1
        : currentPage.value
      await loadRecords(targetPage)
    } catch (err) {
      error.value = '删除失败，请稍后重试'
    }
  }
}

// 工具函数
const getRecordTypeName = (type) => {
  const names = {
    DIAGNOSIS: '诊断结果',
    MEDICATION: '用药记录',
    SURGERY: '手术记录',
    EXAMINATION: '检查报告',
    OTHER: '其他'
  }
  return names[type] || type || '未知'
}

const getRecordTypeIcon = (type) => {
  const icons = {
    DIAGNOSIS: '🩺',
    MEDICATION: '💊',
    SURGERY: '🔬',
    EXAMINATION: '📋',
    OTHER: '📄'
  }
  return icons[type] || '📄'
}

const getRecordTypeClass = (type) => {
  const classes = {
    DIAGNOSIS: 'type-diagnosis',
    MEDICATION: 'type-medication',
    SURGERY: 'type-surgery',
    EXAMINATION: 'type-examination',
    OTHER: 'type-other'
  }
  return classes[type] || 'type-other'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  return new Date(datetime).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.medical-records-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0;
}

section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

/* 记录卡片 */
.records-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.record-card {
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 20px;
  border-left: 4px solid #ccc;
  transition: box-shadow 0.2s;
}

.record-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.type-diagnosis { border-left-color: #e74c3c; }
.type-medication { border-left-color: #3498db; }
.type-surgery { border-left-color: #e67e22; }
.type-examination { border-left-color: #27ae60; }
.type-other { border-left-color: #95a5a6; }

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.record-type-badge {
  display: flex;
  align-items: center;
  gap: 6px;
}

.type-icon { font-size: 18px; }

.type-name {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.record-date {
  color: #888;
  font-size: 13px;
}

.record-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.record-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
}

.meta-item {
  font-size: 13px;
  color: #666;
}

.record-content {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  background: #fafafa;
  padding: 10px 14px;
  border-radius: 6px;
  margin-bottom: 12px;
}

.record-actions {
  display: flex;
  gap: 12px;
}

.btn-text {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.btn-view { color: #667eea; }
.btn-view:hover { background: #f0f0ff; }
.btn-edit { color: #27ae60; }
.btn-edit:hover { background: #f0fff0; }
.btn-delete { color: #e74c3c; }
.btn-delete:hover { background: #fff0f0; }

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #aaa;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #888;
}

.empty-sub {
  font-size: 14px !important;
  color: #bbb !important;
  margin-bottom: 24px !important;
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

/* 弹窗 */
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
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
}

.modal-large {
  max-width: 620px;
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

.detail-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.type-icon-lg { font-size: 24px; }

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #eee;
}

/* 表单 */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

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

.form-input, .form-select, .form-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-input:focus, .form-select:focus, .form-textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102,126,234,0.1);
}

.form-textarea {
  resize: vertical;
  font-family: inherit;
}

/* 详情 */
.detail-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-label {
  font-size: 13px;
  color: #888;
  min-width: 40px;
}

.meta-value {
  font-size: 14px;
  color: #333;
}

.meta-value.type-badge {
  background: #e8f4f8;
  color: #2980b9;
  padding: 2px 10px;
  border-radius: 10px;
}

.detail-content h4 {
  font-size: 15px;
  color: #555;
  margin: 0 0 10px 0;
  border-left: 3px solid #667eea;
  padding-left: 10px;
}

.detail-content p {
  font-size: 14px;
  color: #555;
  line-height: 1.7;
  background: #fafafa;
  padding: 12px 16px;
  border-radius: 6px;
  margin: 0;
  white-space: pre-wrap;
}

.detail-footer-info {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.created-time {
  font-size: 12px;
  color: #bbb;
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

/* 加载 */
.loading-spinner {
  text-align: center;
  padding: 40px;
  color: #888;
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
  .medical-records-container { padding: 12px; }
  .form-row { grid-template-columns: 1fr; }
}
</style>

