<template>
  <div class="logistics-page">
    <h2>🚚 物流管理</h2>

    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #fffbe6; color: #faad14;">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingCount }}</div>
              <div class="stat-label">待发货</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6f7ff; color: #1890ff;">
              <el-icon><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.shippedCount }}</div>
              <div class="stat-label">运输中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f6ffed; color: #52c41a;">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.deliveredCount }}</div>
              <div class="stat-label">已送达</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索订单号/运单号/快递公司" 
              style="width: 300px; margin-right: 10px;" 
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="fetchData">
              <el-icon style="margin-right: 5px"><RefreshRight /></el-icon>
              刷新
            </el-button>
            <div class="data-stat" v-if="allTableData.length > 0">
              <span class="stat-item">总记录: <strong>{{ allTableData.length }}</strong></span>
              <span class="stat-item" v-if="searchKeyword">搜索匹配: <strong class="highlight-text">{{ filteredTableData.length }}</strong></span>
            </div>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <el-tab-pane label="待发货" name="pending">
          <template #label>
            待发货 <el-badge :value="getCountByStatus('pending')" type="warning" v-if="getCountByStatus('pending') > 0" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="运输中" name="shipped">
           <template #label>
            运输中 <el-badge :value="getCountByStatus('shipped')" type="primary" v-if="getCountByStatus('shipped') > 0" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已送达" name="delivered" />
        <el-tab-pane label="全部" name="all" />
      </el-tabs>

      <el-table 
        v-loading="loading" 
        :data="pagedTableData" 
        stripe 
        style="width: 100%; margin-top: 10px;"
        empty-text="暂无物流数据"
      >
        <el-table-column label="订单号" width="180">
          <template #default="{ row }">
            <span v-html="highlight(row.orderId)"></span>
          </template>
        </el-table-column>

        <el-table-column label="快递公司" width="140">
           <template #default="{ row }">
            <span v-if="row.expressCompany" v-html="highlight(row.expressCompany)"></span>
            <el-tag v-else type="info" size="small">未发货</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="运单号" width="180">
           <template #default="{ row }">
            <span v-if="row.trackingNumber" v-html="highlight(row.trackingNumber)"></span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" effect="plain">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="收货地址" min-width="200" show-overflow-tooltip>
           <template #default="{ row }">
             {{ row.receiverAddress || '-' }}
           </template>
        </el-table-column>

        <el-table-column label="时间信息" width="220">
          <template #default="{ row }">
            <div v-if="row.deliveredAt" style="font-size: 12px; color: #67c23a;">
              送达: {{ formatTime(row.deliveredAt) }}
            </div>
            <div v-else-if="row.shippedAt" style="font-size: 12px; color: #409eff;">
              发货: {{ formatTime(row.shippedAt) }}
            </div>
            <div v-else style="font-size: 12px; color: #909399;">
              创建: {{ formatTime(row.createdAt) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'pending'"
              type="primary" 
              size="small" 
              @click="openShipDialog(row)"
            >
              发货
            </el-button>
            
            <el-button 
              v-if="row.status === 'shipped'"
              type="success" 
              size="small" 
              plain
              @click="handleConfirmDelivery(row)"
            >
              确认送达
            </el-button>

             <el-button 
              v-if="row.status !== 'pending'"
              type="primary" 
              link
              size="small" 
              @click="openEditDialog(row)"
            >
              编辑
            </el-button>
            
            <el-button 
              type="danger" 
              link 
              size="small" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          :total="filteredTableData.length"
        />
      </div>
    </el-card>

    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'ship' ? '订单发货' : '修改物流信息'" 
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ form.orderId }}</span>
        </el-form-item>
        
        <el-form-item label="快递公司" prop="expressCompany">
          <el-select v-model="form.expressCompany" placeholder="请选择快递公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="京东物流" value="京东物流" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="EMS" value="EMS" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="运单号" prop="trackingNumber">
          <el-input v-model="form.trackingNumber" placeholder="请输入运单号" />
        </el-form-item>

        <el-form-item label="发货地址" prop="senderAddress">
          <el-input v-model="form.senderAddress" type="textarea" rows="2" placeholder="发货人地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, RefreshRight, Box, Van, CircleCheck } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getLogisticsList, 
  getLogisticsStats, 
  shipLogistics, 
  updateLogisticsStatus,
  updateLogisticsInfo,
  deleteLogistics 
} from '@/api/logistics'

// --- 状态定义 ---
const loading = ref(false)
const activeTab = ref('all') // pending, shipped, delivered, all
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const allTableData = ref([])
const stats = reactive({
  pendingCount: 0,
  shippedCount: 0,
  deliveredCount: 0
})

