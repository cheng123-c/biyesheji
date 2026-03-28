<template>
  <div class="monitor-root" ref="rootRef">
    <!-- 粒子背景 -->
    <canvas class="particle-canvas" ref="canvasRef"></canvas>

    <!-- 顶部标题栏 -->
    <header class="monitor-header">
      <div class="header-left">
        <div class="signal-dots">
          <span class="dot dot-green"></span>
          <span class="dot dot-blue"></span>
          <span class="dot dot-purple"></span>
        </div>
        <span class="system-tag">HEALTH SYSTEM v2.6</span>
      </div>
      <div class="header-center">
        <div class="title-glow">
          <div class="title-line"></div>
          <h1 class="main-title">患者大数据监控中心</h1>
          <div class="title-line"></div>
        </div>
        <div class="sub-title">PATIENT BIG DATA MONITORING CENTER · ADMIN VIEW</div>
      </div>
      <div class="header-right">
        <div class="live-clock">
          <div class="clock-time">{{ currentTime }}</div>
          <div class="clock-date">{{ currentDate }}</div>
        </div>
        <div class="live-badge">
          <span class="live-dot"></span>
          LIVE
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="monitor-body">

      <!-- ===== 第一行：统计数字卡片 ===== -->
      <section class="row-top">
        <div
          v-for="(card, i) in statCards"
          :key="i"
          class="stat-card"
          :style="{ '--accent': card.color }"
          :class="{ 'card-pulse': card.pulse }"
        >
          <div class="card-bg-glow"></div>
          <div class="card-icon-wrap">
            <span class="card-icon">{{ card.icon }}</span>
          </div>
          <div class="card-info">
            <div class="card-num">
              <span class="num-count">{{ card.displayValue }}</span>
              <span class="num-unit">{{ card.unit }}</span>
            </div>
            <div class="card-label">{{ card.label }}</div>
          </div>
          <div class="card-trend">
            <span :class="card.trend > 0 ? 'trend-up' : 'trend-down'">
              {{ card.trend > 0 ? '↑' : '↓' }} {{ Math.abs(card.trend) }}%
            </span>
            <span class="trend-label">vs 上月</span>
          </div>
          <div class="card-sparkline">
            <div
              v-for="(h, j) in card.spark"
              :key="j"
              class="spark-bar"
              :style="{ height: h + '%', background: card.color }"
            ></div>
          </div>
        </div>
      </section>

      <!-- ===== 第二行：左图表 + 中地图 + 右列表 ===== -->
      <section class="row-middle">

        <!-- 左侧：疾病分布环形图 + 年龄分布玫瑰图 -->
        <div class="col-left">
          <div class="panel panel-half">
            <div class="panel-title">
              <span class="title-bar"></span>慢病分布人次
            </div>
            <div ref="diseaseChartRef" class="chart-box"></div>
          </div>
          <div class="panel panel-half">
            <div class="panel-title">
              <span class="title-bar"></span>患者年龄段分布
            </div>
            <div ref="ageChartRef" class="chart-box"></div>
          </div>
        </div>

        <!-- 中间：实时告警 + 风险大地图 -->
        <div class="col-center">
          <!-- 风险热力示意地图（SVG假地图） -->
          <div class="panel panel-map">
            <div class="panel-title">
              <span class="title-bar"></span>全市患者风险热力分布
              <span class="map-legend">
                <span class="legend-dot" style="background:#e74c3c"></span>高风险
                <span class="legend-dot" style="background:#f39c12"></span>中风险
                <span class="legend-dot" style="background:#27ae60"></span>低风险
              </span>
            </div>
            <div class="map-container">
              <div ref="mapChartRef" class="map-echarts"></div>
              <!-- 浮动告警卡 -->
              <div class="alert-float" v-for="(alert, i) in floatAlerts" :key="i"
                :style="{ top: alert.top, left: alert.left, '--a-color': alert.color }">
                <span class="alert-pulse-ring"></span>
                <span class="alert-label">{{ alert.label }}</span>
              </div>
            </div>
          </div>

          <!-- 实时告警滚动 -->
          <div class="panel panel-alerts">
            <div class="panel-title">
              <span class="title-bar"></span>实时预警信息
              <span class="alert-count-badge">{{ alertList.length }}</span>
            </div>
            <div class="alert-scroll-wrap" ref="alertScrollRef">
              <div class="alert-scroll-inner" ref="alertInnerRef">
                <div
                  v-for="(a, i) in [...alertList, ...alertList]"
                  :key="i"
                  class="alert-row"
                  :class="'alert-' + a.level"
                >
                  <span class="alert-level-dot"></span>
                  <span class="alert-time">{{ a.time }}</span>
                  <span class="alert-text">{{ a.text }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：高风险患者列表 -->
        <div class="col-right">
          <div class="panel panel-full">
            <div class="panel-title">
              <span class="title-bar"></span>高风险患者追踪
            </div>
            <div class="patient-list">
              <div class="patient-list-header">
                <span>患者</span>
                <span>风险评分</span>
                <span>主要风险</span>
                <span>状态</span>
              </div>
              <div
                v-for="(p, i) in patientList"
                :key="i"
                class="patient-row"
                :class="{ 'row-critical': p.score >= 85 }"
                @mouseenter="hoveredPatient = i"
                @mouseleave="hoveredPatient = -1"
              >
                <div class="patient-info">
                  <div class="patient-avatar" :style="{ background: p.avatarColor }">
                    {{ p.name[0] }}
                  </div>
                  <div>
                    <div class="patient-name">{{ p.name }}</div>
                    <div class="patient-meta">{{ p.age }}岁 · {{ p.gender }}</div>
                  </div>
                </div>
                <div class="score-wrap">
                  <div class="score-ring" :style="{ '--s': p.score, '--sc': p.scoreColor }">
                    <svg viewBox="0 0 36 36">
                      <circle class="score-bg" cx="18" cy="18" r="15.9"/>
                      <circle class="score-fg" cx="18" cy="18" r="15.9"
                        :stroke="p.scoreColor"
                        :stroke-dasharray="`${p.score} 100`"
                      />
                    </svg>
                    <span class="score-num">{{ p.score }}</span>
                  </div>
                </div>
                <div class="patient-risk">
                  <span class="risk-chip" :style="{ background: p.scoreColor + '22', color: p.scoreColor }">
                    {{ p.risk }}
                  </span>
                </div>
                <div class="patient-status">
                  <span class="status-light" :class="'status-' + p.status"></span>
                  {{ p.statusText }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ===== 第三行：趋势折线图 + 医院收治比 + 评测数量 ===== -->
      <section class="row-bottom">
        <div class="panel panel-trend">
          <div class="panel-title">
            <span class="title-bar"></span>近30天患者就诊趋势
            <div class="chart-tabs">
              <span
                v-for="t in trendTabs"
                :key="t.key"
                class="chart-tab"
                :class="{ active: activeTrend === t.key }"
                @click="activeTrend = t.key"
              >{{ t.label }}</span>
            </div>
          </div>
          <div ref="trendChartRef" class="chart-box chart-box-tall"></div>
        </div>

        <div class="panel panel-hospital">
          <div class="panel-title">
            <span class="title-bar"></span>各医院收治占比
          </div>
          <div ref="hospitalChartRef" class="chart-box chart-box-tall"></div>
        </div>

        <div class="panel panel-assess">
          <div class="panel-title">
            <span class="title-bar"></span>健康评测分布
          </div>
          <div ref="assessChartRef" class="chart-box chart-box-tall"></div>
        </div>
      </section>

    </main>

    <!-- 数字翻滚动效覆盖层（启动时） -->
    <div class="boot-overlay" :class="{ 'boot-done': bootDone }">
      <div class="boot-text">LOADING PATIENT DATA...</div>
      <div class="boot-bar"><div class="boot-progress" :style="{ width: bootProgress + '%' }"></div></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'

// =================== 时钟 ===================
const currentTime = ref('')
const currentDate = ref('')
const updateClock = () => {
  const now = new Date()
  currentTime.value = now.toTimeString().slice(0, 8)
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'short' })
}
let clockTimer = null

