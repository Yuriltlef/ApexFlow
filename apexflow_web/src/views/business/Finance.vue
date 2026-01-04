<template>
  <div class="finance-page">
    <h2>💰 收入管理</h2>

    <el-row :gutter="16" class="finance-stats">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="color: #52c41a; background: #f6ffed;">
              <el-icon size="24">
                <Money />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ formatNumber(stats.totalIncome) }}</div>
              <div class="stat-label">总收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="color: #1890ff; background: #e6f7ff;">
              <el-icon size="24">
                <Wallet />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ formatNumber(stats.netIncome) }}</div>
              <div class="stat-label">净收入 (扣除退款)</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="color: #faad14; background: #fffbe6;">
              <el-icon size="24">
                <PieChart />
              </el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ formatNumber(stats.totalRefund) }}</div>
              <div class="stat-label">总退款支出</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="table-header">
          <div class="actions">
            <el-input v-model="searchKeyword" placeholder="搜索关联订单号" style="width: 250px; margin-right: 10px;" clearable
              @input="handleLocalSearch">
              <template #prefix><el-icon>
                  <Search />
                </el-icon></template>
            </el-input>

            <el-select v-model="filterStatus" placeholder="入账状态" clearable style="width: 120px; margin-right: 10px;"
              @change="handleLocalSearch">
              <el-option label="待入账" :value="1" />
              <el-option label="已入账" :value="2" />
            </el-select>

            <el-button type="primary" @click="fetchData">
              <el-icon style="margin-right: 5px">
                <RefreshRight />
              </el-icon>
              刷新数据
            </el-button>

            <div class="data-stat" v-if="allTableData.length > 0">
              <span class="stat-item">总记录: <strong>{{ allTableData.length }}</strong></span>
              <span class="stat-item" v-if="searchKeyword || filterStatus">
                筛选结果: <strong class="highlight-text">{{ filteredTableData.length }}</strong>
              </span>
            </div>
          </div>
          <el-button type="success" :icon="Plus" @click="openDialog()">录入收入</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="pagedTableData" stripe style="width: 100%; margin-top: 10px;">
        <el-table-column prop="id" label="ID" width="80" />

        <el-table-column prop="orderId" label="关联订单" width="180">
          <template #default="{ row }">
            <span v-html="highlight(row.orderId)"></span>
          </template>
        </el-table-column>

        <el-table-column prop="amount" label="金额" width="150">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">+¥{{ formatNumber(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ formatPayment(row.paymentMethod) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'warning'">
              {{ row.status === 2 ? '已入账' : '待入账' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="transactionTime" label="交易时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.transactionTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="success" link size="small"
              @click="handleConfirm(row)">确认入账</el-button>
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next" :total="filteredTableData.length" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑收入' : '录入收入'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="关联订单" prop="orderId">
          <el-input v-model="form.orderId" placeholder="请输入订单号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="收入金额" prop="amount">
          <el-input-number v-model="form.amount" :precision="2" :step="100" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="form.paymentMethod" placeholder="请选择" style="width: 100%;">
            <el-option label="支付宝" value="alipay" />
            <el-option label="微信支付" value="wxpay" />
            <el-option label="银行卡" value="card" />
            <el-option label="现金" value="cash" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">待入账</el-radio>
            <el-radio :label="2">已入账</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="交易时间" prop="transactionTime">
          <el-date-picker v-model="form.transactionTime" type="datetime" placeholder="选择日期时间" style="width: 100%;"
            value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" rows="2" />
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
import { Money, Wallet, PieChart, Search, RefreshRight, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFinanceList, getFinanceStats, createFinanceRecord, updateFinanceRecord, deleteFinanceRecord, updateFinanceStatus } from '@/api/finance'

// --- 状态 ---
const loading = ref(false)
const allTableData = ref([]) // 存储所有数据
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const filterStatus = ref(null)

const stats = reactive({
  totalIncome: 0,
  totalRefund: 0,
  netIncome: 0
})

// --- [核心] 数据获取逻辑 ---
const fetchData = async () => {
  loading.value = true
  try {
    // 1. 探测总数
    const probeRes = await getFinanceList({ page: 1, pageSize: 1, type: 'income' })
    if (probeRes && probeRes.success) {
      const total = probeRes.data.totalCount || (probeRes.data.incomes ? probeRes.data.incomes.length : 0)

      // 2. 如果有数据，发起全量请求
      if (total > 0) {
        const fullRes = await getFinanceList({ page: 1, pageSize: total, type: 'income' })
        // 兼容 incomes 或 list
        if (fullRes && fullRes.success) {
          allTableData.value = fullRes.data.incomes || fullRes.data.list || fullRes.data.data || []

          // 更新统计数据
          if (fullRes.data.totalIncome !== undefined) {
            stats.totalIncome = fullRes.data.totalIncome
            stats.totalRefund = fullRes.data.totalRefund
            stats.netIncome = fullRes.data.netIncome
          }
        }
      } else {
        allTableData.value = []
      }
    }

    // 独立获取统计以确保准确（可选，如果列表接口没返回）
    const statsRes = await getFinanceStats()
    if (statsRes && statsRes.success && statsRes.data) {
      stats.totalIncome = statsRes.data.totalIncome
      stats.totalRefund = statsRes.data.totalRefund
      stats.netIncome = statsRes.data.netIncome
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取数据失败')
    allTableData.value = []
  } finally {
    loading.value = false
  }
}

// --- [核心] 本地筛选与分页 ---

const filteredTableData = computed(() => {
  let data = allTableData.value

  // 1. 关键词搜索 (订单号)
  if (searchKeyword.value) {
    const kw = searchKeyword.value.trim().toLowerCase()
    data = data.filter(item =>
      String(item.orderId).toLowerCase().includes(kw)
    )
  }

  // 2. 状态筛选
  if (filterStatus.value) {
    data = data.filter(item => item.status === filterStatus.value)
  }

  return data
})

const pagedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredTableData.value.slice(start, end)
})

const handleLocalSearch = () => {
  currentPage.value = 1
}

// 高亮工具
const highlight = (text) => {
  if (!text) return ''
  const str = String(text)
  const kw = searchKeyword.value.trim()
  if (!kw) return str
  const reg = new RegExp(`(${kw})`, 'gi')
  return str.replace(reg, '<span style="color: red; font-weight: bold;">$1</span>')
}

// --- 增删改逻辑 ---
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  orderId: '',
  amount: 0,
  type: 'income',
  paymentMethod: 'alipay',
  status: 1,
  transactionTime: '',
  remark: ''
})

