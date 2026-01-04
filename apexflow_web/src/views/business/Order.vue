<template>
  <div class="order-page">
    <h2>📋 订单列表</h2>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div class="header-actions">
            <el-input 
              v-model="searchKeyword" 
              placeholder="请输入订单号/客户ID" 
              style="width: 250px; margin-right: 10px;" 
              clearable
              @clear="handleReset"
            />
            
            <el-button type="primary" :icon="Search">搜索 (自动过滤)</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
            <el-button type="success" :icon="Plus" @click="openCreateDialog" style="margin-left: 10px;">新建订单</el-button>
          </div>
        </div>
      </template>

      <el-table 
        v-loading="loading"
        :data="pagedTableData" 
        stripe 
        style="width: 100%"
        empty-text="暂无订单数据"
      >
        <el-table-column label="订单号" width="200">
          <template #default="{ row }">
            <span v-html="highlightText(row.orderId)"></span>
          </template>
        </el-table-column>

        <el-table-column label="客户ID" width="120">
          <template #default="{ row }">
            <span v-html="highlightText(String(row.userId))"></span>
          </template>
        </el-table-column>
        
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(row.totalAmount) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }">
             {{ formatPaymentMethod(row.paymentMethod) }}
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              :disabled="![1, 5].includes(row.status)"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredTableData.length" 
        />
      </div>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="新建订单" width="600px">
      <el-form :model="newOrderForm" label-width="80px">
        <el-form-item label="客户ID">
          <el-input v-model.number="newOrderForm.userId" type="number" placeholder="请输入客户ID (例如: 1001)" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="newOrderForm.paymentMethod" placeholder="请选择">
            <el-option label="微信支付" value="wxpay" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="信用卡" value="credit_card" />
          </el-select>
        </el-form-item>
        
        <div class="order-items-section">
          <div style="margin-bottom: 10px; font-weight: bold; color: #606266;">订单商品</div>
          <div v-for="(item, index) in newOrderForm.items" :key="index" class="item-row">
            <el-input v-model.number="item.productId" type="number" placeholder="商品ID" style="width: 140px;" />
            <el-input v-model.number="item.price" type="number" placeholder="单价" style="width: 120px;" />
            <el-input-number v-model="item.quantity" :min="1" style="width: 120px;" placeholder="数量" />
            <el-button v-if="newOrderForm.items.length > 1" type="danger" link :icon="Delete" @click="removeOrderItem(index)" />
          </div>
          <el-button type="primary" link size="small" @click="addOrderItem">+ 添加商品行</el-button>
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitCreateOrder">确定创建</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="700px">
      <div v-if="currentOrder">
        <el-descriptions border :column="2">
          <el-descriptions-item label="订单号">{{ currentOrder.id || currentOrder.orderId }}</el-descriptions-item>
          <el-descriptions-item label="客户ID">{{ currentOrder.userId }}</el-descriptions-item>
          <el-descriptions-item label="总金额">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ formatAmount(currentOrder.totalAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
             <el-tag :type="getStatusType(currentOrder.status)" size="small">
               {{ getStatusText(currentOrder.status) }}
             </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ formatPaymentMethod(currentOrder.paymentMethod) }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ formatTime(currentOrder.createdAt) }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 20px; margin-bottom: 10px;">商品清单</h4>
        <el-table :data="currentOrder.items || []" border style="width: 100%" size="small">
          <el-table-column prop="productId" label="商品ID" width="80" />
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="price" label="单价" width="100">
             <template #default="{ row }">¥{{ formatAmount(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column label="小计" width="120">
             <template #default="{ row }">¥{{ formatAmount(row.subtotal || (row.price * row.quantity)) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Plus, Refresh, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, getOrderDetail, createOrder, deleteOrder } from '@/api/order'

// --- 状态定义 ---
const loading = ref(false)
const allOrders = ref([])     // [核心] 存储后端返回的“全部”原始数据
const searchKeyword = ref('') // 搜索关键词

// 前端分页配置
const currentPage = ref(1) 
const pageSize = ref(10)   

// 弹窗相关
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentOrder = ref(null)

// 新建订单表单
const newOrderForm = reactive({
  userId: null,
  paymentMethod: 'wxpay',
  items: [
    { productId: null, price: null, quantity: 1 } 
  ]
})

