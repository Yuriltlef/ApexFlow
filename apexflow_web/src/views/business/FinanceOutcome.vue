<template>
    <div class="finance-outcome-page">
        <h2>💸 支出与退款</h2>

        <el-row :gutter="16" class="stats-row">
            <el-col :span="12">
                <el-card shadow="hover" class="stat-card-refund">
                    <div class="stat-item">
                        <div class="stat-icon">
                            <el-icon size="32">
                                <Remove />
                            </el-icon>
                        </div>
                        <div class="stat-content">
                            <div class="stat-value">¥{{ formatNumber(totalRefund) }}</div>
                            <div class="stat-label">累计退款/支出总额</div>
                        </div>
                    </div>
                </el-card>
            </el-col>
            <el-col :span="12">
                <el-card shadow="hover">
                    <div class="stat-item">
                        <div class="stat-content">
                            <div class="stat-info">
                                <p>💡 提示：此处展示所有类型为“支出(refund)”的财务记录。</p>
                                <p>通常包含售后退款、采购支出或其他运营成本。</p>
                            </div>
                        </div>
                    </div>
                </el-card>
            </el-col>
        </el-row>

        <el-card shadow="never">
            <template #header>
                <div class="table-header">
                    <div class="actions">
                        <el-input v-model="searchKeyword" placeholder="搜索关联订单号"
                            style="width: 250px; margin-right: 10px;" clearable @input="handleLocalSearch">
                            <template #prefix><el-icon>
                                    <Search />
                                </el-icon></template>
                        </el-input>

                        <el-button type="primary" @click="fetchData">
                            <el-icon style="margin-right: 5px">
                                <RefreshRight />
                            </el-icon>
                            刷新
                        </el-button>

                        <div class="data-stat" v-if="allTableData.length > 0">
                            <span class="stat-item">总记录: <strong>{{ allTableData.length }}</strong></span>
                            <span class="stat-item" v-if="searchKeyword">
                                筛选结果: <strong class="highlight-text">{{ filteredTableData.length }}</strong>
                            </span>
                        </div>
                    </div>
                    <el-button type="warning" :icon="Plus" @click="openDialog()">录入支出</el-button>
                </div>
            </template>

            <el-table v-loading="loading" :data="pagedTableData" stripe style="width: 100%; margin-top: 10px;">
                <el-table-column prop="id" label="ID" width="80" />

                <el-table-column prop="orderId" label="关联订单" width="180">
                    <template #default="{ row }">
                        <span v-html="highlight(row.orderId)"></span>
                    </template>
                </el-table-column>

                <el-table-column prop="amount" label="支出金额" width="150">
                    <template #default="{ row }">
                        <span style="color: #67c23a; font-weight: bold;">-¥{{ formatNumber(Math.abs(row.amount))
                            }}</span>
                    </template>
                </el-table-column>

                <el-table-column prop="paymentMethod" label="退款方式" width="120">
                    <template #default="{ row }">
                        <el-tag type="info">{{ formatPayment(row.paymentMethod) }}</el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="status" label="状态" width="120">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 2 ? 'success' : 'info'">
                            {{ row.status === 2 ? '已支出' : '待处理' }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column prop="transactionTime" label="操作时间" width="180">
                    <template #default="{ row }">
                        {{ formatTime(row.transactionTime) }}
                    </template>
                </el-table-column>

                <el-table-column prop="remark" label="备注/原因" show-overflow-tooltip />

                <el-table-column label="操作" width="150" fixed="right">
                    <template #default="{ row }">
                        <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
                        <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-container">
                <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next"
                    :total="filteredTableData.length" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑支出' : '录入支出'" width="500px">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="关联订单" prop="orderId">
                    <el-input v-model="form.orderId" placeholder="请输入订单号" :disabled="isEdit" />
                </el-form-item>
                <el-form-item label="支出金额" prop="amount">
                    <el-input-number v-model="form.amount" :precision="2" :step="100" :min="0" style="width: 100%;" />
                    <div style="font-size: 12px; color: #999;">系统会自动记录为负数</div>
                </el-form-item>
                <el-form-item label="支出方式" prop="paymentMethod">
                    <el-select v-model="form.paymentMethod" placeholder="请选择" style="width: 100%;">
                        <el-option label="原路退回" value="original" />
                        <el-option label="支付宝" value="alipay" />
                        <el-option label="微信支付" value="wxpay" />
                        <el-option label="银行卡" value="card" />
                    </el-select>
                </el-form-item>
                <el-form-item label="状态" prop="status">
                    <el-radio-group v-model="form.status">
                        <el-radio :label="1">待处理</el-radio>
                        <el-radio :label="2">已支出</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="操作时间" prop="transactionTime">
                    <el-date-picker v-model="form.transactionTime" type="datetime" placeholder="选择日期时间"
                        style="width: 100%;" value-format="YYYY-MM-DD HH:mm:ss" />
                </el-form-item>
                <el-form-item label="原因/备注" prop="remark">
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
import { Remove, RefreshRight, Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFinanceList, getFinanceStats, createFinanceRecord, updateFinanceRecord, deleteFinanceRecord } from '@/api/finance'

// --- 状态 ---
const loading = ref(false)
const allTableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const searchKeyword = ref('')
const totalRefund = ref(0)

// --- [核心] 全量获取 ---
const fetchData = async () => {
    loading.value = true
    try {
        // 1. 探测总数 (type='refund')
        const probeRes = await getFinanceList({ page: 1, pageSize: 1, type: 'refund' })
        if (probeRes && probeRes.success) {
            const total = probeRes.data.totalCount || (probeRes.data.incomes ? probeRes.data.incomes.length : 0)

            // 2. 获取全量
            if (total > 0) {
                const fullRes = await getFinanceList({ page: 1, pageSize: total, type: 'refund' })
                if (fullRes && fullRes.success) {
                    allTableData.value = fullRes.data.incomes || fullRes.data.list || fullRes.data.data || []
                }
            } else {
                allTableData.value = []
            }
        }

        // 2. 获取统计
        const statsRes = await getFinanceStats()
        if (statsRes && statsRes.success && statsRes.data) {
            totalRefund.value = statsRes.data.totalRefund
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('获取数据失败')
        allTableData.value = []
    } finally {
        loading.value = false
    }
}

// --- 本地分页与搜索 ---
const filteredTableData = computed(() => {
    let data = allTableData.value
    if (searchKeyword.value) {
        const kw = searchKeyword.value.trim().toLowerCase()
        data = data.filter(item => String(item.orderId).toLowerCase().includes(kw))
    }
    return data
})

const pagedTableData = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredTableData.value.slice(start, end)
})

