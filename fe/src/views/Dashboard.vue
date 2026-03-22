<template>
  <div class="dashboard-container">
    <div class="page-header">
      <div class="welcome-text">
        <h1>你好，{{ userName }} 👋</h1>
        <p>{{ todayDate }}，祝您健康每一天</p>
      </div>
      <div class="header-actions">
        <div class="notification-btn" @click="goToNotifications">
          <span class="notif-icon">🔔</span>
          <span v-if="unreadCount > 0" class="notif-badge">{{ unreadCount }}</span>
        </div>
      </div>
    </div>

    <!-- 健康概览卡片 -->
    <section class="overview-section">
      <div class="overview-cards">
        <!-- 健康评分 -->
        <div class="overview-card card-score" @click="$router.push('/assessment')">
          <div class="card-icon-bg">🏆</div>
          <div class="card-content">
            <div class="card-label">最新健康评分</div>
            <div class="card-main-value">
              <span v-if="latestAssessment">{{ latestAssessment.overallScore }}</span>
              <span v-else class="no-data">暂无</span>
            </div>
            <div v-if="latestAssessment" class="card-sub">
              <span class="risk-tag" :class="getRiskClass(latestAssessment.riskLevel)">
                {{ getRiskLabel(latestAssessment.riskLevel) }}
              </span>
            </div>
          </div>
          <div class="card-arrow">›</div>
        </div>

        <!-- 数据类型 -->
        <div class="overview-card card-data" @click="$router.push('/health-data')">
          <div class="card-icon-bg">📊</div>
          <div class="card-content">
            <div class="card-label">已记录指标</div>
            <div class="card-main-value">{{ dataTypeCount }}</div>
            <div class="card-sub">种健康数据类型</div>
          </div>
          <div class="card-arrow">›</div>
        </div>

        <!-- 今年评测次数 -->
        <div class="overview-card card-assessment" @click="$router.push('/assessment')">
          <div class="card-icon-bg">🔬</div>
          <div class="card-content">
            <div class="card-label">今年评测次数</div>
            <div class="card-main-value">{{ assessmentCount }}</div>
            <div class="card-sub">次 AI 健康评测</div>
          </div>
          <div class="card-arrow">›</div>
        </div>

        <!-- 未读通知 -->
        <div class="overview-card card-notif" @click="goToNotifications">
          <div class="card-icon-bg">🔔</div>
          <div class="card-content">
            <div class="card-label">未读通知</div>
            <div class="card-main-value">{{ unreadCount }}</div>
            <div class="card-sub">条未读消息</div>
          </div>
          <div class="card-arrow">›</div>
        </div>
      </div>
    </section>

    <!-- 关键健康指标 -->
    <section class="vital-section">
      <div class="section-header">
        <h2>关键健康指标</h2>
        <router-link to="/health-data" class="link-more">查看全部 ›</router-link>
      </div>

      <div v-if="vitalsLoading" class="loading-spinner">加载中...</div>
      <div v-else-if="vitals.length === 0" class="empty-state">
        <p>暂无健康数据</p>
        <router-link to="/health-data" class="btn btn-primary">上传健康数据</router-link>
      </div>
      <div v-else class="vitals-grid">
        <div v-for="item in vitals" :key="item.id" class="vital-card">
          <div class="vital-header">
            <span class="vital-icon">{{ getIcon(item.dataType) }}</span>
            <span class="vital-name">{{ getTypeName(item.dataType) }}</span>
          </div>
          <div class="vital-value">
            {{ item.dataValue }}
            <span class="vital-unit">{{ item.unit }}</span>
          </div>
          <div class="vital-status" :class="getStatus(item)">
            {{ getStatusText(item) }}
          </div>
          <div class="vital-time">{{ formatTime(item.collectedAt) }}</div>
        </div>
      </div>
    </section>

    <!-- 快速操作 -->
    <section class="quick-actions-section">
      <h2>快速操作</h2>
      <div class="quick-actions">
        <router-link to="/health-data" class="quick-action-btn">
          <span class="qa-icon">📤</span>
          <span>上传数据</span>
        </router-link>
        <router-link to="/assessment" class="quick-action-btn">
          <span class="qa-icon">🧠</span>
          <span>AI 评测</span>
        </router-link>
        <router-link to="/profile" class="quick-action-btn">
          <span class="qa-icon">👤</span>
          <span>个人信息</span>
        </router-link>
      </div>
    </section>

    <!-- 最近通知 -->
    <section v-if="recentNotifications.length > 0" class="notifications-section">
      <div class="section-header">
        <h2>最近通知</h2>
        <span class="link-more" @click="goToNotifications">查看全部 ›</span>
      </div>
      <div class="notification-list">
        <div
          v-for="notif in recentNotifications"
          :key="notif.id"
          class="notification-item"
          :class="{ unread: notif.readStatus === 0 }"
        >
          <div class="notif-dot" :class="{ unread: notif.readStatus === 0 }"></div>
          <div class="notif-content">
            <div class="notif-title">{{ notif.title }}</div>
            <div class="notif-text">{{ notif.content }}</div>
            <div class="notif-time">{{ formatTime(notif.createdAt) }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 最新评测摘要（简略） -->
    <section v-if="latestAssessment" class="assessment-summary-section">
      <div class="section-header">
        <h2>最新评测摘要</h2>
        <router-link to="/assessment" class="link-more">查看详情 ›</router-link>
      </div>
      <div class="assessment-summary-card">
        <div class="summary-score">
          <div class="score-ring" :style="scoreStyle">
            <span class="score-num">{{ latestAssessment.overallScore }}</span>
          </div>
          <div class="summary-info">
            <p class="summary-text">{{ latestAssessment.summary }}</p>
            <div class="summary-date">{{ formatDate(latestAssessment.assessmentDate) }}</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getLatestHealthData, getHealthDataStatistics } from '@/services/healthDataApi'