const rules = {
  orderId: [{ required: true, message: '请输入订单号', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  transactionTime: [{ required: true, message: '请选择时间', trigger: 'change' }]
}

const openDialog = (row = null) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
    // 修复编辑时时间回显问题：如果是数组或 Date，这里要转成字符串给表单组件
    if (Array.isArray(row.transactionTime)) {
      const [y, m, d, h, min, s] = row.transactionTime
      const pad = n => (n || 0).toString().padStart(2, '0')
      form.transactionTime = `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}:${pad(s)}`
    }
  } else {
    isEdit.value = false
    Object.assign(form, {
      id: null,
      orderId: '',
      amount: 0,
      type: 'income',
      paymentMethod: 'alipay',
      status: 1,
      transactionTime: getNowString(),
      remark: ''
    })
  }
  dialogVisible.value = true
}

const getNowString = () => {
  const now = new Date()
  const pad = n => n.toString().padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

const submitForm = async () => {
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateFinanceRecord(form)
          ElMessage.success('更新成功')
        } else {
          await createFinanceRecord(form)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchData()
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleConfirm = async (row) => {
  try {
    await updateFinanceStatus(row.id, 2)
    ElMessage.success('已确认为入账状态')
    fetchData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该条记录吗?', '警告', { type: 'warning' })
    .then(async () => {
      try {
        await deleteFinanceRecord(row.id)
        ElMessage.success('删除成功')
        fetchData()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    }).catch(() => { })
}

// --- 格式化工具 ---
const formatNumber = (val) => Number(val || 0).toFixed(2)

// [修复] 强大的时间格式化，支持数组 [2023, 12, 1, 10, 0]
const formatTime = (val) => {
  if (!val) return '-'
  if (Array.isArray(val)) {
    // 兼容 [yyyy, MM, dd, HH, mm, ss]
    const [y, m, d, h, min, s] = val
    const pad = n => (n || 0).toString().padStart(2, '0')
    return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}${s !== undefined ? ':' + pad(s) : ''}`
  }
  return String(val).replace('T', ' ')
}

const formatPayment = (val) => {
  const map = { alipay: '支付宝', wxpay: '微信', card: '银行卡', cash: '现金' }
  return map[val] || val
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.finance-page {
  /* padding: 20px; */
}

h2 {
  color: #0366d6;
  margin-bottom: 20px;
}

.finance-stats {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  display: flex;
  align-items: center;
}

.data-stat {
  margin-left: 15px;
  font-size: 13px;
  color: #606266;
}

.stat-item {
  margin-left: 10px;
}

.highlight-text {
  color: #f56c6c;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>