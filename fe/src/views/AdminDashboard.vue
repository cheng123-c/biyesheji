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
        <div class="section-card risk-dist-card">
          <h2>风险等级分布（今日）</h2>
          <div class="risk-dist-layout">
            <!-- 饼图 -->
            <div ref="riskPieRef" class="echarts-box-admin" v-show="stats.riskDistribution && stats.riskDistribution.length > 0"></div>
            <!-- 文字列表 -->
            <div class="risk-dist-list">
              <div v-if="stats.riskDistribution && stats.riskDistribution.length > 0">
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
          </div>
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

    <!-- ============ 用户反馈 TAB ============ -->
    <div v-if="activeTab === 'feedback'" class="tab-content">
      <div class="section-card">
        <div class="section-top">
          <h2>用户反馈管理</h2>
          <div class="right-controls">
            <select v-model="feedbackFilter" @change="loadFeedbacks" class="day-select">
              <option value="">全部状态</option>
              <option value="PENDING">待处理</option>
              <option value="PROCESSING">处理中</option>
              <option value="RESOLVED">已解决</option>
              <option value="CLOSED">已关闭</option>
            </select>
            <button class="btn btn-refresh" @click="loadFeedbacks">🔄 刷新</button>
          </div>
        </div>
        <div v-if="feedbackLoading" class="loading-box">加载中...</div>
        <div v-else>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户</th>
                  <th>类型</th>
                  <th>标题</th>
                  <th>状态</th>
                  <th>提交时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="fb in feedbackList" :key="fb.id">
                  <td>{{ fb.id }}</td>
                  <td>{{ fb.username || fb.userId }}</td>
                  <td><span class="status-badge">{{ getFeedbackTypeLabel(fb.feedbackType) }}</span></td>
                  <td>{{ fb.feedbackTitle }}</td>
                  <td>
                    <span class="status-badge" :class="getFeedbackStatusClass(fb.status)">
                      {{ getFeedbackStatusLabel(fb.status) }}
                    </span>
                  </td>
                  <td>{{ formatDate(fb.createdAt) }}</td>
                  <td class="td-actions">
                    <button class="action-btn btn-view" @click="openFeedbackDetail(fb)">查看/回复</button>
                    <button v-if="fb.status === 'PENDING'" class="action-btn" @click="markFeedbackProcessing(fb.id)">标记处理</button>
                  </td>
                </tr>
                <tr v-if="feedbackList.length === 0">
                  <td colspan="7" class="empty-cell">暂无反馈数据</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pagination" v-if="feedbackPageInfo.pages > 1">
            <button :disabled="feedbackPageInfo.pageNum <= 1" class="page-btn" @click="changeFeedbackPage(feedbackPageInfo.pageNum - 1)">上一页</button>
            <span class="page-info">第 {{ feedbackPageInfo.pageNum }} / {{ feedbackPageInfo.pages }} 页</span>
            <button :disabled="feedbackPageInfo.pageNum >= feedbackPageInfo.pages" class="page-btn" @click="changeFeedbackPage(feedbackPageInfo.pageNum + 1)">下一页</button>
          </div>
        </div>
      </div>
    </div>

    <!-- ============ 健康内容 TAB ============ -->
    <div v-if="activeTab === 'content'" class="tab-content">
      <div class="section-card">
        <div class="section-top">
          <h2>健康内容管理</h2>
          <button class="btn btn-primary" @click="openContentForm(null)">+ 新建内容</button>
        </div>
        <div v-if="contentLoading" class="loading-box">加载中...</div>
        <div v-else>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>标题</th>
                  <th>类型</th>
                  <th>作者</th>
                  <th>状态</th>
                  <th>发布时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in contentList" :key="c.id">
                  <td>{{ c.id }}</td>
                  <td>{{ c.contentTitle }}</td>
                  <td>{{ c.contentType }}</td>
                  <td>{{ c.author || '-' }}</td>
                  <td>
                    <span class="status-badge" :class="c.isPublished ? 'status-active' : 'status-banned'">
                      {{ c.isPublished ? '已发布' : '未发布' }}
                    </span>
                  </td>
                  <td>{{ formatDate(c.publishedAt) }}</td>
                  <td class="td-actions">
                    <button class="action-btn btn-view" @click="openContentForm(c)">编辑</button>
                    <button class="action-btn" @click="toggleContentPublish(c)">{{ c.isPublished ? '取消发布' : '发布' }}</button>
                    <button class="action-btn btn-delete" @click="deleteContent(c.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="contentList.length === 0">
                  <td colspan="7" class="empty-cell">暂无内容</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- ============ 问卷管理 TAB ============ -->
    <div v-if="activeTab === 'questionnaire'" class="tab-content">
      <div class="section-card">
        <div class="section-top">
          <h2>问卷管理</h2>
          <button class="btn btn-refresh" @click="loadQuestionnaires">🔄 刷新</button>
        </div>
        <div v-if="questionnaireLoading" class="loading-box">加载中...</div>
        <div v-else>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>标题</th>
                  <th>类型</th>
                  <th>题目数</th>
                  <th>状态</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="q in questionnaireList" :key="q.id">
                  <td>{{ q.id }}</td>
                  <td>{{ q.title }}</td>
                  <td>{{ q.questionnaireType }}</td>
                  <td>{{ parseQCount(q.questions) }}</td>
                  <td>
                    <span class="status-badge" :class="q.isActive ? 'status-active' : 'status-banned'">
                      {{ q.isActive ? '启用' : '禁用' }}
                    </span>
                  </td>
                  <td>{{ formatDate(q.createdAt) }}</td>
                  <td class="td-actions">
                    <button class="action-btn btn-delete" @click="deleteQuestionnaire(q.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="questionnaireList.length === 0">
                  <td colspan="7" class="empty-cell">暂无问卷</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- ============ 知识图谱 TAB ============ -->
    <div v-if="activeTab === 'knowledge'" class="tab-content">
      <div class="section-card">
        <div class="section-top">
          <h2>医学概念管理</h2>
          <button class="btn btn-primary" @click="openConceptForm(null)">+ 新增概念</button>
        </div>
        <div v-if="conceptLoading" class="loading-box">加载中...</div>
        <div v-else>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>概念名称</th>
                  <th>类型</th>
                  <th>ICD10</th>
                  <th>描述</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in conceptList" :key="c.id">
                  <td>{{ c.id }}</td>
                  <td><strong>{{ c.conceptName }}</strong></td>
                  <td>
                    <span class="status-badge" :class="getConceptTypeClass(c.conceptType)">
                      {{ getConceptTypeLabel(c.conceptType) }}
                    </span>
                  </td>
                  <td>{{ c.icd10Code || '-' }}</td>
                  <td class="td-summary">{{ c.description }}</td>
                  <td class="td-actions">
                    <button class="action-btn btn-view" @click="openConceptForm(c)">编辑</button>
                    <button class="action-btn btn-delete" @click="deleteConcept(c.id)">删除</button>
                  </td>
                </tr>
                <tr v-if="conceptList.length === 0">
                  <td colspan="6" class="empty-cell">暂无概念数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- ============ 反馈回复弹窗 ============ -->
    <div v-if="showFeedbackModal" class="modal-overlay" @click.self="showFeedbackModal = false">
      <div class="modal-box-admin">
        <h2>查看/回复反馈</h2>
        <div v-if="currentFeedback" class="feedback-detail">
          <p><strong>用户：</strong>{{ currentFeedback.username || currentFeedback.userId }}</p>
          <p><strong>类型：</strong>{{ getFeedbackTypeLabel(currentFeedback.feedbackType) }}</p>
          <p><strong>标题：</strong>{{ currentFeedback.feedbackTitle }}</p>
          <div class="detail-body-admin">{{ currentFeedback.feedbackContent }}</div>
          <div v-if="currentFeedback.adminReply" class="existing-reply">
            <strong>已有回复：</strong>
            <p>{{ currentFeedback.adminReply }}</p>
          </div>
          <div class="reply-form">
            <label>回复内容：</label>
            <textarea v-model="feedbackReply" rows="4" placeholder="输入回复..." class="form-input-admin"></textarea>
            <label>处理状态：</label>
            <select v-model="feedbackReplyStatus" class="form-input-admin">
              <option value="PROCESSING">处理中</option>
              <option value="RESOLVED">已解决</option>
              <option value="CLOSED">已关闭</option>
            </select>
          </div>
        </div>
        <div class="modal-actions-admin">
          <button class="btn btn-primary" @click="submitFeedbackReply">提交回复</button>
          <button class="btn btn-refresh" @click="showFeedbackModal = false">取消</button>
        </div>
      </div>
    </div>

    <!-- ============ 健康内容编辑弹窗 ============ -->
    <div v-if="showContentModal" class="modal-overlay" @click.self="showContentModal = false">
      <div class="modal-box-admin modal-wide">
        <h2>{{ editingContent?.id ? '编辑内容' : '新建内容' }}</h2>
        <div class="edit-form">
          <div class="form-row">
            <label>标题</label>
            <input v-model="editingContent.contentTitle" type="text" class="form-input-admin" placeholder="内容标题" />
          </div>
          <div class="form-row">
            <label>类型</label>
            <select v-model="editingContent.contentType" class="form-input-admin">
              <option value="article">文章</option>
              <option value="guide">指南</option>
              <option value="video">视频</option>
            </select>
          </div>
          <div class="form-row">
            <label>作者</label>
            <input v-model="editingContent.author" type="text" class="form-input-admin" placeholder="作者" />
          </div>
          <div class="form-row">
            <label>来源</label>
            <input v-model="editingContent.contentSource" type="text" class="form-input-admin" placeholder="内容来源" />
          </div>
          <div class="form-row">
            <label>内容（支持Markdown）</label>
            <textarea v-model="editingContent.contentBody" rows="10" class="form-input-admin" placeholder="内容正文，支持Markdown格式"></textarea>
          </div>
          <div class="form-row">
            <label>发布</label>
            <select v-model="editingContent.isPublished" class="form-input-admin">
              <option :value="1">立即发布</option>
              <option :value="0">存为草稿</option>
            </select>
          </div>
        </div>
        <div class="modal-actions-admin">
          <button class="btn btn-primary" @click="saveContent">保存</button>
          <button class="btn btn-refresh" @click="showContentModal = false">取消</button>
        </div>
      </div>
    </div>

    <!-- ============ 概念编辑弹窗 ============ -->
    <div v-if="showConceptModal" class="modal-overlay" @click.self="showConceptModal = false">
      <div class="modal-box-admin">
        <h2>{{ editingConcept?.id ? '编辑概念' : '新增概念' }}</h2>
        <div class="edit-form">
          <div class="form-row">
            <label>概念名称</label>
            <input v-model="editingConcept.conceptName" type="text" class="form-input-admin" placeholder="如：高血压" />
          </div>
          <div class="form-row">
            <label>概念类型</label>
            <select v-model="editingConcept.conceptType" class="form-input-admin">
              <option value="DISEASE">疾病</option>
              <option value="SYMPTOM">症状</option>
              <option value="INDICATOR">指标</option>
              <option value="TREATMENT">治疗</option>
            </select>
          </div>
          <div class="form-row">
            <label>ICD10编码</label>
            <input v-model="editingConcept.icd10Code" type="text" class="form-input-admin" placeholder="如：I10" />
          </div>
          <div class="form-row">
            <label>描述</label>
            <textarea v-model="editingConcept.description" rows="3" class="form-input-admin" placeholder="概念描述"></textarea>
          </div>
        </div>
        <div class="modal-actions-admin">
          <button class="btn btn-primary" @click="saveConcept">保存</button>
          <button class="btn btn-refresh" @click="showConceptModal = false">取消</button>
        </div>
      </div>
    </div>

    <!-- 提示信息 -->
    <div v-if="toastMsg" class="toast" :class="toastType">{{ toastMsg }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getStatisticsOverview,
  getAdminUserList,
  updateUserStatus,
  deleteAdminUser,
  getHighRiskAssessments,
  getSystemHealth
} from '@/services/adminApi'
import {
  adminGetAllFeedbacks,
  adminReplyFeedback,
  adminUpdateFeedbackStatus
} from '@/services/feedbackApi'
import {
  adminGetAllContent,
  adminCreateContent,
  adminUpdateContent,
  adminDeleteContent,
  adminTogglePublish
} from '@/services/healthContentApi'
import {
  adminGetAll as adminGetAllQuestionnaires,
  adminDelete as adminDeleteQuestionnaire
} from '@/services/questionnaireApi'
import {
  adminGetAllConcepts,
  adminCreateConcept,
  adminUpdateConcept,
  adminDeleteConcept
} from '@/services/knowledgeApi'