import { getLatestAssessment, getAssessmentCount, getUnreadCount, getUnreadNotifications } from '@/services/assessmentApi'

const router = useRouter()
const authStore = useAuthStore()

// 数据
const latestAssessment = ref(null)
const vitals = ref([])
const dataTypeCount = ref(0)
const assessmentCount = ref(0)
const unreadCount = ref(0)
const recentNotifications = ref([])
const vitalsLoading = ref(true)

// 用户名
const userName = computed(() => authStore.user?.realName || authStore.user?.username || '用户')

// 今日日期
const todayDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
  })
})

// 评分样式
const scoreStyle = computed(() => {
  if (!latestAssessment.value) return {}
  const score = latestAssessment.value.overallScore
  return {
    '--score-pct': `${score * 3.6}deg`
  }
})

onMounted(async () => {
  await Promise.all([
    loadVitals(),
    loadAssessment(),
    loadCounts(),
    loadNotifications()
  ])
})

const loadVitals = async () => {
  vitalsLoading.value = true
  try {
    const response = await getLatestHealthData()
    vitals.value = (response.data || []).slice(0, 6)

    const statsResponse = await getHealthDataStatistics()
    dataTypeCount.value = (statsResponse.data || []).length
  } catch (err) {
    console.error('加载健康指标失败:', err)
  } finally {
    vitalsLoading.value = false
  }
}

const loadAssessment = async () => {
  try {
    const response = await getLatestAssessment()
    latestAssessment.value = response.data
  } catch {
    latestAssessment.value = null
  }
}

const loadCounts = async () => {
  try {
    const [countRes, unreadRes] = await Promise.all([
      getAssessmentCount(),
      getUnreadCount()
    ])
    assessmentCount.value = countRes.data || 0
    unreadCount.value = unreadRes.data || 0
  } catch (err) {
    console.error('加载统计信息失败:', err)
  }
}

const loadNotifications = async () => {
  try {
    const response = await getUnreadNotifications()
    recentNotifications.value = (response.data || []).slice(0, 3)
  } catch {
    recentNotifications.value = []
  }
}

const goToNotifications = () => {
  router.push('/notifications')
}

// 工具函数
const getTypeName = (type) => {
  const names = {
    heart_rate: '心率', blood_pressure_systolic: '收缩压',
    blood_pressure_diastolic: '舒张压', blood_glucose: '血糖',
    blood_oxygen: '血氧', body_temperature: '体温',
    weight: '体重', height: '身高'
  }
  return names[type] || type
}

const getIcon = (type) => {
  const icons = {
    heart_rate: '❤️', blood_pressure_systolic: '🩺', blood_pressure_diastolic: '🩺',
    blood_glucose: '🩸', blood_oxygen: '💨', body_temperature: '🌡️',
    weight: '⚖️', height: '📏'
  }
  return icons[type] || '📊'
}

const normalRanges = {
  heart_rate: { min: 60, max: 100 },
  blood_pressure_systolic: { min: 90, max: 120 },
  blood_pressure_diastolic: { min: 60, max: 80 },
  blood_glucose: { min: 70, max: 100 },
  blood_oxygen: { min: 95, max: 100 },
  body_temperature: { min: 36.0, max: 37.5 },
}

const getStatus = (item) => {
  const range = normalRanges[item.dataType]
  if (!range) return 'status-normal'
  const val = Number(item.dataValue)
  return (val < range.min || val > range.max) ? 'status-abnormal' : 'status-normal'
}

const getStatusText = (item) => {
  const range = normalRanges[item.dataType]
  if (!range) return '—'
  const val = Number(item.dataValue)
  if (val < range.min) return '偏低'
  if (val > range.max) return '偏高'
  return '正常'
}

const getRiskClass = (level) => {
  const classes = { LOW: 'risk-low', MEDIUM: 'risk-medium', HIGH: 'risk-high', CRITICAL: 'risk-critical' }
  return classes[level] || 'risk-low'
}

