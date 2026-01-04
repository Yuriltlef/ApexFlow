<template>
    <div class="warehouse-inout-page">
        <h2>📋 出入库记录</h2>

        <el-card shadow="never">
            <template #header>
                <div class="table-actions">
                    <el-input 
                        v-model="logQuery.productId" 
                        placeholder="输入商品ID搜索" 
                        style="width: 200px; margin-right: 10px;"
                        clearable 
                        @input="handleLocalSearch"
                    >
                        <template #prefix><el-icon><Search /></el-icon></template>
                    </el-input>

                    <el-select 
                        v-model="logQuery.changeType" 
                        placeholder="变更类型" 
                        clearable
                        style="width: 150px; margin-right: 10px;" 
                        @change="handleLocalSearch"
                    >
                        <el-option label="全部" value="" />
                        <el-option label="采购入库" value="purchase" />
                        <el-option label="销售出库" value="sale" />
                        <el-option label="盘点调整" value="adjust" />
                    </el-select>

                    <el-button type="primary" @click="fetchLogs">
                        <el-icon style="margin-right: 5px"><RefreshRight /></el-icon>
                        刷新数据
                    </el-button>

                    <div class="data-stat" v-if="allLogList.length > 0">
                        <span class="stat-item">总记录: <strong>{{ allLogList.length }}</strong></span>
                        <span class="stat-item" v-if="logQuery.productId || logQuery.changeType">
                            筛选结果: <strong class="highlight-text">{{ filteredLogList.length }}</strong>
                        </span>
                    </div>
                </div>
            </template>

            <el-table v-loading="loading" :data="pagedLogList" stripe style="width: 100%;">
                <el-table-column prop="id" label="日志ID" width="80" />
                <el-table-column prop="productId" label="商品ID" width="100">
                    <template #default="{ row }">
                        <span v-html="highlight(row.productId)"></span>
                    </template>
                </el-table-column>
                
                <el-table-column prop="changeType" label="变更类型" width="120">
                    <template #default="{ row }">
                        <el-tag :type="getLogTypeTag(row.changeType)">
                            {{ getLogTypeText(row.changeType) }}
                        </el-tag>
                    </template>
                </el-table-column>
                
                <el-table-column prop="quantity" label="变动数量" width="120">
                    <template #default="{ row }">
                        <span :style="{ color: row.quantity > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
                            {{ row.quantity > 0 ? '+' + row.quantity : row.quantity }}
                        </span>
                    </template>
                </el-table-column>

                <el-table-column label="库存变化" width="150">
                    <template #default="{ row }">
                        {{ row.beforeStock }} <el-icon><Right /></el-icon> {{ row.afterStock }}
                    </template>
                </el-table-column>

                <el-table-column prop="orderId" label="关联单号" width="180">
                    <template #default="{ row }">
                        {{ row.orderId || '-' }}
                    </template>
                </el-table-column>

                <el-table-column prop="createdAt" label="操作时间" min-width="160">
                    <template #default="{ row }">
                        {{ formatTime(row.createdAt) }}
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-container">
                <el-pagination
                    v-model:current-page="logQuery.page"
                    v-model:page-size="logQuery.pageSize"
                    :page-sizes="[10, 20, 50, 100]"
                    layout="total, sizes, prev, pager, next"
                    :total="filteredLogList.length"
                />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Right, Search, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getInventoryLogs } from '@/api/warehouse'

// --- 状态数据 ---
const loading = ref(false)

// 存储从后端获取的所有数据
const allLogList = ref([])

// 查询与分页状态
const logQuery = reactive({
    page: 1,
    pageSize: 10,
    productId: '',
    changeType: ''
})

// --- [核心] 数据获取策略：探测总数 -> 获取全量 ---
const fetchLogs = async () => {
    loading.value = true
    try {
        // 1. 探测请求：先获取总条数 (pageSize=1 极小开销)
        // 注意：这里不传任何过滤参数，目的是拉取数据库里所有的日志到前端
        const probeParams = {
            page: 1,
            pageSize: 1
        }
        const probeRes = await getInventoryLogs(probeParams)
        
        if (probeRes && probeRes.success) {
            const totalCount = probeRes.data.totalCount || 0
            
            if (totalCount > 0) {
                // 2. 全量请求：根据真实的 totalCount 拉取所有数据
                const fullParams = {
                    page: 1,
                    pageSize: totalCount
                }
                const fullRes = await getInventoryLogs(fullParams)
                
                if (fullRes && fullRes.success) {
                    // 存储全量数据
                    allLogList.value = fullRes.data.logs || []
                    ElMessage.success(`成功加载 ${allLogList.value.length} 条记录`)
                } else {
                    allLogList.value = []
                }
            } else {
                allLogList.value = []
            }
        }
    } catch (error) {
        console.error(error)
        ElMessage.error('获取日志失败')
        allLogList.value = []
    } finally {
        loading.value = false
    }
}

// --- [核心] 前端计算属性：过滤与分页 ---

// 1. 过滤逻辑
const filteredLogList = computed(() => {
    let data = allLogList.value

    // 按商品ID过滤
    if (logQuery.productId) {
        const keyword = String(logQuery.productId).toLowerCase()
        data = data.filter(item => 
            String(item.productId).toLowerCase().includes(keyword)
        )
    }

    // 按变更类型过滤
    if (logQuery.changeType) {
        data = data.filter(item => item.changeType === logQuery.changeType)
    }

    return data
})

// 2. 分页逻辑
const pagedLogList = computed(() => {
    const start = (logQuery.page - 1) * logQuery.pageSize
    const end = start + logQuery.pageSize
    return filteredLogList.value.slice(start, end)
})

// --- 交互处理 ---
const handleLocalSearch = () => {
    // 搜索条件变化时，重置回第一页
    logQuery.page = 1
}

// 高亮搜索词
const highlight = (text) => {
    if (!text) return ''
    const str = String(text)
    const kw = logQuery.productId
    if (!kw) return str
    const reg = new RegExp(`(${kw})`, 'gi')
    return str.replace(reg, '<span style="color: #f56c6c; font-weight: bold;">$1</span>')
}

// --- 格式化工具 ---
const formatTime = (time) => {
    if (!time) return '-'
    if (Array.isArray(time)) {
        const [y, m, d, h, min] = time
        const pad = n => (n < 10 ? '0' + n : n)
        return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}`
    }
    return String(time).replace('T', ' ')
}

const getLogTypeTag = (type) => {
    const map = { 'purchase': 'success', 'sale': 'primary', 'adjust': 'warning' }
    return map[type] || 'info'
}

const getLogTypeText = (type) => {
    const map = { 'purchase': '采购入库', 'sale': '销售出库', 'adjust': '盘点调整' }
    return map[type] || type
}

onMounted(() => {
    fetchLogs()
})
</script>

<style scoped>
.warehouse-inout-page {
    /* padding: 20px; */
}

h2 {
    color: #0366d6;
    margin-bottom: 20px;
}

.table-actions {
    display: flex;
    align-items: center;
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
    display: flex;
    justify-content: flex-end;
}
</style>