// ============ Tab 配置 ============
const tabs = [
  { key: 'overview', label: '系统概览', icon: '📊' },
  { key: 'users', label: '用户管理', icon: '👥' },
  { key: 'risks', label: '高风险报告', icon: '⚠️' },
  { key: 'feedback', label: '用户反馈', icon: '💬' },
  { key: 'content', label: '健康内容', icon: '📚' },
  { key: 'questionnaire', label: '问卷管理', icon: '📋' },
  { key: 'knowledge', label: '知识图谱', icon: '🗺️' }
]
const activeTab = ref('overview')

// 切换 Tab 时懒加载
watch(activeTab, (val) => {
  if (val === 'feedback' && feedbackList.value.length === 0) loadFeedbacks()
  if (val === 'content' && contentList.value.length === 0) loadContents()
  if (val === 'questionnaire' && questionnaireList.value.length === 0) loadQuestionnaires()
  if (val === 'knowledge' && conceptList.value.length === 0) loadConcepts()
})

// ============ 系统概览 ============
const stats = ref({})
const statsLoading = ref(false)
const healthStatus = ref({})
const healthLoading = ref(false)
const riskPieRef = ref(null)
let riskPieChart = null
let riskPieResizeFn = null

const loadStats = async () => {
  statsLoading.value = true
  try {
    const res = await getStatisticsOverview()
    stats.value = res.data || {}
    // 数据加载完毕后渲染饼图
    if (stats.value.riskDistribution?.length > 0) {
      await nextTick()
      initRiskPie()
    }
  } catch (e) {
    showToast('加载统计数据失败', 'error')
  } finally {
    statsLoading.value = false
  }
}

