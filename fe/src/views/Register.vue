<template>
  <div class="register-container">
    <div class="register-box">
      <h1>健康评测系统</h1>
      <p class="subtitle">新用户注册</p>

      <!-- 错误提示 -->
      <div v-if="error" class="alert alert-error">
        <span>{{ error }}</span>
        <button @click="error = null" class="close">&times;</button>
      </div>

      <!-- 成功提示 -->
      <div v-if="success" class="alert alert-success">
        <span>{{ success }}</span>
      </div>

      <!-- 注册表单 -->
      <form @submit.prevent="handleRegister">
        <!-- 用户名 -->
        <div class="form-group">
          <label for="username">用户名 <span class="required">*</span></label>
          <input
            id="username"
            v-model="formData.username"
            type="text"
            placeholder="3-50个字符，支持字母、数字、下划线和连字符"
            required
          />
          <small v-if="errors.username" class="error-text">{{ errors.username }}</small>
        </div>

        <!-- 邮箱 -->
        <div class="form-group">
          <label for="email">邮箱 <span class="required">*</span></label>
          <input
            id="email"
            v-model="formData.email"
            type="email"
            placeholder="请输入有效的邮箱地址"
            required
          />
          <small v-if="errors.email" class="error-text">{{ errors.email }}</small>
        </div>

        <!-- 手机号 -->
        <div class="form-group">
          <label for="phone">手机号</label>
          <input
            id="phone"
            v-model="formData.phone"
            type="tel"
            placeholder="11位手机号"
          />
          <small v-if="errors.phone" class="error-text">{{ errors.phone }}</small>
        </div>

        <!-- 真实姓名 -->
        <div class="form-group">
          <label for="realName">真实姓名</label>
          <input
            id="realName"
            v-model="formData.realName"
            type="text"
            placeholder="请输入真实姓名（可选）"
          />
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <label for="password">密码 <span class="required">*</span></label>
          <input
            id="password"
            v-model="formData.password"
            type="password"
            placeholder="6-50个字符"
            required
          />
          <small v-if="errors.password" class="error-text">{{ errors.password }}</small>
          <small class="hint">密码要求：至少6个字符，建议包含大小写字母、数字和特殊符号</small>
        </div>

        <!-- 确认密码 -->
        <div class="form-group">
          <label for="confirmPassword">确认密码 <span class="required">*</span></label>
          <input
            id="confirmPassword"
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            required
          />
          <small v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</small>
        </div>

        <!-- 同意条款 -->
        <div class="form-group checkbox">
          <input
            id="agree"
            v-model="agreeTerms"
            type="checkbox"
            required
          />
          <label for="agree">我已阅读并同意服务条款和隐私政策</label>
        </div>

        <!-- 注册按钮 -->
        <button
          type="submit"
          class="btn btn-primary"
          :disabled="isLoading"
        >
          {{ isLoading ? '注册中...' : '注册账户' }}
        </button>
      </form>

      <!-- 登录链接 -->
      <p class="footer">
        已有账户？
        <router-link to="/login">直接登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 表单数据
const formData = ref({
  username: '',
  email: '',
  phone: '',
  realName: '',
  password: '',
  confirmPassword: ''
})

// 同意条款
const agreeTerms = ref(false)

// 加载和错误状态
const isLoading = ref(false)
const error = ref(null)
const success = ref(null)

// 字段错误
const errors = ref({})

// 验证表单
const validateForm = () => {
  errors.value = {}

  // 验证用户名
  if (!formData.value.username.trim()) {
    errors.value.username = '用户名不能为空'
  } else if (formData.value.username.length < 3) {
    errors.value.username = '用户名至少3个字符'
  } else if (!/^[a-zA-Z0-9_-]+$/.test(formData.value.username)) {
    errors.value.username = '用户名只能包含字母、数字、下划线和连字符'
  }

  // 验证邮箱
  if (!formData.value.email.trim()) {
    errors.value.email = '邮箱不能为空'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.email)) {
    errors.value.email = '邮箱格式不正确'
  }

  // 验证手机号（可选）
  if (formData.value.phone && !/^1[3-9]\d{9}$/.test(formData.value.phone)) {
    errors.value.phone = '手机号格式不正确'
  }

  // 验证密码
  if (!formData.value.password) {
    errors.value.password = '密码不能为空'
  } else if (formData.value.password.length < 6) {
    errors.value.password = '密码至少6个字符'
  }

  // 验证确认密码
  if (formData.value.password !== formData.value.confirmPassword) {
    errors.value.confirmPassword = '两次输入的密码不一致'
  }

  return Object.keys(errors.value).length === 0
}

// 处理注册
const handleRegister = async () => {
  if (!validateForm()) {
    return
  }

  if (!agreeTerms.value) {
    error.value = '请同意服务条款和隐私政策'
    return
  }

  isLoading.value = true
  error.value = null
  success.value = null

  try {
    await authStore.register(formData.value)
    success.value = '注册成功！正在跳转到登录页面...'

    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (err) {
    error.value = err.response?.data?.message || '注册失败，请稍后重试'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  padding: 20px;
}

.register-box {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 500px;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 10px;
  font-size: 28px;
}

.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 30px;
  font-size: 14px;
}

.alert {
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 20px;
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

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: 500;
  font-size: 14px;
}

.required {
  color: #e74c3c;
}

.form-group input[type="text"],
.form-group input[type="email"],
.form-group input[type="tel"],
.form-group input[type="password"] {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group small {
  display: block;
  margin-top: 5px;
  font-size: 12px;
}

.error-text {
  color: #e74c3c;
}

.hint {
  color: #888;
}

.form-group.checkbox {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
}

.form-group.checkbox input[type="checkbox"] {
  margin-right: 8px;
  margin-top: 4px;
  width: auto;
  cursor: pointer;
}

.form-group.checkbox label {
  margin-bottom: 0;
  font-weight: normal;
  cursor: pointer;
  font-size: 13px;
}

.form-group.checkbox a {
  color: #667eea;
  text-decoration: none;
}

.form-group.checkbox a:hover {
  text-decoration: underline;
}

.btn {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.footer a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.footer a:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 600px) {
  .register-box {
    padding: 30px 20px;
  }

  h1 {
    font-size: 24px;
  }
}
</style>

