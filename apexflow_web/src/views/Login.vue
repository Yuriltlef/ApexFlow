<template>
  <div class="login-container">
    <!-- 背景层 -->
    <div class="background"></div>
    
    <!-- 主内容区 -->
    <main class="content">
      <!-- 登录卡片 -->
      <div class="login-card">
        <!-- 品牌标识 -->
        <div class="brand">
          <h1>ApexFlow</h1>
          <p>登录管理控制台</p>
        </div>
        
        <!-- 登录表单 -->
        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">用户名</label>
            <input 
              type="text" 
              id="username" 
              v-model="username" 
              placeholder="请输入用户名"
              required
            >
          </div>
          
          <div class="form-group">
            <label for="password">密码</label>
            <input 
              type="password" 
              id="password" 
              v-model="password" 
              placeholder="请输入密码"
              required
            >
          </div>
          
          <div class="form-actions">
            <a href="#" class="forgot-link">忘记密码?</a>
          </div>
          
          <button type="submit" class="btn primary">登录</button>
        </form>
        
        <!-- 注册提示 -->
        <div class="register-prompt">
          <span>还没有账号?</span>
          <a href="/register" class="register-link">立即注册</a>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      username: '',
      password: ''
    }
  },
  methods: {
    handleLogin() {
      // 登录逻辑处理
      console.log('登录信息:', {
        username: this.username,
        password: this.password
      })
      // 实际项目中这里会调用登录API
      // 示例: this.$api.login(this.username, this.password).then(...)
      this.$router.push('/dashboard')
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

/* 容器样式 - 保持浅色主题 */
.login-container {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background-color: #f8fafc;
  overflow: hidden;
}

/* 背景效果 - 与首页保持一致 */
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
  width: 100%;
  max-width: 420px;
}

/* 登录卡片 */
.login-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  padding: 40px;
  width: 100%;
}

/* 品牌标识 - 保持与首页一致的风格 */
.brand {
  text-align: center;
  margin-bottom: 36px;
}

.brand h1 {
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(90deg, #2563eb, #7c3aed);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin-bottom: 8px;
}

.brand p {
  color: #475569;
  font-size: 1.1rem;
}

/* 表单样式 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e293b;
}

.form-group input {
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  font-size: 1rem;
  transition: all 0.2s ease;
  background-color: #f8fafc;
}

.form-group input:focus {
  outline: none;
  border-color: #93c5fd;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  background-color: #ffffff;
}

/* 表单操作区 */
.form-actions {
  display: flex;
  justify-content: flex-end;
}

.forgot-link {
  color: #2563eb;
  font-size: 0.9rem;
  text-decoration: none;
  transition: color 0.2s ease;
}

.forgot-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* 按钮样式 - 保持与首页一致 */
.btn {
  padding: 14px 24px;
  border-radius: 12px;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  outline: none;
  width: 100%;
}

.primary {
  background: linear-gradient(90deg, #2563eb, #7c3aed);
  color: #ffffff;
  box-shadow: 0 10px 25px -5px rgba(59, 130, 246, 0.25);
}

.primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 15px 30px -8px rgba(59, 130, 246, 0.35);
}

/* 注册提示 */
.register-prompt {
  text-align: center;
  color: #64748b;
  font-size: 0.95rem;
  margin-top: 16px;
}

.register-link {
  color: #2563eb;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
  transition: color 0.2s ease;
}

.register-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .login-card {
    padding: 30px 24px;
  }
  
  .brand h1 {
    font-size: 2rem;
  }
  
  .btn {
    padding: 12px 20px;
    font-size: 1rem;
  }
}
</style>