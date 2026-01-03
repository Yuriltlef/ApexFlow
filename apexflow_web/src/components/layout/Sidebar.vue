<template>
  <div class="sidebar-container">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '260px'" class="github-sidebar">
      <!-- Logo区域 -->
      <div class="sidebar-logo">
        <div class="logo-icon">⚡</div>
        <h2 class="logo-text" v-if="!collapsed">ApexFlow</h2>
        <div class="collapse-button" @click="toggleCollapse" v-if="!collapsed">
          <el-icon :size="16">
            <Fold />
          </el-icon>
        </div>
      </div>

      <!-- 搜索框 -->
      <div class="sidebar-search" v-if="!collapsed">
        <el-input
          placeholder="搜索功能..."
          size="small"
          :prefix-icon="Search"
          class="search-input"
        />
      </div>

      <!-- 动态导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        class="sidebar-menu"
        @select="handleSelect"
      >
        <!-- 仪表盘（始终显示） -->
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <template #title>
            <span>仪表盘</span>
          </template>
          <div class="badge-wrapper">
            <el-badge :value="3" class="menu-badge" />
          </div>
        </el-menu-item>

        <!-- 订单管理 -->
        <el-sub-menu 
          v-if="hasPermission('canManageOrder') || isAdmin"
          index="order"
        >
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/orders/list">订单列表</el-menu-item>
          <el-menu-item index="/orders/analysis">订单分析</el-menu-item>
        </el-sub-menu>

        <!-- 物流管理 -->
        <el-sub-menu 
          v-if="hasPermission('canManageLogistics') || isAdmin"
          index="logistics"
        >
          <template #title>
            <el-icon><Van /></el-icon>
            <span>物流管理</span>
          </template>
          <el-menu-item index="/logistics/tracking">物流跟踪</el-menu-item>
          <el-menu-item index="/logistics/settings">物流设置</el-menu-item>
        </el-sub-menu>

        <!-- 售后管理 -->
        <el-menu-item 
          v-if="hasPermission('canManageAfterSales') || isAdmin"
          index="/after-sales"
        >
          <el-icon><Refresh /></el-icon>
          <template #title>
            <span>售后管理</span>
          </template>
          <div class="badge-wrapper">
            <el-badge :value="5" class="menu-badge" type="danger" />
          </div>
        </el-menu-item>

        <!-- 评价管理 -->
        <el-menu-item 
          v-if="hasPermission('canManageReview') || isAdmin"
          index="/evaluation"
        >
          <el-icon><Star /></el-icon>
          <template #title>
            <span>评价管理</span>
          </template>
        </el-menu-item>

        <!-- 仓库管理 -->
        <el-sub-menu 
          v-if="hasPermission('canManageInventory') || isAdmin"
          index="warehouse"
        >
          <template #title>
            <el-icon><Box /></el-icon>
            <span>仓库管理</span>
          </template>
          <el-menu-item index="/warehouse/inventory">库存管理</el-menu-item>
          <el-menu-item index="/warehouse/in-out">出入库记录</el-menu-item>
          <el-menu-item index="/warehouse/warning">库存预警</el-menu-item>
        </el-sub-menu>

        <!-- 财务管理 -->
        <el-sub-menu 
          v-if="hasPermission('canManageIncome') || isAdmin"
          index="finance"
        >
          <template #title>
            <el-icon><Money /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/finance/income">收入统计</el-menu-item>
          <el-menu-item index="/finance/expense">支出管理</el-menu-item>
          <el-menu-item index="/finance/report">财务报表</el-menu-item>
        </el-sub-menu>

        <!-- 系统设置（仅管理员可见） -->
        <el-menu-item 
          v-if="isAdmin"
          index="/system"
        >
          <el-icon><Setting /></el-icon>
          <template #title>
            <span>系统设置</span>
          </template>
        </el-menu-item>
      </el-menu>

      <!-- 底部帮助区域 -->
      <div class="sidebar-footer" v-if="!collapsed">
        <el-button type="text" size="small" :icon="QuestionFilled">
          帮助文档
        </el-button>
        <el-button type="text" size="small" :icon="ChatDotRound">
          反馈建议
        </el-button>
      </div>

      <!-- 折叠时的简约底部 -->
      <div class="sidebar-footer-collapsed" v-else>
        <el-button type="text" size="small" :icon="QuestionFilled" class="collapsed-icon-btn" title="帮助文档" />
        <el-button type="text" size="small" :icon="ChatDotRound" class="collapsed-icon-btn" title="反馈建议" />
      </div>
    </el-aside>

    <!-- 外部展开按钮（只在折叠时显示） -->
    <div class="expand-button-outer" v-if="collapsed" @click="toggleCollapse">
      <el-icon :size="16"><Expand /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import userDataManager from '@/utils/userData'
