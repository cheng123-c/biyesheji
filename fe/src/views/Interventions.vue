<template>
  <div class="interventions-container">
    <div class="page-header">
      <div>
        <h1>🏃 干预方案</h1>
        <p class="page-subtitle">管理您的健康干预计划，包括饮食、运动、用药等</p>
      </div>
      <button @click="openCreateModal" class="btn btn-primary">+ 新建方案</button>
    </div>

    <!-- 提示 -->
    <div v-if="error" class="alert alert-error">
      {{ error }}
      <button @click="error = null" class="close">&times;</button>
    </div>
    <div v-if="success" class="alert alert-success">
      {{ success }}
      <button @click="success = null" class="close">&times;</button>
    </div>

    <!-- 进行中方案 -->
    <section v-if="activePlans.length > 0" class="active-section">
      <h2>📌 进行中的方案 ({{ activePlans.length }})</h2>
      <div class="active-grid">
        <div
          v-for="plan in activePlans"
          :key="plan.id"
          class="active-plan-card"
          :class="getPlanTypeClass(plan.planType)"
        >
          <div class="active-header">
            <span class="plan-type-icon">{{ getPlanTypeIcon(plan.planType) }}</span>
            <span class="plan-type-name">{{ getPlanTypeName(plan.planType) }}</span>
            <div class="plan-status-badge status-active">进行中</div>
          </div>
          <div class="active-title">
            {{ plan.targetDisease || '综合健康干预' }}
          </div>
          <div class="active-progress">
            <div class="progress-info">
              <span>开始: {{ formatDate(plan.startDate) }}</span>
              <span>结束: {{ formatDate(plan.endDate) || '无限期' }}</span>
            </div>
            <div v-if="plan.durationDays" class="duration-tag">
              {{ plan.durationDays }}天计划
            </div>
          </div>
          <div class="active-actions">
            <button @click="openDetailModal(plan)" class="btn-sm btn-info">详情</button>
            <button @click="handleStatusChange(plan.id, 'PAUSED')" class="btn-sm btn-warning">暂停</button>
            <button @click="handleStatusChange(plan.id, 'COMPLETED')" class="btn-sm btn-success">完成</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 全部方案 -->
    <section class="all-plans-section">
      <div class="section-header">
        <h2>全部方案</h2>
        <select v-model="statusFilter" @change="loadPlans(1)" class="form-select filter-select">
          <option value="">全部状态</option>
          <option value="ACTIVE">进行中</option>
          <option value="PAUSED">已暂停</option>
          <option value="COMPLETED">已完成</option>
          <option value="CANCELLED">已取消</option>
        </select>
      </div>

      <div v-if="loading" class="loading-spinner">加载中...</div>
      <div v-else-if="plans.length === 0" class="empty-state">
        <div class="empty-icon">🏃</div>
        <p>暂无干预方案</p>
        <p class="empty-sub">创建您的第一个健康干预计划，开始系统性的健康管理</p>
        <button @click="openCreateModal" class="btn btn-primary">新建方案</button>
      </div>
      <div v-else>
        <div class="plans-list">
          <div v-for="plan in plans" :key="plan.id" class="plan-card">
            <div class="plan-card-left">
              <div class="plan-icon-wrap" :class="getPlanTypeClass(plan.planType)">
                {{ getPlanTypeIcon(plan.planType) }}
              </div>
            </div>
            <div class="plan-card-body">
              <div class="plan-card-top">
                <div class="plan-name">{{ plan.targetDisease || getPlanTypeName(plan.planType) }}</div>
                <div class="plan-status-badge" :class="getStatusClass(plan.status)">
                  {{ getStatusLabel(plan.status) }}
                </div>
              </div>
              <div class="plan-type-row">
                <span class="plan-type-tag">{{ getPlanTypeName(plan.planType) }}</span>
                <span v-if="plan.durationDays" class="duration-tag-sm">{{ plan.durationDays }}天</span>
              </div>
              <div class="plan-dates">
                <span>{{ formatDate(plan.startDate) }}</span>
                <span v-if="plan.endDate"> → {{ formatDate(plan.endDate) }}</span>
              </div>
            </div>
            <div class="plan-card-actions">
              <button @click="openDetailModal(plan)" class="btn-icon-action">👁</button>
              <button @click="openEditModal(plan)" class="btn-icon-action">✏️</button>
              <button @click="confirmDelete(plan.id)" class="btn-icon-action">🗑</button>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <button :disabled="currentPage <= 1" @click="loadPlans(currentPage - 1)" class="btn btn-page">上一页</button>
          <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
          <button :disabled="currentPage >= totalPages" @click="loadPlans(currentPage + 1)" class="btn btn-page">下一页</button>
        </div>
      </div>
    </section>

    <!-- 创建/编辑弹窗 -->
    <div v-if="showFormModal" class="modal-overlay" @click.self="closeFormModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingPlan ? '编辑方案' : '新建干预方案' }}</h3>
          <button @click="closeFormModal" class="close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>方案类型 *</label>
            <select v-model="form.planType" class="form-select">
              <option value="">请选择类型</option>
              <option value="DIET">饮食干预</option>
              <option value="EXERCISE">运动锻炼</option>
              <option value="MEDICATION">用药计划</option>
              <option value="REHABILITATION">康复治疗</option>
              <option value="OTHER">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>目标（疾病/健康目标）</label>
            <input v-model="form.targetDisease" type="text" placeholder="如：降低血压、减重5kg等" class="form-input" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>开始日期</label>
              <input v-model="form.startDate" type="date" class="form-input" />
            </div>
            <div class="form-group">
              <label>持续天数</label>
              <input v-model.number="form.durationDays" type="number" min="1" placeholder="天数" class="form-input" />
            </div>
          </div>
          <div class="form-group">
            <label>方案详情</label>
            <textarea
              v-model="form.planDetail"
              rows="4"
              placeholder="描述具体的执行计划、注意事项等..."
              class="form-textarea"
            ></textarea>
          </div>
          <div class="form-group" v-if="editingPlan">
            <label>执行状态</label>
            <select v-model="form.status" class="form-select">
              <option value="ACTIVE">进行中</option>
              <option value="PAUSED">已暂停</option>
              <option value="COMPLETED">已完成</option>
              <option value="CANCELLED">已取消</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="handleSubmit" :disabled="submitting" class="btn btn-primary">
            {{ submitting ? '保存中...' : (editingPlan ? '保存修改' : '创建方案') }}
          </button>
          <button @click="closeFormModal" class="btn btn-cancel">取消</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal && detailPlan" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal modal-large">
        <div class="modal-header">
          <div class="detail-header">
            <span class="detail-icon">{{ getPlanTypeIcon(detailPlan.planType) }}</span>
            <div>
              <h3>{{ detailPlan.targetDisease || getPlanTypeName(detailPlan.planType) }}</h3>
              <span class="plan-status-badge" :class="getStatusClass(detailPlan.status)">
                {{ getStatusLabel(detailPlan.status) }}
              </span>
            </div>
          </div>
          <button @click="showDetailModal = false" class="close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="detail-meta">
            <div class="meta-item-row">
              <span class="meta-key">方案类型</span>
              <span class="meta-val">{{ getPlanTypeName(detailPlan.planType) }}</span>
            </div>
            <div class="meta-item-row">
              <span class="meta-key">开始日期</span>
              <span class="meta-val">{{ formatDate(detailPlan.startDate) || '未设置' }}</span>
            </div>
            <div class="meta-item-row">
              <span class="meta-key">结束日期</span>
              <span class="meta-val">{{ formatDate(detailPlan.endDate) || '无限期' }}</span>
            </div>
            <div v-if="detailPlan.durationDays" class="meta-item-row">
              <span class="meta-key">持续时间</span>
              <span class="meta-val">{{ detailPlan.durationDays }} 天</span>
            </div>
          </div>
          <div v-if="detailPlan.planDetail" class="detail-content">
            <h4>方案详情</h4>
            <p>{{ detailPlan.planDetail }}</p>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="openEditModal(detailPlan)" class="btn btn-primary">编辑</button>
          <button @click="showDetailModal = false" class="btn btn-cancel">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getInterventions,
  getActiveInterventions,
  createIntervention,
  updateIntervention,
  updateInterventionStatus,
  deleteIntervention
} from '@/services/interventionApi'

