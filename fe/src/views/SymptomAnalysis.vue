<template>
  <div class="symptom-analysis-page">
    <div class="page-header">
      <h1>🔬 症状智能分析</h1>
      <p class="subtitle">选择您的症状，AI结合医学知识图谱为您分析潜在健康风险</p>
    </div>

    <div class="main-layout">
      <!-- 左侧：症状选择 -->
      <div class="left-panel">
        <div class="panel-card">
          <h3 class="panel-title">选择症状</h3>
          <p class="panel-hint">请选择您近期出现的症状（可多选，最多10个）</p>

          <!-- 症状搜索 -->
          <div class="symptom-search">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="搜索症状..."
              class="search-input"
              @input="handleSymptomSearch"
            />
          </div>

          <!-- 症状列表 -->
          <div v-if="loadingSymptoms" class="loading-sm">加载中...</div>
          <div v-else class="symptom-list">
            <button
              v-for="symptom in displayedSymptoms"
              :key="symptom.id"
              :class="['symptom-tag', { selected: isSelected(symptom.conceptName) }]"
              @click="toggleSymptom(symptom.conceptName)"
              :disabled="!isSelected(symptom.conceptName) && selectedSymptoms.length >= 10"
            >
              {{ symptom.conceptName }}
            </button>
          </div>

          <!-- 手动输入 -->
          <div class="manual-input-wrap">
            <p class="manual-hint">或手动输入症状描述：</p>
            <div class="manual-input-row">
              <input
                v-model="manualInput"
                type="text"
                placeholder="例如：背痛"
                class="search-input"
                @keyup.enter="addManual"
              />
              <button class="btn-add" @click="addManual">添加</button>
            </div>
          </div>

          <!-- 已选症状 -->
          <div v-if="selectedSymptoms.length > 0" class="selected-symptoms">
            <p class="selected-title">已选症状（{{ selectedSymptoms.length }}/10）：</p>
            <div class="selected-tags">
              <span
                v-for="s in selectedSymptoms"
                :key="s"
                class="selected-tag"
              >
                {{ s }}
                <button class="remove-tag" @click="removeSymptom(s)">✕</button>
              </span>
            </div>
          </div>

          <div class="action-btn-wrap">
            <button
              class="btn-analyze"
              :disabled="selectedSymptoms.length === 0 || analyzing"
              @click="startAnalysis"
            >
              {{ analyzing ? '🔄 分析中...' : '🔬 开始分析' }}
            </button>
            <button
              v-if="selectedSymptoms.length > 0"
              class="btn-clear"
              @click="clearAll"
            >清空</button>
          </div>
        </div>

        <!-- 历史记录 -->
        <div v-if="historyList.length > 0" class="panel-card history-panel">
          <h3 class="panel-title">历史分析</h3>
          <div class="history-items">
            <div
              v-for="item in historyList"
              :key="item.id"
              class="history-item"
              @click="viewHistory(item)"
            >
              <div class="history-symptoms">{{ parseSymptoms(item.inputSymptoms).join('、') }}</div>
              <div class="history-meta">
                <span class="risk-badge" :class="getRiskClass(item.riskScore)">
                  风险 {{ item.riskScore }}
                </span>
                <span class="history-time">{{ formatTime(item.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：分析结果 -->
      <div class="right-panel">
        <!-- 无结果提示 -->
        <div v-if="!result && !analyzing" class="result-placeholder">
          <div class="placeholder-icon">🏥</div>
          <p>选择症状后点击「开始分析」，AI将结合医学知识图谱进行智能推理</p>
          <div class="feature-list">
            <div class="feature-item">
              <span class="feature-icon">🗺️</span>
              <div>
                <strong>知识图谱推理</strong>
                <p>基于医学知识图谱关联症状与疾病</p>
              </div>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🤖</span>
              <div>
                <strong>AI智能分析</strong>
                <p>DeepSeek AI提供专业健康建议</p>
              </div>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚠️</span>
              <div>
                <strong>风险提示</strong>
                <p>量化健康风险，提前预警</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 分析中 -->
        <div v-if="analyzing" class="analyzing-state">
          <div class="analyzing-spinner"></div>
          <p>正在分析症状，请稍候...</p>
          <p class="analyzing-sub">AI正在结合医学知识图谱进行推理</p>
        </div>

        <!-- 结果展示 -->
        <div v-if="result && !analyzing" class="result-card">
          <!-- 风险总览 -->
          <div class="result-header">
            <div class="risk-overview">
              <div class="risk-gauge" :class="getRiskClass(result.riskScore)">
                <div class="risk-score">{{ result.riskScore }}</div>
                <div class="risk-label">风险评分</div>
              </div>
              <div class="risk-info">
                <h2>分析结果</h2>
                <p>基于症状：{{ parseSymptoms(result.inputSymptoms).join('、') }}</p>
                <span class="risk-level-badge" :class="getRiskClass(result.riskScore)">
                  {{ getRiskLabel(result.riskScore) }}
                </span>
              </div>
            </div>
          </div>

          <!-- 可能的疾病 -->
          <div v-if="parseDiseases(result.inferredDiseases).length > 0" class="result-section">
            <h3 class="section-title">🩺 可能相关疾病</h3>
            <div class="disease-list">
              <div
                v-for="d in parseDiseases(result.inferredDiseases)"
                :key="d.name"
                class="disease-item"
              >
                <div class="disease-name">{{ d.name }}</div>
                <div class="disease-score-bar">
                  <div
                    class="disease-score-fill"
                    :style="{ width: d.score + '%' }"
                    :class="getScoreClass(d.score)"
                  ></div>
                </div>
                <div class="disease-score-num">{{ d.score }}%</div>
              </div>
            </div>
            <p class="disclaimer">⚠️ 上述疾病匹配仅供参考，不代表确诊，请结合实际情况咨询医生。</p>
          </div>

          <!-- AI分析建议 -->
          <div class="result-section">
            <h3 class="section-title">🤖 AI健康建议</h3>
            <div class="ai-analysis">{{ result.aiAnalysis }}</div>
          </div>

          <!-- 操作按钮 -->
          <div class="result-actions">
            <button class="btn-primary" @click="goToAssessment">进行完整AI评测</button>
            <button class="btn-secondary" @click="goToContent">查看相关健康知识</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史详情弹窗 -->
    <div v-if="showHistoryDetail" class="modal-overlay" @click.self="showHistoryDetail = false">
      <div class="modal-box history-detail-modal">
        <div class="modal-header">
          <h2>历史分析详情</h2>
          <button class="close-btn" @click="showHistoryDetail = false">✕</button>
        </div>
        <div class="detail-content">
          <p><strong>症状：</strong>{{ parseSymptoms(selectedHistory?.inputSymptoms).join('、') }}</p>
          <p><strong>风险评分：</strong>{{ selectedHistory?.riskScore }}</p>
          <p><strong>时间：</strong>{{ formatTime(selectedHistory?.createdAt) }}</p>
          <div v-if="parseDiseases(selectedHistory?.inferredDiseases).length > 0">
            <strong>相关疾病：</strong>
            <span v-for="d in parseDiseases(selectedHistory?.inferredDiseases)" :key="d.name" class="disease-tag">
              {{ d.name }} ({{ d.score }}%)
            </span>
          </div>
          <div class="ai-analysis-detail">
            <strong>AI分析：</strong>
            <p>{{ selectedHistory?.aiAnalysis }}</p>
          </div>
        </div>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showHistoryDetail = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getSymptoms, inferFromSymptoms, getInferHistory, searchConcepts } from '@/services/knowledgeApi'

const router = useRouter()

const loadingSymptoms = ref(false)
const allSymptoms = ref([])
const filteredSymptoms = ref([])
const searchQuery = ref('')
const selectedSymptoms = ref([])
const manualInput = ref('')
const analyzing = ref(false)
const result = ref(null)
const historyList = ref([])
const showHistoryDetail = ref(false)
const selectedHistory = ref(null)

const displayedSymptoms = computed(() => {
  return searchQuery.value ? filteredSymptoms.value : allSymptoms.value
})

onMounted(async () => {
  await loadSymptoms()
  await loadHistory()
})

async function loadSymptoms() {
  loadingSymptoms.value = true
  try {
    const res = await getSymptoms()
    allSymptoms.value = res.data || res || []
  } catch (e) {
    console.error('加载症状失败:', e)
  } finally {
    loadingSymptoms.value = false
  }
}

async function loadHistory() {
  try {
    const res = await getInferHistory(5)
    historyList.value = res.data || res || []
  } catch (e) {
    // 未登录时忽略
  }
}

let searchTimer = null
function handleSymptomSearch() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    if (!searchQuery.value.trim()) {
      filteredSymptoms.value = []
      return
    }
    try {
      const res = await searchConcepts(searchQuery.value.trim())
      filteredSymptoms.value = (res.data || res || []).filter(c => c.conceptType === 'SYMPTOM')
    } catch (e) {
      filteredSymptoms.value = allSymptoms.value.filter(s =>
        s.conceptName.includes(searchQuery.value)
      )
    }
  }, 300)
}

