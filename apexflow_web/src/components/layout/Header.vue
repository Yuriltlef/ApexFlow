<template>
  <el-header class="github-header">
    <div class="header-left">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in breadcrumb" :key="item.path">
          {{ item.title }}
        </el-breadcrumb-item>
      </el-breadcrumb>

      <div class="quick-actions">
        <el-button v-if="canCreateOrder" type="primary" size="small" :icon="Plus" @click="openCreateDialog">
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

    <div class="header-right">
      <el-dropdown @command="handleUserCommand">
        <div class="user-info">
          <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          <div class="user-details">
            <span class="username">{{ displayName }}</span>
            <span class="user-role">{{ userRole }}</span>
          </div>
          <el-icon>
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-dialog v-model="createDialogVisible" title="新建订单" width="600px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="客户ID" prop="userId">
          <el-input v-model.number="createForm.userId" placeholder="请输入客户ID" />
        </el-form-item>

        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="createForm.paymentMethod" placeholder="请选择支付方式" style="width: 100%;">
            <el-option label="微信支付" value="wxpay" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="信用卡" value="credit_card" />
          </el-select>
        </el-form-item>

        <el-form-item label="订单状态" prop="status">
          <el-select v-model="createForm.status" placeholder="请选择状态" style="width: 100%;">
            <el-option label="待付款" :value="1" />
            <el-option label="已支付" :value="2" />
            <el-option label="已发货" :value="3" />
            <el-option label="已完成" :value="4" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">订单明细</el-divider>

        <div v-for="(item, index) in createForm.items" :key="index" class="order-item-row"
          style="margin-bottom: 10px; display: flex; gap: 10px; align-items: center;">
          <el-input v-model.number="item.productId" placeholder="商品ID" style="width: 120px;" />
          <el-input v-model="item.productName" placeholder="商品名称" style="width: 150px;" />
          <el-input-number v-model="item.price" placeholder="单价" :min="0" :precision="2" style="width: 120px;"
            @change="calculateTotal" />
          <el-input-number v-model="item.quantity" placeholder="数量" :min="1" style="width: 100px;"
            @change="calculateTotal" />
          <el-button type="danger" :icon="Delete" circle size="small" @click="removeItem(index)" />
        </div>

        <div style="margin-bottom: 20px;">
          <el-button type="primary" plain size="small" :icon="Plus" @click="addItem">添加商品</el-button>
        </div>

        <el-form-item label="订单总额" prop="totalAmount">
          <el-input-number v-model="createForm.totalAmount" :min="0" :precision="2" style="width: 100%;" disabled />
          <div style="font-size: 12px; color: #909399;">(根据明细自动计算)</div>
        </el-form-item>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitCreate">确认创建</el-button>
        </span>
      </template>
    </el-dialog>

  </el-header>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Bell, Search, ArrowDown, Plus, Upload, Download, Delete } from '@element-plus/icons-vue'
import userDataManager from '@/utils/userData'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()

// --- 面包屑逻辑 ---
const breadcrumb = ref([])

const getBreadcrumb = () => {
  let matched = route.matched.filter(item => item.meta && item.meta.title)
  breadcrumb.value = matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
}

watch(() => route.path, getBreadcrumb, { immediate: true })

// --- 用户信息逻辑 ---
const displayName = computed(() => userDataManager.getDisplayName())
const userRole = computed(() => userDataManager.getUserRoleText())

// --- 权限判断 ---
const canCreateOrder = computed(() => {
  return userDataManager.isAdmin() || userDataManager.hasPermission('canManageOrder')
})

const handleUserCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/user/profile')
  } else {
    ElMessage.info(`点击了: ${command}`)
  }
}

const handleLogout = () => {
  userDataManager.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

// --- 新建订单逻辑 ---
const createDialogVisible = ref(false)
const submitLoading = ref(false)
const createFormRef = ref(null)

const createForm = reactive({
  userId: null,
  totalAmount: 0,
  paymentMethod: 'wxpay',
  status: 1,
  items: [] // [新增] 订单项列表
})

const createRules = {
  userId: [{ required: true, message: '请输入客户ID', trigger: 'blur' }],
  // totalAmount 移除必填校验，因为是自动计算的
  // paymentMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const openCreateDialog = () => {
  createForm.userId = null
  createForm.totalAmount = 0
  createForm.paymentMethod = 'wxpay'
  createForm.status = 1
  createForm.items = [{ productId: null, productName: '', price: 0, quantity: 1 }] // 默认添加一行
  createDialogVisible.value = true
}

// 添加商品行
const addItem = () => {
  createForm.items.push({ productId: null, productName: '', price: 0, quantity: 1 })
}

// 删除商品行
const removeItem = (index) => {
  createForm.items.splice(index, 1)
  calculateTotal()
}

// 计算总金额
const calculateTotal = () => {
  let sum = 0
  createForm.items.forEach(item => {
    sum += (item.price || 0) * (item.quantity || 0)
  })
  createForm.totalAmount = sum
}

const submitCreate = async () => {
  if (!createFormRef.value) return

  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      // 校验是否有商品
      if (createForm.items.length === 0) {
        ElMessage.warning('请至少添加一个商品')
        return
      }
      // 校验商品必填项
      for (const item of createForm.items) {
        if (!item.productId || !item.quantity) {
          ElMessage.warning('请完善商品信息（ID和数量）')
          return
        }
      }

      submitLoading.value = true
      try {
        const payload = {
          userId: Number(createForm.userId),
          paymentMethod: createForm.paymentMethod,
          totalAmount: createForm.totalAmount,
          addressId: 1,
          status: createForm.status,
          orderItems: createForm.items.map(item => ({
            productId: Number(item.productId),
            quantity: Number(item.quantity),
            price: Number(item.price)
          }))
        }
        await createOrder(payload)
        ElMessage.success('订单创建成功')
        createDialogVisible.value = false

        // 如果当前在订单页，刷新页面
        if (route.path === '/business/order') {
          setTimeout(() => {
            window.location.reload()
          }, 500)
        }
      } catch (error) {
        console.error(error)
        // 错误已经在 request.js 拦截器处理了，这里可以不写，或者写个性化提示
        ElMessage.error('创建订单异常')
      } finally {
        submitLoading.value = false
      }
    }
  })
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
  color: #24292e;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
}

.header-icon:hover {
  background-color: #f6f8fa;
}

.notification-badge :deep(.el-badge__content.is-fixed) {
  top: 4px;
  right: 4px;
  transform: scale(0.8);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f6f8fa;
}

.user-details {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #24292e;
}

.user-role {
  font-size: 12px;
  color: #586069;
}

/* 订单项样式 */
.order-item-row {
  background: #f8f9fa;
  padding: 5px;
  border-radius: 4px;
}
</style>