import {
  Search, Fold, Expand, DataBoard, ShoppingCart,
  Van, Refresh, Star, Box, Money, Setting,
  QuestionFilled, ChatDotRound,
} from '@element-plus/icons-vue'

const props = defineProps<{
  collapsed: boolean
}>()

const emit = defineEmits(['toggle-collapse'])
const route = useRoute()
const router = useRouter()

// 响应式权限状态
const userInfo = ref(userDataManager.getUserInfo())
const permissions = ref(userDataManager.getPermissions())

// 检查是否有特定权限
const hasPermission = (permissionKey: string) => {
  return userDataManager.hasPermission(permissionKey)
}

// 检查是否是管理员
const isAdmin = computed(() => {
  return userDataManager.isAdmin()
})

// 获取当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 切换侧边栏折叠
const toggleCollapse = () => {
  emit('toggle-collapse')
}

// 菜单选择处理
const handleSelect = (index: string) => {
  router.push(index)
}

// 监听用户数据变化（可选）
onMounted(() => {
  console.log('Sidebar权限检查:', {
    userInfo: userInfo.value,
    permissions: permissions.value,
    isAdmin: isAdmin.value,
    canManageOrder: hasPermission('canManageOrder'),
    canManageLogistics: hasPermission('canManageLogistics'),
    canManageAfterSales: hasPermission('canManageAfterSales'),
    canManageReview: hasPermission('canManageReview'),
    canManageInventory: hasPermission('canManageInventory'),
    canManageIncome: hasPermission('canManageIncome'),
  })
})
</script>

<style scoped>
/* 样式部分保持不变 */
.sidebar-container {
  position: relative;
  height: 100vh;
  display: flex;
}

.github-sidebar {
  background: #f6f8fa;
  border-right: 1px solid #e1e4e8;
  height: 100vh;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

/* Logo区域 */
.sidebar-logo {
  padding: 16px;
  border-bottom: 1px solid #e1e4e8;
  display: flex;
  align-items: center;
  height: 64px;
  box-sizing: border-box;
  position: relative;
}

.logo-icon {
  font-size: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #24292e;
  margin: 0;
  margin-left: 12px;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.collapse-button {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: #586069;
  background: white;
  border: 1px solid #e1e4e8;
  flex-shrink: 0;
}

.collapse-button:hover {
  background: #f6f8fa;
  color: #0366d6;
  border-color: #0366d6;
}

/* 搜索框 */
.sidebar-search {
  padding: 16px;
  border-bottom: 1px solid #e1e4e8;
  flex-shrink: 0;
}

.search-input :deep(.el-input__wrapper) {
  background: #fff;
  border: 1px solid #e1e4e8;
  box-shadow: none;
}

.search-input :deep(.el-input__wrapper:hover) {
  border-color: #0366d6;
}

/* 导航菜单 */
.sidebar-menu {
  border-right: none;
  flex: 1;
  background: transparent;
  overflow-x: hidden;
  overflow-y: auto;
}

/* 未折叠状态下的菜单项 */
.sidebar-menu:not(.el-menu--collapse) :deep(.el-menu-item),
.sidebar-menu:not(.el-menu--collapse) :deep(.el-sub-menu__title) {
  color: #24292e;
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  position: relative;
  padding: 0 16px !important;
}

.sidebar-menu:not(.el-menu--collapse) :deep(.el-menu-item:hover),
.sidebar-menu:not(.el-menu--collapse) :deep(.el-sub-menu__title:hover) {
  background: #f0f2f5;
}

.sidebar-menu:not(.el-menu--collapse) :deep(.el-menu-item.is-active) {
  background: #e8f4ff;
  color: #0366d6;
  font-weight: 500;
}

.sidebar-menu:not(.el-menu--collapse) :deep(.el-menu-item .el-icon),
.sidebar-menu:not(.el-menu--collapse) :deep(.el-sub-menu__title .el-icon) {
  margin-right: 12px;
  font-size: 18px;
}

/* 折叠状态下的菜单项 - 使用更强大的选择器覆盖 Element Plus 样式 */
.sidebar-menu.el-menu--collapse {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 4px;
  height: 44px;
}

/* 折叠状态下覆盖 Element Plus 的绝对定位样式 */
.sidebar-menu.el-menu--collapse :deep(.el-menu-item),
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__title) {
  width: 44px !important;
  height: 44px !important;
  margin: 2px 0 !important;
  border-radius: 6px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  position: relative !important;
  padding: 0 !important;
}

/* 针对触发元素的特殊覆盖 */
.sidebar-menu.el-menu--collapse :deep(.el-menu-item .el-menu-tooltip__trigger),
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__title .el-sub-menu__title-content) {
  width: 100% !important;
  height: 44px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  position: relative !important;
  left: 0 !important;
  top: 0 !important;
  padding: 0 !important;
  padding-top: 0px !important;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item .el-icon),
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__title .el-icon) {
  margin: 0 !important;
  font-size: 18px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 44px;
  width: 44px;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item:hover),
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__title:hover) {
  background: #f0f2f5 !important;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item.is-active) {
  background: #e8f4ff !important;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item.is-active .el-icon) {
  color: #0366d6 !important;
}

