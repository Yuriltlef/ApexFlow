<template>
  <div class="login-container">
    <div class="background-anim"></div>

    <main class="content">
      <div class="login-card">
        <div class="brand">
          <h1 class="animate-title">ApexFlow</h1>
          <p>登录管理控制台</p>
        </div>

        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">用户名</label>
            <input type="text" id="username" v-model="username" placeholder="请输入用户名" required autocomplete="username">
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <input type="password" id="password" v-model="password" placeholder="请输入密码" required
              autocomplete="current-password">
          </div>

          <button type="submit" class="btn primary" :disabled="isLoading">
            <span v-if="!isLoading">立即登录</span>
            <span v-if="isLoading">登录中...</span>
          </button>
        </form>

        <div class="card-footer">
          <span class="prompt">还没有账号?</span>
          <a href="https://github.com/Yuriltlef" target="_blank" class="register-link">联系管理员</a>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
// [修复] 引入 getUserPermissions 用于获取角色权限
import { userLogin, getUserPermissions } from '@/api/user'
import userDataManager from '@/utils/userData'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  data() {
    return {
      username: '',
      password: '',
      isLoading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.username || !this.password) {
        ElMessage.warning('请输入用户名和密码')
        return
      }

      this.isLoading = true
      try {
        // 1. 发起登录请求
        const res = await userLogin({
          username: this.username,
          password: this.password
        })

        if (res.data.success) {
          // 注意：通常登录接口只返回 token 和基本 user 信息，不包含完整权限
          const { token, user } = res.data.data

          // [关键修复] 先保存 Token，因为后续的 getUserPermissions 需要用到它
          userDataManager.setToken(token)

          // 2. [关键修复] 单独获取用户权限信息
          // 很多后端设计将权限接口分离，必须单独调用才能获取 isAdmin 等字段
          try {
            const permRes = await getUserPermissions()

            if (permRes.data.success) {
              const permissions = permRes.data.data

              // 3. 保存完整的 用户信息 + 权限信息
              if (userDataManager.setUserData(user, permissions)) {
                ElMessage.success('登录成功')
                this.$router.push('/dashboard')
              } else {
                ElMessage.error('用户数据保存失败')
              }
            } else {
              // 降级处理：如果获取权限失败，但登录成功了，可能无法进入某些页面
              console.warn('获取权限失败，仅保存基本信息')
              userDataManager.setUserData(user, {})
              this.$router.push('/dashboard')
            }
          } catch (permError) {
            console.error('权限接口调用异常', permError)
            // 即使权限获取失败，也允许登录，只是角色可能显示未知
            userDataManager.setUserData(user, {})
            this.$router.push('/dashboard')
          }
        } else {
          ElMessage.error(res.data.message || '登录失败')
        }
      } catch (error) {
        if (error.status == 401) {
          ElMessage.error('用户名或密码错误')
        } else {
          console.error(error)
          ElMessage.error('登录请求异常')
        }
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
/* 容器布局 */
.login-container {
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
  background: linear-gradient(45deg,
      #fdfbfb,
      #ebedee,
      #f3e7e9,
      #e3eeff,
      #e9f3e7,
      #fff5e3);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  z-index: 1;
}

@keyframes gradientBG {
  0% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }

  100% {
    background-position: 0% 50%;
  }
}

/* 内容层级 */
.content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

/* 登录卡片 (Glassmorphism 磨砂质感) */
.login-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  padding: 40px;
  box-shadow:
    0 20px 40px -10px rgba(0, 0, 0, 0.05),
    0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  animation: slideUp 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

/* 品牌区域 */
.brand {
  text-align: center;
  margin-bottom: 32px;
}

.brand h1 {
  font-size: 2.2rem;
  font-weight: 800;
  margin: 0 0 8px 0;
  /* 蓝紫渐变文字 */
  background: linear-gradient(135deg, #2563eb 0%, #9333ea 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  letter-spacing: -0.5px;
}

.brand p {
  color: #64748b;
  font-size: 0.95rem;
  margin: 0;
  font-weight: 500;
}

/* 表单布局 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  text-align: left;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 600;
  color: #334155;
  margin-left: 2px;
}

/* 输入框样式优化 */
.form-group input {
  width: 100%;
  padding: 12px 16px;
  border-radius: 12px;
  border: 2px solid transparent;
  background-color: #f1f5f9;
  /* 浅灰背景 */
  font-size: 1rem;
  color: #1e293b;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  background-color: #ffffff;
  border-color: #3b82f6;
  /* 聚焦变蓝 */
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1);
}

.form-group input::placeholder {
  color: #94a3b8;
}

/* 按钮样式 */
.btn {
  margin-top: 8px;
  width: 100%;
  padding: 14px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.primary {
  /* 蓝紫渐变背景 */
  background: linear-gradient(90deg, #2563eb, #7c3aed);
  color: white;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
}

.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(37, 99, 235, 0.35);
}

.primary:active {
  transform: translateY(0);
}

.primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

/* 底部区域 */
.card-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 0.9rem;
  color: #64748b;
  display: flex;
  justify-content: center;
  gap: 6px;
}

.register-link {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.register-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* 入场动画 */
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 移动端适配 */
@media (max-width: 480px) {
  .login-card {
    padding: 30px 24px;
    border-radius: 20px;
  }

  .brand h1 {
    font-size: 1.8rem;
  }
}
</style>