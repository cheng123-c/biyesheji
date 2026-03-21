<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>个人信息</h1>
      <button @click="handleLogout" class="btn btn-logout">登出</button>
    </div>

    <div v-if="isLoading" class="loading">加载中...</div>

    <div v-else class="profile-content">
      <!-- 基本信息卡 -->
      <div class="card">
        <h2>基本信息</h2>

        <div v-if="editingBasic" class="form">
          <div class="form-group">
            <label>真实姓名</label>
            <input v-model="editForm.realName" type="text" />
          </div>
          <div class="form-group">
            <label>年龄</label>
            <input v-model.number="editForm.age" type="number" />
          </div>
          <div class="form-group">
            <label>性别</label>
            <select v-model="editForm.gender">
              <option value="MALE">男</option>
              <option value="FEMALE">女</option>
              <option value="UNKNOWN">未知</option>
            </select>
          </div>
          <div class="form-group">
            <label>个人简介</label>
            <textarea v-model="editForm.bio" rows="4"></textarea>
          </div>
          <div class="button-group">
            <button @click="handleSaveBasic" class="btn btn-primary">保存</button>
            <button @click="editingBasic = false" class="btn btn-cancel">取消</button>
          </div>
        </div>

        <div v-else class="info">
          <div class="info-row">
            <span class="label">用户名:</span>
            <span class="value">{{ user.username }}</span>
          </div>
          <div class="info-row">
            <span class="label">邮箱:</span>
            <span class="value">{{ user.email }}</span>
          </div>
          <div class="info-row">
            <span class="label">手机号:</span>
            <span class="value">{{ user.phone || '未设置' }}</span>
          </div>
          <div class="info-row">
            <span class="label">真实姓名:</span>
            <span class="value">{{ user.realName || '未设置' }}</span>
          </div>
          <div class="info-row">
            <span class="label">年龄:</span>
            <span class="value">{{ user.age || '未设置' }}</span>
          </div>
          <div class="info-row">
            <span class="label">性别:</span>
            <span class="value">{{ genderLabel(user.gender) }}</span>
          </div>
          <div class="info-row">
            <span class="label">个人简介:</span>
            <span class="value">{{ user.bio || '未设置' }}</span>
          </div>
          <div class="info-row">
            <span class="label">账户状态:</span>
            <span class="value status" :class="user.status">{{ statusLabel(user.status) }}</span>
          </div>
          <div class="info-row">
            <span class="label">注册时间:</span>
            <span class="value">{{ formatDate(user.createdAt) }}</span>
          </div>
          <button @click="editingBasic = true" class="btn btn-primary">编辑信息</button>
        </div>
      </div>

      <!-- 安全设置卡 -->
      <div class="card">
        <h2>安全设置</h2>

        <div v-if="changingPassword" class="form">
          <div class="form-group">
            <label>旧密码</label>
            <input v-model="passwordForm.oldPassword" type="password" />
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input v-model="passwordForm.newPassword" type="password" />
          </div>
          <div class="form-group">
            <label>确认新密码</label>
            <input v-model="passwordForm.confirmNewPassword" type="password" />
          </div>
          <div class="button-group">
            <button @click="handleChangePassword" class="btn btn-primary">保存</button>
            <button @click="changingPassword = false" class="btn btn-cancel">取消</button>
          </div>
        </div>

        <div v-else class="security-actions">
          <button @click="changingPassword = true" class="btn btn-primary">修改密码</button>
          <button @click="handleDeleteAccount" class="btn btn-danger">删除账户</button>
        </div>
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

// 用户信息
const user = computed(() => authStore.user || {})

// 编辑表单
const editForm = ref({
  realName: '',
  age: null,
  gender: 'UNKNOWN',
  bio: ''
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
    bio: user.value.bio || ''
  }
}

// 保存基本信息
const handleSaveBasic = async () => {
  try {
    error.value = null
    success.value = null
    await authStore.updateProfile(editForm.value)
    success.value = '信息更新成功'
    editingBasic.value = false
    setTimeout(() => {
      success.value = null
    }, 2000)
  } catch (err) {
    error.value = '信息更新失败'
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
  if (passwordForm.value.newPassword !== passwordForm.value.confirmNewPassword) {
    error.value = '两次输入的新密码不一致'
    return
  }

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
    }, 2000)
  } catch (err) {
    error.value = err.response?.data?.message || '密码修改失败'
  }
}

// 删除账户
const handleDeleteAccount = async () => {
  if (confirm('确定要删除账户吗？此操作不可撤销！')) {
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
  if (confirm('确定要登出吗？')) {
    authStore.logout()
    router.push('/login')
  }
}

// 辅助函数
const genderLabel = (gender) => {
  const labels = {
    'MALE': '男',
    'FEMALE': '女',
    'UNKNOWN': '未知'
  }
  return labels[gender] || gender
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
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #eee;
}

.profile-header h1 {
  margin: 0;
  color: #333;
}

.btn-logout {
  background-color: #e74c3c;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.btn-logout:hover {
  background-color: #c0392b;
}

.loading {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

.profile-content {
  display: grid;
  gap: 20px;
}

.card {
  background: white;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}

.info {
  display: grid;
  gap: 15px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.info-row .label {
  font-weight: 600;
  color: #555;
  min-width: 120px;
}

.info-row .value {
  color: #333;
  text-align: right;
  flex: 1;
}

.info-row .value.status {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.info-row .value.status.ACTIVE {
  background-color: #d4edda;
  color: #155724;
}

.form {
  display: grid;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 8px;
  font-weight: 600;
  color: #555;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.button-group {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-primary {
  background-color: #667eea;
  color: white;
}

.btn-primary:hover {
  background-color: #5568d3;
  transform: translateY(-2px);
}

.btn-cancel {
  background-color: #ccc;
  color: #333;
}

.btn-cancel:hover {
  background-color: #bbb;
}

.btn-danger {
  background-color: #e74c3c;
  color: white;
}

.btn-danger:hover {
  background-color: #c0392b;
}

.security-actions {
  display: flex;
  gap: 10px;
}

.alert {
  padding: 12px 16px;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-error {
  background-color: #fee;
  color: #c33;
  border: 1px solid #fcc;
}

.alert-success {
  background-color: #efe;
  color: #3c3;
  border: 1px solid #cfc;
}

.alert .close {
  background: none;
  border: none;
  color: inherit;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  margin-left: 10px;
}

/* 响应式设计 */
@media (max-width: 600px) {
  .profile-header {
    flex-direction: column;
    gap: 15px;
  }

  .profile-header h1 {
    width: 100%;
  }

  .btn-logout {
    width: 100%;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-row .value {
    text-align: left;
  }

  .button-group {
    flex-direction: column;
  }

  .button-group .btn {
    width: 100%;
  }

  .security-actions {
    flex-direction: column;
  }

  .security-actions .btn {
    width: 100%;
  }
}
</style>