const initRiskPie = () => {
  if (!riskPieRef.value) return
  if (riskPieChart) riskPieChart.dispose()
  riskPieChart = echarts.init(riskPieRef.value)

  const colorMap = { LOW: '#00b894', MEDIUM: '#fdcb6e', HIGH: '#e17055', CRITICAL: '#e74c3c' }
  const labelMap = { LOW: '低风险', MEDIUM: '中风险', HIGH: '高风险', CRITICAL: '极高风险' }
  const data = (stats.value.riskDistribution || []).map(item => ({
    name: labelMap[item.riskLevel] || item.riskLevel,
    value: item.count,
    itemStyle: { color: colorMap[item.riskLevel] || '#aaa' }
  }))

  riskPieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 人 ({d}%)' },
    legend: { bottom: 0, left: 'center', itemWidth: 10, itemHeight: 10, textStyle: { fontSize: 12 } },
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      center: ['50%', '45%'],
      data,
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } }
    }]
  })

  // 移除旧监听再添加新监听，避免重复注册
  if (riskPieResizeFn) window.removeEventListener('resize', riskPieResizeFn)
  riskPieResizeFn = () => riskPieChart?.resize()
  window.addEventListener('resize', riskPieResizeFn)
}

// 组件销毁时释放 ECharts 实例和事件监听
onUnmounted(() => {
  if (riskPieResizeFn) {
    window.removeEventListener('resize', riskPieResizeFn)
    riskPieResizeFn = null
  }
  if (riskPieChart) {
    riskPieChart.dispose()
    riskPieChart = null
  }
})

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

