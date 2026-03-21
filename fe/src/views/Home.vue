<template>
  <div class="home-container">
    <!-- Hero 部分 -->
    <section class="hero">
      <div class="hero-content">
        <h1>欢迎来到健康评测系统</h1>
        <p class="subtitle">专业的健康数据管理和评测平台</p>
        <p class="description">
          基于先进的算法和医学知识库，为您提供个性化的健康评测、数据分析和建议
        </p>

        <div class="hero-actions">
          <router-link to="/profile" class="btn btn-primary">查看个人信息</router-link>
          <router-link to="/about" class="btn btn-secondary">了解更多</router-link>
        </div>
      </div>

      <div class="hero-image">
        <div class="placeholder-icon">📊</div>
      </div>
    </section>

    <!-- 功能介绍 -->
    <section class="features">
      <h2>主要功能</h2>

      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-icon">📈</div>
          <h3>健康数据管理</h3>
          <p>安全存储和管理您的健康数据，随时查看和导出</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">🔍</div>
          <h3>数据分析</h3>
          <p>深入分析您的健康数据，发现健康趋势</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">🎯</div>
          <h3>个性化评测</h3>
          <p>根据您的数据提供个性化的健康评测结果</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">💡</div>
          <h3>健康建议</h3>
          <p>获取专业的健康改善建议和指导</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">🔒</div>
          <h3>数据安全</h3>
          <p>采用加密技术保护您的隐私和数据安全</p>
        </div>

        <div class="feature-card">
          <div class="feature-icon">📱</div>
          <h3>跨平台支持</h3>
          <p>在任何设备上随时访问您的健康信息</p>
        </div>
      </div>
    </section>

    <!-- 系统状态 -->
    <section class="status-section">
      <h2>系统状态</h2>

      <div class="status-container">
        <div class="status-card">
          <div class="status-indicator online"></div>
          <h3>后端服务</h3>
          <p v-if="backendStatus">{{ backendStatus === 'UP' ? '正常运行' : '异常' }}</p>
          <p v-else>正在连接...</p>
        </div>

        <div class="status-card">
          <div class="status-indicator online"></div>
          <h3>数据库</h3>
          <p>正常运行</p>
        </div>

        <div class="status-card">
          <div class="status-indicator online"></div>
          <h3>缓存服务</h3>
          <p>正常运行</p>
        </div>
      </div>
    </section>

    <!-- 统计信息 -->
    <section class="stats">
      <div class="stat-item">
        <div class="stat-number">{{ currentUser?.id ? '已登录' : '未登录' }}</div>
        <div class="stat-label">账户状态</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">{{ currentUser?.username || '-' }}</div>
        <div class="stat-label">用户名</div>
      </div>
      <div class="stat-item">
        <div class="stat-number">{{ currentUser?.email || '-' }}</div>
        <div class="stat-label">邮箱</div>
      </div>
    </section>

    <!-- CTA 部分 -->
    <section class="cta">
      <h2>准备好了吗？</h2>
      <p>立即开始管理您的健康数据</p>
      <router-link to="/profile" class="btn btn-primary btn-large">开始使用</router-link>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import axios from 'axios'

const authStore = useAuthStore()
const backendStatus = ref(null)

const currentUser = computed(() => authStore.user)

onMounted(async () => {
  try {
    const response = await axios.get('/api/health')
    backendStatus.value = response.data?.status
  } catch (error) {
    console.error('Failed to fetch backend status:', error)
    backendStatus.value = 'DOWN'
  }
})
</script>

<style scoped>
.home-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}

/* Hero 部分 */
.hero {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  padding: 80px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  margin: 40px;
}

.hero-content h1 {
  font-size: 48px;
  margin-bottom: 20px;
  font-weight: bold;
}

.hero-content .subtitle {
  font-size: 24px;
  margin-bottom: 20px;
  opacity: 0.9;
}

.hero-content .description {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 30px;
  opacity: 0.8;
}

.hero-actions {
  display: flex;
  gap: 15px;
}

.btn {
  display: inline-block;
  padding: 12px 30px;
  border-radius: 6px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.3s;
  cursor: pointer;
  border: none;
  font-size: 16px;
}

.btn-primary {
  background-color: white;
  color: #667eea;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.btn-secondary {
  background-color: transparent;
  color: white;
  border: 2px solid white;
}

.btn-secondary:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.btn-large {
  padding: 15px 40px;
  font-size: 18px;
}

.hero-image {
  display: flex;
  justify-content: center;
  align-items: center;
}

.placeholder-icon {
  font-size: 120px;
  opacity: 0.9;
}

/* 功能部分 */
.features {
  padding: 80px 40px;
  background: white;
}

.features h2 {
  text-align: center;
  font-size: 36px;
  margin-bottom: 50px;
  color: #333;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.feature-card {
  padding: 30px;
  background: #f9f9f9;
  border-radius: 8px;
  text-align: center;
  transition: all 0.3s;
  border: 1px solid #eee;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  background: white;
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}

/* 系统状态 */
.status-section {
  padding: 80px 40px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.status-section h2 {
  text-align: center;
  font-size: 36px;
  margin-bottom: 50px;
  color: #333;
}

.status-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
  max-width: 900px;
  margin: 0 auto;
}

.status-card {
  background: white;
  padding: 30px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.status-indicator {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin: 0 auto 15px;
  animation: pulse 2s infinite;
}

.status-indicator.online {
  background-color: #4CAF50;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.7);
  }
  50% {
    box-shadow: 0 0 0 10px rgba(76, 175, 80, 0);
  }
}

.status-card h3 {
  margin-bottom: 10px;
  color: #333;
}

.status-card p {
  color: #666;
}

/* 统计信息 */
.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  padding: 60px 40px;
  background: white;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* CTA 部分 */
.cta {
  padding: 80px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
}

.cta h2 {
  font-size: 36px;
  margin-bottom: 20px;
}

.cta p {
  font-size: 18px;
  margin-bottom: 40px;
  opacity: 0.9;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 60px 30px;
    gap: 40px;
  }

  .hero-content h1 {
    font-size: 36px;
  }

  .placeholder-icon {
    font-size: 80px;
  }
}

@media (max-width: 768px) {
  .home-container {
    padding: 0;
  }

  .hero {
    padding: 40px 20px;
    margin: 20px;
    border-radius: 8px;
  }

  .hero-content h1 {
    font-size: 28px;
  }

  .hero-content .subtitle {
    font-size: 18px;
  }

  .hero-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    text-align: center;
  }

  .features,
  .status-section,
  .stats,
  .cta {
    padding: 40px 20px;
  }

  .features h2,
  .status-section h2,
  .cta h2 {
    font-size: 28px;
  }

  .feature-grid,
  .status-container {
    gap: 20px;
  }

  .feature-card {
    padding: 20px;
  }

  .cta h2 {
    font-size: 24px;
  }
}
</style>

