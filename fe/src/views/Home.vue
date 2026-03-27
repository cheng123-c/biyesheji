<template>
  <div class="home-container">
    <!-- Hero 部分（已登录时展示欢迎信息） -->
    <section class="hero">
      <div class="hero-content">
        <h1>{{ greeting }}，{{ displayName }} 👋</h1>
        <p class="subtitle">欢迎使用健康评测系统</p>
        <p class="description">
          基于 DeepSeek AI，为您提供个性化健康评测、数据分析和干预建议
        </p>

        <div class="hero-actions">
          <router-link to="/assessment" class="btn btn-primary">🧠 发起评测</router-link>
          <router-link to="/health-data" class="btn btn-secondary">📊 健康数据</router-link>
        </div>
      </div>

      <div class="hero-stats">
        <div class="hero-stat">
          <div class="stat-value" :class="getRiskLevelClass(latestAssessment?.riskLevel)">
            {{ latestAssessment?.overallScore != null ? latestAssessment.overallScore.toFixed(1) : '--' }}
          </div>
          <div class="stat-label">健康评分</div>
        </div>
        <div class="hero-stat">
          <div class="stat-value risk-badge" :class="getRiskLevelClass(latestAssessment?.riskLevel)">
            {{ getRiskLevelLabel(latestAssessment?.riskLevel) }}
          </div>
          <div class="stat-label">风险等级</div>
        </div>
        <div class="hero-stat">
          <div class="stat-value">{{ overview.assessmentCountThisYear ?? 0 }}</div>
          <div class="stat-label">今年评测次数</div>
        </div>
      </div>
    </section>

    <!-- 快速操作卡片 -->
    <section class="quick-actions">
      <h2 class="section-title">📌 快速入口</h2>
      <div class="action-grid">
        <router-link to="/health-data" class="action-card">
          <div class="action-icon">📊</div>
          <h3>健康数据</h3>
          <p>录入并查看健康指标趋势</p>
          <span class="action-count" v-if="overview.dataTypeCount">{{ overview.dataTypeCount }} 种数据类型</span>
        </router-link>

        <router-link to="/assessment" class="action-card accent">
          <div class="action-icon">🤖</div>
          <h3>AI 评测</h3>
          <p>基于健康数据生成评测报告</p>
          <span class="action-count" v-if="latestAssessment?.assessmentDate">
            最近: {{ formatDate(latestAssessment.assessmentDate) }}
          </span>
        </router-link>

        <router-link to="/medical-records" class="action-card">
          <div class="action-icon">🏥</div>
          <h3>医疗记录</h3>
          <p>管理就诊记录和体检报告</p>
          <span class="action-count" v-if="overview.medicalRecordCount">{{ overview.medicalRecordCount }} 条记录</span>
        </router-link>

        <router-link to="/suggestions" class="action-card">
          <div class="action-icon">💡</div>
          <h3>健康建议</h3>
          <p>查看个性化健康改善建议</p>
          <span class="action-count unread" v-if="overview.unreadSuggestions > 0">
            {{ overview.unreadSuggestions }} 条未读
          </span>
        </router-link>

        <router-link to="/interventions" class="action-card">
          <div class="action-icon">🏃</div>
          <h3>干预方案</h3>
          <p>制定并追踪健康干预计划</p>
          <span class="action-count active" v-if="overview.activePlans > 0">
            {{ overview.activePlans }} 个进行中
          </span>
        </router-link>

        <router-link to="/notifications" class="action-card">
          <div class="action-icon">🔔</div>
          <h3>系统通知</h3>
          <p>查看健康预警和系统消息</p>
          <span class="action-count unread" v-if="overview.unreadNotifications > 0">
            {{ overview.unreadNotifications }} 条未读
          </span>
        </router-link>
      </div>
    </section>

    <!-- 最新评测摘要 -->
    <section class="latest-assessment-section" v-if="latestAssessment">
      <h2 class="section-title">📋 最新评测摘要</h2>
      <div class="assessment-card" :class="getRiskLevelClass(latestAssessment.riskLevel)">
        <div class="assessment-header">
          <div class="assessment-score">
            <span class="score-number">{{ latestAssessment.overallScore?.toFixed(1) }}</span>
            <span class="score-unit">分</span>
          </div>
          <div class="assessment-meta">
            <div class="risk-level-badge" :class="'risk-' + latestAssessment.riskLevel?.toLowerCase()">
              {{ getRiskLevelLabel(latestAssessment.riskLevel) }}
            </div>
            <div class="assessment-date">{{ formatDate(latestAssessment.assessmentDate) }}</div>
          </div>
        </div>

        <div class="assessment-summary" v-if="latestAssessment.summary">
          {{ latestAssessment.summary }}
        </div>

        <div class="assessment-actions">
          <router-link :to="`/assessment`" class="btn-sm">查看完整报告 →</router-link>
          <router-link to="/assessment" class="btn-sm btn-outline">重新评测</router-link>
        </div>
      </div>
    </section>

    <!-- 无评测引导 -->
    <section class="no-assessment-section" v-else-if="!overviewLoading">
      <div class="guide-card">
        <div class="guide-icon">🚀</div>
        <h3>开始您的第一次健康评测</h3>
        <p>请先上传至少一条健康数据，然后发起 AI 评测，获得个性化健康报告</p>
        <div class="guide-steps">
          <div class="guide-step">
            <span class="step-num">1</span>
            <router-link to="/health-data">上传健康数据</router-link>
          </div>
          <span>→</span>
          <div class="guide-step">
            <span class="step-num">2</span>
            <router-link to="/assessment">发起 AI 评测</router-link>
          </div>
          <span>→</span>
          <div class="guide-step">
            <span class="step-num">3</span>
            <span>获得健康报告</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新健康指标 -->
    <section class="health-metrics-section" v-if="latestHealthData.length > 0">
      <h2 class="section-title">💗 最新健康指标</h2>
      <div class="metrics-grid">
        <div
          v-for="item in latestHealthData"
          :key="item.id"
          class="metric-card"
          :class="getMetricStatus(item)"
        >
          <div class="metric-icon">{{ getDataTypeIcon(item.dataType) }}</div>
          <div class="metric-info">
            <div class="metric-name">{{ getDataTypeLabel(item.dataType) }}</div>
            <div class="metric-value">
              {{ item.dataValue }}
              <span class="metric-unit">{{ item.unit }}</span>
            </div>
            <div class="metric-time">{{ formatDateTime(item.collectedAt) }}</div>
          </div>
          <div class="metric-status-dot" :class="getMetricStatus(item)"></div>
        </div>
      </div>
    </section>

    <!-- 系统状态 -->
    <section class="system-status-section">
      <div class="status-row">
        <div class="status-item">
          <div class="status-dot" :class="backendStatus === 'UP' ? 'online' : 'offline'"></div>
          <span>后端服务：{{ backendStatus === 'UP' ? '正常' : backendStatus === null ? '连接中...' : '异常' }}</span>
        </div>
        <div class="status-item">
          <div class="status-dot online"></div>
          <span>数据库：正常</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import apiClient from '@/services/api'

