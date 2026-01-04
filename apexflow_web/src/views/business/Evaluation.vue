<template>
  <div class="evaluation-page">
    <h2>⭐ 评价管理</h2>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div class="filter-controls">
            <el-select 
              v-model="queryParams.rating" 
              placeholder="评分筛选" 
              size="default" 
              style="width: 120px;"
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

            <el-input 
              v-model="queryParams.productId" 
              placeholder="输入商品ID搜索" 
              size="default" 
              style="width: 200px; margin-left: 10px;"
              clearable
              @clear="handleFilter"
              @keyup.enter="handleFilter"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>

            <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">搜索</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="evaluation-list">
        <el-empty v-if="reviewList.length === 0" description="暂无评价数据" />

        <div v-for="item in reviewList" :key="item.id" class="evaluation-item">
          <div class="evaluation-header">
            <div class="evaluation-info">
              <el-avatar :size="36" icon="UserFilled" class="user-avatar" />
              <div class="user-meta">
                <div class="customer-name">
                  用户ID: {{ item.userId }} 
                  <el-tag v-if="item.anonymous" size="small" type="info" effect="plain">匿名</el-tag>
                </div>
                <div class="meta-sub">
                  <span class="product-name">商品ID: {{ item.productId }}</span>
                  <span class="separator">|</span>
                  <span class="order-no">订单号: {{ item.orderId }}</span>
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

          <div class="evaluation-content">
            {{ item.content || '此用户没有填写评价内容。' }}
          </div>

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
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, UserFilled, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewList, deleteReview } from '@/api/review'

// --- 状态数据 ---
const loading = ref(false)
const reviewList = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  productId: '', // 用于搜索
  userId: '',
  rating: ''     // 前端筛选参数，如果后端API不支持rating筛选，可能需要在前端过滤，或者只需传给后端备用
})

// --- 方法 ---

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 构造请求参数
    // 注意：如果您的后端不支持 'rating' 参数，它会直接忽略，这没关系
    const params = {
      page: queryParams.page,
      pageSize: queryParams.pageSize,
      productId: queryParams.productId || null,
      // userId: queryParams.userId || null 
    }

    const res = await getReviewList(params)
    if (res && res.success) {
      const data = res.data
      console.log('获取评价列表数据', data)
      
      // 处理列表数据
      let list = data.reviews || []
      
      // 处理图片字符串转数组 (DB存的是 "a.jpg,b.jpg")
      list = list.map(item => {
        return {
          ...item,
          imageList: item.images ? item.images.split(',') : []
        }
      })

      // 如果后端没有支持 Rating 筛选，我们在前端简单过滤一下（可选优化）
      if (queryParams.rating) {
        // 注意：这会导致分页不准，最佳实践是后端支持筛选。
        // 这里仅做演示，如果不希望前端过滤，请注释下面三行
        list = list.filter(item => item.rating === queryParams.rating)
      }

      reviewList.value = list
      total.value = data.total || 0
    } else {
      reviewList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选操作
const handleFilter = () => {
  queryParams.page = 1
  fetchData()
}

// 重置
const resetQuery = () => {
  queryParams.productId = ''
  queryParams.rating = ''
  handleFilter()
}

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
        // 如果当前页只有一条且不是第一页，页码减一
        if (reviewList.value.length === 1 && queryParams.page > 1) {
          queryParams.page--
        }
        fetchData()
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
  justify-content: flex-end;
  align-items: center;
}

.filter-controls {
  display: flex;
  align-items: center;
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