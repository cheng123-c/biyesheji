<template>
  <div class="admin-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>🛠️ 系统管理后台</h1>
      <p>管理用户、查看系统统计与健康监控</p>
    </div>

    <!-- Tab 导航 -->
    <div class="tab-nav">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        {{ tab.label }}
      </button>
    </div>

    <!-- ============ 系统概览 TAB ============ -->
    <div v-if="activeTab === 'overview'" class="tab-content">
      <!-- 统计卡片 -->
      <div v-if="statsLoading" class="loading-box">加载统计数据...</div>
      <div v-else>
        <div class="stat-cards">
          <div class="stat-card card-users">
            <div class="stat-icon">👥</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalActiveUsers ?? '-' }}</div>
              <div class="stat-label">活跃用户总数</div>
            </div>
          </div>
          <div class="stat-card card-assessments">
            <div class="stat-icon">🔬</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayAssessments ?? '-' }}</div>
              <div class="stat-label">今日评测次数</div>
            </div>
          </div>
          <div class="stat-card card-risk">
            <div class="stat-icon">⚠️</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.recentHighRiskCount ?? '-' }}</div>
              <div class="stat-label">近7天高风险报告</div>
            </div>
          </div>
          <div class="stat-card" :class="stats.systemStatus === 'RUNNING' ? 'card-healthy' : 'card-down'">
            <div class="stat-icon">{{ stats.systemStatus === 'RUNNING' ? '✅' : '❌' }}</div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.systemStatus === 'RUNNING' ? '正常' : '异常' }}</div>
              <div class="stat-label">系统状态</div>
            </div>
          </div>
        </div>

        <!-- 风险分布 -->
        <div class="section-card">
          <h2>风险等级分布（今日）</h2>
          <div v-if="stats.riskDistribution && stats.riskDistribution.length > 0" class="risk-dist">
            <div
              v-for="item in stats.riskDistribution"
              :key="item.riskLevel"
              class="risk-bar-item"
            >
              <div class="risk-label">
                <span class="risk-tag" :class="getRiskClass(item.riskLevel)">
                  {{ getRiskLabel(item.riskLevel) }}
                </span>
              </div>
              <div class="risk-bar-wrap">
                <div
                  class="risk-bar"
                  :class="getRiskClass(item.riskLevel)"
                  :style="{ width: getRiskPct(item.count) + '%' }"
                ></div>
              </div>
              <div class="risk-count">{{ item.count }} 人</div>
            </div>
          </div>
          <div v-else class="empty-hint">今日暂无评测数据</div>
        </div>

        <!-- 系统健康状态 -->
        <div class="section-card">
          <h2>系统健康检查</h2>
          <div v-if="healthLoading" class="loading-hint">检查中...</div>
          <div v-else class="health-items">
            <div class="health-item" v-for="(val, key) in healthStatus" :key="key">
              <span class="health-key">{{ healthKeyLabel(key) }}</span>
              <span class="health-val" :class="healthClass(val)">
                {{ healthValLabel(val) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ============ 用户管理 TAB ============ -->
    <div v-if="activeTab === 'users'" class="tab-content">
      <div class="section-card">
        <div class="section-top">
          <h2>用户列表</h2>
          <button class="btn btn-refresh" @click="loadUsers">🔄 刷新</button>
        </div>

        <div v-if="usersLoading" class="loading-box">加载用户数据...</div>
        <div v-else>
          <!-- 用户表格 -->
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>真实姓名</th>
                  <th>邮箱</th>
                  <th>手机号</th>
                  <th>状态</th>
                  <th>注册时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in userList" :key="user.id">
                  <td class="td-id">{{ user.id }}</td>
                  <td class="td-username">{{ user.username }}</td>
                  <td>{{ user.realName || '-' }}</td>
                  <td>{{ user.email || '-' }}</td>
                  <td>{{ user.phone || '-' }}</td>
                  <td>
                    <span class="status-badge" :class="statusClass(user.status)">
                      {{ statusLabel(user.status) }}
                    </span>
                  </td>
                  <td>{{ formatDate(user.createdAt) }}</td>
                  <td class="td-actions">
                    <button
                      v-if="user.status !== 'BANNED'"
                      class="action-btn btn-ban"
                      @click="handleBanUser(user)"
                      title="禁用用户"
                    >禁用</button>
                    <button
                      v-else
                      class="action-btn btn-activate"
                      @click="handleActivateUser(user)"
                      title="启用用户"
                    >启用</button>
                    <button
                      class="action-btn btn-delete"
                      @click="handleDeleteUser(user)"
                      title="删除用户"
                    >删除</button>
                  </td>
                </tr>
                <tr v-if="userList.length === 0">
                  <td colspan="8" class="empty-cell">暂无用户数据</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 分页 -->
          <div class="pagination" v-if="userPageInfo.pages > 1">
            <button
              class="page-btn"
              :disabled="userPageInfo.pageNum <= 1"
              @click="changePage(userPageInfo.pageNum - 1)"
            >上一页</button>
            <span class="page-info">
              第 {{ userPageInfo.pageNum }} / {{ userPageInfo.pages }} 页，共 {{ userPageInfo.total }} 条
            </span>
            <button
              class="page-btn"
              :disabled="userPageInfo.pageNum >= userPageInfo.pages"
              @click="changePage(userPageInfo.pageNum + 1)"
            >下一页</button>
          </div>
          <div class="total-info" v-else-if="userPageInfo.total > 0">
            共 {{ userPageInfo.total }} 条记录
          </div>
        </div>
      </div>
    </div>

    <!-- ============ 高风险报告 TAB ============ -->
    <div v-if="activeTab === 'risks'" class="tab-content">
      <div class="section-card">
        <div class="section-top">
          <h2>高风险评测报告（近 {{ riskDays }} 天）</h2>
          <div class="right-controls">
            <select v-model="riskDays" @change="loadHighRiskReports" class="day-select">
              <option :value="7">近7天</option>
              <option :value="30">近30天</option>
              <option :value="90">近90天</option>
            </select>
            <button class="btn btn-refresh" @click="loadHighRiskReports">🔄 刷新</button>
          </div>
        </div>

        <div v-if="risksLoading" class="loading-box">加载高风险报告...</div>
        <div v-else>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>报告ID</th>
                  <th>用户ID</th>
                  <th>风险等级</th>
                  <th>综合评分</th>
                  <th>评测摘要</th>
                  <th>评测时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="report in highRiskReports" :key="report.id">
                  <td>{{ report.id }}</td>
                  <td>{{ report.userId }}</td>
                  <td>
                    <span class="risk-tag" :class="getRiskClass(report.riskLevel)">
                      {{ getRiskLabel(report.riskLevel) }}
                    </span>
                  </td>
                  <td>{{ report.overallScore ?? '-' }}</td>
                  <td class="td-summary">{{ report.summary || '-' }}</td>
                  <td>{{ formatDate(report.assessmentDate) }}</td>
                </tr>
                <tr v-if="highRiskReports.length === 0">
                  <td colspan="6" class="empty-cell">近期暂无高风险评测报告</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- 提示信息 -->
    <div v-if="toastMsg" class="toast" :class="toastType">{{ toastMsg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import {
  getStatisticsOverview,
  getAdminUserList,
  updateUserStatus,
  deleteAdminUser,
  getHighRiskAssessments,
  getSystemHealth
} from '@/services/adminApi'

// ============ Tab 配置 ============
const tabs = [
  { key: 'overview', label: '系统概览', icon: '📊' },
  { key: 'users', label: '用户管理', icon: '👥' },
  { key: 'risks', label: '高风险报告', icon: '⚠️' }
]
const activeTab = ref('overview')

// ============ 系统概览 ============
const stats = ref({})
const statsLoading = ref(false)
const healthStatus = ref({})
const healthLoading = ref(false)

const loadStats = async () => {
  statsLoading.value = true
  try {
    const res = await getStatisticsOverview()
    stats.value = res.data || {}
  } catch (e) {
    showToast('加载统计数据失败', 'error')
  } finally {
    statsLoading.value = false
  }
}

const loadHealth = async () => {
  healthLoading.value = true
  try {
    const res = await getSystemHealth()
    const raw = res.data || {}
    // 只展示关键字段
    healthStatus.value = {
      status: raw.status,
      database: raw.database,
      aiService: raw.aiService
    }
  } catch (e) {
    healthStatus.value = { status: 'UNKNOWN' }
  } finally {
    healthLoading.value = false
  }
}

// 风险分布中最大值（用于计算百分比）
const maxRiskCount = computed(() => {
  if (!stats.value.riskDistribution) return 1
  return Math.max(...stats.value.riskDistribution.map(i => i.count), 1)
})

const getRiskPct = (count) => Math.round((count / maxRiskCount.value) * 100)

// ============ 用户管理 ============
const userList = ref([])
const usersLoading = ref(false)
const userPageInfo = ref({ pageNum: 1, pageSize: 10, total: 0, pages: 0 })

const loadUsers = async (page = 1) => {
  usersLoading.value = true
  try {
    const res = await getAdminUserList(page, 10)
    const data = res.data || {}
    userList.value = data.list || []
    userPageInfo.value = {
      pageNum: data.pageNum || 1,
      pageSize: data.pageSize || 10,
      total: data.total || 0,
      pages: data.pages || 0
    }
  } catch (e) {
    showToast('加载用户列表失败', 'error')
  } finally {
    usersLoading.value = false
  }
}

const changePage = (page) => {
  loadUsers(page)
}

const handleBanUser = async (user) => {
  if (!confirm(`确认禁用用户【${user.username}】？`)) return
  try {
    await updateUserStatus(user.id, 'BANNED')
    showToast('用户已禁用', 'success')
    loadUsers(userPageInfo.value.pageNum)
  } catch {
    showToast('操作失败', 'error')
  }
}

const handleActivateUser = async (user) => {
  if (!confirm(`确认启用用户【${user.username}】？`)) return
  try {
    await updateUserStatus(user.id, 'ACTIVE')
    showToast('用户已启用', 'success')
    loadUsers(userPageInfo.value.pageNum)
  } catch {
    showToast('操作失败', 'error')
  }
}

const handleDeleteUser = async (user) => {
  if (!confirm(`确认删除用户【${user.username}】？此操作不可撤销！`)) return
  try {
    await deleteAdminUser(user.id)
    showToast('用户已删除', 'success')
    loadUsers(userPageInfo.value.pageNum)
  } catch (e) {
    const msg = e.response?.data?.message || '删除失败'
    showToast(msg, 'error')
  }
}

// ============ 高风险报告 ============
const highRiskReports = ref([])
const risksLoading = ref(false)
const riskDays = ref(30)

const loadHighRiskReports = async () => {
  risksLoading.value = true
  try {
    const res = await getHighRiskAssessments(riskDays.value)
    highRiskReports.value = res.data || []
  } catch {
    showToast('加载高风险报告失败', 'error')
  } finally {
    risksLoading.value = false
  }
}

// ============ Toast 提示 ============
const toastMsg = ref('')
const toastType = ref('success')
let toastTimer = null

const showToast = (msg, type = 'success') => {
  toastMsg.value = msg
  toastType.value = type
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toastMsg.value = '' }, 2500)
}

// ============ 工具函数 ============
const getRiskClass = (level) => {
  const map = { LOW: 'risk-low', MEDIUM: 'risk-medium', HIGH: 'risk-high', CRITICAL: 'risk-critical' }
  return map[level] || 'risk-low'
}

const getRiskLabel = (level) => {
  const map = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '极高风险' }
  return map[level] || level
}

const statusClass = (status) => {
  const map = { ACTIVE: 'status-active', INACTIVE: 'status-inactive', BANNED: 'status-banned' }
  return map[status] || ''
}

const statusLabel = (status) => {
  const map = { ACTIVE: '活跃', INACTIVE: '未激活', BANNED: '已禁用' }
  return map[status] || status
}

const healthKeyLabel = (key) => {
  const map = { status: '系统状态', database: '数据库', aiService: 'AI 服务', timestamp: '检查时间' }
  return map[key] || key
}

const healthValLabel = (val) => {
  if (val === 'UP') return '正常'
  if (val === 'DOWN') return '异常'
  if (val === 'RUNNING') return '运行中'
  if (val === 'UNKNOWN') return '未知'
  if (val === 'DEGRADED') return '降级'
  return val
}

const healthClass = (val) => {
  if (val === 'UP' || val === 'RUNNING') return 'health-ok'
  if (val === 'DOWN') return 'health-down'
  return 'health-warn'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}

// ============ 初始化 ============
onMounted(async () => {
  await Promise.all([
    loadStats(),
    loadHealth(),
    loadUsers(),
    loadHighRiskReports()
  ])
})
</script>

<style scoped>
.admin-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
  position: relative;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 26px;
  color: #333;
  margin: 0 0 6px 0;
}