// ============ 用户反馈管理 ============
const feedbackList = ref([])
const feedbackLoading = ref(false)
const feedbackFilter = ref('')
const feedbackPageInfo = ref({ pageNum: 1, pages: 1, total: 0 })
const showFeedbackModal = ref(false)
const currentFeedback = ref(null)
const feedbackReply = ref('')
const feedbackReplyStatus = ref('RESOLVED')

const loadFeedbacks = async (page = 1) => {
  feedbackLoading.value = true
  try {
    const res = await adminGetAllFeedbacks(page, 15, feedbackFilter.value || null)
    const data = res.data || {}
    feedbackList.value = data.list || []
    feedbackPageInfo.value = { pageNum: data.pageNum || 1, pages: data.pages || 1, total: data.total || 0 }
  } catch {
    showToast('加载反馈数据失败', 'error')
  } finally {
    feedbackLoading.value = false
  }
}

const changeFeedbackPage = (page) => loadFeedbacks(page)

const openFeedbackDetail = (fb) => {
  currentFeedback.value = fb
  feedbackReply.value = fb.adminReply || ''
  feedbackReplyStatus.value = fb.status === 'PENDING' ? 'PROCESSING' : 'RESOLVED'
  showFeedbackModal.value = true
}

const submitFeedbackReply = async () => {
  if (!currentFeedback.value) return
  try {
    await adminReplyFeedback(currentFeedback.value.id, feedbackReply.value, feedbackReplyStatus.value)
    showToast('回复已提交', 'success')
    showFeedbackModal.value = false
    loadFeedbacks(feedbackPageInfo.value.pageNum)
  } catch {
    showToast('提交回复失败', 'error')
  }
}

