<template>
  <div class="evaluation-page">
    <h2>⭐ 评价管理</h2>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户ID/商品ID/订单号/评价内容"
              style="width: 300px; margin-right: 10px;"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>

            <el-select
              v-model="ratingFilter"
              placeholder="评分筛选"
              style="width: 120px; margin-right: 10px;"
              clearable
              @change="handleFilter"
            >
              <el-option label="全部评分" value="" />
              <el-option label="5星" :value="5" />
              <el-option label="4星" :value="4" />
              <el-option label="3星" :value="3" />
              <el-option label="2星" :value="2" />
              <el-option label="1星" :value="1" />
            </el-select>

            <el-button type="primary" @click="fetchData">
              <el-icon style="margin-right: 5px"><RefreshRight /></el-icon>
              刷新
            </el-button>

            <div class="data-stat" v-if="allReviewList.length > 0">
              <span class="stat-item">总记录: <strong>{{ allReviewList.length }}</strong></span>
              <span class="stat-item" v-if="searchKeyword || ratingFilter">过滤后: <strong class="highlight-text">{{ filteredReviewList.length }}</strong></span>
            </div>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <el-tab-pane label="全部评价" name="all" />
        <el-tab-pane label="含图片" name="withImages">
          <template #label>
            含图片 <el-badge :value="getCountWithImages()" type="success" v-if="getCountWithImages() > 0" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="匿名评价" name="anonymous">
          <template #label>
            匿名评价 <el-badge :value="getCountAnonymous()" type="info" v-if="getCountAnonymous() > 0" />
          </template>
        </el-tab-pane>
      </el-tabs>

      <div v-loading="loading" class="evaluation-list">
        <el-empty v-if="pagedReviewList.length === 0" description="暂无评价数据" />

        <div v-for="item in pagedReviewList" :key="item.id" class="evaluation-item">
          <div class="evaluation-header">
            <div class="evaluation-info">
              <el-avatar :size="36" icon="UserFilled" class="user-avatar" />
              <div class="user-meta">
                <div class="customer-name">
                  用户ID: <span v-html="highlight(item.userId)"></span>
                  <el-tag v-if="item.anonymous" size="small" type="info" effect="plain" style="margin-left: 8px;">匿名</el-tag>
                </div>
                <div class="meta-sub">
                  <span class="product-name">商品ID: <span v-html="highlight(item.productId)"></span></span>
                  <span class="separator">|</span>
                  <span class="order-no">订单号: <span v-html="highlight(item.orderId)"></span></span>
                </div>
              </div>
            </div>
            <div class="evaluation-right">
              <span class="evaluation-time">{{ formatTime(item.createdAt) }}</span>
              <el-button
                type="danger"
                icon="Delete"
                circle
                size="small"
                plain
                @click="handleDelete(item)"
                style="margin-left: 15px;"
              />
            </div>
          </div>

          <div class="rating-bar">
            <el-rate
              v-model="item.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value} 分"
            />
          </div>

          <div class="evaluation-content" v-html="highlightContent(item.content)"></div>

          <div v-if="item.imageList && item.imageList.length > 0" class="evaluation-images">
            <el-image
              v-for="(img, index) in item.imageList"
              :key="index"
              :src="img"
              :preview-src-list="item.imageList"
              fit="cover"
              class="eval-image"
            />
          </div>
        </div>
      </div>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredReviewList.length"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, RefreshRight, UserFilled, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewList, deleteReview } from '@/api/review'

// --- 状态定义 ---
const loading = ref(false)
const activeTab = ref('all') // all, withImages, anonymous
const searchKeyword = ref('')
const ratingFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const allReviewList = ref([])