function isSelected(name) {
  return selectedSymptoms.value.includes(name)
}

function toggleSymptom(name) {
  if (isSelected(name)) {
    removeSymptom(name)
  } else if (selectedSymptoms.value.length < 10) {
    selectedSymptoms.value.push(name)
  }
}

function removeSymptom(name) {
  selectedSymptoms.value = selectedSymptoms.value.filter(s => s !== name)
}

function addManual() {
  const val = manualInput.value.trim()
  if (!val || selectedSymptoms.value.length >= 10) return
  if (!isSelected(val)) {
    selectedSymptoms.value.push(val)
  }
  manualInput.value = ''
}

function clearAll() {
  selectedSymptoms.value = []
  result.value = null
}

async function startAnalysis() {
  if (selectedSymptoms.value.length === 0) return
  analyzing.value = true
  result.value = null
  try {
    const res = await inferFromSymptoms(selectedSymptoms.value)
    result.value = res.data || res
    await loadHistory()
  } catch (e) {
    alert(e?.response?.data?.message || '分析失败，请重试')
  } finally {
    analyzing.value = false
  }
}

function viewHistory(item) {
  selectedHistory.value = item
  showHistoryDetail.value = true
}

function goToAssessment() { router.push('/assessment') }
function goToContent() { router.push('/health-content') }