const getRiskLabel = (level) => {
  const labels = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '极高风险' }
  return labels[level] || level
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-text h1 {
  font-size: 26px;
  color: #333;
  margin: 0 0 4px 0;
}

.welcome-text p {
  color: #888;
  margin: 0;
  font-size: 14px;
}

.notification-btn {
  position: relative;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.notification-btn:hover { background: #f0f0f0; }

.notif-icon { font-size: 24px; }

.notif-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 18px;
  height: 18px;
  background: #e74c3c;
  color: white;
  font-size: 11px;
  font-weight: bold;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* section通用 */
section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

section h2 {
  font-size: 18px;
  color: #333;
  margin: 0 0 18px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.section-header h2 { margin: 0; }

.link-more {
  color: #667eea;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
}

/* 概览卡片 */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.overview-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}

.overview-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.1);
}

.card-score { background: linear-gradient(135deg, #667eea20, #764ba210); border: 1px solid #667eea30; }
.card-data { background: linear-gradient(135deg, #00b89420, #00856010); border: 1px solid #00b89430; }
.card-assessment { background: linear-gradient(135deg, #f093fb20, #f5576c10); border: 1px solid #f093fb30; }
.card-notif { background: linear-gradient(135deg, #4facfe20, #00f2fe10); border: 1px solid #4facfe30; }

.card-icon-bg { font-size: 32px; }

.card-content { flex: 1; }

.card-label {
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
}

.card-main-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1;
  margin-bottom: 4px;
}

.no-data { color: #bbb; font-size: 18px; }

.card-sub { font-size: 12px; color: #888; }

.card-arrow {
  font-size: 20px;
  color: #ccc;
}

/* 风险标签 */
.risk-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.risk-low { background: #d4edda; color: #155724; }
.risk-medium { background: #fff3cd; color: #856404; }
.risk-high { background: #f8d7da; color: #721c24; }
.risk-critical { background: #f5c6cb; color: #491217; }

/* 健康指标 */
.vitals-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 14px;
}

.vital-card {
  padding: 16px;
  background: #f9f9ff;
  border-radius: 10px;
  border: 1px solid #eee;
  transition: transform 0.2s;
}

.vital-card:hover { transform: translateY(-2px); }

.vital-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
}

.vital-icon { font-size: 20px; }

.vital-name {
  font-size: 12px;
  color: #888;
}

.vital-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 6px;
}

.vital-unit {
  font-size: 12px;
  color: #aaa;
  font-weight: normal;
}

.vital-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  display: inline-block;
  margin-bottom: 6px;
}

.status-normal { background: #d4edda; color: #155724; }
.status-abnormal { background: #f8d7da; color: #721c24; }

.vital-time {
  font-size: 11px;
  color: #bbb;
}

/* 快速操作 */
.quick-actions {
  display: flex;
  gap: 16px;
}

.quick-action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 30px;
  border-radius: 10px;
  background: #f8f9ff;
  border: 1px solid #e8ecff;
  text-decoration: none;
  color: #333;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
}

.quick-action-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102,126,234,0.3);
}

.qa-icon { font-size: 28px; }

/* 通知列表 */
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
}

.notification-item.unread {
  background: #f0f7ff;
  border-color: #c3deff;
}

.notif-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ccc;
  margin-top: 5px;
  flex-shrink: 0;
}

.notif-dot.unread { background: #667eea; }

.notif-title {
  font-weight: 600;
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.notif-text {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  line-height: 1.5;
}

.notif-time {
  font-size: 11px;
  color: #bbb;
}

/* 评测摘要 */
.assessment-summary-card {
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 20px;
}

.summary-score {
  display: flex;
  gap: 20px;
  align-items: center;
}

.score-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: conic-gradient(
    #667eea var(--score-pct, 270deg),
    #e8ecff 0deg
  );
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;
}

.score-ring::before {
  content: '';
  position: absolute;
  width: 60px;
  height: 60px;
  background: white;
  border-radius: 50%;
}

.score-num {
  position: relative;
  z-index: 1;
  font-size: 20px;
  font-weight: bold;
  color: #667eea;
}

.summary-text {
  color: #555;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 8px 0;
}

.summary-date {
  color: #aaa;
  font-size: 12px;
}

/* 通用 */
.loading-spinner { text-align: center; padding: 40px; color: #888; }
.empty-state { text-align: center; padding: 30px; color: #aaa; }
.empty-state p { margin-bottom: 16px; }

.btn-primary {
  display: inline-block;
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .overview-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .dashboard-container { padding: 12px; }
  .overview-cards { grid-template-columns: repeat(2, 1fr); }
  .vitals-grid { grid-template-columns: repeat(2, 1fr); }
  .quick-actions { flex-wrap: wrap; }
}

@media (max-width: 480px) {
  .overview-cards { grid-template-columns: 1fr 1fr; }
}
</style>

