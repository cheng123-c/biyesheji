<template>
  <div class="suggestions-container">
    <div class="page-header">
      <div>
        <h1>💡 健康建议</h1>
        <p class="page-subtitle">基于您的健康数据和AI分析生成的个性化建议</p>
      </div>
      <div class="header-actions">
        <button v-if="unreadCount > 0" @click="markAllRead" class="btn btn-outline">
          全部标记已读 ({{ unreadCount }})
        </button>
      </div>
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

    <!-- 统计概览 -->
    <section class="stats-section">
      <div class="stat-card">
        <div class="stat-icon">📬</div>
        <div class="stat-info">
          <div class="stat-num">{{ unreadCount }}</div>
          <div class="stat-label">未读建议</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📋</div>
        <div class="stat-info">
          <div class="stat-num">{{ totalCount }}</div>
          <div class="stat-label">全部建议</div>
        </div>
      </div>
    </section>

    <!-- 建议列表 -->
    <section class="suggestions-section">
      <div class="section-header">
        <h2>全部建议</h2>
        <select v-model="priorityFilter" @change="applyFilter" class="form-select filter-select">
          <option value="">全部优先级</option>
          <option value="HIGH">高优先级</option>
          <option value="MEDIUM">中优先级</option>
          <option value="LOW">低优先级</option>
        </select>
      </div>

      <div v-if="loading" class="loading-spinner">加载中...</div>
      <div v-else-if="suggestions.length === 0" class="empty-state">
        <div class="empty-icon">💡</div>
        <p>暂无健康建议</p>
        <p class="empty-sub">完成AI健康评测后，系统将自动为您生成个性化健康建议</p>
        <router-link to="/assessment" class="btn btn-primary">去进行AI评测</router-link>
      </div>
      <div v-else class="suggestions-list">
        <div
          v-for="suggestion in suggestions"
          :key="suggestion.id"
          class="suggestion-card"
          :class="{ 'unread': !suggestion.readAt }"
          @click="handleCardClick(suggestion)"
        >
          <div class="suggestion-header">
            <div class="suggestion-meta">
              <span class="priority-badge" :class="getPriorityClass(suggestion.priority)">
                {{ getPriorityLabel(suggestion.priority) }}
              </span>
              <span class="suggestion-type-badge">
                {{ getTypeName(suggestion.suggestionType) }}
              </span>
            </div>
            <div class="suggestion-right">
              <span v-if="!suggestion.readAt" class="unread-dot"></span>
              <span class="suggestion-time">{{ formatTime(suggestion.createdAt) }}</span>
            </div>
          </div>

          <div class="suggestion-content">
            {{ suggestion.suggestionContent }}
          </div>

          <div class="suggestion-footer">
            <span v-if="suggestion.evidenceLevel" class="evidence-badge">
              循证等级: {{ suggestion.evidenceLevel }}
            </span>
            <div class="suggestion-actions">
              <button
                v-if="!suggestion.readAt"
                @click.stop="markRead(suggestion.id)"
                class="btn-text btn-mark"
              >
                标记已读
              </button>
              <button @click.stop="confirmDelete(suggestion.id)" class="btn-text btn-delete">
                删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="suggestions.length > 0" class="pagination">
        <button :disabled="currentPage <= 1" @click="loadSuggestions(currentPage - 1)" class="btn btn-page">上一页</button>
        <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <button :disabled="currentPage >= totalPages" @click="loadSuggestions(currentPage + 1)" class="btn btn-page">下一页</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getSuggestions,
  getSuggestionUnreadCount,
  markSuggestionRead,
  markAllSuggestionsRead,
  deleteSuggestion
} from '@/services/suggestionApi'

const loading = ref(true)
const error = ref(null)
const success = ref(null)

const suggestions = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const unreadCount = ref(0)
const totalCount = ref(0)
const priorityFilter = ref('')

onMounted(async () => {
  await Promise.all([loadSuggestions(), loadStats()])
})

