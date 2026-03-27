<template>
  <div class="questionnaire-page">
    <div class="page-header">
      <h1>📋 健康问卷</h1>
      <p class="subtitle">完成问卷，帮助AI更精准地了解您的健康状况</p>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-nav">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        :class="['tab-btn', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 问卷列表 -->
    <div v-if="activeTab === 'list'" class="tab-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="questionnaires.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <p>暂无可用问卷</p>
      </div>
      <div v-else class="questionnaire-grid">
        <div
          v-for="q in questionnaires"
          :key="q.id"
          class="questionnaire-card"
          @click="openQuestionnaire(q)"
        >
          <div class="card-header">
            <span class="type-badge" :class="getTypeClass(q.questionnaireType)">
              {{ getTypeLabel(q.questionnaireType) }}
            </span>
            <span v-if="hasCompleted(q.id)" class="completed-badge">✓ 已完成</span>
          </div>
          <h3 class="card-title">{{ q.title }}</h3>
          <p class="card-desc">{{ q.description }}</p>
          <div class="card-footer">
            <span class="question-count">{{ parseQuestions(q.questions).length }} 道题</span>
            <button class="btn-start">开始作答 →</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 我的回答历史 -->
    <div v-if="activeTab === 'history'" class="tab-content">
      <div v-if="historyLoading" class="loading">加载中...</div>
      <div v-else-if="historyList.length === 0" class="empty-state">
        <div class="empty-icon">📝</div>
        <p>您还没有完成任何问卷</p>
        <button class="btn-primary" @click="activeTab = 'list'">去完成问卷</button>
      </div>
      <div v-else class="history-list">
        <div
          v-for="resp in historyList"
          :key="resp.id"
          class="history-item"
          @click="viewResponse(resp)"
        >
          <div class="history-left">
            <span class="type-badge" :class="getTypeClass(resp.questionnaireType)">
              {{ getTypeLabel(resp.questionnaireType) }}
            </span>
            <h4>{{ resp.questionnaireTitle || '健康问卷' }}</h4>
            <span class="history-time">{{ formatTime(resp.completedAt) }}</span>
          </div>
          <div class="history-right">
            <div v-if="resp.score != null" class="score-display">
              <span class="score-num">{{ resp.score }}</span>
              <span class="score-unit">分</span>
            </div>
            <button class="btn-sm">查看详情</button>
          </div>
        </div>
        <!-- 分页 -->
        <div v-if="historyTotal > historyPageSize" class="pagination">
          <button :disabled="historyPage <= 1" @click="loadHistory(historyPage - 1)">上一页</button>
          <span>第 {{ historyPage }} 页 / 共 {{ Math.ceil(historyTotal / historyPageSize) }} 页</span>
          <button :disabled="historyPage >= Math.ceil(historyTotal / historyPageSize)" @click="loadHistory(historyPage + 1)">下一页</button>
        </div>
      </div>
    </div>

    <!-- 作答弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-box questionnaire-modal">
        <div class="modal-header">
          <h2>{{ currentQuestionnaire?.title }}</h2>
          <button class="close-btn" @click="closeModal">✕</button>
        </div>
        <p class="modal-desc">{{ currentQuestionnaire?.description }}</p>
        <div class="progress-bar-wrap">
          <div class="progress-bar-track">
            <div class="progress-bar-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <span class="progress-text">{{ currentQuestionIndex + 1 }} / {{ currentQuestions.length }}</span>
        </div>

        <!-- 当前问题 -->
        <div v-if="currentQuestion" class="question-block">
          <h3 class="question-title">
            {{ currentQuestionIndex + 1 }}. {{ currentQuestion.question }}
          </h3>

          <!-- 单选 -->
          <div v-if="currentQuestion.type === 'radio'" class="options-list">
            <label
              v-for="opt in currentQuestion.options"
              :key="opt.value"
              :class="['option-item', { selected: answers[currentQuestion.id] === opt.value }]"
            >
              <input
                type="radio"
                :name="'q_' + currentQuestion.id"
                :value="opt.value"
                v-model="answers[currentQuestion.id]"
              />
              <span>{{ opt.label }}</span>
            </label>
          </div>

          <!-- 多选 -->
          <div v-else-if="currentQuestion.type === 'checkbox'" class="options-list">
            <label
              v-for="opt in currentQuestion.options"
              :key="opt.value"
              :class="['option-item', { selected: (answers[currentQuestion.id] || []).includes(opt.value) }]"
            >
              <input
                type="checkbox"
                :value="opt.value"
                v-model="answers[currentQuestion.id]"
              />
              <span>{{ opt.label }}</span>
            </label>
          </div>

          <!-- 文本 -->
          <div v-else-if="currentQuestion.type === 'text'" class="text-input-wrap">
            <textarea
              v-model="answers[currentQuestion.id]"
              placeholder="请输入您的回答（可选）"
              rows="3"
              class="text-input"
            ></textarea>
          </div>
        </div>

        <!-- 导航按钮 -->
        <div class="modal-actions">
          <button
            v-if="currentQuestionIndex > 0"
            class="btn-secondary"
            @click="prevQuestion"
          >← 上一题</button>
          <button
            v-if="currentQuestionIndex < currentQuestions.length - 1"
            class="btn-primary"
            :disabled="!canNext"
            @click="nextQuestion"
          >下一题 →</button>
          <button
            v-if="currentQuestionIndex === currentQuestions.length - 1"
            class="btn-submit"
            :disabled="submitting"
            @click="submitAnswers"
          >{{ submitting ? '提交中...' : '✓ 提交问卷' }}</button>
        </div>
      </div>
    </div>

    <!-- 结果弹窗 -->
    <div v-if="showResult" class="modal-overlay" @click.self="closeResult">
      <div class="modal-box result-modal">
        <div class="result-icon">🎉</div>
        <h2>问卷完成！</h2>
        <div v-if="submitResult?.score != null" class="result-score">
          <span class="result-score-num">{{ submitResult.score }}</span>
          <span class="result-score-unit">分</span>
        </div>
        <p class="result-desc">感谢您完成问卷！您的回答将帮助AI为您提供更精准的健康建议。</p>
        <div class="result-actions">
          <button class="btn-primary" @click="goToAssessment">立即进行AI评测</button>
          <button class="btn-secondary" @click="closeResult">返回问卷列表</button>
        </div>
      </div>
    </div>

    <!-- 回答详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click.self="showDetailModal = false">
      <div class="modal-box detail-modal">
        <div class="modal-header">
          <h2>{{ detailResponse?.questionnaireTitle }}</h2>
          <button class="close-btn" @click="showDetailModal = false">✕</button>
        </div>
        <div class="detail-meta">
          <span>完成时间：{{ formatTime(detailResponse?.completedAt) }}</span>
          <span v-if="detailResponse?.score != null">得分：{{ detailResponse.score }} 分</span>
        </div>
        <div v-if="detailAnswers" class="detail-answers">
          <div
            v-for="(val, key) in detailAnswers"
            :key="key"
            class="detail-answer-item"
          >
            <span class="answer-key">{{ key }}：</span>
            <span class="answer-val">{{ Array.isArray(val) ? val.join(', ') : val }}</span>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showDetailModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  getQuestionnaires,
  submitQuestionnaire,
  getMyResponses
} from '@/services/questionnaireApi'

