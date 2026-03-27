<template>
  <div class="feedback-page">
    <div class="page-header">
      <h1>💬 意见反馈</h1>
      <p class="subtitle">您的意见是我们改进的动力，期待您的宝贵建议</p>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-nav">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >{{ tab.label }}</button>
    </div>

    <!-- 提交反馈 -->
    <div v-if="activeTab === 'submit'" class="tab-content">
      <div class="form-card">
        <div class="form-group">
          <label class="form-label">反馈类型 <span class="required">*</span></label>
          <div class="type-options">
            <button
              v-for="t in feedbackTypes"
              :key="t.value"
              :class="['type-btn', { active: form.feedbackType === t.value }]"
              @click="form.feedbackType = t.value"
            >{{ t.icon }} {{ t.label }}</button>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">反馈标题 <span class="required">*</span></label>
          <input
            v-model="form.feedbackTitle"
            type="text"
            placeholder="请简要描述您的问题或建议（5-200字）"
            class="form-input"
            maxlength="200"
          />
          <span class="char-count">{{ form.feedbackTitle.length }}/200</span>
        </div>

        <div class="form-group">
          <label class="form-label">详细描述 <span class="required">*</span></label>
          <textarea
            v-model="form.feedbackContent"
            placeholder="请详细描述您遇到的问题或您的建议..."
            class="form-textarea"
            rows="6"
            maxlength="2000"
          ></textarea>
          <span class="char-count">{{ form.feedbackContent.length }}/2000</span>
        </div>

        <div class="form-group">
          <label class="form-label">联系方式（可选）</label>
          <input
            v-model="form.contactInfo"
            type="text"
            placeholder="如邮箱或手机号，方便我们与您联系"
            class="form-input"
            maxlength="100"
          />
        </div>

        <div class="form-actions">
          <button
            class="btn-submit"
            :disabled="submitting || !isFormValid"
            @click="handleSubmit"
          >{{ submitting ? '提交中...' : '提交反馈' }}</button>
        </div>

        <div v-if="submitSuccess" class="success-notice">
          <span class="success-icon">✅</span>
          <div>
            <strong>反馈已提交！</strong>
            <p>我们会认真查看您的反馈，感谢您的支持！</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的反馈 -->
    <div v-if="activeTab === 'history'" class="tab-content">
      <div v-if="historyLoading" class="loading">加载中...</div>
      <div v-else-if="historyList.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <p>您还没有提交过反馈</p>
        <button class="btn-primary" @click="activeTab = 'submit'">立即反馈</button>
      </div>
      <div v-else class="history-list">
        <div
          v-for="fb in historyList"
          :key="fb.id"
          class="history-card"
          @click="openDetail(fb)"
        >
          <div class="history-card-header">
            <div class="type-badge" :class="getTypeClass(fb.feedbackType)">
              {{ getTypeLabel(fb.feedbackType) }}
            </div>
            <div class="status-badge" :class="getStatusClass(fb.status)">
              {{ getStatusLabel(fb.status) }}
            </div>
          </div>
          <h4 class="history-title">{{ fb.feedbackTitle }}</h4>
          <p class="history-preview">{{ fb.feedbackContent?.substring(0, 80) }}{{ fb.feedbackContent?.length > 80 ? '...' : '' }}</p>
          <div class="history-footer">
            <span class="history-time">{{ formatTime(fb.createdAt) }}</span>
            <span v-if="fb.adminReply" class="has-reply">💬 有回复</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 反馈详情弹窗 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal-box feedback-detail-modal">
        <div class="modal-header">
          <h2>反馈详情</h2>
          <button class="close-btn" @click="showDetail = false">✕</button>
        </div>
        <div class="detail-content">
          <div class="detail-meta">
            <span class="type-badge" :class="getTypeClass(currentFeedback?.feedbackType)">
              {{ getTypeLabel(currentFeedback?.feedbackType) }}
            </span>
            <span class="status-badge" :class="getStatusClass(currentFeedback?.status)">
              {{ getStatusLabel(currentFeedback?.status) }}
            </span>
            <span class="detail-time">{{ formatTime(currentFeedback?.createdAt) }}</span>
          </div>
          <h3>{{ currentFeedback?.feedbackTitle }}</h3>
          <div class="detail-body">{{ currentFeedback?.feedbackContent }}</div>

          <div v-if="currentFeedback?.adminReply" class="admin-reply">
            <div class="reply-label">📋 官方回复</div>
            <div class="reply-content">{{ currentFeedback.adminReply }}</div>
            <div class="reply-time">{{ formatTime(currentFeedback.repliedAt) }}</div>
          </div>
          <div v-else class="no-reply">
            <span>⏳ 等待处理中，我们会尽快回复您</span>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showDetail = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { submitFeedback, getMyFeedbacks } from '@/services/feedbackApi'

