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

        <!-- 登录表单 - 保留原有结构，仅添加表单验证相关标识 -->
        <form class="login-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">用户名</label>
            <input type="text" id="username" v-model="username" placeholder="请输入用户名" required>
          </div>

          <div class="form-group">
            <label for="password">密码</label>
            <input type="password" id="password" v-model="password" placeholder="请输入密码" required>
          </div>

          <div class="form-actions">
            <a href="#" class="forgot-link">忘记密码?</a>
          </div>

          <button type="submit" class="btn primary" :disabled="isLoading">
            <!-- 加载状态提示 -->
            <span v-if="!isLoading">登录</span>
            <span v-if="isLoading">登录中...</span>
          </button>
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
// 在文件顶部导入
import userDataManager from '@/utils/userData';

import { getUserPermissions } from '@/api/user';

// 1. 导入封装的登录API
import { userLogin } from '@/api/user';
// 2. 导入Element Plus的消息提示（用于弹窗提示登录结果，和之前的配置一致）
import { ElMessage } from 'element-plus';

export default {
  name: 'Login',
  data() {
    return {
      username: '', // 默认填充管理员用户名，方便测试
      password: '', // 密码手动输入
      isLoading: false // 登录加载状态，防止重复点击
    }
  },
  methods: {
    // 异步处理登录逻辑
    async handleLogin() {
      // 第一步：简单表单验证（非空校验，保留原有required的基础上增强）
      if (!this.username.trim()) {
        ElMessage.warning('请输入用户名！');
        return;
      }
      if (!this.password.trim()) {
        ElMessage.warning('请输入密码！');
        return;
      }

      // 第二步：设置加载状态，禁用按钮
      this.isLoading = true;

      try {
          const res = await userLogin({
          username: this.username.trim(),
          password: this.password.trim()
        });

        console.log('登录响应：', res);

        if (res.data.success) {
          // 1. 保存Token到用户管理器
          userDataManager.setToken(res.data.data.token);

          // 2. 保存用户基本信息到用户管理器
          const userInfo = res.data.data.user;
          userDataManager.setUserData(userInfo, null);

          // 3. 获取用户权限（新增）
          try {
            const permissionRes = await getUserPermissions();
            if (permissionRes.data.success) {
              // 保存权限信息到用户管理器
              userDataManager.setUserData(userInfo, permissionRes.data.data);

              // 显示权限获取成功
              console.log('权限获取成功:', permissionRes.data.data);
            } else {
              console.warn('获取权限失败:', permissionRes.data.message);
              // 如果获取权限失败，至少保存用户基本信息
              userDataManager.setUserData(userInfo, { isAdmin: userInfo.isAdmin });
            }
          } catch (permissionError) {
            console.error('获取权限异常:', permissionError);
            // 如果权限API调用失败，至少保存用户基本信息
            userDataManager.setUserData(userInfo, { isAdmin: userInfo.isAdmin });
          }

          // 4. 显示成功提示
          ElMessage.success('登录成功！即将跳转到控制台');

          // 5. 跳转到仪表盘
          setTimeout(() => {
            this.$router.push('/dashboard');
          }, 500);

        } else {
          ElMessage.error(res.data.message || '登录失败，请重试！');
        }
      } catch (error) {
        if (error.status == 401) {
          console.error('登录请求失败：', error);
          ElMessage.error('用户名或密码错误，请重试！');
        } else {
          console.error('登录请求异常：', error);
          ElMessage.error('网络异常，请检查后端服务是否启动！');
        }

      } finally {
        this.isLoading = false;
      }
    }
  }
}

</script>

<style scoped>
/* 完全保留你原有所有CSS样式，未做任何修改 */
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