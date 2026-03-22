<template>
  <div class="notifications-container">
    <div class="page-header">
      <h1>系统通知</h1>
      <button
        v-if="notifications.length > 0"
        @click="handleMarkAllRead"
        class="btn btn-outline"
      >
        全部标记已读
      </button>
    </div>

    <div v-if="error" class="alert alert-error">
      {{ error }}
      <button @click="error = null" class="close">&times;</button>
    </div>

    <div v-if="loading" class="loading-spinner">加载中...</div>

    <div v-else-if="notifications.length === 0" class="empty-state">
      <div class="empty-icon">🔔</div>
      <p>暂无通知消息</p>
    </div>

    <div v-else class="notifications-list">
      <div
        v-for="notif in notifications"
        :key="notif.id"
        class="notification-item"
        :class="{ unread: notif.readStatus === 0 }"
      >
        <div class="notif-left">
          <div class="notif-dot" :class="{ unread: notif.readStatus === 0 }"></div>
          <div class="notif-type-icon">{{ getTypeIcon(notif.notificationType) }}</div>
        </div>

        <div class="notif-body">
          <div class="notif-header">
            <span class="notif-title">{{ notif.title }}</span>
            <span class="notif-time">{{ formatTime(notif.createdAt) }}</span>
          </div>
          <div class="notif-content">{{ notif.content }}</div>
          <div v-if="notif.readStatus === 0" class="notif-unread-label">未读</div>
        </div>

        <div class="notif-actions">
          <button
            v-if="notif.readStatus === 0"
            @click="handleMarkRead(notif.id)"
            class="btn-icon btn-read"
            title="标记已读"
          >✓</button>
          <button
            @click="handleDelete(notif.id)"
            class="btn-icon btn-delete"
            title="删除"
          >×</button>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <button :disabled="currentPage <= 1" @click="loadPage(currentPage - 1)" class="btn btn-page">上一页</button>
        <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
        <button :disabled="currentPage >= totalPages" @click="loadPage(currentPage + 1)" class="btn btn-page">下一页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  getNotifications,
  markNotificationRead,
  markAllNotificationsRead,
  deleteNotification
} from '@/services/assessmentApi'

const notifications = ref([])
const loading = ref(true)
const error = ref(null)
const currentPage = ref(1)
const totalPages = ref(1)

onMounted(() => loadPage(1))

const loadPage = async (page) => {
  loading.value = true
  try {
    const response = await getNotifications(page, 20)
    notifications.value = response.data?.list || []
    totalPages.value = response.data?.pages || 1
    currentPage.value = page
  } catch (err) {
    error.value = '加载通知失败'
  } finally {
    loading.value = false
  }
}

const handleMarkRead = async (id) => {
  try {
    await markNotificationRead(id)
    const item = notifications.value.find(n => n.id === id)
    if (item) item.readStatus = 1
  } catch {
    error.value = '操作失败'
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach(n => n.readStatus = 1)
  } catch {
    error.value = '操作失败'
  }
}

const handleDelete = async (id) => {
  try {
    await deleteNotification(id)
    notifications.value = notifications.value.filter(n => n.id !== id)
  } catch {
    error.value = '删除失败'
  }
}

const getTypeIcon = (type) => {
  const icons = {
    assessment: '🔬',
    alert: '⚠️',
    suggestion: '💡',
    reminder: '⏰'
  }
  return icons[type] || '🔔'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.notifications-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0;
}

.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: white;
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  transition: all 0.2s;
}

.notification-item.unread {
  background: #f5f8ff;
  border-color: #c3d4ff;
}

.notif-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding-top: 3px;
}

.notif-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ccc;
  flex-shrink: 0;
}

.notif-dot.unread { background: #667eea; }

.notif-type-icon { font-size: 18px; }

.notif-body {
  flex: 1;
}

.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.notif-title {
  font-weight: 600;
  font-size: 15px;
  color: #333;
}

.notif-time {
  font-size: 12px;
  color: #bbb;
}

.notif-content {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

.notif-unread-label {
  display: inline-block;
  margin-top: 6px;
  padding: 2px 8px;
  background: #667eea;
  color: white;
  font-size: 11px;
  border-radius: 10px;
}

.notif-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.btn-icon {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-read { background: #d4edda; color: #155724; }
.btn-read:hover { background: #c3e6cb; }
.btn-delete { background: #fee; color: #c33; }
.btn-delete:hover { background: #fcc; }

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
}

.btn-page:disabled { opacity: 0.4; cursor: not-allowed; }

.btn-outline {
  padding: 8px 18px;
  border: 1px solid #667eea;
  color: #667eea;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-outline:hover {
  background: #667eea;
  color: white;
}

.loading-spinner { text-align: center; padding: 40px; color: #888; }

.empty-state {
  text-align: center;
  padding: 60px;
  color: #aaa;
}

.empty-icon { font-size: 60px; margin-bottom: 16px; }

.alert {
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-error { background: #fee; color: #c33; border: 1px solid #fcc; }

.close {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  color: inherit;
}
</style>