const markFeedbackProcessing = async (id) => {
  try {
    await adminUpdateFeedbackStatus(id, 'PROCESSING')
    showToast('已标记为处理中', 'success')
    loadFeedbacks(feedbackPageInfo.value.pageNum)
  } catch {
    showToast('操作失败', 'error')
  }
}

const getFeedbackTypeLabel = (type) => {
  const map = { BUG: '问题反馈', SUGGESTION: '功能建议', COMPLAINT: '投诉', OTHER: '其他' }
  return map[type] || type || '-'
}

const getFeedbackStatusLabel = (status) => {
  const map = { PENDING: '待处理', PROCESSING: '处理中', RESOLVED: '已解决', CLOSED: '已关闭' }
  return map[status] || status
}

const getFeedbackStatusClass = (status) => {
  const map = { PENDING: 'status-banned', PROCESSING: 'status-inactive', RESOLVED: 'status-active', CLOSED: '' }
  return map[status] || ''
}

// ============ 健康内容管理 ============
const contentList = ref([])
const contentLoading = ref(false)
const showContentModal = ref(false)
const editingContent = ref({})

const loadContents = async () => {
  contentLoading.value = true
  try {
    const res = await adminGetAllContent(1, 50)
    const data = res.data || {}
    contentList.value = data.list || data || []
  } catch {
    showToast('加载内容数据失败', 'error')
  } finally {
    contentLoading.value = false
  }
}

const openContentForm = (item) => {
  editingContent.value = item
    ? { ...item }
    : { contentTitle: '', contentType: 'article', author: '', contentSource: '', contentBody: '', isPublished: 0 }
  showContentModal.value = true
}

const saveContent = async () => {
  const c = editingContent.value
  if (!c.contentTitle?.trim()) {
    showToast('请填写内容标题', 'error')
    return
  }
  try {
    if (c.id) {
      await adminUpdateContent(c.id, c)
    } else {
      await adminCreateContent(c)
    }
    showToast('保存成功', 'success')
    showContentModal.value = false
    loadContents()
  } catch {
    showToast('保存失败', 'error')
  }
}

const toggleContentPublish = async (item) => {
  try {
    await adminTogglePublish(item.id)
    showToast(item.isPublished ? '已取消发布' : '已发布', 'success')
    loadContents()
  } catch {
    showToast('操作失败', 'error')
  }
}

const deleteContent = async (id) => {
  if (!confirm('确认删除该内容？')) return
  try {
    await adminDeleteContent(id)
    showToast('已删除', 'success')
    loadContents()
  } catch {
    showToast('删除失败', 'error')
  }
}

// ============ 问卷管理 ============
const questionnaireList = ref([])
const questionnaireLoading = ref(false)

const loadQuestionnaires = async () => {
  questionnaireLoading.value = true
  try {
    const res = await adminGetAllQuestionnaires()
    questionnaireList.value = res.data || []
  } catch {
    showToast('加载问卷数据失败', 'error')
  } finally {
    questionnaireLoading.value = false
  }
}

const deleteQuestionnaire = async (id) => {
  if (!confirm('确认删除该问卷？')) return
  try {
    await adminDeleteQuestionnaire(id)
    showToast('问卷已删除', 'success')
    loadQuestionnaires()
  } catch {
    showToast('删除失败', 'error')
  }
}