// =================== 启动动画 ===================
const bootDone = ref(false)
const bootProgress = ref(0)

// =================== 粒子背景 ===================
const canvasRef = ref(null)
let animFrame = null
const initParticles = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const resize = () => {
    canvas.width = canvas.offsetWidth
    canvas.height = canvas.offsetHeight
  }
  resize()
  window.addEventListener('resize', resize)

  const particles = Array.from({ length: 80 }, () => ({
    x: Math.random() * canvas.width,
    y: Math.random() * canvas.height,
    r: Math.random() * 1.5 + 0.5,
    vx: (Math.random() - 0.5) * 0.4,
    vy: (Math.random() - 0.5) * 0.4,
    a: Math.random() * 0.5 + 0.2
  }))

  const draw = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    particles.forEach(p => {
      p.x += p.vx; p.y += p.vy
      if (p.x < 0 || p.x > canvas.width) p.vx *= -1
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1
      ctx.beginPath()
      ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(0,212,255,${p.a})`
      ctx.fill()
    })
    // 连线
    particles.forEach((a, i) => {
      particles.slice(i + 1).forEach(b => {
        const d = Math.hypot(a.x - b.x, a.y - b.y)
        if (d < 100) {
          ctx.beginPath()
          ctx.moveTo(a.x, a.y)
          ctx.lineTo(b.x, b.y)
          ctx.strokeStyle = `rgba(0,212,255,${0.08 * (1 - d / 100)})`
          ctx.lineWidth = 0.5
          ctx.stroke()
        }
      })
    })
    animFrame = requestAnimationFrame(draw)
  }
  draw()
}

// =================== 统计卡片 ===================
const statCards = ref([
  {
    icon: '🏥', label: '在院患者总数', value: 12847, displayValue: '12,847', unit: '人',
    color: '#00d4ff', trend: 8.3, pulse: false,
    spark: [30, 45, 38, 62, 55, 70, 65, 80, 72, 85]
  },
  {
    icon: '⚠️', label: '高风险患者', value: 386, displayValue: '386', unit: '人',
    color: '#e74c3c', trend: -5.2, pulse: true,
    spark: [70, 65, 72, 58, 50, 62, 55, 48, 45, 40]
  },
  {
    icon: '📋', label: '今日评测次数', value: 2341, displayValue: '2,341', unit: '次',
    color: '#00d084', trend: 12.7, pulse: false,
    spark: [20, 35, 28, 50, 45, 60, 55, 70, 65, 78]
  },
  {
    icon: '💊', label: '慢病管理人数', value: 5629, displayValue: '5,629', unit: '人',
    color: '#a855f7', trend: 3.1, pulse: false,
    spark: [50, 52, 55, 58, 60, 63, 67, 70, 73, 76]
  },
  {
    icon: '📊', label: '本月数据采集', value: 188000, displayValue: '18.8', unit: '万条',
    color: '#f39c12', trend: 15.4, pulse: false,
    spark: [30, 40, 45, 55, 50, 65, 60, 75, 70, 82]
  }
])

// =================== 浮动告警点 ===================
const floatAlerts = ref([
  { label: '高风险', color: '#e74c3c', top: '20%', left: '30%' },
  { label: '高风险', color: '#e74c3c', top: '55%', left: '60%' },
  { label: '中风险', color: '#f39c12', top: '35%', left: '70%' },
  { label: '中风险', color: '#f39c12', top: '65%', left: '25%' },
  { label: '低风险', color: '#27ae60', top: '45%', left: '45%' },
])

// =================== 实时告警 ===================
const alertList = ref([
  { level: 'critical', time: '14:32:15', text: '患者 李某某 (ID:1024) 血压异常，收缩压达 185mmHg' },
  { level: 'high',     time: '14:28:03', text: '患者 王某某 (ID:2891) 血糖超标，FPG 12.3mmol/L' },
  { level: 'medium',   time: '14:21:44', text: '患者 张某某 (ID:0553) 体重指数 BMI 32.1，超重提醒' },
  { level: 'high',     time: '14:15:22', text: '患者 陈某某 (ID:3678) 心率持续偏高 105bpm' },
  { level: 'critical', time: '14:09:11', text: '患者 赵某某 (ID:0127) 肌酐水平升高，疑似肾功能异常' },
  { level: 'medium',   time: '14:02:58', text: '患者 刘某某 (ID:4412) 6个月未随访，触发提醒' },
  { level: 'high',     time: '13:55:30', text: '患者 杨某某 (ID:2233) 胸痛症状自评风险分 88 分' },
  { level: 'medium',   time: '13:48:07', text: '患者 吴某某 (ID:5566) LDL胆固醇 5.2，超过正常上限' },
])
const alertScrollRef = ref(null)
const alertInnerRef = ref(null)
let alertTimer = null
let alertOffset = 0

const scrollAlerts = () => {
  if (!alertInnerRef.value) return
  alertOffset += 0.5
  const itemH = 44
  const total = alertList.value.length * itemH
  if (alertOffset >= total) alertOffset = 0
  alertInnerRef.value.style.transform = `translateY(-${alertOffset}px)`
}

// =================== 高风险患者列表 ===================
const hoveredPatient = ref(-1)
const patientList = ref([
  { name: '李建国', age: 67, gender: '男', score: 92, risk: '心血管高危', status: 'critical', statusText: '重点监控', avatarColor: '#e74c3c', scoreColor: '#e74c3c' },
  { name: '王美华', age: 72, gender: '女', score: 88, risk: '糖尿病并发', status: 'high', statusText: '跟踪随访', avatarColor: '#f39c12', scoreColor: '#f39c12' },
  { name: '张志远', age: 58, gender: '男', score: 85, risk: '高血压III期', status: 'critical', statusText: '重点监控', avatarColor: '#e74c3c', scoreColor: '#e74c3c' },
  { name: '陈秀英', age: 65, gender: '女', score: 79, risk: '慢性肾病', status: 'high', statusText: '跟踪随访', avatarColor: '#a855f7', scoreColor: '#a855f7' },
  { name: '刘德华', age: 55, gender: '男', score: 74, risk: 'COPD稳定期', status: 'medium', statusText: '定期随访', avatarColor: '#00b4d8', scoreColor: '#00b4d8' },
  { name: '赵雪梅', age: 70, gender: '女', score: 71, risk: '骨质疏松', status: 'medium', statusText: '定期随访', avatarColor: '#f39c12', scoreColor: '#f39c12' },
  { name: '孙明亮', age: 62, gender: '男', score: 68, risk: '代谢综合征', status: 'medium', statusText: '定期随访', avatarColor: '#00d084', scoreColor: '#00d084' },
  { name: '周淑芬', age: 68, gender: '女', score: 63, risk: '甲状腺结节', status: 'low', statusText: '常规管理', avatarColor: '#27ae60', scoreColor: '#27ae60' },
])

// =================== 趋势图数据 ===================
const activeTrend = ref('visit')
const trendTabs = [
  { key: 'visit', label: '就诊人次' },
  { key: 'risk', label: '风险评分' },
  { key: 'data', label: '数据采集' },
]

// ECharts refs
const diseaseChartRef = ref(null)
const ageChartRef = ref(null)
const mapChartRef = ref(null)
const trendChartRef = ref(null)
const hospitalChartRef = ref(null)
const assessChartRef = ref(null)

let charts = {}

const ECHARTS_THEME = {
  backgroundColor: 'transparent',
  textStyle: { color: '#a0c4d8' },
}

// 通用配置
const commonAxis = {
  axisLine: { lineStyle: { color: '#1a4060' } },
  axisTick: { show: false },
  axisLabel: { color: '#7ab3cc', fontSize: 11 },
  splitLine: { lineStyle: { color: '#0d2d45', type: 'dashed' } }
}

const initDiseaseChart = () => {
  const c = echarts.init(diseaseChartRef.value)
  charts.disease = c
  c.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)', backgroundColor: '#0a1e35', borderColor: '#0ff', textStyle: { color: '#fff' } },
    legend: { bottom: 4, textStyle: { color: '#7ab3cc', fontSize: 10 }, itemWidth: 10, itemHeight: 10 },
    series: [{
      type: 'pie',
      radius: ['38%', '65%'],
      center: ['50%', '45%'],
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold', color: '#fff' } },
      data: [
        { value: 3245, name: '高血压', itemStyle: { color: '#e74c3c' } },
        { value: 2890, name: '糖尿病', itemStyle: { color: '#f39c12' } },
        { value: 1654, name: '冠心病', itemStyle: { color: '#00d4ff' } },
        { value: 1123, name: 'COPD', itemStyle: { color: '#a855f7' } },
        { value: 987, name: '慢性肾病', itemStyle: { color: '#00d084' } },
        { value: 730, name: '其他', itemStyle: { color: '#445566' } },
      ]
    }]
  })
}

const initAgeChart = () => {
  const c = echarts.init(ageChartRef.value)
  charts.age = c
  c.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', formatter: '{b}: {c}人', backgroundColor: '#0a1e35', borderColor: '#0ff', textStyle: { color: '#fff' } },
    series: [{
      type: 'pie',
      radius: [20, '70%'],
      center: ['50%', '50%'],
      roseType: 'area',
      label: { color: '#7ab3cc', fontSize: 10 },
      labelLine: { lineStyle: { color: '#2a5f80' } },
      data: [
        { value: 85, name: '18-30岁', itemStyle: { color: '#00d4ff' } },
        { value: 320, name: '31-45岁', itemStyle: { color: '#a855f7' } },
        { value: 890, name: '46-60岁', itemStyle: { color: '#f39c12' } },
        { value: 2340, name: '61-75岁', itemStyle: { color: '#e74c3c' } },
        { value: 1580, name: '75岁+', itemStyle: { color: '#e74c3c99' } },
      ]
    }]
  })
}

const initMapChart = () => {
  const c = echarts.init(mapChartRef.value)
  charts.map = c
  // 用散点图模拟城市风险分布
  const riskData = [
    [30, 50, 92, '中心医院·李某', '#e74c3c'],
    [60, 30, 85, '北区医院·张某', '#e74c3c'],
    [75, 65, 78, '东区诊所·王某', '#f39c12'],
    [20, 70, 71, '南区医院·陈某', '#f39c12'],
    [50, 80, 63, '西区门诊·刘某', '#00d084'],
    [85, 45, 88, '高新区医院', '#e74c3c'],
    [40, 20, 55, '老城区诊所', '#27ae60'],
    [65, 85, 74, '开发区卫生院', '#f39c12'],
  ]

  c.setOption({
    backgroundColor: 'transparent',
    xAxis: { min: 0, max: 100, show: false },
    yAxis: { min: 0, max: 100, show: false },
    series: [
      {
        type: 'effectScatter',
        data: riskData.map(d => ({
          value: [d[0], d[1], d[2]],
          name: d[3],
          itemStyle: { color: d[4] }
        })),
        symbolSize: (data) => Math.max(data[2] / 5, 8),
        rippleEffect: { brushType: 'stroke', scale: 3 },
        label: {
          show: true,
          formatter: '{b}',
          position: 'right',
          color: '#a0c4d8',
          fontSize: 10
        },
        tooltip: {
          trigger: 'item',
          formatter: (p) => `${p.name}<br/>风险评分：${p.value[2]}`,
          backgroundColor: '#0a1e35',
          borderColor: '#0ff',
          textStyle: { color: '#fff' }
        }
      }
    ],
    tooltip: { show: true }
  })
}

const getTrendData = (key) => {
  const days = Array.from({ length: 30 }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - 29 + i)
    return `${d.getMonth() + 1}/${d.getDate()}`
  })
  const dataMap = {
    visit: [120,145,132,160,155,178,165,190,182,205,198,220,215,238,230,250,245,268,260,282,275,290,285,305,298,312,305,325,318,335],
    risk:  [45,42,48,44,50,47,52,49,55,52,58,54,60,57,62,59,65,62,68,64,70,67,73,69,76,72,78,74,80,77],
    data:  [1200,1350,1280,1450,1380,1520,1460,1600,1540,1680,1620,1760,1700,1840,1780,1920,1860,2000,1940,2080,2020,2160,2100,2240,2180,2320,2260,2400,2340,2480]
  }
  return { days, values: dataMap[key] }
}

let trendChart = null
const initTrendChart = (key = 'visit') => {
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
    charts.trend = trendChart
  }
  const { days, values } = getTrendData(key)
  const labelMap = { visit: '就诊人次(人)', risk: '平均风险分', data: '采集条数' }
  const colorMap = { visit: '#00d4ff', risk: '#e74c3c', data: '#a855f7' }

  trendChart.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#0a1e35',
      borderColor: colorMap[key],
      textStyle: { color: '#fff', fontSize: 12 }
    },
    grid: { left: '3%', right: '3%', bottom: '8%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: days,
      ...commonAxis,
      axisLabel: { ...commonAxis.axisLabel, interval: 4 }
    },
    yAxis: { type: 'value', ...commonAxis },
    series: [{
      name: labelMap[key],
      type: 'line',
      smooth: true,
      data: values,
      lineStyle: { color: colorMap[key], width: 2 },
      itemStyle: { color: colorMap[key] },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: colorMap[key] + '55' },
            { offset: 1, color: colorMap[key] + '00' }
          ]
        }
      },
      symbol: 'none'
    }]
  })
}

const initHospitalChart = () => {
  const c = echarts.init(hospitalChartRef.value)
  charts.hospital = c
  const hospitals = ['中心医院', '北区医院', '东区医院', '南区医院', '西区门诊', '高新医院']
  const vals = [3245, 2890, 2100, 1800, 1234, 978]
  c.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, backgroundColor: '#0a1e35', borderColor: '#0ff', textStyle: { color: '#fff' } },
    grid: { left: '3%', right: '8%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: { type: 'value', ...commonAxis },
    yAxis: { type: 'category', data: hospitals, ...commonAxis },
    series: [{
      type: 'bar',
      data: vals.map((v, i) => ({
        value: v,
        itemStyle: {
          color: {
            type: 'linear', x: 0, y: 0, x2: 1, y2: 0,
            colorStops: [
              { offset: 0, color: ['#00d4ff', '#a855f7', '#00d084', '#e74c3c', '#f39c12', '#00b4d8'][i] },
              { offset: 1, color: ['#00d4ff44', '#a855f744', '#00d08444', '#e74c3c44', '#f39c1244', '#00b4d844'][i] }
            ]
          },
          borderRadius: [0, 4, 4, 0]
        }
      })),
      label: { show: true, position: 'right', color: '#7ab3cc', fontSize: 11 },
      barMaxWidth: 20
    }]
  })
}

const initAssessChart = () => {
  const c = echarts.init(assessChartRef.value)
  charts.assess = c
  c.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', backgroundColor: '#0a1e35', borderColor: '#0ff', textStyle: { color: '#fff' } },
    legend: { bottom: 4, textStyle: { color: '#7ab3cc', fontSize: 10 }, itemWidth: 10 },
    series: [{
      type: 'pie',
      radius: ['30%', '65%'],
      center: ['50%', '45%'],
      label: { color: '#7ab3cc', fontSize: 10 },
      labelLine: { lineStyle: { color: '#2a5f80' } },
      data: [
        { value: 4521, name: '低风险', itemStyle: { color: '#27ae60' } },
        { value: 2840, name: '中风险', itemStyle: { color: '#f39c12' } },
        { value: 1256, name: '高风险', itemStyle: { color: '#e74c3c' } },
        { value: 386,  name: '极高风险', itemStyle: { color: '#8e44ad' } },
      ]
    }]
  })
}

// 监听趋势tab切换
watch(activeTrend, (val) => {
  initTrendChart(val)
})

// 窗口resize
const resizeCharts = () => {
  Object.values(charts).forEach(c => c && c.resize())
}

// =================== 生命周期 ===================
onMounted(async () => {
  updateClock()
  clockTimer = setInterval(updateClock, 1000)

  // 启动动画
  let prog = 0
  const bootInterval = setInterval(() => {
    prog += Math.random() * 15
    if (prog >= 100) {
      prog = 100
      bootProgress.value = 100
      clearInterval(bootInterval)
      setTimeout(() => { bootDone.value = true }, 400)
    }
    bootProgress.value = Math.min(prog, 100)
  }, 80)

  await nextTick()
  initParticles()

  setTimeout(() => {
    initDiseaseChart()
    initAgeChart()
    initMapChart()
    initTrendChart('visit')
    initHospitalChart()
    initAssessChart()
  }, 600)

  // 告警滚动
  alertTimer = setInterval(scrollAlerts, 16)

  window.addEventListener('resize', resizeCharts)
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (alertTimer) clearInterval(alertTimer)
  if (animFrame) cancelAnimationFrame(animFrame)
  window.removeEventListener('resize', resizeCharts)
  Object.values(charts).forEach(c => c && c.dispose())
})
</script>

<style scoped>
/* ===================== 根容器 ===================== */
.monitor-root {
  position: fixed;
  inset: 0;
  background: #020d1a;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: #a0c4d8;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  z-index: 0;
}

/* ===================== 粒子背景 ===================== */
.particle-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

/* ===================== 启动遮罩 ===================== */
.boot-overlay {
  position: absolute;
  inset: 0;
  background: #020d1a;
  z-index: 999;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  transition: opacity 0.6s ease;
}
.boot-overlay.boot-done {
  opacity: 0;
  pointer-events: none;
}
.boot-text {
  font-size: 14px;
  letter-spacing: 4px;
  color: #00d4ff;
  font-weight: 300;
  animation: blink 1s infinite;
}
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0.3} }
.boot-bar {
  width: 300px;
  height: 2px;
  background: #0d2d45;
  border-radius: 2px;
  overflow: hidden;
}
.boot-progress {
  height: 100%;
  background: linear-gradient(90deg, #00d4ff, #a855f7);
  transition: width 0.1s linear;
  box-shadow: 0 0 8px #00d4ff;
}

/* ===================== 顶部 Header ===================== */
.monitor-header {
  position: relative;
  z-index: 10;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: linear-gradient(180deg, #030f1e 0%, transparent 100%);
  border-bottom: 1px solid #0d2d45;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 200px;
}
.signal-dots { display: flex; gap: 6px; align-items: center; }
.dot {
  width: 8px; height: 8px; border-radius: 50%;
  animation: dotPulse 2s infinite;
}
.dot-green { background: #27ae60; box-shadow: 0 0 6px #27ae60; animation-delay: 0s; }
.dot-blue  { background: #00d4ff; box-shadow: 0 0 6px #00d4ff; animation-delay: 0.3s; }
.dot-purple{ background: #a855f7; box-shadow: 0 0 6px #a855f7; animation-delay: 0.6s; }
@keyframes dotPulse { 0%,100%{opacity:1} 50%{opacity:0.4} }

.system-tag {
  font-size: 11px;
  letter-spacing: 2px;
  color: #2a7a9a;
  font-weight: 300;
}

.header-center {
  flex: 1;
  text-align: center;
}
.title-glow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}
.title-line {
  flex: 1;
  max-width: 120px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #00d4ff55);
}
.title-line:last-child { background: linear-gradient(270deg, transparent, #00d4ff55); }
.main-title {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #00d4ff, #a855f7, #00d4ff);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: shimmer 3s linear infinite;
  letter-spacing: 2px;
  white-space: nowrap;
  text-shadow: none;
}
@keyframes shimmer { 0%{background-position:0%} 100%{background-position:200%} }
.sub-title {
  font-size: 10px;
  letter-spacing: 3px;
  color: #2a5f80;
  margin-top: 2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 200px;
  justify-content: flex-end;
}
.live-clock { text-align: right; }
.clock-time { font-size: 20px; color: #00d4ff; font-weight: 600; font-variant-numeric: tabular-nums; letter-spacing: 2px; line-height: 1; }
.clock-date { font-size: 11px; color: #2a7a9a; margin-top: 2px; }
.live-badge {
  display: flex;
  align-items: center;
  gap: 5px;
  border: 1px solid #e74c3c44;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  color: #e74c3c;
  letter-spacing: 1px;
}
.live-dot {
  width: 6px; height: 6px; background: #e74c3c;
  border-radius: 50%;
  animation: dotPulse 1s infinite;
  box-shadow: 0 0 4px #e74c3c;
}

/* ===================== 主体 ===================== */
.monitor-body {
  position: relative;
  z-index: 1;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px 16px 10px;
  overflow: hidden;
}

/* ===================== 第一行：统计卡片 ===================== */
.row-top {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.stat-card {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #050f1e 0%, #081828 100%);
  border: 1px solid #0d2d45;
  border-radius: 8px;
  padding: 14px 16px 10px;
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
  cursor: default;
  transition: transform 0.2s, border-color 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  border-color: var(--accent);
}
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--accent), transparent);
}
.card-bg-glow {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: radial-gradient(ellipse at top left, var(--accent) 0%, transparent 60%);
  opacity: 0.04;
  pointer-events: none;
}
.card-pulse .card-bg-glow { animation: glowPulse 2s infinite; }
@keyframes glowPulse { 0%,100%{opacity:0.04} 50%{opacity:0.1} }

.card-icon-wrap {
  width: 44px; height: 44px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--accent) 12%, transparent);
  border: 1px solid color-mix(in srgb, var(--accent) 30%, transparent);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 22px;
}
.card-info { flex: 1; }
.card-num { display: flex; align-items: baseline; gap: 4px; }
.num-count { font-size: 26px; font-weight: 700; color: #e0f4ff; line-height: 1; }
.num-unit { font-size: 13px; color: var(--accent); }
.card-label { font-size: 11px; color: #4a7a96; margin-top: 3px; }
.card-trend { display: flex; flex-direction: column; align-items: flex-end; font-size: 11px; }
.trend-up { color: #27ae60; }
.trend-down { color: #e74c3c; }
.trend-label { color: #2a5f80; font-size: 10px; }

.card-sparkline {
  display: flex;
  align-items: flex-end;
  gap: 2px;
  height: 28px;
  margin-top: 2px;
}
.spark-bar {
  width: 4px;
  border-radius: 2px 2px 0 0;
  opacity: 0.6;
  min-height: 2px;
  transition: opacity 0.2s;
}
.stat-card:hover .spark-bar { opacity: 1; }

/* ===================== 第二行 ===================== */
.row-middle {
  flex: 1;
  display: flex;
  gap: 10px;
  min-height: 0;
}

.col-left {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.col-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}
.col-right {
  width: 300px;
  flex-shrink: 0;
}

/* ===================== 面板通用 ===================== */
.panel {
  background: linear-gradient(135deg, #050f1e 0%, #081828 100%);
  border: 1px solid #0d2d45;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}
.panel::after {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #00d4ff33, transparent);
}

.panel-half { flex: 1; display: flex; flex-direction: column; }
.panel-full { height: 100%; display: flex; flex-direction: column; }
.panel-map { flex: 3; min-height: 0; display: flex; flex-direction: column; }
.panel-alerts { flex: 1; min-height: 0; display: flex; flex-direction: column; }

.panel-title {
  padding: 8px 14px;
  font-size: 12px;
  color: #7ab3cc;
  font-weight: 600;
  letter-spacing: 1px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(90deg, #030d1a, transparent);
  border-bottom: 1px solid #0a2030;
  flex-shrink: 0;
}
.title-bar {
  width: 3px;
  height: 14px;
  background: linear-gradient(180deg, #00d4ff, #a855f7);
  border-radius: 2px;
  flex-shrink: 0;
}

/* ===================== 图表区 ===================== */
.chart-box {
  flex: 1;
  min-height: 0;
  width: 100%;
}
.chart-box-tall { flex: 1; min-height: 0; }

/* ===================== 地图区 ===================== */
.map-container {
  flex: 1;
  position: relative;
  min-height: 0;
}
.map-echarts {
  width: 100%;
  height: 100%;
}
.alert-float {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 5px;
  pointer-events: none;
}
.alert-pulse-ring {
  width: 10px; height: 10px;
  border-radius: 50%;
  background: var(--a-color);
  box-shadow: 0 0 0 0 var(--a-color);
  animation: pulseRing 1.5s infinite;
}
@keyframes pulseRing {
  0% { box-shadow: 0 0 0 0 color-mix(in srgb, var(--a-color) 70%, transparent); }
  100% { box-shadow: 0 0 0 10px transparent; }
}
.alert-label { font-size: 10px; color: var(--a-color); letter-spacing: 1px; }

.map-legend {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
  font-size: 10px;
}
.legend-dot {
  display: inline-block;
  width: 6px; height: 6px;
  border-radius: 50%;
  margin-right: 3px;
}

/* ===================== 告警滚动 ===================== */
.alert-scroll-wrap {
  flex: 1;
  overflow: hidden;
  position: relative;
}
.alert-scroll-inner {
  will-change: transform;
}
.alert-row {
  height: 44px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  font-size: 11px;
  border-bottom: 1px solid #0a2030;
  transition: background 0.2s;
}
.alert-row:hover { background: #0d2030; }

.alert-level-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}
.alert-critical .alert-level-dot { background: #e74c3c; box-shadow: 0 0 6px #e74c3c; }
.alert-high     .alert-level-dot { background: #f39c12; box-shadow: 0 0 6px #f39c12; }
.alert-medium   .alert-level-dot { background: #f39c12; }
.alert-low      .alert-level-dot { background: #27ae60; }

.alert-critical .alert-text { color: #e74c3c; }
.alert-high     .alert-text { color: #f39c12; }
.alert-medium   .alert-text { color: #a0c4d8; }

.alert-time { color: #2a5f80; font-size: 10px; min-width: 55px; font-variant-numeric: tabular-nums; }
.alert-text { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.alert-count-badge {
  margin-left: auto;
  background: #e74c3c;
  color: white;
  font-size: 10px;
  padding: 1px 7px;
  border-radius: 10px;
  font-weight: 600;
}

/* ===================== 患者列表 ===================== */
.patient-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}
.patient-list::-webkit-scrollbar { width: 4px; }
.patient-list::-webkit-scrollbar-thumb { background: #0d2d45; border-radius: 2px; }

.patient-list-header {
  display: grid;
  grid-template-columns: 1fr 60px 90px 70px;
  padding: 6px 14px;
  font-size: 10px;
  color: #2a5f80;
  letter-spacing: 1px;
  border-bottom: 1px solid #0a2030;
  flex-shrink: 0;
}

.patient-row {
  display: grid;
  grid-template-columns: 1fr 60px 90px 70px;
  padding: 8px 14px;
  align-items: center;
  border-bottom: 1px solid #060f1a;
  transition: background 0.2s;
  cursor: pointer;
}
.patient-row:hover { background: #0a1e30; }
.row-critical {
  border-left: 2px solid #e74c3c;
  animation: criticalGlow 2s infinite;
}
@keyframes criticalGlow {
  0%,100% { border-left-color: #e74c3c; }
  50% { border-left-color: #e74c3c44; }
}

.patient-info { display: flex; align-items: center; gap: 8px; }
.patient-avatar {
  width: 30px; height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
  opacity: 0.85;
}
.patient-name { font-size: 12px; color: #c0e0f0; font-weight: 600; }
.patient-meta { font-size: 10px; color: #2a5f80; }

/* 分数环 */
.score-wrap { display: flex; justify-content: center; }
.score-ring { position: relative; width: 38px; height: 38px; }
.score-ring svg { width: 100%; height: 100%; transform: rotate(-90deg); }
.score-bg { fill: none; stroke: #0d2d45; stroke-width: 3; }
.score-fg { fill: none; stroke-width: 3; stroke-linecap: round; stroke-dashoffset: 0; transition: stroke-dasharray 1s ease; }
.score-num {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #e0f4ff;
}

.risk-chip {
  font-size: 10px;
  padding: 2px 7px;
  border-radius: 4px;
  font-weight: 600;
}

.patient-status { display: flex; align-items: center; gap: 5px; font-size: 10px; color: #4a7a96; }
.status-light {
  width: 6px; height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-critical { background: #e74c3c; box-shadow: 0 0 6px #e74c3c; animation: dotPulse 1s infinite; }
.status-high     { background: #f39c12; box-shadow: 0 0 4px #f39c12; }
.status-medium   { background: #f39c12; }
.status-low      { background: #27ae60; }

/* ===================== 第三行：底部图表 ===================== */
.row-bottom {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  height: 190px;
}

.panel-trend   { flex: 2; display: flex; flex-direction: column; }
.panel-hospital { flex: 1.2; display: flex; flex-direction: column; }
.panel-assess  { flex: 1; display: flex; flex-direction: column; }

.chart-tabs {
  display: flex;
  gap: 8px;
  margin-left: auto;
}
.chart-tab {
  font-size: 10px;
  padding: 2px 10px;
  border-radius: 12px;
  cursor: pointer;
  color: #2a5f80;
  border: 1px solid #0d2d45;
  transition: all 0.2s;
  letter-spacing: 0.5px;
}
.chart-tab:hover { color: #00d4ff; border-color: #00d4ff44; }
.chart-tab.active { background: #00d4ff22; color: #00d4ff; border-color: #00d4ff55; }

/* ===================== 通用网格线背景 ===================== */
.monitor-body::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(#0d2d4511 1px, transparent 1px),
    linear-gradient(90deg, #0d2d4511 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

/* ===================== 角落装饰 ===================== */
.panel::before {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 12px; height: 12px;
  border-top: 1px solid #00d4ff44;
  border-left: 1px solid #00d4ff44;
}
</style>

