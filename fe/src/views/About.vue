<template>
  <div class="about-container">
    <!-- 页面头部 -->
    <section class="about-hero">
      <div class="hero-icon">🏥</div>
      <h1>基于 DeepSeek AI 的人体健康状态评测系统</h1>
      <p class="hero-subtitle">智能健康管理，让每个人都能轻松掌握自己的健康状态</p>
      <div class="version-badge">版本 v1.0.0 | 毕业设计项目</div>
    </section>

    <!-- 项目简介 -->
    <section class="content-section">
      <h2>📖 项目简介</h2>
      <div class="intro-card">
        <p>
          本系统是一个集 <strong>采集-评估-干预-追踪</strong> 为一体的闭环健康管理平台，
          通过先进的 DeepSeek AI 技术，为用户提供个性化的健康评测、数据分析和干预建议。
        </p>
        <p>
          系统解决了传统健康管理中数据孤岛、缺乏个性化、追踪困难等问题，
          帮助用户实现 <strong>早期预警、精准干预、持续跟踪</strong> 的健康管理目标。
        </p>
      </div>
    </section>

    <!-- 核心功能 -->
    <section class="content-section">
      <h2>⚡ 核心功能</h2>
      <div class="features-grid">
        <div class="feature-item">
          <div class="feature-icon">📊</div>
          <h3>健康数据管理</h3>
          <p>支持心率、血压、血糖、血氧、体温等多维度健康数据的录入与管理，提供历史趋势分析</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon">🤖</div>
          <h3>AI 智能评测</h3>
          <p>集成 DeepSeek AI 大模型，对用户健康数据进行综合分析，生成个性化健康评估报告</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon">🔬</div>
          <h3>症状分析</h3>
          <p>通过 AI 对用户描述的症状进行初步分析，给出可能的病因及就医建议（仅供参考）</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon">💊</div>
          <h3>健康建议</h3>
          <p>根据评测结果自动生成饮食、运动、生活方式等方面的个性化健康改善建议</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon">📋</div>
          <h3>干预方案</h3>
          <p>制定和跟踪个性化健康干预计划，包括运动方案、饮食计划等，并追踪执行进度</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon">🏥</div>
          <h3>医疗记录</h3>
          <p>安全存储用户的就诊记录、体检报告、用药情况等重要医疗信息</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon">🔔</div>
          <h3>智能通知</h3>
          <p>实时推送健康预警、评测完成通知、干预提醒等系统消息</p>
        </div>
        <div class="feature-item">
          <div class="feature-icon">🔒</div>
          <h3>数据安全</h3>
          <p>采用 JWT 身份认证、BCrypt 密码加密、Spring Security 权限控制，全方位保护用户隐私</p>
        </div>
      </div>
    </section>

    <!-- 技术栈 -->
    <section class="content-section">
      <h2>🛠 技术栈</h2>
      <div class="tech-stack">
        <div class="tech-category">
          <h3>🖥️ 前端技术</h3>
          <div class="tech-tags">
            <span class="tag tag-vue">Vue 3</span>
            <span class="tag tag-vite">Vite 4</span>
            <span class="tag tag-pinia">Pinia</span>
            <span class="tag tag-router">Vue Router 4</span>
            <span class="tag tag-axios">Axios</span>
            <span class="tag tag-element">Element Plus</span>
            <span class="tag tag-echarts">ECharts 5</span>
          </div>
        </div>
        <div class="tech-category">
          <h3>⚙️ 后端技术</h3>
          <div class="tech-tags">
            <span class="tag tag-spring">Spring Boot 2.7</span>
            <span class="tag tag-mybatis">MyBatis</span>
            <span class="tag tag-security">Spring Security</span>
            <span class="tag tag-jwt">JWT</span>
            <span class="tag tag-swagger">OpenAPI 3.0</span>
            <span class="tag tag-pagehelper">PageHelper</span>
          </div>
        </div>
        <div class="tech-category">
          <h3>💾 数据存储</h3>
          <div class="tech-tags">
            <span class="tag tag-mysql">MySQL 8.0</span>
            <span class="tag tag-redis">Redis 7</span>
            <span class="tag tag-docker">Docker</span>
            <span class="tag tag-compose">Docker Compose</span>
          </div>
        </div>
        <div class="tech-category">
          <h3>🤖 AI 服务</h3>
          <div class="tech-tags">
            <span class="tag tag-deepseek">DeepSeek AI</span>
            <span class="tag tag-openai">OpenAI 兼容 API</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 系统架构 -->
    <section class="content-section">
      <h2>🏗 系统架构</h2>
      <div class="architecture-card">
        <div class="arch-layer">
          <div class="arch-label">前端层</div>
          <div class="arch-content">Vue 3 SPA（单页应用）</div>
        </div>
        <div class="arch-arrow">↕ HTTP/HTTPS REST API</div>
        <div class="arch-layer">
          <div class="arch-label">API层</div>
          <div class="arch-content">Spring Boot 2.7 + Spring Security (JWT 认证)</div>
        </div>
        <div class="arch-arrow">↕ MyBatis ORM</div>
        <div class="arch-layer">
          <div class="arch-label">数据层</div>
          <div class="arch-content">MySQL 8.0 + Redis 缓存（Docker 容器化）</div>
        </div>
        <div class="arch-arrow">↕ REST API</div>
        <div class="arch-layer arch-ai">
          <div class="arch-label">AI 服务</div>
          <div class="arch-content">DeepSeek AI API（健康分析 + 症状推理）</div>
        </div>
      </div>
    </section>

    <!-- 项目结构 -->
    <section class="content-section">
      <h2>📁 项目结构</h2>
      <div class="code-block">
        <pre>biyesheji/
