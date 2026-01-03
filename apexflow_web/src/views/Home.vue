<template>
  <div class="home-container">
    <div class="background"></div>
    
    <main class="content">
      <div class="brand">
        <h1>ApexFlow</h1>
        <p>轻量级电商管理平台</p>
      </div>
      
      <div class="action-buttons">
        <button @click="goToLogin" class="btn primary">登录 管理控制台</button>
        <button @click="enterAsGuest" class="btn secondary">以游客方式进入</button>
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
      // 1. 创建虚拟游客数据
      const guestUser = {
        username: 'Guest',
        realName: '游客',
        avatar: '', 
        isGuest: true // 核心标记
      }

      // 2. 设置极简权限
      const guestPermissions = {
        canViewDashboard: true,
        isAdmin: false
      }

      // 3. 保存数据到管理器
      if (userDataManager.setUserData(guestUser, guestPermissions)) {
        // 设置一个虚拟 Token
        userDataManager.setToken('guest-mock-token-' + Date.now())
        
        ElMessage.success('已进入游客体验模式')
        
        // 4. 重定向到仪表盘
        this.$router.push('/dashboard')
      } else {
        ElMessage.error('进入游客模式失败')
      }
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

/* 容器样式 - 浅色主题 */
.home-container {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #f8fafc;
  overflow: hidden;
}

/* 背景装饰 */
.background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 10% 20%, rgba(37, 99, 235, 0.05) 0%, transparent 40%),
    radial-gradient(circle at 90% 80%, rgba(124, 58, 237, 0.05) 0%, transparent 40%);
  z-index: 1;
}

/* 内容区 */
.content {
  position: relative;
  z-index: 2;
  text-align: center;
  max-width: 600px;
  width: 100%;
}

/* 品牌标识 */
.brand {
  margin-bottom: 56px;
}

.brand h1 {
  font-size: 4rem;
  font-weight: 800;
  background: linear-gradient(90deg, #2563eb, #7c3aed);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 16px;
}

.brand p {
  color: #475569;
  font-size: 1.4rem;
}

/* 按钮组 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 按钮样式 */
.btn {
  padding: 16px 28px;
  border-radius: 12px;
  font-size: 1.15rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;
}

/* 主按钮 */
.primary {
  background: linear-gradient(90deg, #2563eb, #7c3aed);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.3);
}

/* 次要按钮 */
.secondary {
  background: #ffffff;
  color: #475569;
  border: 1px solid #cbd5e1;
}

.secondary:hover {
  background: #f1f5f9;
  color: #1e293b;
  border-color: #94a3b8;
}
</style>