// --- [核心功能 1]：智能全量获取数据 ---
const fetchData = async () => {
  loading.value = true
  try {
    // 步骤 1: 探测请求。只取 1 条，为了拿到 totalCount (数据库总条数)
    const probeRes = await getOrderList({ page: 1, pageSize: 1 })
    
    if (probeRes && probeRes.data) {
      const totalCount = probeRes.data.totalCount || 0
      
      if (totalCount > 0) {
        // 步骤 2: 全量请求。使用 totalCount 作为 pageSize，一次性拉取所有数据
        // 这样就避免了硬编码 1000 或 9999
        const fullRes = await getOrderList({ page: 1, pageSize: totalCount })
        if (fullRes && fullRes.data) {
          allOrders.value = fullRes.data.orders || []
        }
      } else {
        allOrders.value = []
      }
    } else {
      allOrders.value = []
    }
  } catch (error) {
    console.error('获取列表失败', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// --- [核心功能 2]：前端搜索过滤 ---
const filteredTableData = computed(() => {
  // 如果没有搜索关键词，返回所有数据
  if (!searchKeyword.value) {
    return allOrders.value
  }
  
  // 关键词转小写，去空格
  const keyword = searchKeyword.value.trim().toLowerCase()
  
  // 在所有数据中过滤
  return allOrders.value.filter(item => {
    // 匹配 订单号 (安全检查是否存在)
    const orderIdMatch = item.orderId && item.orderId.toLowerCase().includes(keyword)
    // 匹配 用户ID (转字符串后匹配)
    const userIdMatch = item.userId && String(item.userId).includes(keyword)
    
    return orderIdMatch || userIdMatch
  })
})

// --- [核心功能 3]：前端分页处理 ---
const pagedTableData = computed(() => {
  // 基于 filteredTableData (过滤后的结果) 进行切片
  // 即使有1000条数据，这里每次也只渲染 pageSize (10) 条，保证页面不卡顿
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return filteredTableData.value.slice(startIndex, endIndex)
})

// --- [核心功能 4]：高亮显示 ---
const highlightText = (text) => {
  if (text === null || text === undefined) return ''
  const str = String(text)
  
  // 如果没有关键词，直接返回原文本
  if (!searchKeyword.value || !searchKeyword.value.trim()) {
    return str
  }
  
  const keyword = searchKeyword.value.trim()
  // 创建正则，gi 表示全局(global)且忽略大小写(case-insensitive)
  const reg = new RegExp(keyword, 'gi') 
  
  // 将匹配到的文本用红色 span 包裹
  return str.replace(reg, (match) => {
    return `<span style="color: red; font-weight: bold; background-color: #ffeb3b;">${match}</span>`
  })
}

// --- 重置功能 ---
const handleReset = () => {
  searchKeyword.value = '' // 清空关键词，computed会自动重新计算显示全部
  currentPage.value = 1    // 回到第一页
  // 可选：如果希望重置时也去后端刷新数据，可以解开下面这行
  // fetchData() 
}

// --- 删除功能 ---
const handleDelete = (row) => {
  // 1: 待支付, 5: 已取消
  if (row.status !== 1 && row.status !== 5) {
    ElMessage.warning('当前订单状态不可删除')
    return
  }

  ElMessageBox.confirm(
    `确定要删除订单 ${row.orderId} 吗？`,
    '删除确认',
    { type: 'warning' }
  ).then(async () => {
    try {
      await deleteOrder(row.orderId)
      ElMessage.success('删除成功')
      // 删除后重新拉取全量数据
      fetchData()
    } catch (error) {
      console.error(error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// --- 查看详情 ---
const handleView = async (row) => {
  try {
    const res = await getOrderDetail(row.orderId)
    if(res && res.data) {
      currentOrder.value = res.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取订单详情异常')
  }
}

// --- 新建订单逻辑 ---
const openCreateDialog = () => {
  newOrderForm.userId = null
  newOrderForm.items = [{ productId: null, price: null, quantity: 1 }]
  createDialogVisible.value = true
  console.log('打开新建订单弹窗', newOrderForm)
}

const addOrderItem = () => {
  newOrderForm.items.push({ productId: null, price: null, quantity: 1 })
}

const removeOrderItem = (index) => {
  newOrderForm.items.splice(index, 1)
}

const submitCreateOrder = async () => {
  if (!newOrderForm.userId) {
    return ElMessage.warning('请输入客户ID')
  }
  
  for (const item of newOrderForm.items) {
    if (!item.productId || !item.price) {
      return ElMessage.warning('请补全商品ID和单价')
    }
  }
  
  try {
    const total = newOrderForm.items.reduce((sum, item) => {
      return sum + (Number(item.price) * Number(item.quantity))
    }, 0)
    
    const payload = {
      userId: Number(newOrderForm.userId),
      paymentMethod: newOrderForm.paymentMethod,
      totalAmount: total,
      addressId: 1, 
      orderItems: newOrderForm.items.map(item => ({
        productId: Number(item.productId),
        quantity: Number(item.quantity),
        price: Number(item.price)
      }))
    }
    
    const res = await createOrder(payload)
    if (res && (res.success || res.code === 200)) {
      ElMessage.success('订单创建成功')
      createDialogVisible.value = false
      fetchData() // 刷新列表
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('创建订单异常')
  }
}

// --- 格式化工具 ---
const formatAmount = (val) => Number(val || 0).toFixed(2)

const formatTime = (timeArr) => {
  if (!Array.isArray(timeArr)) return timeArr
  const [year, month, day, hour, minute] = timeArr
  const pad = (n) => (n < 10 ? '0' + n : n)
  return `${year}-${pad(month)}-${pad(day)} ${pad(hour)}:${pad(minute)}`
}

const getStatusText = (status) => {
  const map = { 1: '待付款', 2: '已支付', 3: '已发货', 4: '已完成', 5: '已取消' }
  return map[status] || `状态${status}`
}

const getStatusType = (status) => {
  if (status === 4) return 'success'
  if (status === 2 || status === 3) return 'primary'
  if (status === 1) return 'warning'
  if (status === 5) return 'info'
  return ''
}

const formatPaymentMethod = (val) => {
  const map = { 'wxpay': '微信支付', 'alipay': '支付宝', 'credit_card': '信用卡' }
  return map[val] || val || '-'
}

// 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  align-items: center;
}
.amount {
  font-family: Consolas, Monaco, monospace;
  font-weight: 600;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.order-items-section {
  border: 1px dashed #dcdfe6;
  padding: 15px;
  border-radius: 4px;
  margin-top: 10px;
  background-color: #fafafa;
}
.item-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}
</style>