const router = useRouter()

const tabs = [
  { key: 'list', label: '📋 问卷列表' },
  { key: 'history', label: '📝 我的记录' }
]
const activeTab = ref('list')

// 问卷列表
const loading = ref(false)
const questionnaires = ref([])
const completedIds = ref(new Set())

// 历史记录
const historyLoading = ref(false)
const historyList = ref([])
const historyPage = ref(1)
const historyPageSize = ref(10)
const historyTotal = ref(0)

// 作答弹窗
const showModal = ref(false)
const currentQuestionnaire = ref(null)
const currentQuestions = ref([])
const currentQuestionIndex = ref(0)
const answers = ref({})
const submitting = ref(false)

// 结果弹窗
const showResult = ref(false)
const submitResult = ref(null)

// 详情弹窗
const showDetailModal = ref(false)
const detailResponse = ref(null)

// 计算属性
const currentQuestion = computed(() => currentQuestions.value[currentQuestionIndex.value])
const progressPercent = computed(() => {
  if (currentQuestions.value.length === 0) return 0
  return Math.round(((currentQuestionIndex.value + 1) / currentQuestions.value.length) * 100)
})
const canNext = computed(() => {
  if (!currentQuestion.value) return false
  if (currentQuestion.value.type === 'text') return true // 文本题可跳过
  return answers.value[currentQuestion.value.id] != null &&
    answers.value[currentQuestion.value.id] !== ''
})
const detailAnswers = computed(() => {
  if (!detailResponse.value?.responseData) return null
  try {
    return JSON.parse(detailResponse.value.responseData)
  } catch { return null }
})