// --- 数据获取 (全量获取) ---
const fetchData = async () => {
  loading.value = true
  try {
    // 先探测总数
    const initRes = await getReviewList({ page: 1, pageSize: 1 })
    if (initRes && initRes.success && initRes.data) {
      const total = initRes.data.total || 0
      if (total > 0) {
        // 全量拉取
        const fullRes = await getReviewList({ page: 1, pageSize: total })
        if (fullRes && fullRes.success && fullRes.data) {
          const list = fullRes.data.reviews || []
          // 处理图片字符串转数组
          allReviewList.value = list.map(item => ({
            ...item,
            imageList: item.images ? item.images.split(',').filter(img => img.trim()) : []
          }))
        }
      } else {
        allReviewList.value = []
      }
    } else {
      allReviewList.value = []
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loading.value = false
  }
}

// --- 计算属性：过滤与分页 ---
const filteredReviewList = computed(() => {
  let data = allReviewList.value

  // 1. Tab 过滤
  if (activeTab.value === 'withImages') {
    data = data.filter(item => item.imageList && item.imageList.length > 0)
  } else if (activeTab.value === 'anonymous') {
    data = data.filter(item => item.anonymous)
  }

  // 2. 评分过滤
  if (ratingFilter.value !== '') {
    data = data.filter(item => item.rating === ratingFilter.value)
  }

  // 3. 关键词过滤
  const kw = searchKeyword.value.trim().toLowerCase()
  if (kw) {
    data = data.filter(item => {
      const userId = String(item.userId || '').toLowerCase()
      const productId = String(item.productId || '').toLowerCase()
      const orderId = String(item.orderId || '').toLowerCase()
      const content = String(item.content || '').toLowerCase()
      return userId.includes(kw) ||
             productId.includes(kw) ||
             orderId.includes(kw) ||
             content.includes(kw)
    })
  }

  return data
})

const pagedReviewList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredReviewList.value.slice(start, end)
})

// --- 辅助功能 ---
const handleTabChange = () => {
  currentPage.value = 1
}

const handleFilter = () => {
  currentPage.value = 1
}

const handleSearch = () => {
  currentPage.value = 1
}

const handlePageChange = () => {
  // 分页变化，不需要额外处理，计算属性会自动更新
}

const handlePageSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

const getCountWithImages = () => {
  return allReviewList.value.filter(item => item.imageList && item.imageList.length > 0).length
}

const getCountAnonymous = () => {
  return allReviewList.value.filter(item => item.anonymous).length
}

// 高亮显示搜索关键词
const highlight = (text) => {
  if (!text) return ''
  const str = String(text)
  const kw = searchKeyword.value.trim()
  if (!kw) return str
  const reg = new RegExp(`(${kw})`, 'gi')
  return str.replace(reg, '<span style="color: red; font-weight: bold;">$1</span>')
}

// 高亮评价内容（处理空内容情况）
const highlightContent = (content) => {
  if (!content || content.trim() === '') {
    return '<span style="color: #909399; font-style: italic;">此用户没有填写评价内容。</span>'
  }
  const kw = searchKeyword.value.trim()
  if (!kw) return content

  const reg = new RegExp(`(${kw})`, 'gi')
  return content.replace(reg, '<span style="color: red; font-weight: bold;">$1</span>')
}

// --- 业务操作 ---
// 删除评价
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除这条评价吗？此操作不可恢复。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await deleteReview(row.id)
      if (res && res.success) {
        ElMessage.success('删除成功')
        // 从本地数据中移除
        const index = allReviewList.value.findIndex(item => item.id === row.id)
        if (index !== -1) {
          allReviewList.value.splice(index, 1)
        }
        // 如果当前页没有数据了且不是第一页，则返回上一页
        if (pagedReviewList.value.length === 0 && currentPage.value > 1) {
          currentPage.value--
        }
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除请求失败')
    }
  }).catch(() => {})
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  // 处理 Java LocalDateTime 数组 [2023, 12, 1, 10, 30, 0]
  if (Array.isArray(time)) {
    const [y, m, d, h, min] = time
    const pad = n => (n < 10 ? '0' + n : n)
    return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}`
  }
  // 处理字符串 "2023-12-01T10:30:00"
  return time.replace('T', ' ')
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.evaluation-page {
  padding: 20px;
}

h2 {
  color: #0366d6;
  margin-bottom: 20px;
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

.evaluation-list {
  padding: 10px 0;
}

.evaluation-item {
  padding: 20px;
  border-bottom: 1px solid #e1e4e8;
  transition: background-color 0.2s;
}

.evaluation-item:hover {
  background-color: #fafbfc;
}

.evaluation-item:last-child {
  border-bottom: none;
}

.evaluation-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.evaluation-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.customer-name {
  font-weight: 600;
  color: #24292e;
  font-size: 14px;
}

.meta-sub {
  font-size: 12px;
  color: #6a737d;
  margin-top: 4px;
}

.separator {
  margin: 0 6px;
  color: #d1d5da;
}

.evaluation-time {
  color: #909399;
  font-size: 12px;
}

.rating-bar {
  margin-bottom: 12px;
}

.evaluation-content {
  margin-bottom: 15px;
  line-height: 1.6;
  color: #333;
  font-size: 14px;
  background: #f6f8fa;
  padding: 10px;
  border-radius: 6px;
  white-space: pre-wrap;
}

.evaluation-images {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.eval-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  border: 1px solid #e1e4e8;
  cursor: pointer;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