├── be/                          # 后端项目（Spring Boot）
│   ├── src/main/java/com/health/assessment/
│   │   ├── controller/          # REST API 控制器
│   │   ├── service/             # 业务逻辑层
│   │   ├── mapper/              # MyBatis 数据访问层
│   │   ├── entity/              # 数据实体类
│   │   ├── dto/                 # 数据传输对象
│   │   ├── security/            # JWT 安全配置
│   │   └── config/              # 系统配置
│   └── src/main/resources/
│       ├── mapper/              # MyBatis XML 映射文件
│       └── application.yml      # 应用配置
├── fe/                          # 前端项目（Vue 3）
│   ├── src/
│   │   ├── views/               # 页面组件
│   │   ├── components/          # 公共组件
│   │   ├── services/            # API 服务层
│   │   ├── stores/              # Pinia 状态管理
│   │   └── router/              # Vue Router 路由
│   └── vite.config.js           # Vite 构建配置
├── docker-compose.yml           # Docker 编排配置
├── init.sql                     # 数据库初始化脚本
└── start.sh                     # 一键启动脚本</pre>
      </div>
    </section>

    <!-- 快速开始 -->
    <section class="content-section">
      <h2>🚀 快速开始</h2>
      <div class="steps">
        <div class="step">
          <div class="step-number">1</div>
          <div class="step-content">
            <h4>启动 Docker 服务</h4>
            <code>docker-compose up -d</code>
          </div>
        </div>
        <div class="step">
          <div class="step-number">2</div>
          <div class="step-content">
            <h4>启动后端服务</h4>
            <code>cd be && mvn spring-boot:run</code>
          </div>
        </div>
        <div class="step">
          <div class="step-number">3</div>
          <div class="step-content">
            <h4>启动前端服务</h4>
            <code>cd fe && npm install && npm run dev</code>
          </div>
        </div>
        <div class="step">
          <div class="step-number">4</div>
          <div class="step-content">
            <h4>或者使用一键启动脚本</h4>
            <code>./start.sh</code>
          </div>
        </div>
      </div>
    </section>

    <!-- 作者信息 -->
    <section class="content-section">
      <h2>👨‍💻 关于项目</h2>
      <div class="author-card">
        <p><strong>项目类型：</strong>毕业设计项目</p>
        <p><strong>技术框架：</strong>Spring Boot + Vue 3 + MyBatis + DeepSeek AI</p>
        <p><strong>项目版本：</strong>v1.0.0</p>
        <p class="disclaimer">
          ⚠️ <strong>免责声明：</strong>本系统提供的健康分析、症状分析仅供参考，
          不构成医疗诊断意见。如有健康问题，请咨询专业医疗人员。
        </p>
      </div>
    </section>
  </div>
</template>

<script setup>
</script>

<style scoped>
.about-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

/* Hero 部分 */
.about-hero {
  text-align: center;
  padding: 60px 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  margin-bottom: 40px;
}

.hero-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.about-hero h1 {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 15px;
  line-height: 1.4;
}

.hero-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 20px;
}

.version-badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 13px;
}