onMounted(() => {
  loadQuestionnaires()
  loadHistory()
})

async function loadQuestionnaires() {
  loading.value = true
  try {
    const res = await getQuestionnaires()
    questionnaires.value = (res.data || res) || []
  } catch (e) {
    console.error('加载问卷失败:', e)
  } finally {
    loading.value = false
  }
}

async function loadHistory(page = 1) {
  historyLoading.value = true
  historyPage.value = page
  try {
    const res = await getMyResponses(page, historyPageSize.value)
    const data = res.data || res
    historyList.value = data.list || []
    historyTotal.value = data.total || 0
    // 记录已完成的问卷ID
    historyList.value.forEach(r => completedIds.value.add(r.questionnaireId))
  } catch (e) {
    console.error('加载历史失败:', e)
  } finally {
    historyLoading.value = false
  }
}

function parseQuestions(json) {
  try { return JSON.parse(json) || [] } catch { return [] }
}

function hasCompleted(id) { return completedIds.value.has(id) }

function openQuestionnaire(q) {
  currentQuestionnaire.value = q
  currentQuestions.value = parseQuestions(q.questions)
  currentQuestionIndex.value = 0
  answers.value = {}
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  currentQuestionnaire.value = null
}

function prevQuestion() {
  if (currentQuestionIndex.value > 0) currentQuestionIndex.value--
}