const tabs = [
  { key: 'submit', label: '✍️ 提交反馈' },
  { key: 'history', label: '📋 我的反馈' }
]
const activeTab = ref('submit')

const feedbackTypes = [
  { value: 'BUG', label: '问题/Bug', icon: '🐛' },
  { value: 'SUGGESTION', label: '功能建议', icon: '💡' },
  { value: 'QUESTION', label: '使用疑问', icon: '❓' },
  { value: 'PRAISE', label: '点赞', icon: '👍' },
  { value: 'OTHER', label: '其他', icon: '📝' }
]

const form = ref({
  feedbackType: 'SUGGESTION',
  feedbackTitle: '',
  feedbackContent: '',
  contactInfo: ''
})
const submitting = ref(false)
const submitSuccess = ref(false)

const historyLoading = ref(false)
const historyList = ref([])
const showDetail = ref(false)
const currentFeedback = ref(null)

const isFormValid = computed(() => {
  return form.value.feedbackType &&
    form.value.feedbackTitle.length >= 5 &&
    form.value.feedbackContent.length >= 10
})

onMounted(() => {
  loadHistory()
})

async function loadHistory() {
  historyLoading.value = true
  try {
    const res = await getMyFeedbacks()
    historyList.value = res.data || res || []
  } catch (e) {
    console.error('加载反馈历史失败:', e)
  } finally {
    historyLoading.value = false
  }
}