// --- 数据获取 (全量获取 + 本地过滤) ---
const fetchData = async () => {
  loading.value = true
  try {
    // 1. 获取统计数据
    const statsRes = await getLogisticsStats()
    console.log('物流统计数据', statsRes)
    if (statsRes && statsRes.success) {
      stats.pendingCount = statsRes.data.pendingCount || 0
      stats.shippedCount = statsRes.data.shippedCount || 0
      stats.deliveredCount = statsRes.data.deliveredCount || 0
    }

    // 2. 获取列表数据 (先探测总数)
    const initRes = await getLogisticsList({ page: 1, pageSize: 1 })
    if (initRes && initRes.success && initRes.data) {
      console.log('物流列表初始数据', initRes)
      const total = initRes.data.totalCount || 0
      if (total > 0) {
        // 全量拉取
        const fullRes = await getLogisticsList({ page: 1, pageSize: total })
        console.log('物流列表全量数据', fullRes)
        if (fullRes && fullRes.success && fullRes.data) {
          allTableData.value = fullRes.data.data || []
        }
      } else {
        allTableData.value = []
      }
    } else {
      allTableData.value = []
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取物流数据失败')
  } finally {
    loading.value = false
  }
}

// --- 计算属性：过滤与分页 ---
const filteredTableData = computed(() => {
  let data = allTableData.value

  // 1. Tab 过滤
  if (activeTab.value !== 'all') {
    data = data.filter(item => item.status === activeTab.value)
  }

  // 2. 关键词过滤
  const kw = searchKeyword.value.trim().toLowerCase()
  if (kw) {
    data = data.filter(item => {
      const orderId = String(item.orderId || '').toLowerCase()
      const tracking = String(item.trackingNumber || '').toLowerCase()
      const company = String(item.expressCompany || '').toLowerCase()
      return orderId.includes(kw) || tracking.includes(kw) || company.includes(kw)
    })
  }

  return data
})

const pagedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTableData.value.slice(start, end)
})

// --- 辅助功能 ---
const handleTabChange = () => { currentPage.value = 1 }
const handleSearch = () => { currentPage.value = 1 }

const getCountByStatus = (status) => {
  return allTableData.value.filter(item => item.status === status).length
}

const highlight = (text) => {
  if (!text) return ''
  const str = String(text)
  const kw = searchKeyword.value.trim()
  if (!kw) return str
  const reg = new RegExp(`(${kw})`, 'gi')
  return str.replace(reg, '<span style="color: red; font-weight: bold;">$1</span>')
}

// --- 业务操作 ---

// 弹窗相关
const dialogVisible = ref(false)
const dialogType = ref('ship') // 'ship' or 'edit'
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  orderId: '',
  expressCompany: '',
  trackingNumber: '',
  senderAddress: '上海市中心仓' // 默认发货地址
})

const rules = {
  expressCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  trackingNumber: [{ required: true, message: '请输入运单号', trigger: 'blur' }]
}

// 打开“发货”弹窗
const openShipDialog = (row) => {
  dialogType.value = 'ship'
  form.id = row.id
  form.orderId = row.orderId
  form.expressCompany = ''
  form.trackingNumber = ''
  form.senderAddress = '上海市中心仓'
  dialogVisible.value = true
}

// 打开“编辑”弹窗
const openEditDialog = (row) => {
  dialogType.value = 'edit'
  form.id = row.id
  form.orderId = row.orderId
  form.expressCompany = row.expressCompany
  form.trackingNumber = row.trackingNumber
  form.senderAddress = row.senderAddress
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'ship') {
          // 发货接口 PUT /api/logistics/{id}/shipping
          await shipLogistics(form.id, {
            expressCompany: form.expressCompany,
            trackingNumber: form.trackingNumber,
            senderAddress: form.senderAddress
          })
          ElMessage.success('发货成功')
        } else {
          // 编辑接口 PUT /api/logistics/{id}
          await updateLogisticsInfo(form.id, {
            orderId: form.orderId, // 必填校验带上
            expressCompany: form.expressCompany,
            trackingNumber: form.trackingNumber,
            senderAddress: form.senderAddress,
            receiverAddress: '' // 可选
          })
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchData() // 刷新
      } catch (error) {
        console.error(error)
        ElMessage.error(dialogType.value === 'ship' ? '发货失败' : '更新失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 确认送达
const handleConfirmDelivery = (row) => {
  ElMessageBox.confirm(`确认订单 ${row.orderId} 的物流已送达?`, '提示', {
    confirmButtonText: '确认送达',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    try {
      await updateLogisticsStatus(row.id, 'delivered')
      ElMessage.success('状态更新成功')
      fetchData()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// 删除记录
const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该物流记录吗？删除后无法恢复。', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteLogistics(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// --- Formatters ---
const formatTime = (time) => {
  if (!time) return '-'
  if (Array.isArray(time)) {
    const [y, m, d, h, min] = time
    const pad = n => (n < 10 ? '0' + n : n)
    return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}`
  }
  return String(time).replace('T', ' ')
}

const getStatusText = (status) => {
  const map = { 'pending': '待发货', 'shipped': '运输中', 'delivered': '已送达' }
  return map[status] || status
}

const getStatusTag = (status) => {
  const map = { 'pending': 'warning', 'shipped': 'primary', 'delivered': 'success' }
  return map[status] || 'info'
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.logistics-page {
  /* padding: 20px; */
}

h2 {
  color: #0366d6;
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  width: 100%;
}

.data-stat {
  margin-left: auto;
  font-size: 13px;
  color: #606266;
}

.stat-item {
  margin-left: 15px;
}

.highlight-text {
  color: #f56c6c;
}

.pagination-container {
  margin-top: 20px;
}
</style>