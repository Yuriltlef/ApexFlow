<template>
  <div class="dashboard-page">

    <div v-if="isGuestMode" class="guest-overlay">
      <div class="guest-alert">
        <el-icon class="guest-icon">
          <Lock />
        </el-icon>
        <h3>访客模式限制</h3>
        <p>当前用户为游客模式，数据仅供演示预览。</p>
        <p>若要正常使用所有功能，请联系管理员获取账户。</p>
        <el-button type="primary" @click="contactAdmin">联系管理员</el-button>
      </div>
    </div>

    <div :class="['dashboard-content', { 'content-blur': isGuestMode }]">

      <el-row :gutter="16" class="stats-row">
        <el-col :span="6">
          <el-card class="stat-card" shadow="never">
            <div class="stat-content">
              <div class="stat-icon" style="background: #e8f4ff; color: #0366d6;">
                <el-icon>
                  <ShoppingCart />
                </el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.todayCount }}</div>
                <div class="stat-label">今日订单</div>
              </div>
              <div class="stat-trend">
                <el-tag size="small" type="primary">实时</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="never">
            <div class="stat-content">
              <div class="stat-icon" style="background: #f6ffed; color: #52c41a;">
                <el-icon>
                  <Money />
                </el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ formatAmount(stats.totalSales) }}</div>
                <div class="stat-label">总销售额</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="never">
            <div class="stat-content">
              <div class="stat-icon" style="background: #fff7e6; color: #fa8c16;">
                <el-icon>
                  <List />
                </el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalOrders }}</div>
                <div class="stat-label">总订单数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="never">
            <div class="stat-content">
              <div class="stat-icon" style="background: #fff1f0; color: #f5222d;">
                <el-icon>
                  <Timer />
                </el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingCount }}</div>
                <div class="stat-label">待处理订单</div>
              </div>
              <div class="stat-trend" v-if="stats.pendingCount > 0">
                <el-tag size="small" type="danger">急</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="charts-row">
        <el-col :span="16">
          <el-card class="chart-card" shadow="never">
            <div class="card-header">
              <span>近7天订单趋势</span>
            </div>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card" shadow="never">
            <div class="card-header">
              <span>订单状态分布</span>
            </div>
            <div ref="pieChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="table-card" shadow="never">
        <div class="card-header">
          <span>最新订单</span>
        </div>
        <el-table :data="recentOrders" stripe style="width: 100%" v-loading="loading">
          <el-table-column label="订单号" min-width="180">
            <template #default="{ row }">
              {{ row.orderId }}
            </template>
          </el-table-column>
          <el-table-column label="客户ID" width="100" prop="userId" />
          <el-table-column label="金额" width="120">
            <template #default="{ row }">
              ¥{{ formatAmount(row.totalAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small" effect="plain">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付方式" width="100">
            <template #default="{ row }">
              {{ formatPaymentMethod(row.paymentMethod) }}
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ShoppingCart, Money, List, Timer, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
// 引入 API 和 UserData
import { getAllOrdersForAnalysis } from '@/api/order'
import userDataManager from '@/utils/userData'

// --- 状态定义 ---
const loading = ref(false)
const isGuestMode = ref(false) // [新增] 访客模式状态
const recentOrders = ref([])

// 统计数据
const stats = reactive({
  todayCount: 0,
  totalSales: 0,
  totalOrders: 0,
  pendingCount: 0
})

// 图表 Refs
const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChartInstance = null
let pieChartInstance = null

// --- [新增] 访客检查逻辑 ---
const checkGuestMode = () => {
  // 从 userDataManager 获取是否为游客
  isGuestMode.value = userDataManager.isGuest()
}

const contactAdmin = () => {
  ElMessage.info('请联系管理员 admin@apexflow.com')
}

// --- 数据获取与处理 ---
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAllOrdersForAnalysis()

    // 兼容不同的返回结构
    let allData = []
    if (res && res.data && Array.isArray(res.data.orders)) {
      allData = res.data.orders
    } else if (res && res.data && Array.isArray(res.data)) {
      allData = res.data
    } else if (res && Array.isArray(res)) {
      allData = res
    }
    console.log('获取仪表盘订单数据', allData)

    if (allData.length > 0) {
      calculateStats(allData)
      renderCharts(allData)

      // 提取最新5条订单
      const sortedData = [...allData].sort((a, b) => {
        const t1 = Array.isArray(a.createTime) ? formatTime(a.createTime) : a.createTime
        const t2 = Array.isArray(b.createTime) ? formatTime(b.createTime) : b.createTime
        return new Date(t2) - new Date(t1)
      })
      recentOrders.value = sortedData.slice(0, 5)
    }

  } catch (error) {
    console.error('仪表盘数据获取失败', error)
  } finally {
    loading.value = false
  }
}

// 计算顶部卡片数据
const calculateStats = (data) => {
  const now = new Date()
  const todayStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`

  let todayCnt = 0
  let sales = 0
  let pending = 0

  data.forEach(item => {
    sales += Number(item.totalAmount || 0)
    if (item.status === 1) pending++
    const itemTimeStr = formatTime(item.createTime)
    if (itemTimeStr.startsWith(todayStr)) {
      todayCnt++
    }
  })

  stats.totalOrders = data.length
  stats.totalSales = sales
  stats.pendingCount = pending
  stats.todayCount = todayCnt
}

// 渲染图表
const renderCharts = (data) => {
  if (!trendChartRef.value || !pieChartRef.value) return

  // 1. 趋势图数据 (近7天)
  const dateMap = {}
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
    dateMap[dateStr] = 0
  }

  data.forEach(item => {
    const dateStr = formatTime(item.createTime).substring(0, 10)
    if (dateMap.hasOwnProperty(dateStr)) {
      dateMap[dateStr]++
    }
  })

  // 2. 饼图数据 (状态)
  const statusMap = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
  data.forEach(item => {
    if (statusMap[item.status] !== undefined) {
      statusMap[item.status]++
    }
  })

  // ECharts 初始化
  trendChartInstance = echarts.init(trendChartRef.value)
  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: '10%', left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: Object.keys(dateMap) },
    yAxis: { type: 'value' },
    series: [{
      data: Object.values(dateMap),
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.2 },
      itemStyle: { color: '#409EFF' },
      lineStyle: { color: '#409EFF' }
    }]
  })

  pieChartInstance = echarts.init(pieChartRef.value)
  pieChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: '16', fontWeight: 'bold' } },
      data: [
        { value: statusMap[1], name: '待付款', itemStyle: { color: '#e6a23c' } },
        { value: statusMap[2], name: '已支付', itemStyle: { color: '#409eff' } },
        { value: statusMap[3], name: '已发货', itemStyle: { color: '#67c23a' } },
        { value: statusMap[4], name: '已完成', itemStyle: { color: '#67c23a' } },
        { value: statusMap[5], name: '已取消', itemStyle: { color: '#909399' } }
      ]
    }]
  })
}

// --- 格式化工具 ---
const formatAmount = (val) => Number(val || 0).toFixed(2)

const formatTime = (timeArr) => {
  if (!timeArr) return '-'
  if (!Array.isArray(timeArr)) return String(timeArr)
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

const handleResize = () => {
  trendChartInstance && trendChartInstance.resize()
  pieChartInstance && pieChartInstance.resize()
}

// 生命周期
onMounted(() => {
  checkGuestMode() // [新增] 检查身份
  fetchData() // 依然加载数据用于背景显示 (或者可以加 if(!isGuest) 来阻止请求)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChartInstance && trendChartInstance.dispose()
  pieChartInstance && pieChartInstance.dispose()
})
</script>

<style scoped>
.dashboard-page {
  position: relative;
  /* padding: 20px; */
}

/* [新增] 访客模式样式 */
.content-blur {
  filter: blur(5px);
  pointer-events: none;
  user-select: none;
  overflow: hidden;
  /* 防止滚动 */
}

.guest-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.4);
  /* 半透明背景 */
  height: 100vh;
  /* 确保覆盖全屏高度 */
}

.guest-alert {
  background: white;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  text-align: center;
  max-width: 400px;
  border: 1px solid #e1e4e8;
}

.guest-icon {
  font-size: 48px;
  color: #f56c6c;
  margin-bottom: 20px;
}

/* 统计卡片样式 (保持原样) */
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
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #24292e;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #586069;
  margin-top: 4px;
}

.stat-trend {
  font-size: 14px;
  font-weight: 500;
}

/* 图表样式 */
.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 380px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 300px;
  width: 100%;
}
</style>