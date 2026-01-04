<template>
  <div class="warehouse-page">
    <h2>📦 库存管理</h2>

    <el-row :gutter="16" class="warehouse-stats">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="color: #1890ff; background: #e6f7ff;">
              <el-icon size="24"><Box /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ totalProducts }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="color: #52c41a; background: #f6ffed;">
              <el-icon size="24"><Goods /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ totalStock }}</div>
              <div class="stat-label">当前库存总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="color: #faad14; background: #fffbe6;">
              <el-icon size="24"><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ lowStockCount }}</div>
              <div class="stat-label">低库存预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <div class="table-actions">
          <el-input 
            v-model="productQuery.keyword" 
            placeholder="搜索商品名称/类别" 
            style="width: 250px; margin-right: 10px;" 
            clearable
            @clear="fetchProductList"
            @keyup.enter="fetchProductList"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          
          <el-select v-model="productQuery.status" placeholder="状态" clearable style="width: 120px; margin-right: 10px;" @change="fetchProductList">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>

          <el-button type="primary" @click="fetchProductList">搜索</el-button>
          <el-button type="success" :icon="Plus" @click="openProductDialog()">新增商品</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="inventoryList" stripe style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image 
              style="width: 50px; height: 50px; border-radius: 4px;"
              :src="row.image" 
              fit="cover"
              :preview-src-list="[row.image]" 
              preview-teleported
            >
              <template #error>
                <div class="image-slot"><el-icon><Picture /></el-icon></div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">¥{{ formatAmount(row.price) }}</template>
        </el-table-column>
        
        <el-table-column prop="stock" label="当前库存" width="120">
          <template #default="{ row }">
            <span :style="row.stock <= 10 ? 'color: #f56c6c; font-weight: bold;' : ''">
              {{ row.stock }}
            </span>
            <el-tag v-if="row.stock <= 10" size="small" type="danger" style="margin-left: 5px;">紧缺</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openProductDialog(row)">编辑</el-button>
            <el-button type="warning" link size="small" @click="openStockDialog(row)">库存调整</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">下架</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="productQuery.page"
          v-model:page-size="productQuery.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="productTotal"
          @size-change="fetchProductList"
          @current-change="fetchProductList"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="productDialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="500px"
    >
      <el-form ref="productFormRef" :model="productForm" :rules="productRules" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="productForm.category" placeholder="如：手机、数码" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="productForm.price" :precision="2" :step="0.1" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="初始库存" prop="stock" v-if="!isEdit">
          <el-input-number v-model="productForm.stock" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <el-input v-model="productForm.image" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="productForm.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="productDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitProduct">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="stockDialogVisible"
      title="库存盘点调整"
      width="400px"
    >
      <el-form ref="stockFormRef" :model="stockForm" label-width="100px">
        <el-form-item label="商品名称">
          <span>{{ currentStockRow?.name }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ currentStockRow?.stock }}</span>
        </el-form-item>
        <el-form-item label="调整后库存" prop="newStock">
          <el-input-number v-model="stockForm.newStock" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="stockForm.reason" type="textarea" placeholder="如：盘点差异、损耗" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stockDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="submitStockAdjustment">确定调整</el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Box, Goods, Warning, Search, Plus, Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getProductList, 
  createProduct, 
  updateProduct, 
  deleteProduct, 
  adjustStock, 
  getLowStockList
} from '@/api/warehouse'

// --- 状态数据 ---
const loading = ref(false)

// 统计数据
const totalProducts = ref(0)
const totalStock = ref(0)
const lowStockCount = ref(0)

// 库存列表相关
const inventoryList = ref([])
const productTotal = ref(0)
const productQuery = reactive({
  page: 1,
  pageSize: 10,
  keyword: '',
  status: null
})

// --- 方法：数据获取 ---

// 获取商品列表
const fetchProductList = async () => {
  loading.value = true
  try {
    const res = await getProductList(productQuery)
    if (res && res.success) {
      console.log('获取商品列表数据', res.data)
      inventoryList.value = res.data.products || []
      productTotal.value = res.data.totalCount || 0
      
      // 更新统计数据 (列表接口通常返回总数)
      totalProducts.value = res.data.totalCount || 0
      // 简单累加当前页库存作为示例，实际应由后端返回总库存
      totalStock.value = inventoryList.value.reduce((sum, item) => sum + item.stock, 0)
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 获取低库存统计
const fetchLowStockStats = async () => {
  try {
    const res = await getLowStockList(10) // 阈值10
    if (res && res.success) {
      lowStockCount.value = res.data.count || 0
    }
  } catch (e) {
    console.error('获取低库存统计失败', e)
  }
}

// --- 商品增删改 ---
const productDialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const productFormRef = ref(null)
const productForm = reactive({
  id: null,
  name: '',
  category: '',
  price: 0,
  stock: 0,
  image: '',
  status: 1
})
const productRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const openProductDialog = (row = null) => {
  if (row) {
    isEdit.value = true
    Object.assign(productForm, row)
  } else {
    isEdit.value = false
    Object.assign(productForm, { id: null, name: '', category: '', price: 0, stock: 0, image: '', status: 1 })
  }
  productDialogVisible.value = true
}

const submitProduct = async () => {
  await productFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await updateProduct(productForm)
          ElMessage.success('更新成功')
        } else {
          await createProduct(productForm)
          ElMessage.success('创建成功')
        }
        productDialogVisible.value = false
        fetchProductList()
      } catch (error) {
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定下架商品 "${row.name}" 吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProduct(row.id)
      ElMessage.success('已下架')
      fetchProductList()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

// --- 库存调整 ---
const stockDialogVisible = ref(false)
const currentStockRow = ref(null)
const stockForm = reactive({
  newStock: 0,
  reason: ''
})

const openStockDialog = (row) => {
  currentStockRow.value = row
  stockForm.newStock = row.stock
  stockForm.reason = ''
  stockDialogVisible.value = true
}

const submitStockAdjustment = async () => {
  if (!stockForm.reason) {
    ElMessage.warning('请输入调整原因')
    return
  }
  submitLoading.value = true
  try {
    await adjustStock(currentStockRow.value.id, {
      newStock: stockForm.newStock,
      reason: stockForm.reason
    })
    ElMessage.success('库存调整成功')
    stockDialogVisible.value = false
    fetchProductList()
    fetchLowStockStats()
  } catch (error) {
    ElMessage.error('调整失败')
  } finally {
    submitLoading.value = false
  }
}

// --- 格式化工具 ---
const formatAmount = (val) => Number(val || 0).toFixed(2)

onMounted(() => {
  fetchProductList()
  fetchLowStockStats()
})
</script>

<style scoped>
.warehouse-page {
  /* padding: 20px; */
}

h2 {
  color: #0366d6;
  margin-bottom: 20px;
}

.warehouse-stats {
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

.table-actions {
  display: flex;
  align-items: center;
  /* margin-bottom: 15px; */
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>