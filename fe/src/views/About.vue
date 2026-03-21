<template>
  <div class="about">
    <h1>关于本项目</h1>
    <div class="content">
      <section>
        <h2>技术栈</h2>
        <ul>
          <li><strong>前端:</strong> Vue 3 + Vite + Pinia + Vue Router</li>
          <li><strong>后端:</strong> Spring Boot 2.7 + Spring Data JPA</li>
          <li><strong>数据库:</strong> MySQL 8.0</li>
          <li><strong>构建工具:</strong> Maven (后端) / Vite (前端)</li>
        </ul>
      </section>

      <section>
        <h2>项目结构</h2>
        <pre>biyesheji/
├── be/                    # 后端项目
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/
│           │   ├── BiyeshejApplication.java
│           │   ├── controller/
│           │   ├── entity/
│           │   ├── repository/
│           │   ├── service/
│           │   └── common/
│           └── resources/
│               └── application.yml
├── fe/                    # 前端项目
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── router/
│       ├── views/
│       ├── components/
│       └── stores/
└── start.sh             # 安装脚本</pre>
      </section>

      <section>
        <h2>快速开始</h2>
        <p>运行项目根目录下的 <code>./start.sh</code> 脚本自动安装依赖并启动项目。</p>
      </section>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.about {
  max-width: 800px;
  margin: 0 auto;
}

.about h1 {
  color: #333;
  text-align: center;
  margin-bottom: 2rem;
}

.content {
  color: #666;
}

.content section {
  margin-bottom: 2rem;
  padding: 1rem;
  border-left: 4px solid #4CAF50;
  background-color: #f9f9f9;
}

.content h2 {
  color: #333;
  margin-top: 0;
}

.content ul {
  margin: 0.5rem 0;
  padding-left: 1.5rem;
}

.content li {
  margin: 0.5rem 0;
}

.content code {
  background-color: #e8e8e8;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
}

.content pre {
  background-color: #f5f5f5;
  padding: 1rem;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 0.9rem;
}
</style>