/* 内容区块 */
.content-section {
  margin-bottom: 40px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.content-section h2 {
  font-size: 22px;
  color: #333;
  margin-bottom: 25px;
  padding-bottom: 12px;
  border-bottom: 2px solid #667eea;
}

/* 项目简介 */
.intro-card {
  background: #f8f9ff;
  border-left: 4px solid #667eea;
  padding: 20px;
  border-radius: 0 8px 8px 0;
}

.intro-card p {
  color: #555;
  line-height: 1.8;
  margin-bottom: 12px;
}

.intro-card p:last-child {
  margin-bottom: 0;
}

/* 功能网格 */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.feature-item {
  padding: 20px;
  background: #f8f9ff;
  border-radius: 8px;
  border: 1px solid #e8eeff;
  transition: all 0.3s;
}

.feature-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.feature-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.feature-item h3 {
  font-size: 15px;
  color: #333;
  margin-bottom: 8px;
}

.feature-item p {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

/* 技术栈 */
.tech-stack {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 25px;
}

.tech-category h3 {
  font-size: 15px;
  color: #555;
  margin-bottom: 12px;
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  border: 1px solid transparent;
}

.tag-vue { background: #e8f5e8; color: #41b883; border-color: #b3dfb3; }
.tag-vite { background: #fff3e0; color: #ff7043; border-color: #ffccbc; }
.tag-pinia { background: #fff8e1; color: #f59e0b; border-color: #fde68a; }
.tag-router { background: #e8f5e8; color: #41b883; border-color: #b3dfb3; }
.tag-axios { background: #e3f2fd; color: #1565c0; border-color: #90caf9; }
.tag-element { background: #e8f0fe; color: #409eff; border-color: #b3d3ff; }
.tag-echarts { background: #fce4ec; color: #e91e63; border-color: #f48fb1; }
.tag-spring { background: #e8f5e9; color: #43a047; border-color: #a5d6a7; }
.tag-mybatis { background: #e0f2f1; color: #00897b; border-color: #80cbc4; }
.tag-security { background: #fce4ec; color: #c62828; border-color: #ef9a9a; }
.tag-jwt { background: #f3e5f5; color: #7b1fa2; border-color: #ce93d8; }
.tag-swagger { background: #e8f5e9; color: #2e7d32; border-color: #a5d6a7; }
.tag-pagehelper { background: #e0f7fa; color: #00838f; border-color: #80deea; }
.tag-mysql { background: #e3f2fd; color: #1565c0; border-color: #90caf9; }
.tag-redis { background: #ffebee; color: #b71c1c; border-color: #ef9a9a; }
.tag-docker { background: #e3f2fd; color: #1565c0; border-color: #90caf9; }
.tag-compose { background: #e8eaf6; color: #3949ab; border-color: #9fa8da; }
.tag-deepseek { background: linear-gradient(135deg, #667eea, #764ba2); color: white; }
.tag-openai { background: #e8f5e9; color: #2e7d32; border-color: #a5d6a7; }

/* 架构图 */
.architecture-card {
  background: #f8f9ff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e8eeff;
}

.arch-layer {
  display: flex;
  align-items: center;
  background: white;
  border: 2px solid #667eea;
  border-radius: 8px;
  padding: 12px 20px;
  margin: 4px 0;
}

.arch-layer.arch-ai {
  border-color: #764ba2;
  background: #f9f0ff;
}

.arch-label {
  font-weight: bold;
  color: #667eea;
  min-width: 80px;
  font-size: 13px;
}

.arch-layer.arch-ai .arch-label {
  color: #764ba2;
}

.arch-content {
  color: #555;
  font-size: 14px;
  flex: 1;
  text-align: center;
}

.arch-arrow {
  text-align: center;
  color: #999;
  font-size: 12px;
  padding: 4px 0;
}

/* 代码块 */
.code-block {
  background: #1e1e2e;
  border-radius: 8px;
  padding: 20px;
  overflow-x: auto;
}

.code-block pre {
  color: #cdd6f4;
  font-family: 'Courier New', Monaco, monospace;
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
  white-space: pre;
}

/* 快速开始步骤 */
.steps {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.step {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 16px;
  background: #f8f9ff;
  border-radius: 8px;
  border: 1px solid #e8eeff;
}

.step-number {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  flex-shrink: 0;
}

.step-content h4 {
  font-size: 14px;
  color: #333;
  margin-bottom: 6px;
}

.step-content code {
  display: block;
  background: #1e1e2e;
  color: #a6e3a1;
  padding: 8px 12px;
  border-radius: 4px;
  font-family: 'Courier New', Monaco, monospace;
  font-size: 13px;
}

/* 作者卡片 */
.author-card {
  background: #f8f9ff;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e8eeff;
}

.author-card p {
  color: #555;
  margin-bottom: 10px;
  line-height: 1.6;
  font-size: 14px;
}

.author-card p:last-child {
  margin-bottom: 0;
}

.disclaimer {
  background: #fff8e1;
  border-left: 4px solid #f59e0b;
  padding: 12px 16px !important;
  border-radius: 0 6px 6px 0;
  color: #92400e !important;
  margin-top: 15px !important;
}

/* 响应式 */
@media (max-width: 768px) {
  .about-container {
    padding: 15px;
  }

  .about-hero {
    padding: 40px 20px;
  }

  .about-hero h1 {
    font-size: 22px;
  }

  .content-section {
    padding: 20px 15px;
  }

  .features-grid {
    grid-template-columns: 1fr 1fr;
  }

  .tech-stack {
    grid-template-columns: 1fr;
  }

  .arch-layer {
    flex-direction: column;
    text-align: center;
    gap: 8px;
  }

  .arch-label {
    min-width: auto;
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>

