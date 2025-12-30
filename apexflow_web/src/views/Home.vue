<template>
  <div class="home-container">
    <!-- 背景层 -->
    <div class="background"></div>
    
    <!-- 主内容区 -->
    <main class="content">
      <!-- 品牌标识 -->
      <div class="brand">
        <h1>ApexFlow</h1>
        <p>轻量级电商管理平台</p>
      </div>
      
      <!-- 操作按钮组 -->
      <div class="action-buttons">
        <button @click="goToLogin" class="btn primary">登录 管理控制台</button>
        <button @click="enterAsGuest" class="btn secondary">以游客方式进入</button>
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'Home',
  methods: {
    goToLogin() {
      this.$router.push('/login')
    },
    enterAsGuest() {
      // 游客访问路径可根据实际需求修改
      this.$router.push('/guest-dashboard')
    }
  }
}
</script>

<style scoped>
/* 基础样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

/* 容器样式 - 改为浅色主题 */
.home-container {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #f8fafc; /* 浅色背景 */
  overflow: hidden;
}

/* 背景效果 - 适配浅色主题 */
.background {
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(59, 130, 246, 0.08), transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(139, 92, 246, 0.08), transparent 40%);
  z-index: 1;
}

/* 主内容区 */
.content {
  position: relative;
  z-index: 2;
  text-align: center;
  max-width: 600px; /* 适度加宽 */
  width: 100%;
}

/* 品牌标识 - 字体放大并调整颜色 */
.brand {
  margin-bottom: 56px;
}

.brand h1 {
  font-size: 4rem; /* 放大标题 */
  font-weight: 800;
  background: linear-gradient(90deg, #2563eb, #7c3aed); /* 深色渐变适应浅色背景 */
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 16px;
}

.brand p {
  color: #475569; /* 深色文字 */
  font-size: 1.4rem; /* 放大描述文字 */
}

/* 按钮组 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 按钮样式 - 放大并调整颜色 */
.btn {
  padding: 16px 28px; /* 加大内边距 */
  border-radius: 12px;
  font-size: 1.15rem; /* 放大按钮文字 */
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;
}

/* 主按钮 - 适配浅色主题 */
.primary {
  background: linear-gradient(90deg, #2563eb, #7c3aed);
  color: #ffffff;
  box-shadow: 0 10px 25px -5px rgba(59, 130, 246, 0.25);
}

.primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 30px -8px rgba(59, 130, 246, 0.35);
}

/* 次要按钮 - 适配浅色主题 */
.secondary {
  background: rgba(226, 232, 240, 0.8); /* 浅灰色背景 */
  color: #1e293b; /* 深色文字 */
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.secondary:hover {
  background: rgba(226, 232, 240, 1);
  transform: translateY(-3px);
}

/* 响应式调整 - 保持比例放大 */
@media (max-width: 768px) {
  .brand h1 {
    font-size: 3rem;
  }
  
  .brand p {
    font-size: 1.2rem;
  }
  
  .btn {
    padding: 14px 24px;
    font-size: 1.05rem;
  }
}
</style>