function parseSymptoms(json) {
  try { return JSON.parse(json) || [] } catch { return [] }
}

function parseDiseases(json) {
  try { return JSON.parse(json) || [] } catch { return [] }
}

function getRiskClass(score) {
  if (!score && score !== 0) return 'risk-low'
  const s = Number(score)
  if (s >= 70) return 'risk-high'
  if (s >= 40) return 'risk-medium'
  return 'risk-low'
}

function getRiskLabel(score) {
  const s = Number(score)
  if (s >= 70) return '⚠️ 高风险'
  if (s >= 40) return '⚡ 中等风险'
  return '✅ 低风险'
}

function getScoreClass(score) {
  if (score >= 70) return 'score-high'
  if (score >= 40) return 'score-medium'
  return 'score-low'
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.symptom-analysis-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 1.8rem; margin-bottom: 6px; }
.subtitle { color: #666; font-size: 0.95rem; }

.main-layout {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 768px) {
  .main-layout { grid-template-columns: 1fr; }
}

/* 面板卡片 */
.panel-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}
.panel-title { font-size: 1.05rem; font-weight: 600; margin-bottom: 8px; }
.panel-hint { color: #666; font-size: 0.85rem; margin-bottom: 14px; }

/* 搜索 */
.symptom-search { margin-bottom: 12px; }
.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1.5px solid #e5e7eb;
  border-radius: 7px;
  font-size: 0.9rem;
  box-sizing: border-box;
  outline: none;
}
.search-input:focus { border-color: #3b82f6; }

/* 症状标签 */
.symptom-list {
  display: flex; flex-wrap: wrap; gap: 8px;
  max-height: 240px; overflow-y: auto;
  margin-bottom: 16px;
  padding: 4px;
}
.symptom-tag {
  padding: 5px 12px;
  border: 1.5px solid #e5e7eb;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.85rem;
  background: #fff;
  color: #374151;
  transition: all 0.15s;
}
.symptom-tag:hover { border-color: #93c5fd; background: #eff6ff; }
.symptom-tag.selected { border-color: #3b82f6; background: #3b82f6; color: #fff; }
.symptom-tag:disabled { opacity: 0.4; cursor: not-allowed; }

/* 手动输入 */
.manual-input-wrap { margin-bottom: 16px; }
.manual-hint { color: #666; font-size: 0.83rem; margin-bottom: 8px; }
.manual-input-row { display: flex; gap: 8px; }
.btn-add {
  padding: 8px 14px;
  background: #6366f1;
  color: #fff;
  border: none;
  border-radius: 7px;
  cursor: pointer;
  font-size: 0.85rem;
  white-space: nowrap;
}

/* 已选症状 */
.selected-symptoms { margin-bottom: 16px; }
.selected-title { font-size: 0.85rem; font-weight: 500; margin-bottom: 8px; }
.selected-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.selected-tag {
  display: flex; align-items: center; gap: 4px;
  padding: 4px 10px;
  background: #eff6ff; border: 1px solid #bfdbfe;
  border-radius: 20px; font-size: 0.83rem; color: #1d4ed8;
}
.remove-tag {
  background: none; border: none; cursor: pointer;
  font-size: 0.75rem; color: #6b7280; padding: 0;
}

/* 操作按钮 */
.action-btn-wrap { display: flex; gap: 10px; }
.btn-analyze {
  flex: 1;
  padding: 10px 0;
  background: #3b82f6; color: #fff;
  border: none; border-radius: 8px;
  cursor: pointer; font-size: 0.95rem; font-weight: 600;
}
.btn-analyze:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-clear {
  padding: 10px 16px;
  background: #f3f4f6; color: #374151;
  border: none; border-radius: 8px; cursor: pointer;
}

/* 历史记录 */
.history-panel .history-items { display: flex; flex-direction: column; gap: 10px; }
.history-item {
  padding: 10px 12px;
  background: #f8fafc; border-radius: 8px;
  cursor: pointer; transition: background 0.15s;
}
.history-item:hover { background: #f0f9ff; }
.history-symptoms { font-size: 0.88rem; color: #374151; margin-bottom: 4px; }
.history-meta { display: flex; justify-content: space-between; align-items: center; }
.history-time { font-size: 0.78rem; color: #999; }
.risk-badge {
  padding: 2px 8px; border-radius: 20px; font-size: 0.75rem; font-weight: 500;
}
.risk-low { background: #dcfce7; color: #15803d; }
.risk-medium { background: #fef3c7; color: #b45309; }
.risk-high { background: #fee2e2; color: #b91c1c; }

/* 右侧结果区域 */
.right-panel { min-height: 400px; }
.loading-sm { color: #999; font-size: 0.9rem; text-align: center; padding: 20px; }

/* 占位提示 */
.result-placeholder {
  background: #fff; border: 1px solid #e5e7eb; border-radius: 12px;
  padding: 40px; text-align: center;
}
.placeholder-icon { font-size: 3rem; margin-bottom: 12px; }
.result-placeholder > p { color: #666; margin-bottom: 24px; }
.feature-list { display: flex; flex-direction: column; gap: 16px; text-align: left; }
.feature-item { display: flex; gap: 12px; align-items: flex-start; }
.feature-icon { font-size: 1.5rem; flex-shrink: 0; }
.feature-item strong { display: block; font-size: 0.95rem; margin-bottom: 2px; }
.feature-item p { color: #666; font-size: 0.85rem; margin: 0; }

/* 分析中 */
.analyzing-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  min-height: 300px; background: #fff; border: 1px solid #e5e7eb;
  border-radius: 12px; padding: 40px;
}
.analyzing-spinner {
  width: 48px; height: 48px;
  border: 4px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16px;
}
@keyframes spin { to { transform: rotate(360deg); } }
.analyzing-sub { color: #999; font-size: 0.85rem; }

/* 结果卡片 */
.result-card {
  background: #fff; border: 1px solid #e5e7eb;
  border-radius: 12px; padding: 24px;
}

/* 风险总览 */
.risk-overview { display: flex; align-items: center; gap: 20px; margin-bottom: 24px; }
.risk-gauge {
  display: flex; flex-direction: column; align-items: center;
  width: 90px; height: 90px; border-radius: 50%;
  justify-content: center;
}
.risk-gauge.risk-low { background: #dcfce7; border: 3px solid #22c55e; }
.risk-gauge.risk-medium { background: #fef3c7; border: 3px solid #f59e0b; }
.risk-gauge.risk-high { background: #fee2e2; border: 3px solid #ef4444; }
.risk-score { font-size: 1.4rem; font-weight: 700; line-height: 1; }
.risk-label { font-size: 0.72rem; color: #666; }
.risk-info h2 { font-size: 1.2rem; margin-bottom: 6px; }
.risk-info p { color: #666; font-size: 0.88rem; margin-bottom: 8px; }
.risk-level-badge {
  display: inline-block; padding: 4px 12px; border-radius: 20px;
  font-size: 0.85rem; font-weight: 500;
}

/* 结果区块 */
.result-section { margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #f3f4f6; }
.result-section:last-of-type { border-bottom: none; }
.section-title { font-size: 1rem; font-weight: 600; margin-bottom: 14px; }

/* 疾病列表 */
.disease-list { display: flex; flex-direction: column; gap: 10px; }
.disease-item { display: flex; align-items: center; gap: 10px; }
.disease-name { width: 100px; font-size: 0.9rem; color: #374151; flex-shrink: 0; }
.disease-score-bar { flex: 1; height: 8px; background: #f3f4f6; border-radius: 4px; overflow: hidden; }
.disease-score-fill { height: 100%; border-radius: 4px; transition: width 0.5s; }
.score-high { background: #ef4444; }
.score-medium { background: #f59e0b; }
.score-low { background: #22c55e; }
.disease-score-num { width: 40px; text-align: right; font-size: 0.85rem; color: #666; }
.disclaimer { color: #f59e0b; font-size: 0.8rem; margin-top: 12px; }

/* AI分析 */
.ai-analysis {
  background: #f8fafc; border-left: 3px solid #3b82f6;
  padding: 16px; border-radius: 0 8px 8px 0;
  font-size: 0.92rem; line-height: 1.8; color: #374151;
  white-space: pre-wrap;
}

/* 操作 */
.result-actions { display: flex; gap: 12px; margin-top: 20px; }
.btn-primary {
  background: #3b82f6; color: #fff; border: none;
  padding: 10px 20px; border-radius: 8px; cursor: pointer; font-size: 0.95rem;
}
.btn-secondary {
  background: #f3f4f6; color: #374151; border: none;
  padding: 10px 20px; border-radius: 8px; cursor: pointer;
}

/* 弹窗 */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.history-detail-modal {
  background: #fff; border-radius: 12px; padding: 24px;
  width: 90%; max-width: 540px; max-height: 80vh; overflow-y: auto;
}
.modal-header { display: flex; justify-content: space-between; margin-bottom: 16px; }
.close-btn { background: none; border: none; font-size: 1.2rem; cursor: pointer; }
.detail-content > p { margin-bottom: 10px; line-height: 1.6; }
.disease-tag {
  display: inline-block; margin: 4px;
  padding: 2px 8px; background: #fee2e2; color: #b91c1c;
  border-radius: 20px; font-size: 0.8rem;
}
.ai-analysis-detail { margin-top: 12px; }
.ai-analysis-detail p { color: #555; font-size: 0.9rem; line-height: 1.7; white-space: pre-wrap; }
.modal-actions { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>

