<template>
  <div class="health-content-page">
    <div class="page-header">
      <h1>📚 健康知识库</h1>
      <p class="subtitle">专业健康知识，科学指导您的健康管理</p>
    </div>

    <!-- 搜索和过滤 -->
    <div class="search-bar">
      <div class="search-input-wrap">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索健康知识..."
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">🔍 搜索</button>
      </div>
      <div class="filter-tabs">
        <button
          v-for="f in filters"
          :key="f.key"
          :class="['filter-btn', { active: activeFilter === f.key }]"
          @click="applyFilter(f.key)"
        >{{ f.label }}</button>
      </div>
    </div>

    <!-- 内容列表 -->
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="contentList.length === 0" class="empty-state">
      <div class="empty-icon">📭</div>
      <p>暂无相关内容</p>
      <button v-if="searchKeyword" class="btn-secondary" @click="clearSearch">清除搜索</button>
    </div>
    <div v-else class="content-grid">
      <div
        v-for="item in contentList"
        :key="item.id"
        class="content-card"
        @click="openDetail(item)"
      >
        <div class="card-top">
          <span class="content-type-badge" :class="getTypeClass(item.contentType)">
            {{ getTypeLabel(item.contentType) }}
          </span>
          <span class="content-date">{{ formatDate(item.publishedAt) }}</span>
        </div>
        <h3 class="content-title">{{ item.contentTitle }}</h3>
        <p class="content-preview">{{ getPreview(item.contentBody) }}</p>
        <div class="card-bottom">
          <span class="author-info">✍️ {{ item.author || '健康管理团队' }}</span>
          <span v-if="item.contentSource" class="source-info">📖 {{ item.contentSource }}</span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="!isSearchMode && total > pageSize" class="pagination">
      <button :disabled="page <= 1" @click="loadContent(page - 1)">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ Math.ceil(total / pageSize) }} 页</span>
      <button :disabled="page >= Math.ceil(total / pageSize)" @click="loadContent(page + 1)">下一页</button>
    </div>

    <!-- 内容详情弹窗 -->
    <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal-box content-detail-modal">
        <div class="modal-header">
          <div class="detail-header-info">
            <span class="content-type-badge" :class="getTypeClass(currentContent?.contentType)">
              {{ getTypeLabel(currentContent?.contentType) }}
            </span>
            <h2>{{ currentContent?.contentTitle }}</h2>
            <div class="detail-meta">
              <span v-if="currentContent?.author">✍️ {{ currentContent.author }}</span>
              <span v-if="currentContent?.contentSource">📖 {{ currentContent.contentSource }}</span>
              <span>📅 {{ formatDate(currentContent?.publishedAt) }}</span>
            </div>
          </div>
          <button class="close-btn" @click="showDetail = false">✕</button>
        </div>
        <div class="content-body" v-html="renderMarkdown(currentContent?.contentBody)"></div>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showDetail = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHealthContents, searchHealthContent } from '@/services/healthContentApi'

const filters = [
  { key: '', label: '全部' },
  { key: 'article', label: '📰 文章' },
  { key: 'guide', label: '📋 指南' },
  { key: 'video', label: '🎬 视频' }
]

const loading = ref(false)
const contentList = ref([])
const page = ref(1)
const pageSize = ref(9)
const total = ref(0)
const activeFilter = ref('')
const searchKeyword = ref('')
const isSearchMode = ref(false)

const showDetail = ref(false)
const currentContent = ref(null)

onMounted(() => {
  loadContent()
})

async function loadContent(p = 1) {
  loading.value = true
  page.value = p
  isSearchMode.value = false
  try {
    const res = await getHealthContents(p, pageSize.value, activeFilter.value || null)
    const data = res.data || res
    contentList.value = data.list || []
    total.value = data.total || 0
  } catch (e) {
    console.error('加载内容失败:', e)
  } finally {
    loading.value = false
  }
}

function applyFilter(key) {
  activeFilter.value = key
  searchKeyword.value = ''
  loadContent(1)
}

async function handleSearch() {
  if (!searchKeyword.value.trim()) {
    clearSearch()
    return
  }
  loading.value = true
  isSearchMode.value = true
  try {
    const res = await searchHealthContent(searchKeyword.value.trim())
    contentList.value = res.data || res || []
    total.value = contentList.value.length
  } catch (e) {
    console.error('搜索失败:', e)
  } finally {
    loading.value = false
  }
}

function clearSearch() {
  searchKeyword.value = ''
  activeFilter.value = ''
  loadContent(1)
}

function openDetail(item) {
  currentContent.value = item
  showDetail.value = true
}