const handleLocalSearch = () => { currentPage.value = 1 }

const highlight = (text) => {
    if (!text) return ''
    const str = String(text)
    const kw = searchKeyword.value.trim()
    if (!kw) return str
    const reg = new RegExp(`(${kw})`, 'gi')
    return str.replace(reg, '<span style="color: red; font-weight: bold;">$1</span>')
}

// --- 增删改 ---
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const form = reactive({
    id: null,
    orderId: '',
    amount: 0,
    type: 'refund',
    paymentMethod: 'original',
    status: 1,
    transactionTime: '',
    remark: ''
})

const rules = {
    orderId: [{ required: true, message: '请输入关联订单号', trigger: 'blur' }],
    amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
    transactionTime: [{ required: true, message: '请选择时间', trigger: 'change' }]
}

const openDialog = (row = null) => {
    if (row) {
        isEdit.value = true
        Object.assign(form, row)
        form.amount = Math.abs(form.amount)

        // 修复回显
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
            type: 'refund',
            paymentMethod: 'original',
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
                const payload = { ...form }
                payload.amount = Math.abs(payload.amount)

                if (isEdit.value) {
                    await updateFinanceRecord(payload)
                    ElMessage.success('更新成功')
                } else {
                    await createFinanceRecord(payload)
                    ElMessage.success('录入成功')
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

const handleDelete = (row) => {
    ElMessageBox.confirm('确定删除该条支出记录吗?', '警告', { type: 'warning' })
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

// --- 格式化 ---
const formatNumber = (val) => Number(val || 0).toFixed(2)

// [修复] 时间格式化
const formatTime = (val) => {
    if (!val) return '-'
    if (Array.isArray(val)) {
        const [y, m, d, h, min, s] = val
        const pad = n => (n || 0).toString().padStart(2, '0')
        return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}${s !== undefined ? ':' + pad(s) : ''}`
    }
    return String(val).replace('T', ' ')
}

const formatPayment = (val) => {
    const map = { alipay: '支付宝', wxpay: '微信', card: '银行卡', original: '原路退回', cash: '现金' }
    return map[val] || val
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.finance-outcome-page {
    /* padding: 20px; */
}

h2 {
    color: #0366d6;
    margin-bottom: 20px;
}

.stats-row {
    margin-bottom: 20px;
}

.stat-card-refund {
    background-color: #fff1f0;
    border-color: #ffccc7;
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 15px;
}

.stat-icon {
    width: 54px;
    height: 54px;
    border-radius: 50%;
    background: #ff4d4f;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
}

.stat-content {
    flex: 1;
}

.stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #cf1322;
}

.stat-label {
    font-size: 14px;
    color: #820014;
}

.stat-info {
    font-size: 13px;
    color: #666;
    line-height: 1.6;
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