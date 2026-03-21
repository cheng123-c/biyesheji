<template>
  <div class="not-found-container">
    <div class="content">
      <h1>404</h1>
      <p>页面未找到</p>
      <p class="description">抱歉，您访问的页面不存在或已被移除</p>
      <router-link to="/" class="btn">返回首页</router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.content {
  text-align: center;
  background: white;
  padding: 60px 40px;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

h1 {
  font-size: 80px;
  margin: 0;
  color: #667eea;
  font-weight: bold;
}

p {
  margin: 20px 0;
  color: #666;
  font-size: 18px;
}

.description {
  color: #999;
  font-size: 14px;
  margin-bottom: 30px;
}

.btn {
  display: inline-block;
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  border-radius: 4px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}
</style>