function nextQuestion() {
  if (currentQuestionIndex.value < currentQuestions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

async function submitAnswers() {
  submitting.value = true
  try {
    const res = await submitQuestionnaire({
      questionnaireId: currentQuestionnaire.value.id,
      answers: answers.value
    })
    submitResult.value = res.data || res
    completedIds.value.add(currentQuestionnaire.value.id)
    showModal.value = false
    showResult.value = true
    loadHistory()
  } catch (e) {
    alert(e?.response?.data?.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

function closeResult() {
  showResult.value = false
  submitResult.value = null
}

function goToAssessment() {
  closeResult()
  router.push('/assessment')
}

function viewResponse(resp) {
  detailResponse.value = resp
  showDetailModal.value = true
}

function getTypeLabel(type) {
  const map = {
    LIFESTYLE: '生活习惯',
    SYMPTOM: '症状自评',
    MENTAL: '心理健康',
    DIET: '饮食习惯',
    EXERCISE: '运动情况'
  }
  return map[type] || type || '综合'
}

function getTypeClass(type) {
  const map = {
    LIFESTYLE: 'type-lifestyle',
    SYMPTOM: 'type-symptom',
    MENTAL: 'type-mental',
    DIET: 'type-diet',
    EXERCISE: 'type-exercise'
  }
  return map[type] || ''
}

function formatTime(t) {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.questionnaire-page {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 1.8rem; margin-bottom: 6px; }
.subtitle { color: #666; font-size: 0.95rem; }

.tab-nav {
  display: flex;
  gap: 4px;
  margin-bottom: 24px;
  border-bottom: 2px solid #e5e7eb;
}
.tab-btn {
  padding: 10px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 0.95rem;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s;
}
.tab-btn.active { color: #3b82f6; border-bottom-color: #3b82f6; font-weight: 600; }

.loading, .empty-state { text-align: center; padding: 60px 20px; color: #999; }
.empty-icon { font-size: 3rem; margin-bottom: 12px; }

.questionnaire-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.questionnaire-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
}
.questionnaire-card:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.1); transform: translateY(-2px); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.card-title { font-size: 1.05rem; font-weight: 600; margin-bottom: 8px; }
.card-desc { color: #666; font-size: 0.9rem; margin-bottom: 16px; line-height: 1.5; }
.card-footer { display: flex; justify-content: space-between; align-items: center; }
.question-count { color: #999; font-size: 0.85rem; }

.type-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 500;
}
.type-lifestyle { background: #dbeafe; color: #1d4ed8; }
.type-symptom { background: #fee2e2; color: #b91c1c; }
.type-mental { background: #fae8ff; color: #7c3aed; }
.type-diet { background: #dcfce7; color: #15803d; }
.type-exercise { background: #fef3c7; color: #b45309; }
.completed-badge { background: #dcfce7; color: #15803d; padding: 2px 8px; border-radius: 20px; font-size: 0.75rem; }

.btn-start {
  background: #3b82f6;
  color: #fff;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}

/* 历史记录 */
.history-list { display: flex; flex-direction: column; gap: 12px; }
.history-item {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  transition: background 0.15s;
}
.history-item:hover { background: #f8fafc; }
.history-left h4 { margin: 6px 0 4px; font-size: 1rem; }
.history-time { color: #999; font-size: 0.83rem; }
.history-right { display: flex; align-items: center; gap: 16px; }
.score-display { text-align: center; }
.score-num { font-size: 1.6rem; font-weight: 700; color: #3b82f6; }
.score-unit { font-size: 0.85rem; color: #666; margin-left: 2px; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; margin-top: 20px; }
.pagination button {
  padding: 6px 14px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  background: #fff;
}
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }

/* 弹窗 */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  width: 90%;
  max-width: 620px;
  max-height: 85vh;
  overflow-y: auto;
}
.modal-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.modal-header h2 { font-size: 1.3rem; font-weight: 600; }
.close-btn { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: #999; }
.modal-desc { color: #666; font-size: 0.9rem; margin-bottom: 16px; }

.progress-bar-wrap { display: flex; align-items: center; gap: 10px; margin-bottom: 24px; }
.progress-bar-track { flex: 1; height: 6px; background: #e5e7eb; border-radius: 3px; }
.progress-bar-fill { height: 100%; background: #3b82f6; border-radius: 3px; transition: width 0.3s; }
.progress-text { font-size: 0.83rem; color: #666; white-space: nowrap; }

.question-block { margin-bottom: 24px; }
.question-title { font-size: 1.05rem; font-weight: 500; margin-bottom: 16px; line-height: 1.6; }

.options-list { display: flex; flex-direction: column; gap: 10px; }
.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}
.option-item:hover { border-color: #93c5fd; background: #f0f9ff; }
.option-item.selected { border-color: #3b82f6; background: #eff6ff; }
.option-item input { accent-color: #3b82f6; }

.text-input-wrap { margin-top: 8px; }
.text-input {
  width: 100%;
  padding: 10px;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  resize: vertical;
  font-size: 0.95rem;
  box-sizing: border-box;
}

.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 8px; }
.btn-primary { background: #3b82f6; color: #fff; border: none; padding: 10px 20px; border-radius: 8px; cursor: pointer; font-size: 0.95rem; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-secondary { background: #f3f4f6; color: #374151; border: none; padding: 10px 20px; border-radius: 8px; cursor: pointer; font-size: 0.95rem; }
.btn-submit { background: #22c55e; color: #fff; border: none; padding: 10px 24px; border-radius: 8px; cursor: pointer; font-size: 0.95rem; font-weight: 600; }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-sm { background: #f3f4f6; border: none; padding: 5px 12px; border-radius: 6px; cursor: pointer; font-size: 0.83rem; }

/* 结果弹窗 */
.result-modal { text-align: center; }
.result-icon { font-size: 3.5rem; margin-bottom: 12px; }
.result-modal h2 { font-size: 1.5rem; margin-bottom: 16px; }
.result-score { margin: 16px 0; }
.result-score-num { font-size: 3rem; font-weight: 700; color: #3b82f6; }
.result-score-unit { font-size: 1.2rem; color: #666; }
.result-desc { color: #666; margin-bottom: 24px; line-height: 1.6; }
.result-actions { display: flex; justify-content: center; gap: 12px; }

/* 详情弹窗 */
.detail-meta { color: #666; font-size: 0.9rem; margin-bottom: 16px; display: flex; gap: 20px; }
.detail-answers { border-top: 1px solid #e5e7eb; padding-top: 16px; }
.detail-answer-item { padding: 8px 0; border-bottom: 1px solid #f3f4f6; }
.answer-key { font-weight: 500; color: #374151; }
.answer-val { color: #555; }
</style>