/* 修复折叠菜单悬浮tooltip黑框问题 */
.sidebar-menu.el-menu--collapse :deep(.el-tooltip__trigger) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  width: 100% !important;
  height: 100% !important;
}

/* 修复折叠菜单tooltip内容 */
.sidebar-menu.el-menu--collapse :deep(.el-tooltip) {
  font-size: 12px !important;
  padding: 4px 8px !important;
  background: #24292e !important;
  color: white !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
  border-radius: 4px !important;
  max-width: 200px !important;
}

/* 修复折叠菜单tooltip箭头 */
.sidebar-menu.el-menu--collapse :deep(.el-popper__arrow::before) {
  background: #24292e !important;
  border: none !important;
}

/* 强制隐藏折叠状态下的子菜单箭头 */
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu__icon-arrow) {
  display: none !important;
}

/* 折叠状态下的子菜单也强制隐藏箭头 */
.sidebar-menu.el-menu--collapse :deep(.el-sub-menu .el-sub-menu__icon-arrow) {
  display: none !important;
  opacity: 0 !important;
  visibility: hidden !important;
  width: 0 !important;
  height: 0 !important;
  margin: 0 !important;
}

/* 徽章样式 */
.badge-wrapper {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  padding: 0px;
}

.el-menu-item * {
    vertical-align: 0;
}

/* 折叠状态下的徽章样式 */
.sidebar-menu.el-menu--collapse :deep(.badge-wrapper) {
  right: 4px !important;
  top: 8px !important;
  transform: none !important;
  z-index: 10 !important;
}

/* Element Plus Badge 自定义样式 */
:deep(.el-badge__content) {
  line-height: 16px !important;
  height: 15px !important;
  min-width: 18px !important;
  font-size: 10px !important;
  padding: 0 4px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  border: 2px solid #f6f8fa !important;
}

/* 外部展开按钮 */
.expand-button-outer {
  position: absolute;
  top: 50%;
  right: -16px;
  transform: translateY(-50%);
  width: 16px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0 6px 6px 0;
  cursor: pointer;
  color: white;
  background: #6992c2;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  z-index: 100;
}

.expand-button-outer:hover {
  background: #0256c0;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

/* 底部区域 */
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #e1e4e8;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
  margin: 0px !important;
}

.sidebar-footer .el-button {
  justify-content: flex-start;
  color: #586069;
    margin: 0px !important;
}

.sidebar-footer .el-button:hover {
  color: #0366d6;
  background: transparent;
}

/* 折叠时的底部样式 */
.sidebar-footer-collapsed {
  padding: 16px 0 !important;
  border-top: 1px solid #e1e4e8;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  gap: 8px !important;
  flex-shrink: 0;
}

.sidebar-footer-collapsed .collapsed-icon-btn {
  color: #586069;
  padding: 8px !important;
  width: 40px !important;
  height: 40px !important;
  border-radius: 4px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  margin: 0px !important;
}

.sidebar-footer-collapsed .collapsed-icon-btn:hover {
  color: #0366d6;
  background: #e1e4e8;
}

/* 自定义滚动条 */
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>