const loading = ref(true)
const submitting = ref(false)
const error = ref(null)
const success = ref(null)

const plans = ref([])
const activePlans = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const statusFilter = ref('')

const showFormModal = ref(false)
const showDetailModal = ref(false)
const editingPlan = ref(null)
const detailPlan = ref(null)

const form = ref({
  planType: '',
  targetDisease: '',
  planDetail: '',
  durationDays: null,
  startDate: '',
  status: 'ACTIVE'
})

onMounted(async () => {
  await Promise.all([loadPlans(), loadActivePlans()])
})

const loadPlans = async (page = 1) => {
  loading.value = true
  try {
    const response = await getInterventions(page, 10, statusFilter.value || undefined)
    plans.value = response.data?.list || []
    totalPages.value = response.data?.pages || 1
    currentPage.value = page
  } catch (err) {
    error.value = '加载干预方案失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadActivePlans = async () => {
  try {
    const response = await getActiveInterventions()
    activePlans.value = response.data || []
  } catch { /* ignore */ }
}

const openCreateModal = () => {
  editingPlan.value = null
  form.value = {
    planType: '',
    targetDisease: '',
    planDetail: '',
    durationDays: null,
    startDate: new Date().toLocaleDateString('sv'),
    status: 'ACTIVE'
  }
  showFormModal.value = true
}

const openEditModal = (plan) => {
  editingPlan.value = plan
  form.value = {
    planType: plan.planType || '',
    targetDisease: plan.targetDisease || '',
    planDetail: plan.planDetail || '',
    durationDays: plan.durationDays || null,
    startDate: plan.startDate || '',
    endDate: plan.endDate || '',
    status: plan.status || 'ACTIVE'
  }
  showDetailModal.value = false
  showFormModal.value = true
}

const openDetailModal = (plan) => {
  detailPlan.value = plan
  showDetailModal.value = true
}

const closeFormModal = () => {
  showFormModal.value = false
  editingPlan.value = null
}

const handleSubmit = async () => {
  if (!form.value.planType) {
    error.value = '请选择方案类型'
    return
  }

  submitting.value = true
  error.value = null

  try {
    const payload = { ...form.value }
    if (editingPlan.value) {
      await updateIntervention(editingPlan.value.id, payload)
      success.value = '方案更新成功'
    } else {
      await createIntervention(payload)
      success.value = '方案创建成功'
    }
    closeFormModal()
    await Promise.all([loadPlans(currentPage.value), loadActivePlans()])
  } catch (err) {
    error.value = err.response?.data?.message || '操作失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

const handleStatusChange = async (id, status) => {
  try {
    await updateInterventionStatus(id, status)
    success.value = `方案已${getStatusLabel(status)}`
    await Promise.all([loadPlans(currentPage.value), loadActivePlans()])
  } catch (err) {
    error.value = '状态更新失败'
  }
}

const confirmDelete = async (id) => {
  if (confirm('确定要删除这个干预方案吗？')) {
    try {
      await deleteIntervention(id)
      success.value = '删除成功'
      // 若当前页删除后已无数据且不是第一页，跳回上一页
      const targetPage = plans.value.length === 1 && currentPage.value > 1
        ? currentPage.value - 1
        : currentPage.value
      await Promise.all([loadPlans(targetPage), loadActivePlans()])
    } catch (err) {
      error.value = '删除失败'
    }
  }
}

const getPlanTypeName = (type) => {
  const names = { DIET: '饮食干预', EXERCISE: '运动锻炼', MEDICATION: '用药计划', REHABILITATION: '康复治疗', OTHER: '其他' }
  return names[type] || type || '—'
}

const getPlanTypeIcon = (type) => {
  const icons = { DIET: '🥗', EXERCISE: '🏃', MEDICATION: '💊', REHABILITATION: '🏥', OTHER: '📌' }
  return icons[type] || '📌'
}

const getPlanTypeClass = (type) => {
  const classes = { DIET: 'type-diet', EXERCISE: 'type-exercise', MEDICATION: 'type-medication', REHABILITATION: 'type-rehab' }
  return classes[type] || 'type-other'
}

const getStatusLabel = (status) => {
  const labels = { ACTIVE: '进行中', PAUSED: '已暂停', COMPLETED: '已完成', CANCELLED: '已取消' }
  return labels[status] || status
}

const getStatusClass = (status) => {
  const classes = { ACTIVE: 'status-active', PAUSED: 'status-paused', COMPLETED: 'status-completed', CANCELLED: 'status-cancelled' }
  return classes[status] || ''
}

const formatDate = (date) => {
  if (!date) return null
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}
</script>

<style scoped>
.interventions-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h1 { font-size: 28px; color: #333; margin: 0 0 6px 0; }
.page-subtitle { color: #888; font-size: 14px; margin: 0; }

section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

section h2 { font-size: 18px; color: #333; margin: 0 0 18px 0; }

/* 进行中方案 */
.active-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.active-plan-card {
  border: 2px solid #eee;
  border-radius: 12px;
  padding: 18px;
  transition: all 0.2s;
}

.active-plan-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.1); }

.type-diet { border-color: #27ae6040; background: #f0fff4; }
.type-exercise { border-color: #e67e2240; background: #fffbf0; }
.type-medication { border-color: #3498db40; background: #f0f8ff; }
.type-rehab { border-color: #9b59b640; background: #fdf5ff; }
.type-other { border-color: #95a5a640; background: #f9f9f9; }

.active-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.plan-type-icon { font-size: 24px; }

.plan-type-name {
  font-size: 13px;
  color: #666;
  flex: 1;
}

.active-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
}

.active-progress {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.progress-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 12px;
  color: #888;
}

.duration-tag {
  background: #667eea20;
  color: #667eea;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.active-actions {
  display: flex;
  gap: 8px;
}

/* 全部方案列表 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 { margin: 0; }

.filter-select {
  width: auto;
  padding: 6px 12px;
}

.plans-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.plan-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #eee;
  border-radius: 10px;
  gap: 16px;
  transition: box-shadow 0.2s;
}

.plan-card:hover { box-shadow: 0 3px 10px rgba(0,0,0,0.07); }

.plan-card-left {
  flex-shrink: 0;
}

.plan-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  background: #f0f0f0;
}

.type-diet .plan-icon-wrap { background: #f0fff4; }
.type-exercise .plan-icon-wrap { background: #fffbf0; }
.type-medication .plan-icon-wrap { background: #f0f8ff; }

.plan-card-body { flex: 1; min-width: 0; }

.plan-card-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.plan-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.plan-type-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.plan-type-tag {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 8px;
}

.duration-tag-sm {
  font-size: 12px;
  color: #888;
}

.plan-dates {
  font-size: 12px;
  color: #aaa;
}

.plan-card-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.btn-icon-action {
  width: 32px;
  height: 32px;
  border: none;
  background: #f8f9fa;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon-action:hover { background: #e8ecff; }

/* 状态标签 */
.plan-status-badge {
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.status-active { background: #d4edda; color: #155724; }
.status-paused { background: #fff3cd; color: #856404; }
.status-completed { background: #cce5ff; color: #004085; }
.status-cancelled { background: #f8d7da; color: #721c24; }

/* 按钮 */
.btn-sm {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-info { background: #e8f4f8; color: #2980b9; }
.btn-info:hover { background: #d5eaf3; }
.btn-warning { background: #fff3cd; color: #856404; }
.btn-warning:hover { background: #ffe8a0; }
.btn-success { background: #d4edda; color: #155724; }
.btn-success:hover { background: #b8dfc5; }

/* 详情弹窗 */
.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-icon { font-size: 28px; }

.detail-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.meta-item-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meta-key { font-size: 13px; color: #888; min-width: 60px; }
.meta-val { font-size: 14px; color: #333; }

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

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon { font-size: 64px; margin-bottom: 16px; }
.empty-state p { color: #888; margin: 0 0 8px 0; font-size: 16px; }
.empty-sub { font-size: 14px !important; color: #bbb !important; margin-bottom: 24px !important; }

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}

.page-info { color: #666; font-size: 14px; }

/* 通用按钮 */
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

.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.btn-cancel { background: #f0f0f0; color: #555; }
.btn-cancel:hover { background: #e0e0e0; }

.btn-page {
  background: #f8f9fa;
  color: #333;
  padding: 8px 16px;
  border: 1px solid #ddd;
}

.btn-page:disabled { opacity: 0.4; cursor: not-allowed; }

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
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

.modal-large { max-width: 580px; }

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 { margin: 0; font-size: 18px; color: #333; }
.modal-body { padding: 24px; }
.modal-footer { display: flex; gap: 12px; padding: 16px 24px; border-top: 1px solid #eee; }

/* 表单 */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group { margin-bottom: 16px; }

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

.form-textarea { resize: vertical; font-family: inherit; }

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

.close { background: none; border: none; cursor: pointer; font-size: 18px; color: inherit; }
.loading-spinner { text-align: center; padding: 40px; color: #888; }

/* 响应式 */
@media (max-width: 768px) {
  .interventions-container { padding: 12px; }
  .active-grid { grid-template-columns: 1fr; }
  .form-row { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; gap: 12px; }
}
</style>