function getPreview(body) {
  if (!body) return '暂无摘要'
  // 去除 Markdown 标记，取前100字
  return body.replace(/#{1,6}\s/g, '').replace(/\*\*/g, '').replace(/\n/g, ' ').substring(0, 100) + '...'
}

function getTypeLabel(type) {
  const map = { article: '📰 文章', guide: '📋 指南', video: '🎬 视频' }
  return map[type] || '内容'
}

function getTypeClass(type) {
  const map = { article: 'type-article', guide: 'type-guide', video: 'type-video' }
  return map[type] || ''
}

function formatDate(t) {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN')
}

/**
 * 简单 Markdown 渲染：支持标题、粗体、列表
 */
function renderMarkdown(text) {
  if (!text) return ''
  return text
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>\n?)+/g, '<ul>$&</ul>')
    .replace(/^(\d+)\. (.+)$/gm, '<li>$2</li>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br>')
    .replace(/^(.+)$(?!<\/h[23]>)/, '<p>$1</p>')
}
</script>

<style scoped>
.health-content-page {
  padding: 24px;
  max-width: 1100px;
  margin: 0 auto;
}
.page-header { margin-bottom: 24px; }
.page-header h1 { font-size: 1.8rem; margin-bottom: 6px; }
.subtitle { color: #666; font-size: 0.95rem; }

/* 搜索栏 */
.search-bar { margin-bottom: 24px; }
.search-input-wrap { display: flex; gap: 8px; margin-bottom: 14px; }
.search-input {
  flex: 1;
  padding: 10px 16px;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}
.search-input:focus { border-color: #3b82f6; }
.search-btn {
  padding: 10px 20px;
  background: #3b82f6;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.95rem;
  white-space: nowrap;
}
.filter-tabs { display: flex; gap: 8px; flex-wrap: wrap; }
.filter-btn {
  padding: 6px 16px;
  border: 1.5px solid #e5e7eb;
  background: #fff;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.88rem;
  color: #555;
  transition: all 0.15s;
}
.filter-btn.active { border-color: #3b82f6; background: #eff6ff; color: #1d4ed8; }

.loading, .empty-state { text-align: center; padding: 60px; color: #999; }
.empty-icon { font-size: 3rem; margin-bottom: 12px; }

/* 内容网格 */
.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}
.content-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
}
.content-card:hover { box-shadow: 0 4px 20px rgba(0,0,0,0.1); transform: translateY(-2px); }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.content-date { color: #999; font-size: 0.8rem; }
.content-title { font-size: 1.05rem; font-weight: 600; margin-bottom: 10px; line-height: 1.4; }
.content-preview { color: #666; font-size: 0.88rem; line-height: 1.6; flex: 1; margin-bottom: 14px; }
.card-bottom { display: flex; gap: 12px; flex-wrap: wrap; }
.author-info, .source-info { color: #999; font-size: 0.8rem; }

.content-type-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 0.78rem;
  font-weight: 500;
}
.type-article { background: #dbeafe; color: #1d4ed8; }
.type-guide { background: #dcfce7; color: #15803d; }
.type-video { background: #fce7f3; color: #be185d; }

/* 分页 */
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; }
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
.content-detail-modal {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  width: 90%;
  max-width: 720px;
  max-height: 85vh;
  overflow-y: auto;
}
.modal-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.detail-header-info { flex: 1; }
.detail-header-info h2 { font-size: 1.3rem; margin: 8px 0; line-height: 1.4; }
.detail-meta { display: flex; gap: 14px; flex-wrap: wrap; color: #666; font-size: 0.85rem; }
.close-btn { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: #999; margin-left: 12px; }

.content-body {
  line-height: 1.8;
  color: #374151;
  font-size: 0.97rem;
}
.content-body :deep(h2) { font-size: 1.2rem; font-weight: 600; margin: 20px 0 10px; color: #111; }
.content-body :deep(h3) { font-size: 1.05rem; font-weight: 600; margin: 16px 0 8px; color: #333; }
.content-body :deep(strong) { font-weight: 600; color: #111; }
.content-body :deep(ul) { padding-left: 20px; margin: 8px 0; }
.content-body :deep(li) { margin-bottom: 6px; }
.content-body :deep(p) { margin: 10px 0; }

.modal-actions { display: flex; justify-content: flex-end; margin-top: 20px; padding-top: 16px; border-top: 1px solid #e5e7eb; }
.btn-secondary {
  background: #f3f4f6; color: #374151; border: none;
  padding: 8px 18px; border-radius: 8px; cursor: pointer;
}
</style>

