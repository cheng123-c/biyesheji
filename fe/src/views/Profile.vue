<template>
  <div class="profile-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>👤 个人信息</h1>
      <p>管理您的账户信息和安全设置</p>
    </div>

    <div v-if="isLoading" class="loading-box">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else class="profile-layout">
      <!-- 左侧：头像与快捷操作 -->
      <div class="left-panel">
        <!-- 头像卡片 -->
        <div class="card avatar-card">
          <div class="avatar-wrap">
            <div class="avatar-circle">
              <img v-if="user.avatarUrl" :src="user.avatarUrl" alt="头像" class="avatar-img" />
              <span v-else class="avatar-initial">{{ userInitial }}</span>
            </div>
          </div>
          <div class="avatar-info">
            <div class="avatar-name">{{ user.realName || user.username }}</div>
            <div class="avatar-username">@{{ user.username }}</div>
            <div class="avatar-status">
              <span class="status-dot" :class="user.status === 'ACTIVE' ? 'dot-active' : 'dot-inactive'"></span>
              {{ statusLabel(user.status) }}
            </div>
          </div>
        </div>

        <!-- 账户摘要 -->
        <div class="card summary-card">
          <h3>账户摘要</h3>
          <div class="summary-items">
            <div class="summary-item">
              <span class="summary-icon">📅</span>
              <div>
                <div class="summary-label">注册时间</div>
                <div class="summary-value">{{ formatDate(user.createdAt) }}</div>
              </div>
            </div>
            <div class="summary-item">
              <span class="summary-icon">🔄</span>
              <div>
                <div class="summary-label">最后更新</div>
                <div class="summary-value">{{ formatDate(user.updatedAt) }}</div>
              </div>
            </div>
            <div class="summary-item">
              <span class="summary-icon">🆔</span>
              <div>
                <div class="summary-label">用户ID</div>
                <div class="summary-value">{{ user.id || '-' }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 快速导航 -->
        <div class="card quick-nav-card">
          <h3>快捷入口</h3>
          <div class="quick-links">
            <router-link to="/health-data" class="quick-link">
              <span class="ql-icon">📊</span><span>健康数据</span>
            </router-link>
            <router-link to="/assessment" class="quick-link">
              <span class="ql-icon">🧠</span><span>AI 评测</span>
            </router-link>
            <router-link to="/medical-records" class="quick-link">
              <span class="ql-icon">🏥</span><span>医疗记录</span>
            </router-link>
            <router-link to="/" class="quick-link">
              <span class="ql-icon">🏠</span><span>首页仪表板</span>
            </router-link>
          </div>
        </div>
      </div>

      <!-- 右侧：信息编辑区 -->
      <div class="right-panel">
        <!-- 提示信息 -->
        <transition name="fade">
          <div v-if="error" class="alert alert-error">
            <span>⚠️ {{ error }}</span>
            <button @click="error = null" class="alert-close">&times;</button>
          </div>
        </transition>
        <transition name="fade">
          <div v-if="success" class="alert alert-success">
            <span>✅ {{ success }}</span>
            <button @click="success = null" class="alert-close">&times;</button>
          </div>
        </transition>

        <!-- 基本信息卡 -->
        <div class="card info-card">
          <div class="card-header">
            <h2>📋 基本信息</h2>
            <button v-if="!editingBasic" @click="startEditBasic" class="btn btn-edit">编辑</button>
          </div>

          <!-- 编辑模式 -->
          <div v-if="editingBasic" class="edit-form">
            <div class="form-row">
              <div class="form-group">
                <label>真实姓名</label>
                <input v-model="editForm.realName" type="text" placeholder="请输入真实姓名" />
              </div>
              <div class="form-group">
                <label>年龄</label>
                <input v-model.number="editForm.age" type="number" min="1" max="150" placeholder="请输入年龄" />
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>性别</label>
                <select v-model="editForm.gender">
                  <option value="MALE">男</option>
                  <option value="FEMALE">女</option>
                  <option value="UNKNOWN">不填写</option>
                </select>
              </div>
              <div class="form-group">
                <label>头像URL（可选）</label>
                <input v-model="editForm.avatarUrl" type="url" placeholder="https://..." />
              </div>
            </div>
            <div class="form-group full-width">
              <label>个人简介</label>
              <textarea v-model="editForm.bio" rows="3" placeholder="介绍一下自己..."></textarea>
            </div>
            <div class="form-actions">
              <button @click="handleSaveBasic" class="btn btn-primary" :disabled="savingBasic">
                {{ savingBasic ? '保存中...' : '保存修改' }}
              </button>
              <button @click="cancelEditBasic" class="btn btn-cancel">取消</button>
            </div>
          </div>

          <!-- 展示模式 -->
          <div v-else class="info-display">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ user.username || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ user.email || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">手机号</span>
                <span class="info-value">{{ user.phone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">真实姓名</span>
                <span class="info-value">{{ user.realName || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">年龄</span>
                <span class="info-value">{{ user.age || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">性别</span>
                <span class="info-value">{{ genderLabel(user.gender) }}</span>
              </div>
            </div>
            <div class="info-bio" v-if="user.bio">
              <span class="info-label">个人简介</span>
              <p class="bio-text">{{ user.bio }}</p>
            </div>
          </div>
        </div>

        <!-- 安全设置卡 -->
        <div class="card security-card">
          <div class="card-header">
            <h2>🔐 安全设置</h2>
          </div>

          <!-- 修改密码 -->
          <div v-if="changingPassword" class="edit-form">
            <div class="form-group">
              <label>旧密码</label>
              <input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" />
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>新密码</label>
                <input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
              </div>
              <div class="form-group">
                <label>确认新密码</label>
                <input v-model="passwordForm.confirmNewPassword" type="password" placeholder="再次输入新密码" />
              </div>
            </div>
            <div class="form-actions">
              <button @click="handleChangePassword" class="btn btn-primary" :disabled="savingPassword">
                {{ savingPassword ? '修改中...' : '确认修改' }}
              </button>
              <button @click="changingPassword = false" class="btn btn-cancel">取消</button>
            </div>
          </div>

          <div v-else class="security-actions">
            <div class="security-item">
              <div class="security-info">
                <span class="security-icon">🔑</span>
                <div>
                  <div class="security-title">登录密码</div>
                  <div class="security-desc">定期更换密码可以提高账户安全性</div>
                </div>
              </div>
              <button @click="changingPassword = true" class="btn btn-outline">修改密码</button>
            </div>
            <div class="security-item danger-zone">
              <div class="security-info">
                <span class="security-icon">🗑️</span>
                <div>
                  <div class="security-title danger-title">删除账户</div>
                  <div class="security-desc">删除后数据将无法恢复，请谨慎操作</div>
                </div>
              </div>
              <button @click="handleDeleteAccount" class="btn btn-danger">删除账户</button>
            </div>
          </div>
        </div>

        <!-- 登出按钮 -->
        <div class="card logout-card">
          <button @click="handleLogout" class="btn btn-logout-full">
            <span>🚪</span> 退出登录
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 状态
const isLoading = ref(true)
const editingBasic = ref(false)
const changingPassword = ref(false)
const error = ref(null)
const success = ref(null)
const savingBasic = ref(false)
const savingPassword = ref(false)

// 用户信息
const user = computed(() => authStore.user || {})

// 用户首字母
const userInitial = computed(() => {
  const name = user.value.realName || user.value.username
  return name ? name.charAt(0).toUpperCase() : 'U'
})

// 编辑表单
const editForm = ref({
  realName: '',
  age: null,
  gender: 'UNKNOWN',
  bio: '',
  avatarUrl: ''
})

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmNewPassword: ''
})

// 页面加载时获取用户信息
onMounted(async () => {
  try {
    await authStore.fetchCurrentUser()
    initEditForm()
  } catch (err) {
    error.value = '加载用户信息失败'
  } finally {
    isLoading.value = false
  }
})

// 初始化编辑表单
const initEditForm = () => {
  editForm.value = {
    realName: user.value.realName || '',
    age: user.value.age || null,
    gender: user.value.gender || 'UNKNOWN',
    bio: user.value.bio || '',
    avatarUrl: user.value.avatarUrl || ''
  }
}

// 开始编辑
const startEditBasic = () => {
  initEditForm()
  editingBasic.value = true
}

// 取消编辑
const cancelEditBasic = () => {
  editingBasic.value = false
  initEditForm()
}

// 保存基本信息
const handleSaveBasic = async () => {
  savingBasic.value = true
  try {
    error.value = null
    success.value = null
    await authStore.updateProfile(editForm.value)
    success.value = '信息更新成功'
    editingBasic.value = false
    setTimeout(() => {
      success.value = null
    }, 2500)
  } catch (err) {
    error.value = err.response?.data?.message || '信息更新失败'
  } finally {
    savingBasic.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  // 验证
  if (!passwordForm.value.oldPassword) {
    error.value = '请输入旧密码'
    return
  }
  if (!passwordForm.value.newPassword) {
    error.value = '请输入新密码'
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    error.value = '新密码长度至少为6位'
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmNewPassword) {
    error.value = '两次输入的新密码不一致'
    return
  }

  savingPassword.value = true
  try {
    error.value = null
    success.value = null
    await authStore.changePassword(passwordForm.value)
    success.value = '密码修改成功'
    changingPassword.value = false
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmNewPassword: ''
    }
    setTimeout(() => {
      success.value = null
    }, 2500)
  } catch (err) {
    error.value = err.response?.data?.message || '密码修改失败'
  } finally {
    savingPassword.value = false
  }
}

// 删除账户
const handleDeleteAccount = async () => {
  if (confirm('确定要删除账户吗？此操作不可撤销！所有数据将被永久删除。')) {
    try {
      error.value = null
      await authStore.deleteAccount()
      success.value = '账户已删除，正在跳转...'
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } catch (err) {
      error.value = '账户删除失败'
    }
  }
}

// 登出
const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    authStore.logout()
    router.push('/login')
  }
}

// 辅助函数
const genderLabel = (gender) => {
  const labels = {
    'MALE': '男',
    'FEMALE': '女',
    'UNKNOWN': '未设置'
  }
  return labels[gender] || '未设置'
}

const statusLabel = (status) => {
  const labels = {
    'ACTIVE': '活跃',
    'INACTIVE': '非活跃',
    'BANNED': '已禁用'
  }
  return labels[status] || status
}

const formatDate = (date) => {
  if (!date) return '未知'
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.profile-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 20px;
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

.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #888;
  gap: 16px;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e0e0e0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 布局 */
.profile-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.right-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 卡片通用 */
.card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.card h2 {
  font-size: 16px;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.card h3 {
  font-size: 14px;
  color: #555;
  margin: 0 0 14px 0;
  font-weight: 600;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f0f0f0;
}

/* 头像卡片 */
.avatar-card {
  text-align: center;
  padding: 24px;
}

.avatar-wrap {
  margin-bottom: 16px;
}

.avatar-circle {
  width: 90px;
  height: 90px;
  margin: 0 auto;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-initial {
  font-size: 36px;
  font-weight: bold;
  color: white;
}

.avatar-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.avatar-username {
  font-size: 13px;
  color: #888;
  margin-bottom: 10px;
}

.avatar-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-active { background: #27ae60; }
.dot-inactive { background: #bbb; }

/* 摘要卡片 */
.summary-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f9faff;
  border-radius: 8px;
}

.summary-icon {
  font-size: 20px;
}

.summary-label {
  font-size: 11px;
  color: #aaa;
  margin-bottom: 2px;
}

.summary-value {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

/* 快捷入口 */
.quick-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.quick-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f9faff;
  border-radius: 8px;
  text-decoration: none;
  color: #555;
  font-size: 13px;
  transition: all 0.2s;
}

.quick-link:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
}

.ql-icon { font-size: 16px; }

/* 信息展示 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #aaa;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #333;
}

.info-bio {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.bio-text {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #555;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 表单 */
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: #555;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

/* 按钮 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-cancel {
  background: #f0f0f0;
  color: #666;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-edit {
  background: #f0f3ff;
  color: #667eea;
  padding: 6px 14px;
  font-size: 13px;
}

.btn-edit:hover {
  background: #667eea;
  color: white;
}

.btn-outline {
  background: white;
  border: 1px solid #ddd;
  color: #555;
}

.btn-outline:hover {
  border-color: #667eea;
  color: #667eea;
}

.btn-danger {
  background: #fff5f5;
  border: 1px solid #ffcdd2;
  color: #e53935;
}

.btn-danger:hover {
  background: #e53935;
  color: white;
  border-color: #e53935;
}

/* 安全设置 */
.security-actions {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  background: #f9faff;
  border-radius: 10px;
}

.security-item.danger-zone {
  background: #fff5f5;
}

.security-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.security-icon {
  font-size: 22px;
}

.security-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.danger-title {
  color: #e53935;
}

.security-desc {
  font-size: 12px;
  color: #888;
}

/* 登出按钮 */
.logout-card {
  padding: 0;
  overflow: hidden;
}

.btn-logout-full {
  width: 100%;
  padding: 14px;
  background: #f9faff;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-logout-full:hover {
  background: #667eea;
  color: white;
}

/* 提示框 */
.alert {
  padding: 12px 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.alert-error {
  background: #fff5f5;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

.alert-success {
  background: #f1f8e9;
  color: #33691e;
  border: 1px solid #c5e1a5;
}

.alert-close {
  background: none;
  border: none;
  color: inherit;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .left-panel {
    order: -1;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .quick-links {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 480px) {
  .profile-container {
    padding: 12px;
  }

  .quick-links {
    grid-template-columns: repeat(2, 1fr);
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .btn {
    width: 100%;
  }

  .security-item {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .security-item .btn {
    width: 100%;
  }
}
</style>