async function handleSubmit() {
  if (!isFormValid.value) return
  submitting.value = true
  submitSuccess.value = false
  try {
    await submitFeedback({ ...form.value })
    submitSuccess.value = true
    form.value = { feedbackType: 'SUGGESTION', feedbackTitle: '', feedbackContent: '', contactInfo: '' }
    loadHistory()
  } catch (e) {
    alert(e?.response?.data?.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

function openDetail(fb) {
  currentFeedback.value = fb
  showDetail.value = true
}

function getTypeLabel(type) {
  const map = { BUG: '🐛 问题/Bug', SUGGESTION: '💡 功能建议', QUESTION: '❓ 使用疑问', PRAISE: '👍 点赞', OTHER: '📝 其他' }
  return map[type] || type || '其他'
}

function getTypeClass(type) {
  const map = { BUG: 'type-bug', SUGGESTION: 'type-suggestion', QUESTION: 'type-question', PRAISE: 'type-praise', OTHER: 'type-other' }
  return map[type] || ''
}

function getStatusLabel(status) {
  const map = { PENDING: '⏳ 待处理', PROCESSING: '⚙️ 处理中', RESOLVED: '✅ 已解决', CLOSED: '🔒 已关闭' }
  return map[status] || status || '待处理'
}

function getStatusClass(status) {
  const map = { PENDING: 'status-pending', PROCESSING: 'status-processing', RESOLVED: 'status-resolved', CLOSED: 'status-closed' }
  return map[status] || 'status-pending'
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.feedback-page {
  padding: 24px;
  max-width: 760px;
  margin: 0 auto;
}
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 1.8rem; margin-bottom: 6px; }
.subtitle { color: #666; font-size: 0.95rem; }

.tab-nav { display: flex; gap: 4px; margin-bottom: 24px; border-bottom: 2px solid #e5e7eb; }
.tab-btn {
  padding: 10px 20px; border: none; background: none; cursor: pointer;
  font-size: 0.95rem; color: #666; border-bottom: 2px solid transparent; margin-bottom: -2px;
}
.tab-btn.active { color: #3b82f6; border-bottom-color: #3b82f6; font-weight: 600; }

.form-card {
  background: #fff; border: 1px solid #e5e7eb; border-radius: 12px; padding: 28px;
}
.form-group { margin-bottom: 20px; }
.form-label { display: block; font-size: 0.93rem; font-weight: 500; margin-bottom: 8px; }
.required { color: #ef4444; }
.form-input {
  width: 100%; padding: 10px 14px;
  border: 1.5px solid #e5e7eb; border-radius: 8px;
  font-size: 0.95rem; box-sizing: border-box; outline: none;
}
.form-input:focus { border-color: #3b82f6; }
.form-textarea {
  width: 100%; padding: 10px 14px;
  border: 1.5px solid #e5e7eb; border-radius: 8px;
  font-size: 0.95rem; resize: vertical; box-sizing: border-box; outline: none;
}
.form-textarea:focus { border-color: #3b82f6; }
.char-count { font-size: 0.78rem; color: #999; display: block; text-align: right; margin-top: 4px; }

.type-options { display: flex; flex-wrap: wrap; gap: 8px; }
.type-btn {
  padding: 7px 14px; border: 1.5px solid #e5e7eb;
  border-radius: 20px; cursor: pointer; font-size: 0.88rem; background: #fff;
  transition: all 0.15s;
}
.type-btn:hover { border-color: #93c5fd; }
.type-btn.active { border-color: #3b82f6; background: #eff6ff; color: #1d4ed8; }

.form-actions { display: flex; justify-content: flex-end; }
.btn-submit {
  padding: 11px 28px; background: #3b82f6; color: #fff;
  border: none; border-radius: 8px; cursor: pointer; font-size: 0.95rem; font-weight: 600;
}
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }

.success-notice {
  display: flex; gap: 12px; align-items: flex-start;
  background: #f0fdf4; border: 1px solid #bbf7d0;
  border-radius: 8px; padding: 16px; margin-top: 16px;
}
.success-icon { font-size: 1.3rem; }
.success-notice strong { display: block; color: #15803d; margin-bottom: 4px; }
.success-notice p { color: #166534; font-size: 0.88rem; margin: 0; }

/* 历史记录 */
.loading, .empty-state { text-align: center; padding: 60px 20px; color: #999; }
.empty-icon { font-size: 3rem; margin-bottom: 12px; }
.btn-primary {
  background: #3b82f6; color: #fff; border: none;
  padding: 10px 20px; border-radius: 8px; cursor: pointer; font-size: 0.95rem;
}

.history-list { display: flex; flex-direction: column; gap: 14px; }
.history-card {
  background: #fff; border: 1px solid #e5e7eb; border-radius: 10px;
  padding: 16px 20px; cursor: pointer; transition: all 0.15s;
}
.history-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.08); }
.history-card-header { display: flex; gap: 8px; margin-bottom: 10px; }
.history-title { font-size: 0.97rem; font-weight: 500; margin-bottom: 6px; }
.history-preview { color: #666; font-size: 0.88rem; margin-bottom: 10px; }
.history-footer { display: flex; justify-content: space-between; align-items: center; }
.history-time { color: #999; font-size: 0.8rem; }
.has-reply { background: #dbeafe; color: #1d4ed8; padding: 2px 8px; border-radius: 20px; font-size: 0.78rem; }

.type-badge, .status-badge {
  display: inline-block; padding: 2px 8px; border-radius: 20px; font-size: 0.75rem; font-weight: 500;
}
.type-bug { background: #fee2e2; color: #b91c1c; }
.type-suggestion { background: #dbeafe; color: #1d4ed8; }
.type-question { background: #fef3c7; color: #b45309; }
.type-praise { background: #dcfce7; color: #15803d; }
.type-other { background: #f3f4f6; color: #374151; }
.status-pending { background: #fef3c7; color: #b45309; }
.status-processing { background: #dbeafe; color: #1d4ed8; }
.status-resolved { background: #dcfce7; color: #15803d; }
.status-closed { background: #f3f4f6; color: #6b7280; }

/* 弹窗 */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.feedback-detail-modal {
  background: #fff; border-radius: 12px; padding: 28px;
  width: 90%; max-width: 580px; max-height: 80vh; overflow-y: auto;
}
.modal-header { display: flex; justify-content: space-between; margin-bottom: 16px; }
.modal-header h2 { font-size: 1.2rem; }
.close-btn { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: #999; }
.detail-meta { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; margin-bottom: 12px; }
.detail-time { color: #999; font-size: 0.82rem; }
.detail-content h3 { font-size: 1.05rem; margin-bottom: 10px; }
.detail-body {
  background: #f8fafc; border-radius: 8px; padding: 14px;
  font-size: 0.92rem; line-height: 1.7; color: #374151; white-space: pre-wrap;
  margin-bottom: 16px;
}
.admin-reply {
  background: #eff6ff; border: 1px solid #bfdbfe;
  border-radius: 8px; padding: 14px; margin-bottom: 16px;
}
.reply-label { font-weight: 600; color: #1d4ed8; margin-bottom: 8px; }
.reply-content { font-size: 0.92rem; line-height: 1.7; color: #374151; }
.reply-time { font-size: 0.8rem; color: #6b7280; margin-top: 8px; }
.no-reply {
  background: #fef3c7; border: 1px solid #fde68a;
  border-radius: 8px; padding: 12px; color: #b45309; font-size: 0.9rem;
}
.modal-actions { display: flex; justify-content: flex-end; margin-top: 8px; }
.btn-secondary {
  background: #f3f4f6; color: #374151; border: none;
  padding: 8px 18px; border-radius: 8px; cursor: pointer;
}
</style>

