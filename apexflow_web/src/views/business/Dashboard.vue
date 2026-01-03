<template>
  <div class="dashboard-page">
    
    <div v-if="isGuestMode" class="guest-overlay">
      <div class="guest-alert">
        <el-icon class="guest-icon"><Lock /></el-icon>
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
                <el-icon><ShoppingCart /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">1,256</div>
                <div class="stat-label">今日订单</div>
              </div>
              <div class="stat-trend" style="color: #52c41a;">
                +12.5%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="never">
            <div class="stat-content">
              <div class="stat-icon" style="background: #f0f9eb; color: #67c23a;">
                <el-icon><Van /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">89%</div>
                <div class="stat-label">发货率</div>
              </div>
              <div class="stat-trend" style="color: #67c23a;">
                +3.2%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="never">
            <div class="stat-content">
              <div class="stat-icon" style="background: #fff0f6; color: #eb2f96;">
                <el-icon><Refresh /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">12</div>
                <div class="stat-label">待处理退款</div>
              </div>
              <div class="stat-trend" style="color: #f5222d;">
                -5.0%
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="never">
            <div class="stat-content">
              <div class="stat-icon" style="background: #fff7e6; color: #fa8c16;">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥128k</div>
                <div class="stat-label">本月营收</div>
              </div>
              <div class="stat-trend" style="color: #52c41a;">
                +24.0%
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="charts-row">
        <el-col :span="16">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>销售趋势 (近7天)</span>
                <el-radio-group v-model="timeRange" size="small">
                  <el-radio-button label="7d">7天</el-radio-button>
                  <el-radio-button label="30d">30天</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div class="chart-placeholder">
              <div class="placeholder-content">
                <el-icon :size="48" color="#e1e4e8"><TrendCharts /></el-icon>
                <p>销售趋势图表区域</p>
                <span class="placeholder-hint">集成 ECharts 后此处显示折线图</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="chart-card" shadow="never">
            <template #header>
              <div class="card-header">
                <span>订单分布</span>
              </div>
            </template>
            <div class="chart-placeholder distribution-chart">
              <div class="pie-placeholder"></div>
              <ul class="legend-list">
                <li v-for="(item, index) in orderDistribution" :key="index">
                  <span class="dot" :style="{ background: item.color }"></span>
                  <span class="name">{{ item.name }}</span>
                  <span class="value">{{ item.value }}%</span>
                </li>
              </ul>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="recent-orders" shadow="never">
        <template #header>
          <div class="card-header">
            <span>最近订单</span>
            <el-button link type="primary">查看全部</el-button>
          </div>
        </template>
        <el-table :data="recentOrders" style="width: 100%">
          <el-table-column prop="id" label="订单号" width="120" />
          <el-table-column prop="product" label="商品" />
          <el-table-column prop="customer" label="客户" width="120" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.statusType" size="small">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="100" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import userDataManager from '@/utils/userData'
import { ElMessage } from 'element-plus'
import {
  ShoppingCart,
  Van,
  Refresh,
  Money,
  TrendCharts,
  Lock // [新增] 引入锁图标
} from '@element-plus/icons-vue'

// [新增] 游客模式状态管理
const isGuestMode = ref(false)

onMounted(() => {
  // 组件挂载时检查用户状态
  isGuestMode.value = userDataManager.isGuest()
})

const contactAdmin = () => {
  ElMessage.info('请联系系统管理员: admin@apexflow.com')
}

// 原有数据逻辑保持不变
const timeRange = ref('7d')

const orderDistribution = ref([
  { name: '待付款', value: 15, color: '#fa8c16' },
  { name: '待发货', value: 25, color: '#1890ff' },
  { name: '已发货', value: 40, color: '#52c41a' },
  { name: '已完成', value: 20, color: '#13c2c2' }
])

const recentOrders = ref([
  { id: 'ORD-001', product: '无线降噪耳机 Pro', customer: '张三', status: '已完成', statusType: 'success', amount: '¥1,299' },
  { id: 'ORD-002', product: '智能手表 S7', customer: '李四', status: '待发货', statusType: 'primary', amount: '¥2,499' },
  { id: 'ORD-003', product: '机械键盘 K8', customer: '王五', status: '待付款', statusType: 'warning', amount: '¥499' },
  { id: 'ORD-004', product: '4K 显示器 27寸', customer: '赵六', status: '处理中', statusType: 'info', amount: '¥1,899' },
])
</script>

<style scoped>
.dashboard-page {
  /* [修改] 确保容器可以相对定位遮罩层 */
  position: relative;
  min-height: 80vh;
}

/* [新增] 模糊与遮罩样式 */
.content-blur {
  filter: blur(8px);
  pointer-events: none;
  user-select: none;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.guest-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
}

.guest-alert {
  background: #ffffff;
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  padding: 32px 48px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(149, 157, 165, 0.2);
  max-width: 400px;
}

.guest-icon {
  font-size: 48px;
  color: #586069;
  margin-bottom: 16px;
  background: #f6f8fa;
  padding: 16px;
  border-radius: 50%;
}

.guest-alert h3 {
  margin: 0 0 8px 0;
  color: #24292e;
  font-size: 18px;
  font-weight: 600;
}

.guest-alert p {
  color: #586069;
  margin: 0 0 16px 0;
  font-size: 14px;
  line-height: 1.5;
}

/* 以下保持原有样式不变 */
.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  height: 100%;
  border: 1px solid #e1e4e8;
  transition: all 0.3s;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
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

.charts-row {
  margin-bottom: 16px;
}

.chart-card {
  border: 1px solid #e1e4e8;
  height: 320px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.chart-placeholder {
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-content {
  text-align: center;
  color: #586069;
}

.placeholder-content p {
  margin: 8px 0 0;
}

.placeholder-hint {
  font-size: 12px;
  color: #a0a0a0;
}

.distribution-chart {
  padding: 0 20px;
  justify-content: space-around;
}

.pie-placeholder {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: conic-gradient(
    #fa8c16 0% 15%,
    #1890ff 15% 40%,
    #52c41a 40% 80%,
    #13c2c2 80% 100%
  );
}

.legend-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.legend-list li {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 13px;
  color: #24292e;
}

.legend-list .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
}

.legend-list .name {
  width: 60px;
}

.legend-list .value {
  color: #586069;
}

.recent-orders {
  border: 1px solid #e1e4e8;
}
</style>