const loadSuggestions = async (page = 1) => {
  loading.value = true
  try {
    const response = await getSuggestions(page, 10, priorityFilter.value || undefined)
    suggestions.value = response.data?.list || []
    totalPages.value = response.data?.pages || 1
    totalCount.value = response.data?.total || 0
    currentPage.value = page
  } catch (err) {
    error.value = '加载建议失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadStats = async () => {
  try {
    const res = await getSuggestionUnreadCount()
    unreadCount.value = res.data || 0
  } catch { /* ignore */ }
}

const applyFilter = () => {
  loadSuggestions(1)
}

const handleCardClick = async (suggestion) => {
  if (!suggestion.readAt) {
    await markRead(suggestion.id)
  }
}

const markRead = async (id) => {
  try {
    await markSuggestionRead(id)
    const item = suggestions.value.find(s => s.id === id)
    if (item) {
      item.readAt = new Date().toISOString()
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  } catch (err) {
    error.value = '操作失败'
  }
}

const markAllRead = async () => {
  try {
    await markAllSuggestionsRead()
    suggestions.value.forEach(s => {
      if (!s.readAt) s.readAt = new Date().toISOString()
    })
    unreadCount.value = 0
    success.value = '已全部标记为已读'
  } catch (err) {
    error.value = '操作失败'
  }
}

const confirmDelete = async (id) => {
  if (confirm('确定要删除这条建议吗？')) {
    try {
      await deleteSuggestion(id)
      success.value = '删除成功'
      // 若当前页删除后已无数据且不是第一页，跳回上一页
      const targetPage = suggestions.value.length === 1 && currentPage.value > 1
        ? currentPage.value - 1
        : currentPage.value
      await loadSuggestions(targetPage)
      await loadStats()
    } catch (err) {
      error.value = '删除失败'
    }
  }
}

const getPriorityLabel = (priority) => {
  const labels = { HIGH: '高优先', MEDIUM: '中优先', LOW: '低优先' }
  return labels[priority] || priority || '—'
}

const getPriorityClass = (priority) => {
  const classes = { HIGH: 'priority-high', MEDIUM: 'priority-medium', LOW: 'priority-low' }
  return classes[priority] || 'priority-medium'
}

const getTypeName = (type) => {
  const names = {
    DIET: '饮食',
    EXERCISE: '运动',
    MEDICATION: '用药',
    LIFESTYLE: '生活方式',
    OTHER: '其他'
  }
  return names[type] || type || '综合'
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  const now = new Date()
  const diffMs = now - d
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  if (diffDays === 0) return '今天 ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays}天前`
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}
</script>

<style scoped>
.suggestions-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 6px 0;
}

.page-subtitle {
  color: #888;
  font-size: 14px;
  margin: 0;
}

section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

/* 统计 */
.stats-section {
  display: flex;
  gap: 16px;
  padding: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  padding: 16px 20px;
  background: #f8f9ff;
  border-radius: 10px;
  border: 1px solid #e8ecff;
}

.stat-icon { font-size: 32px; }

.stat-num {
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
}

.stat-label {
  font-size: 13px;
  color: #888;
}

/* 建议列表 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.filter-select {
  width: auto;
  padding: 6px 12px;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-card {
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 18px 20px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.suggestion-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  border-color: #ddd;
}

.suggestion-card.unread {
  background: #fffef0;
  border-color: #ffe58f;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.suggestion-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.priority-badge {
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.priority-high { background: #fde8e8; color: #c0392b; }
.priority-medium { background: #fff3cd; color: #856404; }
.priority-low { background: #d4edda; color: #155724; }

.suggestion-type-badge {
  font-size: 12px;
  color: #666;
  background: #f0f0f0;
  padding: 3px 10px;
  border-radius: 10px;
}

.suggestion-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #f39c12;
  border-radius: 50%;
}

.suggestion-time {
  font-size: 12px;
  color: #aaa;
}

.suggestion-content {
  font-size: 15px;
  color: #333;
  line-height: 1.7;
  margin-bottom: 12px;
}

.suggestion-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.evidence-badge {
  font-size: 12px;
  color: #888;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 8px;
}

.suggestion-actions {
  display: flex;
  gap: 8px;
}

.btn-text {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.btn-mark { color: #667eea; }
.btn-mark:hover { background: #f0f0ff; }
.btn-delete { color: #e74c3c; }
.btn-delete:hover { background: #fff0f0; }

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon { font-size: 64px; margin-bottom: 16px; }

.empty-state p {
  color: #888;
  margin: 0 0 8px 0;
  font-size: 16px;
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

.page-info { color: #666; font-size: 14px; }

/* 按钮 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  font-size: 14px;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102,126,234,0.4);
}

.btn-outline {
  background: white;
  color: #667eea;
  border: 1px solid #667eea;
}

.btn-outline:hover {
  background: #f0f0ff;
}

.btn-page {
  background: #f8f9fa;
  color: #333;
  padding: 8px 16px;
  border: 1px solid #ddd;
}

.btn-page:disabled { opacity: 0.4; cursor: not-allowed; }

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
}

.loading-spinner { text-align: center; padding: 40px; color: #888; }

/* 响应式 */
@media (max-width: 768px) {
  .suggestions-container { padding: 12px; }
  .stats-section { flex-direction: column; }
  .page-header { flex-direction: column; gap: 12px; }
}

.form-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  background: white;
}

.form-select:focus {
  border-color: #667eea;
}
</style>