const authStore = useAuthStore()

// 状态
const overviewLoading = ref(true)
const backendStatus = ref(null)
const overview = ref({})
const latestAssessment = ref(null)
const latestHealthData = ref([])

// 计算属性
const currentUser = computed(() => authStore.user)
const displayName = computed(() => {
  const user = authStore.user
  return user?.realName || user?.username || '用户'
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 加载健康总览数据
const loadOverview = async () => {
  try {
    const res = await apiClient.get('/v1/health/overview')
    const data = res.data || res
    overview.value = data
    latestAssessment.value = data.latestAssessment || null
    latestHealthData.value = data.latestHealthData || []
  } catch (e) {
    console.warn('加载健康总览失败:', e.message)
  } finally {
    overviewLoading.value = false
  }
}

// 检查后端状态
const checkBackendStatus = async () => {
  try {
    const res = await apiClient.get('/v1/health/status')
    const data = res.data || res
    backendStatus.value = data?.status || 'UP'
  } catch (e) {
    backendStatus.value = 'DOWN'
  }
}

onMounted(() => {
  loadOverview()
  checkBackendStatus()
})

// 风险等级相关辅助函数
const getRiskLevelLabel = (level) => {
  const labels = {
    'LOW': '低风险',
    'MEDIUM': '中等风险',
    'HIGH': '高风险',
    'CRITICAL': '极高风险'
  }
  return labels[level] || (level ? level : '暂无')
}

const getRiskLevelClass = (level) => {
  const classes = {
    'LOW': 'level-low',
    'MEDIUM': 'level-medium',
    'HIGH': 'level-high',
    'CRITICAL': 'level-critical'
  }
  return classes[level] || ''
}

// 健康数据相关辅助函数
const getDataTypeLabel = (type) => {
  const labels = {
    'heart_rate': '心率',
    'blood_pressure_systolic': '收缩压',
    'blood_pressure_diastolic': '舒张压',
    'blood_glucose': '血糖',
    'blood_oxygen': '血氧',
    'body_temperature': '体温',
    'weight': '体重',
    'height': '身高',
    'bmi': 'BMI',
    'steps': '步数',
    'sleep_duration': '睡眠时长'
  }
  return labels[type] || type
}

const getDataTypeIcon = (type) => {
  const icons = {
    'heart_rate': '❤️',
    'blood_pressure_systolic': '🩸',
    'blood_pressure_diastolic': '🩸',
    'blood_glucose': '💉',
    'blood_oxygen': '🫁',
    'body_temperature': '🌡️',
    'weight': '⚖️',
    'height': '📏',
    'bmi': '📐',
    'steps': '👟',
    'sleep_duration': '😴'
  }
  return icons[type] || '📊'
}

const getMetricStatus = (item) => {
  const type = item.dataType
  const val = parseFloat(item.dataValue)

  const ranges = {
    'heart_rate': { min: 60, max: 100 },
    'blood_pressure_systolic': { min: 90, max: 130 },
    'blood_pressure_diastolic': { min: 60, max: 85 },
    'blood_glucose': { min: 3.9, max: 6.1 },
    'blood_oxygen': { min: 95, max: 100 },
    'body_temperature': { min: 36.0, max: 37.5 }
  }

  if (ranges[type]) {
    const r = ranges[type]
    if (val < r.min || val > r.max) return 'status-warning'
  }
  return 'status-normal'
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '--'
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (dt) => {
  if (!dt) return '--'
  const d = new Date(dt)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.home-container {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* Hero */
.hero {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 30px;
  align-items: center;
  padding: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
}

.hero-content h1 {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.description {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 25px;
  line-height: 1.5;
}

.hero-actions {
  display: flex;
  gap: 12px;
}

.btn {
  display: inline-block;
  padding: 10px 24px;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
  border: none;
  cursor: pointer;
}

.btn-primary {
  background: white;
  color: #667eea;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.2);
}

.btn-secondary {
  background: transparent;
  color: white;
  border: 2px solid rgba(255,255,255,0.6);
}

.btn-secondary:hover {
  background: rgba(255,255,255,0.15);
}

.hero-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
  min-width: 120px;
}

.hero-stat {
  background: rgba(255,255,255,0.15);
  border-radius: 8px;
  padding: 12px 16px;
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 4px;
}

/* 风险等级颜色 */
.level-low { color: #4caf50; }
.level-medium { color: #ff9800; }
.level-high { color: #f44336; }
.level-critical { color: #b71c1c; }

/* 区块通用 */
.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
  font-weight: 600;
}

/* 快速入口网格 */
.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 15px;
}

.action-card {
  display: block;
  padding: 20px;
  background: white;
  border: 1px solid #eee;
  border-radius: 10px;
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  border-color: #667eea;
}

.action-card.accent {
  border-color: #667eea;
  background: linear-gradient(135deg, #f0f3ff, #f9f0ff);
}

.action-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.action-card h3 {
  font-size: 15px;
  color: #333;
  margin-bottom: 6px;
}

.action-card p {
  font-size: 12px;
  color: #888;
  line-height: 1.4;
}

.action-count {
  display: inline-block;
  margin-top: 8px;
  padding: 2px 8px;
  background: #f5f5f5;
  border-radius: 10px;
  font-size: 11px;
  color: #666;
}

.action-count.unread {
  background: #fee;
  color: #e74c3c;
}

.action-count.active {
  background: #e8f5e9;
  color: #2e7d32;
}

/* 最新评测卡片 */
.assessment-card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  border-left: 4px solid #ccc;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.assessment-card.level-low { border-left-color: #4caf50; }
.assessment-card.level-medium { border-left-color: #ff9800; }
.assessment-card.level-high { border-left-color: #f44336; }
.assessment-card.level-critical { border-left-color: #b71c1c; }

.assessment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.assessment-score {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-number {
  font-size: 52px;
  font-weight: bold;
  color: #667eea;
  line-height: 1;
}

.score-unit {
  font-size: 18px;
  color: #999;
}

.assessment-meta {
  text-align: right;
}

.risk-level-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: bold;
  margin-bottom: 6px;
}

.risk-low { background: #e8f5e9; color: #2e7d32; }
.risk-medium { background: #fff3e0; color: #e65100; }
.risk-high { background: #ffebee; color: #b71c1c; }
.risk-critical { background: #fce4ec; color: #880e4f; }

.assessment-date {
  font-size: 12px;
  color: #999;
}

.assessment-summary {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  padding: 12px;
  background: #f8f9ff;
  border-radius: 6px;
  margin-bottom: 16px;
}

.assessment-actions {
  display: flex;
  gap: 10px;
}

.btn-sm {
  display: inline-block;
  padding: 6px 16px;
  border-radius: 4px;
  font-size: 13px;
  text-decoration: none;
  background: #667eea;
  color: white;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-sm:hover {
  background: #5568d3;
}

.btn-sm.btn-outline {
  background: transparent;
  color: #667eea;
  border: 1px solid #667eea;
}

.btn-sm.btn-outline:hover {
  background: #f0f3ff;
}

/* 引导卡片 */
.guide-card {
  text-align: center;
  padding: 50px 40px;
  background: linear-gradient(135deg, #f0f3ff, #f9f0ff);
  border: 2px dashed #c9d0ff;
  border-radius: 12px;
}

.guide-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.guide-card h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 12px;
}

.guide-card p {
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
  line-height: 1.6;
}

.guide-steps {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.guide-step {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-num {
  width: 24px;
  height: 24px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  flex-shrink: 0;
}

.guide-step a {
  color: #667eea;
  font-weight: 600;
  text-decoration: none;
}

/* 健康指标网格 */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #eee;
  position: relative;
  transition: all 0.2s;
}

.metric-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.metric-card.status-warning {
  border-color: #ff9800;
  background: #fffbf0;
}

.metric-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.metric-info {
  flex: 1;
  min-width: 0;
}

.metric-name {
  font-size: 12px;
  color: #888;
}

.metric-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  line-height: 1.3;
}

.metric-unit {
  font-size: 12px;
  color: #aaa;
  font-weight: normal;
}

.metric-time {
  font-size: 11px;
  color: #bbb;
}

.metric-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.metric-status-dot.status-normal { background: #4caf50; }
.metric-status-dot.status-warning { background: #ff9800; }

/* 系统状态 */
.system-status-section {
  padding: 16px 20px;
  background: #f8f9ff;
  border-radius: 8px;
}

.status-row {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-dot.online { background: #4caf50; }
.status-dot.offline { background: #f44336; animation: none; }

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(76,175,80,0.5); }
  50% { box-shadow: 0 0 0 6px rgba(76,175,80,0); }
}

/* 响应式 */
@media (max-width: 768px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 30px 20px;
  }

  .hero-stats {
    flex-direction: row;
    justify-content: space-around;
  }

  .hero-content h1 {
    font-size: 24px;
  }

  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .assessment-header {
    flex-direction: column;
    gap: 12px;
  }

  .assessment-meta {
    text-align: left;
  }
}

@media (max-width: 480px) {
  .action-grid {
    grid-template-columns: 1fr;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .hero-stats {
    flex-direction: column;
    align-items: center;
  }
}
</style>

