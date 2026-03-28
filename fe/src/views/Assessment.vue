<template>
  <div class="assessment-container">
    <div class="page-header">
      <h1>AI 健康评测</h1>
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

    <!-- 发起评测区域 -->
    <section class="evaluate-section">
      <div class="evaluate-card">
        <div class="evaluate-icon">🧠</div>
        <div class="evaluate-info">
          <h2>发起 AI 健康评测</h2>
          <p>基于您已上传的健康数据，由 DeepSeek AI 进行综合分析，生成个性化健康报告。</p>
          <div v-if="assessmentCount !== null" class="count-info">
            今年已评测 <strong>{{ assessmentCount }}</strong> 次
          </div>
        </div>
        <button
          @click="handleEvaluate"
          :disabled="evaluating"
          class="btn btn-evaluate"
        >
          <span v-if="evaluating">
            <span class="spinner"></span> 评测中...
          </span>
          <span v-else>开始评测</span>
        </button>
      </div>
    </section>

    <!-- 最新评测报告 -->
    <section v-if="latestReport" class="latest-report-section">
      <h2>最新评测报告</h2>
      <div class="report-card">
        <!-- 评测时间 -->
        <div class="report-meta">
          <span>评测日期：{{ formatDate(latestReport.assessmentDate) }}</span>
          <span class="risk-badge" :class="getRiskClass(latestReport.riskLevel)">
            {{ getRiskLabel(latestReport.riskLevel) }}
          </span>
        </div>

        <!-- 健康评分 -->
        <div class="score-section">
          <div class="score-circle" :style="{ '--score': latestReport.overallScore }">
            <div class="score-value">{{ latestReport.overallScore }}</div>
            <div class="score-label">健康评分</div>
          </div>
          <div class="score-detail">
            <div
              v-if="parsedAiAnalysis && parsedAiAnalysis.scoreBreakdown"
              class="score-breakdown"
            >
              <div class="breakdown-item" v-for="(val, key) in parsedAiAnalysis.scoreBreakdown" :key="key">
                <span class="breakdown-label">{{ getBreakdownLabel(key) }}</span>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: val + '%' }"></div>
                </div>
                <span class="breakdown-val">{{ val }}</span>
              </div>
            </div>
            <div v-if="parsedAiAnalysis && parsedAiAnalysis.physiologicalAge" class="physiological-age">
              <span class="age-label">生理年龄</span>
              <span class="age-value">{{ parsedAiAnalysis.physiologicalAge }} 岁</span>
            </div>
          </div>
        </div>

        <!-- 评估摘要 -->
        <div class="report-summary">
          <h3>健康摘要</h3>
          <p>{{ latestReport.summary }}</p>
        </div>

        <!-- 健康建议 -->
        <div class="report-recommendation">
          <h3>健康建议</h3>
          <div class="recommendation-list">
            <div
              v-for="(rec, index) in recommendationList"
              :key="index"
              class="recommendation-item"
            >
              <span class="rec-number">{{ index + 1 }}</span>
              <span>{{ rec }}</span>
            </div>
          </div>
        </div>

        <!-- 风险疾病 -->
        <div
          v-if="parsedAiAnalysis && parsedAiAnalysis.predictedDiseases && parsedAiAnalysis.predictedDiseases.length > 0"
          class="predicted-diseases"
        >
          <h3>⚠️ 需要关注的风险</h3>
          <div class="disease-tags">
            <span
              v-for="disease in parsedAiAnalysis.predictedDiseases"
              :key="disease"
              class="disease-tag"
            >
              {{ disease }}
            </span>
          </div>
        </div>

        <!-- 置信度 -->
        <div v-if="parsedAiAnalysis && parsedAiAnalysis.confidenceScore" class="confidence-section">
          <span class="confidence-label">AI置信度：</span>
          <span class="confidence-value">{{ parsedAiAnalysis.confidenceScore }}%</span>
        </div>

        <!-- PDF 导出按钮 -->
        <div class="export-section">
          <button
            @click="handleExportPdf"
            :disabled="exporting"
            class="btn btn-export-pdf"
          >
            <span v-if="exporting">
              <span class="spinner spinner-dark"></span> 生成中...
            </span>
            <span v-else>📄 导出 PDF 报告</span>
          </button>
        </div>
      </div>
    </section>

    <!-- 评分趋势图 -->
    <section v-if="reports.length >= 2" class="chart-section">
      <h2>健康评分趋势</h2>
      <div ref="chartRef" class="echarts-box"></div>
    </section>

    <!-- 历史评测报告 -->
    <section class="history-section">
      <h2>历史评测记录</h2>

      <div v-if="historyLoading" class="loading-spinner">加载中...</div>
      <div v-else-if="reports.length === 0" class="empty-state">
        <p>暂无历史评测记录</p>
      </div>
      <div v-else>
        <div class="history-list">
          <div
            v-for="report in reports"
            :key="report.id"
            class="history-item"
            :class="{ active: selectedReportId === report.id }"
            @click="selectReport(report)"
          >
            <div class="history-date">{{ formatDate(report.assessmentDate) }}</div>
            <div class="history-score">
              <span class="score-num">{{ report.overallScore }}</span>
              <span class="score-unit">分</span>
            </div>
            <div class="history-risk">
              <span class="risk-badge-sm" :class="getRiskClass(report.riskLevel)">
                {{ getRiskLabel(report.riskLevel) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination">
          <button :disabled="historyPage <= 1" @click="loadHistory(historyPage - 1)" class="btn btn-page">上一页</button>
          <span class="page-info">第 {{ historyPage }} 页 / 共 {{ historyTotalPages }} 页</span>
          <button :disabled="historyPage >= historyTotalPages" @click="loadHistory(historyPage + 1)" class="btn btn-page">下一页</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  startEvaluation,
  getLatestAssessment,
  getAssessmentReports,
  getAssessmentCount,
  exportAssessmentPdf,
  exportLatestAssessmentPdf
} from '@/services/assessmentApi'

const error = ref(null)
const success = ref(null)
const evaluating = ref(false)
const exporting = ref(false)
const latestReport = ref(null)
const reports = ref([])
const assessmentCount = ref(null)
const historyLoading = ref(false)
const historyPage = ref(1)
const historyTotalPages = ref(1)
const selectedReportId = ref(null)

// ECharts 趋势图
const chartRef = ref(null)
let chartInstance = null
let chartResizeFn = null

const initChart = () => {
  if (!chartRef.value || reports.value.length < 2) return
  if (chartInstance) chartInstance.dispose()

  chartInstance = echarts.init(chartRef.value)

  // 历史记录按时间正序排列（报告列表一般是倒序的）
  const sorted = [...reports.value].reverse()
  const dates = sorted.map(r => {
    const d = new Date(r.assessmentDate)
    return `${d.getMonth() + 1}/${d.getDate()}`
  })
  const scores = sorted.map(r => r.overallScore ?? 0)
  const riskColors = sorted.map(r => {
    const colorMap = { LOW: '#00b894', MEDIUM: '#fdcb6e', HIGH: '#e17055', CRITICAL: '#e74c3c' }
    return colorMap[r.riskLevel] || '#667eea'
  })

  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        const r = sorted[p.dataIndex]
        return `${p.name}<br/>健康评分：<b>${p.value}</b><br/>风险等级：${getRiskLabel(r.riskLevel)}`
      }
    },
    grid: { top: 20, right: 20, bottom: 40, left: 50 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { color: '#888', fontSize: 12 },
      axisLine: { lineStyle: { color: '#eee' } }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLabel: { color: '#888', fontSize: 12 },
      splitLine: { lineStyle: { color: '#f5f5f5' } }
    },
    series: [{
      type: 'line',
      data: scores,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#667eea', width: 2.5 },
      itemStyle: {
        color: (params) => riskColors[params.dataIndex] || '#667eea',
        borderWidth: 2,
        borderColor: '#fff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102,126,234,0.25)' },
          { offset: 1, color: 'rgba(102,126,234,0.02)' }
        ])
      }
    }]
  }
  chartInstance.setOption(option)

  // 先移除旧的 resize 监听，再添加新的，避免重复监听
  if (chartResizeFn) window.removeEventListener('resize', chartResizeFn)
  chartResizeFn = () => chartInstance?.resize()
  window.addEventListener('resize', chartResizeFn)
}