const parseQCount = (questionsJson) => {
  if (!questionsJson) return 0
  try {
    const arr = typeof questionsJson === 'string' ? JSON.parse(questionsJson) : questionsJson
    return Array.isArray(arr) ? arr.length : 0
  } catch {
    return 0
  }
}

// ============ 知识图谱管理 ============
const conceptList = ref([])
const conceptLoading = ref(false)
const showConceptModal = ref(false)
const editingConcept = ref({})

const loadConcepts = async () => {
  conceptLoading.value = true
  try {
    const res = await adminGetAllConcepts()
    conceptList.value = res.data || []
  } catch {
    showToast('加载概念数据失败', 'error')
  } finally {
    conceptLoading.value = false
  }
}

const openConceptForm = (item) => {
  editingConcept.value = item
    ? { ...item }
    : { conceptName: '', conceptType: 'DISEASE', icd10Code: '', description: '' }
  showConceptModal.value = true
}

const saveConcept = async () => {
  const c = editingConcept.value
  if (!c.conceptName?.trim()) {
    showToast('请填写概念名称', 'error')
    return
  }
  try {
    if (c.id) {
      await adminUpdateConcept(c.id, c)
    } else {
      await adminCreateConcept(c)
    }
    showToast('保存成功', 'success')
    showConceptModal.value = false
    loadConcepts()
  } catch {
    showToast('保存失败', 'error')
  }
}

const deleteConcept = async (id) => {
  if (!confirm('确认删除该概念？')) return
  try {
    await adminDeleteConcept(id)
    showToast('已删除', 'success')
    loadConcepts()
  } catch {
    showToast('删除失败', 'error')
  }
}

const getConceptTypeLabel = (type) => {
  const map = { DISEASE: '疾病', SYMPTOM: '症状', INDICATOR: '指标', TREATMENT: '治疗' }
  return map[type] || type
}

const getConceptTypeClass = (type) => {
  const map = { DISEASE: 'status-banned', SYMPTOM: 'status-inactive', INDICATOR: 'status-active', TREATMENT: '' }
  return map[type] || ''
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

/* ECharts 饼图 */
.echarts-box-admin {
  width: 220px;
  height: 220px;
  flex-shrink: 0;
}

/* 风险分布布局 */
.risk-dist-layout {
  display: flex;
  align-items: center;
  gap: 24px;
}
.risk-dist-list {
  flex: 1;
}

/* 主按钮 */
.btn-primary {
  padding: 8px 18px;
  border: none;
  border-radius: 6px;
  background: #667eea;
  color: white;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: background 0.2s;
}
.btn-primary:hover { background: #5a6fd6; }

/* 查看按钮 */
.btn-view { background: #e3f2fd; color: #1565c0; }

/* 右侧控件组 */
.right-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 弹窗遮罩 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

/* 弹窗框 */
.modal-box-admin {
  background: white;
  border-radius: 12px;
  padding: 28px 32px;
  width: 100%;
  max-width: 520px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}
.modal-box-admin h2 {
  font-size: 18px;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}
.modal-wide { max-width: 760px; }

/* 表单行 */
.edit-form { display: flex; flex-direction: column; gap: 14px; }
.form-row {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.form-row label {
  font-size: 13px;
  font-weight: 500;
  color: #555;
}
.form-input-admin {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  box-sizing: border-box;
  transition: border-color 0.2s;
  font-family: inherit;
}
.form-input-admin:focus {
  outline: none;
  border-color: #667eea;
}

/* 弹窗操作按钮 */
.modal-actions-admin {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

/* 反馈详情 */
.feedback-detail {
  font-size: 14px;
  color: #444;
}
.feedback-detail p { margin: 6px 0; }
.detail-body-admin {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 12px 14px;
  margin: 10px 0;
  font-size: 14px;
  color: #555;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}
.existing-reply {
  background: #e8f5e9;
  border-radius: 6px;
  padding: 10px 14px;
  margin: 10px 0;
  font-size: 13px;
  color: #2e7d32;
}
.reply-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 14px;
}
.reply-form label {
  font-size: 13px;
  font-weight: 500;
  color: #555;
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
  .modal-box-admin { padding: 20px; }
}

@media (max-width: 480px) {
  .stat-cards { grid-template-columns: 1fr 1fr; }
}
</style>