.page-header p {
  color: #888;
  margin: 0;
  font-size: 14px;
}

/* Tab 导航 */
.tab-nav {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  border-bottom: 2px solid #eee;
  padding-bottom: 0;
}

.tab-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #888;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s;
  border-radius: 4px 4px 0 0;
}

.tab-btn:hover { color: #667eea; background: #f5f7ff; }

.tab-btn.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: #f5f7ff;
  font-weight: 600;
}

.tab-icon { font-size: 16px; }

/* 内容区 */
.tab-content { }

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  border-radius: 12px;
  background: white;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.card-users { border-left: 4px solid #667eea; }
.card-assessments { border-left: 4px solid #00b894; }
.card-risk { border-left: 4px solid #e17055; }
.card-healthy { border-left: 4px solid #00b894; }
.card-down { border-left: 4px solid #e74c3c; }

.stat-icon { font-size: 36px; }

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1.1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #888;
}

/* Section 卡片 */
.section-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

.section-card h2 {
  font-size: 17px;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.section-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.right-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 风险分布条 */
.risk-dist { margin-top: 16px; display: flex; flex-direction: column; gap: 14px; }

.risk-bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.risk-label { min-width: 80px; }

.risk-bar-wrap {
  flex: 1;
  background: #f0f0f0;
  border-radius: 6px;
  height: 12px;
  overflow: hidden;
}

.risk-bar {
  height: 100%;
  border-radius: 6px;
  transition: width 0.6s ease;
}

.risk-low.risk-bar { background: #27ae60; }
.risk-medium.risk-bar { background: #f39c12; }
.risk-high.risk-bar { background: #e74c3c; }
.risk-critical.risk-bar { background: #8e44ad; }

.risk-count { min-width: 50px; text-align: right; font-size: 13px; color: #555; }

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
.risk-critical { background: #e8d4f8; color: #6c1990; }

/* 健康状态 */
.health-items { display: flex; flex-direction: column; gap: 10px; margin-top: 16px; }

.health-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.health-key { font-size: 14px; color: #555; font-weight: 500; }

.health-val {
  font-size: 13px;
  font-weight: 600;
  padding: 3px 12px;
  border-radius: 10px;
}

.health-ok { background: #d4edda; color: #155724; }
.health-down { background: #f8d7da; color: #721c24; }
.health-warn { background: #fff3cd; color: #856404; }

/* 表格 */
.table-wrap {
  overflow-x: auto;
  margin-top: 16px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.data-table th {
  padding: 10px 12px;
  background: #f5f7ff;
  color: #555;
  font-weight: 600;
  text-align: left;
  border-bottom: 2px solid #e8ecff;
  white-space: nowrap;
}

.data-table td {
  padding: 11px 12px;
  border-bottom: 1px solid #f0f0f0;
  color: #333;
}

.data-table tr:hover td { background: #f9faff; }

.td-id { color: #888; font-size: 12px; }
.td-username { font-weight: 600; color: #2c3e50; }
.td-summary { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #666; }
.td-actions { display: flex; gap: 6px; white-space: nowrap; }

/* 状态标签 */
.status-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.status-active { background: #d4edda; color: #155724; }
.status-inactive { background: #fff3cd; color: #856404; }
.status-banned { background: #f8d7da; color: #721c24; }

/* 操作按钮 */
.action-btn {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: opacity 0.2s;
}

.action-btn:hover { opacity: 0.8; }

.btn-ban { background: #fff3cd; color: #856404; }
.btn-activate { background: #d4edda; color: #155724; }
.btn-delete { background: #f8d7da; color: #721c24; }

/* 空单元格 */
.empty-cell {
  text-align: center;
  color: #bbb;
  padding: 30px;
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 20px;
}

.page-btn {
  padding: 7px 18px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) { border-color: #667eea; color: #667eea; }
.page-btn:disabled { color: #bbb; cursor: not-allowed; }

.page-info { font-size: 13px; color: #666; }
.total-info { text-align: center; margin-top: 16px; font-size: 13px; color: #888; }

/* 刷新按钮 */
.btn-refresh {
  padding: 7px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-refresh:hover { border-color: #667eea; color: #667eea; }

/* 天数选择 */
.day-select {
  padding: 7px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}

/* 加载/提示 */
.loading-box { text-align: center; padding: 40px; color: #888; font-size: 14px; }
.loading-hint { color: #aaa; font-size: 13px; margin-top: 16px; }
.empty-hint { color: #aaa; font-size: 13px; margin-top: 16px; text-align: center; padding: 20px; }

/* Toast */
.toast {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  z-index: 9999;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
  animation: slideUp 0.3s ease;
}

.toast.success { background: #27ae60; color: white; }
.toast.error { background: #e74c3c; color: white; }

@keyframes slideUp {
  from { opacity: 0; transform: translateX(-50%) translateY(20px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}

/* 响应式 */
@media (max-width: 1024px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .admin-container { padding: 12px; }
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
  .tab-btn { padding: 8px 12px; font-size: 13px; }
  .data-table { font-size: 12px; }
  .data-table th, .data-table td { padding: 8px; }
}

@media (max-width: 480px) {
  .stat-cards { grid-template-columns: 1fr 1fr; }
}
</style>

