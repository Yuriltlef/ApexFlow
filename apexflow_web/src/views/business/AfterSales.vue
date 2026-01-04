<template>
  <div class="aftersales-page">
    <h2>🔄 售后管理</h2>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索售后单号/订单号/用户ID" 
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
              刷新数据
            </el-button>
            <div class="data-stat" v-if="allTableData.length > 0">
              <span class="stat-item">总数据: <strong>{{ allTableData.length }}</strong> 条</span>
              <span class="stat-item" v-if="searchKeyword">搜索匹配: <strong class="highlight-text">{{ filteredTableData.length }}</strong> 条</span>
            </div>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <el-tab-pane label="待审核" name="pending">
          <template #label>
            待审核 <el-badge :value="getCountByStatus(1)" type="danger" v-if="getCountByStatus(1) > 0" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="审核通过(处理中)" name="approved">
          <template #label>
             处理中 <el-badge :value="getCountByStatus(2)" type="warning" v-if="getCountByStatus(2) > 0" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="全部" name="all" />
      </el-tabs>

      <el-table 
        v-loading="loading" 
        :data="pagedTableData" 
        stripe 
        style="width: 100%; margin-top: 10px;"
        empty-text="暂无相关数据"
      >
        <el-table-column label="售后单号/ID" width="140">
           <template #default="{ row }">
             <span v-html="highlight(row.refundNo || String(row.id))"></span>
           </template>
        </el-table-column>
        
        <el-table-column label="关联订单" width="180">
           <template #default="{ row }">
             <span v-html="highlight(row.orderId)"></span>
           </template>
        </el-table-column>
        
        <el-table-column label="用户ID" width="120">
           <template #default="{ row }">
             <span v-html="highlight(row.userId || '-')"></span>
           </template>
        </el-table-column>
        
        <el-table-column prop="type" label="售后类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getServiceTypeTag(row.type)" size="small">
              {{ getServiceTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="refundAmount" label="申请金额" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c;">¥{{ formatAmount(row.refundAmount || row.amount) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="reason" label="申请原因" min-width="150" show-overflow-tooltip>
           <template #default="{ row }">
             <span v-html="highlight(row.reason)"></span>
           </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" effect="plain">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申请时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime || row.applyTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="[1, 2].includes(row.status)"
              type="primary" 
              size="small" 
              link 
              @click="openProcessDialog(row)"
            >
              {{ row.status === 1 ? '审核' : '结单' }}
            </el-button>
            <el-button type="info" size="small" link @click="openDetailDialog(row)">查看</el-button>
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

    <el-dialog v-model="processDialogVisible" :title="processTitle" width="500px">
      <el-form ref="processFormRef" :model="processForm" label-width="80px">
        <el-form-item label="售后单号">
          <span>{{ currentRow?.refundNo || currentRow?.id }}</span>
        </el-form-item>
        <el-form-item label="申请金额">
          <span style="color: red;">¥{{ formatAmount(currentRow?.refundAmount || currentRow?.amount) }}</span>
        </el-form-item>
        
        <el-form-item label="处理结果" prop="action">
          <el-radio-group v-model="processForm.action">
            <el-radio label="approve">{{ approveText }}</el-radio>
            <el-radio label="reject" v-if="currentRow?.status === 1">拒绝申请</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="退款金额" v-if="processForm.action === 'approve'">
           <el-input-number 
             v-model="processForm.refundAmount" 
             :min="0" 
             :max="currentRow?.refundAmount || currentRow?.amount" 
             :precision="2" 
           />
           <div style="font-size: 12px; color: #999;">不得超过申请金额</div>
        </el-form-item>

        <el-form-item label="处理备注">
          <el-input 
            v-model="processForm.remark" 
            type="textarea" 
            rows="3" 
            placeholder="请输入处理意见" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="processDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitProcess">确认处理</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="售后详情" width="600px">
      <div v-if="detailData">
        <el-descriptions border :column="2">
          <el-descriptions-item label="售后ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(detailData.applyTime || detailData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="关联订单">{{ detailData.orderId }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ detailData.userId || currentRow?.userId || '-' }}</el-descriptions-item>
          
          <el-descriptions-item label="售后类型">
            {{ getServiceTypeText(detailData.type) }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="getStatusTag(detailData.status)">{{ getStatusText(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请金额">¥{{ formatAmount(detailData.refundAmount) }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.phone || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px;">
          <h4>申请原因</h4>
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px;">
            {{ detailData.reason }}
          </div>
        </div>
        
        <div style="margin-top: 20px;" v-if="detailData.processRemark">
           <h4>处理备注</h4>
           <div style="padding: 10px; background: #f0f9eb; border-radius: 4px; color: #67c23a;">
             {{ detailData.processRemark }}
           </div>
        </div>

        <div style="margin-top: 20px;" v-if="detailData.processTime">
           <h4>处理时间</h4>
           <div>{{ formatTime(detailData.processTime) }}</div>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAfterSalesList, auditAfterSales, getAfterSalesDetail } from '@/api/aftersales'
import { getOrderList } from '@/api/order'

// --- 状态定义 ---
const loading = ref(false)
const activeTab = ref('pending') 
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const allTableData = ref([])

// --- [核心修复] 获取所有数据并合并订单用户ID ---
const fetchData = async () => {
  loading.value = true
  try {
    // 1. 获取售后数据 (探测总数 -> 获取全部)
    const afterSalesPromise = (async () => {
      // 先只取1条看看总数
      const initRes = await getAfterSalesList({ page: 1, pageSize: 1, status: null, keyword: '' })
      if (initRes && initRes.success && initRes.data) {
        const total = initRes.data.totalCount || 0
        if (total > 0) {
          // 获取全部
          const fullRes = await getAfterSalesList({ page: 1, pageSize: total, status: null, keyword: '' })
          if (fullRes && fullRes.success && fullRes.data && Array.isArray(fullRes.data.data)) {
            return fullRes.data.data
          }
        }
      }
      return []
    })()

    // 2. 获取订单数据 (探测总数 -> 获取全部) - 用来匹配用户ID
    const ordersPromise = (async () => {
       const initRes = await getOrderList({ page: 1, pageSize: 1, keyword: '' })
       let total = 0
       // 根据您的日志，success为true时，data里可能有totalCount等分页信息
       if (initRes && initRes.success && initRes.data) {
          // 兼容写法，取 totalCount 或 total
          total = initRes.data.totalCount || initRes.data.total || 0
       }
       
       if (total > 0) {
          const fullRes = await getOrderList({ page: 1, pageSize: total, keyword: '' })
          // 【核心修复】根据您的日志，列表在 data.orders 数组中
          if (fullRes && fullRes.success && fullRes.data) {
              if (Array.isArray(fullRes.data.orders)) {
                  return fullRes.data.orders
              }
              // 防御性代码：万一结构变了
              if (Array.isArray(fullRes.data.data)) return fullRes.data.data
          }
       }
       return []
    })()

    // 3. 并行执行
    const [afterSalesList, orderList] = await Promise.all([afterSalesPromise, ordersPromise])

    // 4. 构建订单 Map (OrderId -> UserId)
    const orderMap = {}
    if (Array.isArray(orderList)) {
        orderList.forEach(order => {
            // orderId 是 String, userId 是 Number (如 111)
            const key = order.orderId
            const uid = order.userId
            if (key) orderMap[key] = uid
        })
    }

    // 5. 将 UserId 注入售后列表
    allTableData.value = afterSalesList.map(item => {
        return {
            ...item,
            // 匹配 OrderId 找到 UserId
            userId: orderMap[item.orderId] || '-' 
        }
    })

  } catch (error) {
    console.error(error)
    ElMessage.error('获取数据失败')
    allTableData.value = []
  } finally {
    loading.value = false
  }
}

// --- 核心：前端过滤与分页 (Computed) ---

const filteredTableData = computed(() => {
  let data = allTableData.value

  // A. 状态过滤
  if (activeTab.value === 'pending') {
    data = data.filter(item => item.status === 1)
  } else if (activeTab.value === 'approved') {
    data = data.filter(item => item.status === 2)
  } else if (activeTab.value === 'completed') {
    data = data.filter(item => item.status === 4)
  }

  // B. 关键词搜索过滤 (支持 ID、订单号、用户ID)
  const kw = searchKeyword.value.trim().toLowerCase()
  if (kw) {
    data = data.filter(item => {
      const id = String(item.id || '').toLowerCase()
      const refundNo = String(item.refundNo || '').toLowerCase()
      const orderId = String(item.orderId || '').toLowerCase()
      const userId = String(item.userId || '').toLowerCase() // 搜索用户ID
      const reason = String(item.reason || '').toLowerCase()
      
      return id.includes(kw) || 
             refundNo.includes(kw) || 
             orderId.includes(kw) || 
             userId.includes(kw) ||
             reason.includes(kw)
    })
  }

  return data
})

const pagedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTableData.value.slice(start, end)
})

const highlight = (text) => {
  if (!text) return ''
  const str = String(text)
  const kw = searchKeyword.value.trim()
  if (!kw) return str
  const reg = new RegExp(`(${kw})`, 'gi')
  return str.replace(reg, '<span style="color: red; font-weight: bold;">$1</span>')
}

const getCountByStatus = (status) => {
  return allTableData.value.filter(item => item.status === status).length
}

const handleTabChange = () => { currentPage.value = 1 }
const handleSearch = () => { currentPage.value = 1 }

// --- 业务操作 ---

const processDialogVisible = ref(false)
const submitLoading = ref(false)
const currentRow = ref(null)
const processForm = reactive({
  action: 'approve',
  remark: '',
  refundAmount: 0
})

const processTitle = computed(() => currentRow.value?.status === 1 ? '售后审核' : '确认结单')
const approveText = computed(() => currentRow.value?.status === 1 ? '审核通过' : '确认完成')

const openProcessDialog = (row) => {
  currentRow.value = row
  processForm.action = 'approve'
  processForm.remark = ''
  processForm.refundAmount = row.refundAmount || row.amount || 0
  processDialogVisible.value = true
}

const submitProcess = async () => {
  submitLoading.value = true
  try {
    let newStatus = 1;
    if (processForm.action === 'reject') {
        newStatus = 3; 
    } else {
        if (currentRow.value.status === 1) newStatus = 2;
        else if (currentRow.value.status === 2) newStatus = 4;
    }

    // 注意：auditAfterSales 已改为将ID拼在URL中
    const res = await auditAfterSales({
      id: currentRow.value.id, 
      status: newStatus,
      remark: processForm.remark
    })
    
    if(res && res.success) {
       ElMessage.success('处理成功')
       processDialogVisible.value = false
       fetchData()
    } else {
       ElMessage.error(res.message || '处理失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('处理失败')
  } finally {
    submitLoading.value = false
  }
}

const detailDialogVisible = ref(false)
const detailData = ref(null)

const openDetailDialog = async (row) => {
  try {
    const res = await getAfterSalesDetail(row.id)
    if (res && res.success) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取详情失败')
  }
}

// --- Formatters ---
const formatAmount = (val) => Number(val || 0).toFixed(2)

const formatTime = (time) => {
  if (!time) return '-'
  if (Array.isArray(time)) {
    const [y, m, d, h, min] = time
    const pad = n => (n < 10 ? '0' + n : n)
    return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}`
  }
  return time 
}

const getServiceTypeText = (type) => {
  const map = { 1: '退货', 2: '换货', 3: '维修' }
  return map[type] || type || '未知'
}

const getServiceTypeTag = (type) => {
  if (type === 1) return 'danger'
  if (type === 2) return 'warning'
  if (type === 3) return 'primary'
  return ''
}

const getStatusText = (status) => {
  const map = { 1: '申请中', 2: '审核通过', 3: '审核拒绝', 4: '已完成' }
  return map[status] || `状态${status}`
}

const getStatusTag = (status) => {
  if (status === 4) return 'success'
  if (status === 3) return 'info'
  if (status === 2) return 'warning'
  if (status === 1) return 'danger'
  return ''
}

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