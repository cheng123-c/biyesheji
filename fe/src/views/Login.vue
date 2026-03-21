<template>
  <div class="login-container">
    <div class="login-box">
      <h1>健康评测系统</h1>
      <p class="subtitle">用户登录</p>

      <!-- 错误提示 -->
      <div v-if="error" class="alert alert-error">
        <span>{{ error }}</span>
        <button @click="error = null" class="close">&times;</button>
      </div>

      <!-- 登录表单 -->
      <form @submit.prevent="handleLogin">
        <!-- 用户名/邮箱 -->
        <div class="form-group">
          <label for="username">用户名或邮箱</label>
          <input
            id="username"
            v-model="formData.username"
            type="text"
            placeholder="请输入用户名或邮箱"
            required
          />
        </div>

        <!-- 密码 -->
        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            required
          />
        </div>

        <!-- 记住我 -->
        <div class="form-group checkbox">
          <input
            id="remember"
            v-model="rememberMe"
            type="checkbox"
          />
          <label for="remember">记住我</label>
        </div>

        <!-- 登录按钮 -->
        <button
          type="submit"
          class="btn btn-primary"
          :disabled="isLoading"
        >
          {{ isLoading ? '登录中...' : '登录' }}
        </button>
      </form>

      <!-- 注册链接 -->
      <p class="footer">
        还没有账户？
        <router-link to="/register">立即注册</router-link>
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
  password: ''
})

// 记住我
const rememberMe = ref(false)

// 加载和错误状态
const isLoading = ref(false)
const error = ref(null)

// 处理登录
const handleLogin = async () => {
  // 验证表单
  if (!formData.value.username.trim() || !formData.value.password.trim()) {
    error.value = '请填写用户名和密码'
    return
  }

  isLoading.value = true
  error.value = null

  try {
    await authStore.login(formData.value)

    // 如果勾选了记住我，保存凭证
    if (rememberMe.value) {
      localStorage.setItem('rememberMe', 'true')
      localStorage.setItem('savedUsername', formData.value.username)
    } else {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('savedUsername')
    }

    // 登录成功，跳转到首页
    router.push('/')
  } catch (err) {
    error.value = err.response?.data?.message || '登录失败，请检查用户名和密码'
  } finally {
    isLoading.value = false
  }
}

// 页面加载时恢复记住我的信息
if (localStorage.getItem('rememberMe') === 'true') {
  rememberMe.value = true
  formData.value.username = localStorage.getItem('savedUsername') || ''
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  width: 100%;
  max-width: 400px;
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

.form-group input[type="text"],
.form-group input[type="password"] {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input[type="text"]:focus,
.form-group input[type="password"]:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group.checkbox {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.form-group.checkbox input[type="checkbox"] {
  margin-right: 8px;
  width: auto;
  cursor: pointer;
}

.form-group.checkbox label {
  margin-bottom: 0;
  font-weight: normal;
  cursor: pointer;
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
@media (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
  }

  h1 {
    font-size: 24px;
  }
}
</style>

