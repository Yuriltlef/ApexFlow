<template>
  <div class="home-container">
    <div class="background-anim"></div>
    
    <main class="content">
      <div class="brand">
        <h1 class="animate-title">ApexFlow</h1>
        <p>轻量级现代化电商管理平台</p>
      </div>
      
      <div class="action-buttons">
        <button @click="openGithub" class="btn github-btn">
          <svg height="20" viewBox="0 0 16 16" width="20" class="github-icon">
            <path fill="currentColor" d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"></path>
          </svg>
          开源仓库
        </button>

        <button @click="goToLogin" class="btn login-btn">
          登录控制台
        </button>

        <button @click="enterAsGuest" class="btn guest-btn">
          游客体验
        </button>
      </div>

      <div class="info-cards">
        <div class="card">
          <div class="card-icon">🏗️</div>
          <h3>项目架构</h3>
          <p>基于 Vue 3 + Vite 前端生态，配合 Java Servlet 后端与 MySQL 数据库，打造轻量高效的单体架构。</p>
        </div>
        <div class="card">
          <div class="card-icon">⚡</div>
          <h3>技术路线</h3>
          <p>集成 Element Plus 组件库、ECharts 数据可视化与 Axios 网络请求，实现现代化的 SPA 体验。</p>
        </div>
        <div class="card">
          <div class="card-icon">📚</div>
          <h3>使用帮助</h3>
          <p>支持管理员与游客双模式。涵盖订单处理、物流追踪、库存盘点及财务报表分析全流程。</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import userDataManager from '@/utils/userData'
import { ElMessage } from 'element-plus'

export default {
  name: 'Home',
  methods: {
    goToLogin() {
      this.$router.push('/login')
    },
    enterAsGuest() {
      const guestUser = {
        username: 'Guest',
        realName: '访客',
        avatar: '', 
        isGuest: true
      }
      const guestPermissions = {
        canViewDashboard: true,
        isAdmin: false
      }

      if (userDataManager.setUserData(guestUser, guestPermissions)) {
        userDataManager.setToken('guest-mock-token-' + Date.now())
        ElMessage.success('已进入游客体验模式')
        this.$router.push('/dashboard')
      } else {
        ElMessage.error('进入失败')
      }
    },
    openGithub() {
      window.open('https://github.com/Yuriltlef/ApexFlow', '_blank')
    }
  }
}
</script>

<style scoped>
/* 容器布局 */
.home-container {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 动态浅色渐变背景 */
.background-anim {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    #fdfbfb,
    #ebedee,
    #f3e7e9,
    #e3eeff,
    #e9f3e7,
    #fff5e3
  );
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  z-index: 1;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 内容主体 */
.content {
  position: relative;
  z-index: 2;
  text-align: center;
  width: 100%;
  max-width: 1000px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  height: 90vh; /* 占据大部分高度 */
  justify-content: center; /* 垂直居中 */
}

/* 品牌标题 */
.brand {
  margin-bottom: 40px;
  flex-grow: 1; /* 推向中间 */
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand h1 {
  font-size: 7rem;
  font-weight: 900;
  letter-spacing: -2px;
  background: linear-gradient(120deg, #2563eb, #7c3aed, #db2777);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin: 0;
  text-shadow: 0 10px 30px rgba(37, 99, 235, 0.15);
  animation: fadeInUp 1s ease-out;
}

.brand p {
  color: #64748b;
  font-size: 2rem;
  font-weight: 500;
  margin-top: 10px;
  animation: fadeInUp 1s ease-out 0.2s backwards;
}

/* 按钮区域 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 60px; /* 距离底部卡片 */
  animation: fadeInUp 1s ease-out 0.4s backwards;
}

.btn {
  padding: 12px 28px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.btn:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.15);
}

.btn:active {
  transform: translateY(0);
}

/* 1. GitHub 按钮样式 (深灰底+深蓝框) */
.github-btn {
  background-color: #24292e; /* GitHub 深灰 */
  color: #ffffff;
  border: 2px solid #0366d6; /* 深蓝边框 */
}
.github-icon {
  fill: white;
}

/* 2. 登录按钮 (绿色底 + 彩色边框) */
.login-btn {
  position: relative;
  color: white;
  border: 3px solid transparent; /* 透明边框用于显示背景 */
  background-clip: padding-box, border-box;
  background-origin: padding-box, border-box;
  /* 内部绿色，边框彩色渐变 */
  background-image: 
    linear-gradient(#10b981, #10b981), /* 内部绿色 */
    linear-gradient(90deg, #3b82f6, #8b5cf6, #ec4899); /* 边框渐变 */
}

/* 3. 游客按钮 (浅灰底 + 粉红边框) */
.guest-btn {
  background-color: #f1f5f9; /* 浅灰 */
  color: #475569;
  border: 2px solid #f472b6; /* 粉红边框 */
}

/* 底部信息卡片区域 */
.info-cards {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-top: auto; /* 推到底部 */
  padding-bottom: 20px;
  animation: fadeInUp 1s ease-out 0.6s backwards;
}

.card {
  flex: 1;
  background: rgba(255, 255, 255, 0.6); /* 半透明白 */
  backdrop-filter: blur(12px); /* 磨砂玻璃效果 */
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 24px;
  text-align: left;
  transition: transform 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.8);
}

.card-icon {
  font-size: 2rem;
  margin-bottom: 12px;
}

.card h3 {
  font-size: 1.1rem;
  color: #1e293b;
  margin-bottom: 8px;
  font-weight: 700;
}

.card p {
  font-size: 0.9rem;
  color: #64748b;
  line-height: 1.5;
  margin: 0;
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .brand h1 { font-size: 3rem; }
  .action-buttons { flex-direction: column; width: 100%; max-width: 300px; margin: 0 auto 40px; }
  .info-cards { flex-direction: column; }
}
</style>