// 组件销毁时释放 ECharts 实例和事件监听
onUnmounted(() => {
  if (chartResizeFn) {
    window.removeEventListener('resize', chartResizeFn)
    chartResizeFn = null
  }
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

watch(reports, async (val) => {
  if (val.length >= 2) {
    await nextTick()
    initChart()
  }
}, { deep: true })

// 解析 AI 分析结果
const parsedAiAnalysis = computed(() => {
  if (!latestReport.value?.aiAnalysis) return null
  try {
    return JSON.parse(latestReport.value.aiAnalysis)
  } catch {
    return null
  }
})

// 解析建议列表
const recommendationList = computed(() => {
  if (!latestReport.value?.recommendation) return []
  const rec = latestReport.value.recommendation
  // 如果是换行分隔的列表
  const lines = rec.split('\n').filter(l => l.trim())
  return lines.map(l => l.replace(/^\d+\.\s*/, '').trim()).filter(l => l)
})

onMounted(async () => {
  await Promise.all([
    loadLatestReport(),
    loadHistory(),
    loadAssessmentCount()
  ])
})

const loadLatestReport = async () => {
  try {
    const response = await getLatestAssessment()
    latestReport.value = response.data
  } catch (err) {
    if (err.response?.status !== 404) {
      console.error('加载最新报告失败:', err)
    }
  }
}

const loadHistory = async (page = 1) => {
  historyLoading.value = true
  try {
    const response = await getAssessmentReports(page, 10)
    reports.value = response.data?.list || []
    historyTotalPages.value = response.data?.pages || 1
    historyPage.value = page
  } catch (err) {
    console.error('加载历史记录失败:', err)
  } finally {
    historyLoading.value = false
  }
}

const loadAssessmentCount = async () => {
  try {
    const response = await getAssessmentCount()
    assessmentCount.value = response.data
  } catch {
    assessmentCount.value = 0
  }
}

const handleEvaluate = async () => {
  evaluating.value = true
  error.value = null
  success.value = null

  try {
    const response = await startEvaluation()
    latestReport.value = response.data
    success.value = '评测完成！已生成最新健康报告。'
    await Promise.all([loadHistory(), loadAssessmentCount()])
  } catch (err) {
    error.value = err.response?.data?.message || '评测失败，请确保已上传健康数据后重试'
  } finally {
    evaluating.value = false
  }
}

const selectReport = (report) => {
  selectedReportId.value = report.id
  latestReport.value = report
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleExportPdf = async () => {
  if (!latestReport.value) return
  exporting.value = true
  try {
    let response
    if (latestReport.value.id) {
      response = await exportAssessmentPdf(latestReport.value.id)
    } else {
      response = await exportLatestAssessmentPdf()
    }

    // 从响应头获取文件名，否则使用默认名
    const contentDisposition = response.headers?.['content-disposition'] || ''
    let filename = '健康评测报告.pdf'
    const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
    if (match && match[1]) {
      filename = decodeURIComponent(match[1].replace(/['"]/g, ''))
    } else if (latestReport.value.assessmentDate) {
      const d = latestReport.value.assessmentDate.replace(/-/g, '')
      filename = `健康评测报告_${d}.pdf`
    }

    // 创建下载链接
    const url = URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    success.value = 'PDF 报告已成功导出！'
  } catch (err) {
    console.error('PDF 导出失败:', err)
    error.value = 'PDF 导出失败，请稍后重试'
  } finally {
    exporting.value = false
  }
}

// 工具函数
const getRiskClass = (level) => {
  const classes = {
    LOW: 'risk-low',
    MEDIUM: 'risk-medium',
    HIGH: 'risk-high',
    CRITICAL: 'risk-critical'
  }
  return classes[level] || 'risk-low'
}

const getRiskLabel = (level) => {
  const labels = {
    LOW: '低风险',
    MEDIUM: '中风险',
    HIGH: '高风险',
    CRITICAL: '极高风险'
  }
  return labels[level] || level
}

const getBreakdownLabel = (key) => {
  const labels = {
    cardiovascular: '心血管',
    metabolic: '代谢',
    lifestyle: '生活方式'
  }
  return labels[key] || key
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}
</script>

<style scoped>
.assessment-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 24px;
}

section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #f0f0f0;
}

section h2 {
  font-size: 20px;
  color: #333;
  margin: 0 0 20px 0;
}

/* ECharts 图表容器 */
.echarts-box {
  width: 100%;
  height: 280px;
}

/* 发起评测区 */
.evaluate-card {
  display: flex;
  align-items: center;
  gap: 24px;
}

.evaluate-icon {
  font-size: 60px;
  flex-shrink: 0;
}

.evaluate-info {
  flex: 1;
}

.evaluate-info h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
}

.evaluate-info p {
  color: #666;
  margin: 0 0 12px 0;
  line-height: 1.6;
}

.count-info {
  color: #888;
  font-size: 14px;
}

.btn-evaluate {
  padding: 14px 28px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-evaluate:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102,126,234,0.4);
}

.btn-evaluate:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 报告卡片 */
.report-card {
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 24px;
}

.report-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  color: #888;
  font-size: 14px;
}

/* 风险标签 */
.risk-badge {
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.risk-badge-sm {
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.risk-low { background: #d4edda; color: #155724; }
.risk-medium { background: #fff3cd; color: #856404; }
.risk-high { background: #f8d7da; color: #721c24; }
.risk-critical { background: #f5c6cb; color: #491217; }

/* 评分区 */
.score-section {
  display: flex;
  gap: 30px;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: #f8f9ff;
  border-radius: 10px;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: conic-gradient(
    #667eea calc(var(--score, 75) * 3.6deg),
    #e0e0e0 0deg
  );
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
  flex-shrink: 0;
}

.score-circle::before {
  content: '';
  position: absolute;
  width: 90px;
  height: 90px;
  background: white;
  border-radius: 50%;
}

.score-value {
  font-size: 28px;
  font-weight: bold;
  color: #667eea;
  position: relative;
  z-index: 1;
}

.score-label {
  font-size: 11px;
  color: #888;
  position: relative;
  z-index: 1;
}

.score-detail {
  flex: 1;
}

.score-breakdown {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.breakdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.breakdown-label {
  font-size: 13px;
  color: #666;
  min-width: 60px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(to right, #667eea, #764ba2);
  border-radius: 4px;
  transition: width 0.6s ease;
}

.breakdown-val {
  font-size: 13px;
  font-weight: 600;
  color: #667eea;
  min-width: 30px;
  text-align: right;
}

.physiological-age {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff3e0;
  border-radius: 6px;
  margin-top: 8px;
}

.age-label { font-size: 13px; color: #888; }
.age-value { font-size: 18px; font-weight: bold; color: #e67e22; }

/* 摘要和建议 */
.report-summary, .report-recommendation {
  margin-bottom: 20px;
}

.report-summary h3, .report-recommendation h3, .predicted-diseases h3 {
  font-size: 16px;
  color: #555;
  margin: 0 0 12px 0;
  border-left: 3px solid #667eea;
  padding-left: 10px;
}

.report-summary p {
  color: #555;
  line-height: 1.7;
  background: #f9f9f9;
  padding: 12px 16px;
  border-radius: 6px;
}

.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recommendation-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 14px;
  background: #f0f7ff;
  border-radius: 6px;
  font-size: 14px;
  color: #555;
}

.rec-number {
  width: 22px;
  height: 22px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 风险疾病 */
.predicted-diseases {
  margin-bottom: 16px;
}

.disease-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.disease-tag {
  padding: 6px 14px;
  background: #fff3f3;
  border: 1px solid #ffb3b3;
  color: #c0392b;
  border-radius: 16px;
  font-size: 13px;
}

/* 置信度 */
.confidence-section {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #888;
  font-size: 13px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.confidence-value {
  color: #667eea;
  font-weight: 600;
}

/* PDF 导出区域 */
.export-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
}

.btn-export-pdf {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 22px;
  background: linear-gradient(135deg, #00b894, #00cec9);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-export-pdf:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 184, 148, 0.4);
}

.btn-export-pdf:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner-dark {
  display: inline-block;
  width: 13px;
  height: 13px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 历史列表 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 16px;
}

.history-item:hover, .history-item.active {
  background: #f5f3ff;
  border-color: #667eea;
}

.history-date {
  color: #555;
  font-size: 14px;
  flex: 1;
}

.history-score {
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.score-num {
  font-size: 22px;
  font-weight: bold;
  color: #667eea;
}

.score-unit {
  font-size: 12px;
  color: #888;
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

.btn-page {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-page:disabled {
  opacity: 0.4;
  cursor: not-allowed;
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
}

.loading-spinner {
  text-align: center;
  padding: 40px;
  color: #888;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #aaa;
}

/* 响应式 */
@media (max-width: 768px) {
  .evaluate-card {
    flex-direction: column;
    text-align: center;
  }

  .score-section {
    flex-direction: column;
  }
}
</style>

