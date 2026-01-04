<template>
  <div class="order-analysis">
    <h2>📊 订单数据分析</h2>

    <el-row :gutter="20" class="analysis-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="analysis-card">
            <div class="card-icon blue-bg">
              <el-icon size="24"><List /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ stats.totalOrders }}</div>
              <div class="card-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="analysis-card">
            <div class="card-icon green-bg">
              <el-icon size="24"><Money /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">¥{{ formatAmount(stats.totalSales) }}</div>
              <div class="card-label">总销售额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="analysis-card">
            <div class="card-icon orange-bg">
              <el-icon size="24"><Goods /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">¥{{ formatAmount(stats.avgOrderValue) }}</div>
              <div class="card-label">客单价</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="analysis-card">
            <div class="card-icon purple-bg">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">{{ stats.activeUsers }}</div>
              <div class="card-label">下单用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>📈 近期销售趋势 (Sales Trend)</span>
            </div>
          </template>
          <div ref="trendChartRef" style="width: 100%; height: 350px;" v-loading="loading"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>🥧 订单状态分布</span>
            </div>
          </template>
          <div ref="pieChartRef" style="width: 100%; height: 350px;" v-loading="loading"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, onUnmounted } from 'vue'
import { List, Money, Goods, User } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getAllOrdersForAnalysis } from '@/api/order' // 导入我们刚新增的API
import { ElMessage } from 'element-plus'

// --- 状态定义 ---
const loading = ref(false)
const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChartInstance = null
let pieChartInstance = null

// 统计数据
const stats = reactive({
  totalOrders: 0,
  totalSales: 0,
  avgOrderValue: 0,
  activeUsers: 0
})

// --- 数据处理与图表渲染 ---

const fetchDataAndRender = async () => {
  loading.value = true
  try {
    const res = await getAllOrdersForAnalysis()
    if (res && res.data && res.data.orders) {
      const orders = res.data.orders
      calculateStats(orders)
      initCharts(orders)
    }
  } catch (error) {
    console.error('获取分析数据失败', error)
    ElMessage.error('无法加载分析数据')
  } finally {
    loading.value = false
  }
}

// 1. 计算核心指标
const calculateStats = (orders) => {
  stats.totalOrders = orders.length
  
  // 计算总金额 (累加 totalAmount)
  const totalMoney = orders.reduce((sum, order) => sum + (Number(order.totalAmount) || 0), 0)
  stats.totalSales = totalMoney
  
  // 计算客单价
  stats.avgOrderValue = orders.length > 0 ? (totalMoney / orders.length) : 0
  
  // 计算独立用户数 (Set去重)
  const uniqueUsers = new Set(orders.map(o => o.userId))
  stats.activeUsers = uniqueUsers.size
}

// ... 前面的代码保持不变 ...

// 2. 初始化图表
const initCharts = (orders) => {
  if (!orders || orders.length === 0) return

  // --- A. 处理趋势图数据 (按日期聚合) ---
  const dateMap = new Map()
  
  orders.forEach(order => {
    let dateStr = ''
    if (Array.isArray(order.createdAt)) {
      const [y, m, d] = order.createdAt
      dateStr = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    } else if (typeof order.createdAt === 'string') {
      dateStr = order.createdAt.substring(0, 10)
    }

    if (!dateMap.has(dateStr)) {
      dateMap.set(dateStr, { amount: 0, count: 0 })
    }
    const data = dateMap.get(dateStr)
    data.amount += Number(order.totalAmount || 0)
    data.count += 1
  })

  const sortedDates = Array.from(dateMap.keys()).sort()
  const seriesAmount = sortedDates.map(d => dateMap.get(d).amount.toFixed(2))
  const seriesCount = sortedDates.map(d => dateMap.get(d).count)

  // 渲染趋势图
  if (trendChartRef.value) {
    trendChartInstance = echarts.init(trendChartRef.value)
    trendChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      // [修改点 1] 趋势图图例移动到右上角
      legend: { 
        data: ['销售额', '订单数'],
        top: 0,      // 距离顶部 0
        right: 10    // 距离右侧 10px
      },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: sortedDates },
      yAxis: [
        { type: 'value', name: '金额 (¥)' },
        { type: 'value', name: '单量' }
      ],
      series: [
        {
          name: '销售额',
          type: 'line',
          smooth: true,
          itemStyle: { color: '#52c41a' },
          areaStyle: { color: 'rgba(82, 196, 26, 0.1)' },
          data: seriesAmount
        },
        {
          name: '订单数',
          type: 'line',
          yAxisIndex: 1,
          smooth: true,
          itemStyle: { color: '#1890ff' },
          data: seriesCount
        }
      ]
    })
  }

  // --- B. 处理饼图数据 (按状态聚合) ---
  const statusMap = { 1: '待付款', 2: '已支付', 3: '已发货', 4: '已完成', 5: '已取消' }
  const statusCount = {}
  
  orders.forEach(order => {
    const s = order.status
    const name = statusMap[s] || '未知'
    statusCount[name] = (statusCount[name] || 0) + 1
  })

  const pieData = Object.keys(statusCount).map(key => ({
    name: key,
    value: statusCount[key]
  }))

  // 渲染饼图
  if (pieChartRef.value) {
    pieChartInstance = echarts.init(pieChartRef.value)
    pieChartInstance.setOption({
      tooltip: { trigger: 'item' },
      // [修改点 2] 饼图图例移动到右上角 (竖向排列)
      legend: { 
        orient: 'vertical', // 竖向排列，避免挤占高度
        right: 0,           // 靠右对齐
        top: 20             // 稍微留点顶部边距
      },
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: '70%',
          // [修改点 3] 将饼图圆心向左移，给右侧图例腾出空间
          // 原来是 ['60%', '50%']，现在改为 ['40%', '50%']
          center: ['40%', '50%'], 
          data: pieData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }
}

// ... 后面的代码保持不变 ...

// 辅助函数
const formatAmount = (val) => {
  return Number(val || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 响应式图表大小
const handleResize = () => {
  trendChartInstance && trendChartInstance.resize()
  pieChartInstance && pieChartInstance.resize()
}

onMounted(() => {
  fetchDataAndRender()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChartInstance && trendChartInstance.dispose()
  pieChartInstance && pieChartInstance.dispose()
})
</script>

<style scoped>
.order-analysis {
  /* padding: 20px; */
}
h2 {
  margin-bottom: 20px;
  color: #303133;
}
.analysis-cards .el-card {
  border: none;
  background: #fff;
}
.analysis-card {
  display: flex;
  align-items: center;
  gap: 15px;
}
.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
}
/* 配色方案 */
.blue-bg { background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%); }
.green-bg { background: linear-gradient(135deg, #52c41a 0%, #95de64 100%); }
.orange-bg { background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%); }
.purple-bg { background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%); }

.card-content {
  flex: 1;
}
.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}
.card-label {
  font-size: 13px;
  color: #909399;
}
.chart-card {
  margin-bottom: 20px;
}
</style>