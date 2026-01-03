<template>
  <el-header class="github-header">
    <!-- 左侧：面包屑和快速操作 -->
    <div class="header-left">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumb" :key="item.path">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>

      <div class="quick-actions">
        <el-button type="primary" size="small" :icon="Plus">
          新建订单
        </el-button>
        <el-button size="small" :icon="Upload">
          导入数据
        </el-button>
        <el-button size="small" :icon="Download">
          导出报表
        </el-button>
      </div>
    </div>

    <!-- 右侧：用户信息和工具 -->
    <div class="header-right">
      <!-- ... 通知中心和搜索按钮保持不变 ... -->

      <!-- 用户信息 - 修改为动态显示 -->
      <el-dropdown @command="handleUserCommand">
        <div class="user-info">
          <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          <div class="user-details">
            <span class="username">{{ displayName }}</span>
            <span class="user-role">{{ userRole }}</span>
          </div>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <!-- 动态显示用户信息 -->
            <el-dropdown-item disabled v-if="userInfo">
              <div style="font-size: 12px; color: #999;">
                {{ userInfo.username }}<br/>
                {{ userInfo.realName || '未设置姓名' }}
              </div>
            </el-dropdown-item>
            <el-dropdown-item :icon="Close" @click="handleLogout">切换账号</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import userDataManager from '@/utils/userData'
import {
  Plus, Upload, Download, Bell, Search,
  User, Setting, Lock, SwitchButton, Close,
  ArrowDown, ChatDotRound
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'  // 导入ElMessage

const route = useRoute()
const router = useRouter()

// 响应式用户数据
const userInfo = ref(userDataManager.getUserInfo())
const permissions = ref(userDataManager.getPermissions())

// 计算属性：显示名称
const displayName = computed(() => {
  return userDataManager.getDisplayName()
})

// 计算属性：用户角色
const userRole = computed(() => {
  return userDataManager.getUserRoleText()
})

// 面包屑 - 保持不变
const breadcrumb = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta?.title as string
  }))
})

// 初始化时检查用户状态
onMounted(() => {
  if (!userDataManager.isLoggedIn()) {
    console.log('用户未登录，Header组件将显示默认信息')
  } else {
    console.log('用户已登录，显示用户信息:', {
      userInfo: userInfo.value,
      permissions: permissions.value
    })
  }
})

// 处理用户命令
const handleUserCommand = (command: string) => {
  console.log('用户命令:', command)
  if (command === 'logout') {
    handleLogout()
  }
}

// 退出登录
const handleLogout = () => {
  // 清除用户数据
  userDataManager.logout()
  
  // 显示提示
  ElMessage.success('已退出登录')
  
  // 跳转到登录页面
  router.push('/login')
}
</script>


<style scoped>
.github-header {
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #e1e4e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

:deep(.el-breadcrumb) {
  font-size: 12px;
  color: #586069;
}

:deep(.el-breadcrumb__inner) {
  color: #0366d6;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #24292e;
  font-weight: 500;
}

.quick-actions {
  display: flex;
  gap: 8px;
}

.quick-actions .el-button {
  font-size: 12px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  color: #586069;
}

.header-icon:hover {
  background: #f6f8fa;
  color: #0366d6;
}

:deep(.el-badge__content) {
  transform: scale(0.8) translate(50%, -50%);
}

.search-btn {
  color: #586069;
}

.search-btn:hover {
  color: #0366d6;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
}

.user-info:hover {
  background: #f6f8fa;
}

.user-details {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #24292e;
}

.user-role {
  font-size: 12px;
  color: #586069;
}
</style>
