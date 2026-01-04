<template>
  <div class="system-page">
    <h2>⚙️ 系统设置</h2>

    <el-card class="settings-card">
      <el-tabs v-model="activeTab" class="settings-tabs" @tab-change="handleTabChange">

        <el-tab-pane label="基本设置" name="basic">
          <div class="settings-form">
            <el-form :model="basicSettings" label-width="120px">
              <el-form-item label="系统名称">
                <el-input v-model="basicSettings.systemName" placeholder="请输入系统名称" />
              </el-form-item>
              <el-form-item label="系统Logo">
                <el-upload class="logo-upload" action="#" :show-file-list="false" :auto-upload="false"
                  :on-change="handleLogoChange">
                  <img v-if="basicSettings.logoUrl" :src="basicSettings.logoUrl" class="logo-preview" />
                  <el-icon v-else class="logo-upload-icon">
                    <Plus />
                  </el-icon>
                </el-upload>
                <div class="upload-hint">点击上传Logo，建议尺寸 200x60px</div>
              </el-form-item>
              <el-form-item label="版权信息">
                <el-input v-model="basicSettings.copyright" type="textarea" :rows="3" placeholder="请输入版权信息" />
              </el-form-item>
              <el-form-item label="系统版本">
                <el-tag>v1.0.0</el-tag>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveBasicSettings">保存基本设置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="用户管理" name="users">
          <div class="user-management-panel" style="padding: 20px;">
            <div class="header-actions">
              <el-input v-model="userQuery.keyword" placeholder="搜索用户名" style="width: 250px; margin-right: 10px;"
                clearable @clear="fetchUserList" @keyup.enter="fetchUserList">
                <template #prefix>
                  <el-icon>
                    <Search />
                  </el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="fetchUserList">搜索</el-button>
              <el-button type="success" :icon="Plus" @click="openUserDialog()">新建用户</el-button>
            </div>

            <el-table v-loading="userLoading" :data="userList" stripe style="width: 100%; margin-top: 20px;"
              empty-text="暂无用户数据">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" width="150" />
              <el-table-column prop="email" label="邮箱" min-width="180" />

              <el-table-column label="角色类型" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.isAdmin ? 'danger' : 'success'">
                    {{ row.isAdmin ? '超级管理员' : '普通用户' }}
                  </el-tag>
                </template>
              </el-table-column>

              <el-table-column label="权限概览" min-width="200">
                <template #default="{ row }">
                  <span v-if="row.isAdmin" style="color: #f56c6c; font-size: 12px;">拥有所有系统权限</span>
                  <div v-else class="permission-tags">
                    <el-tag v-if="row.canManageOrder" size="small" type="info">订单</el-tag>
                    <el-tag v-if="row.canManageLogistics" size="small" type="info">物流</el-tag>
                    <el-tag v-if="row.canManageAfterSales" size="small" type="info">售后</el-tag>
                    <el-tag v-if="row.canManageReview" size="small" type="info">评价</el-tag>
                    <el-tag v-if="row.canManageInventory" size="small" type="info">库存</el-tag>
                    <el-tag v-if="row.canManageIncome" size="small" type="info">财务</el-tag>
                    <span v-if="!hasAnyPermission(row)" style="color: #909399; font-size: 12px;">无权限</span>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-switch v-model="row.status" :active-value="1" :inactive-value="0"
                    @change="handleStatusChange(row)" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click="openUserDialog(row)">编辑</el-button>
                  <el-button link type="danger" size="small" @click="handleDeleteUser(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end;">
              <el-pagination v-model:current-page="userQuery.page" v-model:page-size="userQuery.pageSize"
                :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" :total="userTotal"
                @size-change="fetchUserList" @current-change="fetchUserList" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="通知设置" name="notification">
          <div class="settings-form">
            <el-form :model="notificationSettings" label-width="120px">
              <el-form-item label="邮件通知">
                <el-switch v-model="notificationSettings.emailEnabled" />
                <span class="setting-desc">开启后将通过邮件接收重要通知</span>
              </el-form-item>
              <el-form-item label="系统消息">
                <el-switch v-model="notificationSettings.systemEnabled" />
                <span class="setting-desc">开启后将在系统内接收通知消息</span>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveNotificationSettings">保存通知设置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="安全设置" name="security">
          <div class="settings-form">
            <el-form :model="securitySettings" label-width="120px">
              <el-form-item label="密码策略">
                <el-checkbox-group v-model="securitySettings.passwordPolicy">
                  <el-checkbox label="uppercase">强制大写字母</el-checkbox>
                  <el-checkbox label="number">强制数字</el-checkbox>
                  <el-checkbox label="special">强制特殊字符</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="登录历史">
                <el-button type="info" plain @click="viewLoginHistory">查看最近登录记录</el-button>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveSecuritySettings">保存安全设置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="备份与恢复" name="backup">
          <div class="settings-form">
            <el-form label-width="120px">
              <el-form-item label="数据备份">
                <div class="backup-section">
                  <el-button type="primary" :loading="backupLoading" @click="createBackup">立即备份</el-button>
                  <p class="backup-info" v-if="backupSettings.lastBackup">
                    上次备份时间: {{ backupSettings.lastBackup }}
                  </p>
                </div>
              </el-form-item>
              <el-divider />
              <el-form-item label="数据恢复">
                <el-button type="warning" plain @click="restoreBackup">恢复备份数据</el-button>
                <div class="upload-hint">请谨慎操作，恢复后现有数据将被覆盖</div>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

      </el-tabs>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'create' ? '新建用户' : '编辑用户'" width="600px" append-to-body>
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogType === 'edit'" placeholder="请输入登录账号" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password
            :placeholder="dialogType === 'edit' ? '留空则不修改密码' : '请输入密码'" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">权限设置</el-divider>

        <el-form-item label="角色类型">
          <el-radio-group v-model="userForm.isAdmin">
            <el-radio :label="true">超级管理员 (拥有所有权限)</el-radio>
            <el-radio :label="false">普通用户 (自定义权限)</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="详细权限" v-if="!userForm.isAdmin">
          <el-checkbox-group v-model="userForm.permissions">
            <el-checkbox v-for="option in PERMISSION_OPTIONS" :key="option.key" :label="option.key">
              {{ option.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <div v-else class="admin-tip">
          <el-alert title="超级管理员拥有系统内所有模块的操作权限" type="warning" :closable="false" show-icon />
        </div>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, updateUserPermissions, resetUserPassword, deleteUser } from '@/api/user'

const activeTab = ref('basic')

// --- 权限定义 ---
const PERMISSION_OPTIONS = [
  { label: '订单管理', key: 'canManageOrder' },
  { label: '物流管理', key: 'canManageLogistics' },
  { label: '售后管理', key: 'canManageAfterSales' },
  { label: '评价管理', key: 'canManageReview' },
  { label: '库存管理', key: 'canManageInventory' },
  { label: '财务管理', key: 'canManageIncome' }
]

// ================== 基础设置 ==================
const basicSettings = reactive({
  systemName: 'ApexFlow 电商管理系统',
  logoUrl: '',
  copyright: '© 2023 ApexFlow Team. All Rights Reserved.'
})
const handleLogoChange = (file) => { basicSettings.logoUrl = URL.createObjectURL(file.raw) }
const saveBasicSettings = () => { ElMessage.success('基本设置已保存') }

const notificationSettings = reactive({ emailEnabled: true, systemEnabled: true })
const saveNotificationSettings = () => { ElMessage.success('通知设置已保存') }

const securitySettings = reactive({ passwordPolicy: ['number', 'special'] })
const saveSecuritySettings = () => { ElMessage.success('安全设置已保存') }
const viewLoginHistory = () => { ElMessage.info('查看登录历史功能开发中...') }

const backupSettings = reactive({ lastBackup: '' })
const backupLoading = ref(false)
const createBackup = () => {
  backupLoading.value = true
  setTimeout(() => {
    backupLoading.value = false
    backupSettings.lastBackup = new Date().toLocaleString()
    ElMessage.success('备份创建成功')
  }, 1500)
}
const restoreBackup = () => { ElMessage.warning('恢复备份功能开发中...') }

// ================== 用户管理 ==================

const handleTabChange = (tabName) => {
  if (tabName === 'users') fetchUserList()
}

const userLoading = ref(false)
const userList = ref([])
const userTotal = ref(0)
const userQuery = reactive({ page: 1, pageSize: 10, keyword: '' })

// 辅助函数：判断是否有任意权限 (用于列表显示)
const hasAnyPermission = (row) => {
  return row.canManageOrder || row.canManageLogistics || row.canManageAfterSales ||
    row.canManageReview || row.canManageInventory || row.canManageIncome
}

const fetchUserList = async () => {
  userLoading.value = true
  try {
    const res = await getUserList(userQuery)
    if (res && res.data && res.data.success) {
      const data = res.data.data
      if (data) {
        userList.value = data.users || []
        userTotal.value = data.totalCount || 0
      }
    }
  } catch (error) {
    console.error('获取列表失败', error)
  } finally {
    userLoading.value = false
  }
}

// 弹窗相关
const dialogVisible = ref(false)
const dialogType = ref('create')
const submitLoading = ref(false)
const userFormRef = ref(null)

const userForm = reactive({
  id: null,
  username: '',
  password: '',
  email: '',
  status: 1,
  isAdmin: false,
  permissions: [] // 存储选中的权限Key, 如 ['canManageOrder', 'canManageLogistics']
})

const userRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入邮箱', trigger: 'blur' }]
}

// 打开弹窗
const openUserDialog = (row = null) => {
  if (userFormRef.value) userFormRef.value.resetFields()

  if (row) {
    dialogType.value = 'edit'
    userForm.id = row.id
    userForm.username = row.username
    userForm.email = row.email
    userForm.status = row.status
    userForm.password = ''

    // 映射权限
    userForm.isAdmin = row.isAdmin
    userForm.permissions = []
    if (!row.isAdmin) {
      // 只有不是管理员时，才回显具体权限。管理员默认全有，不需要勾选
      PERMISSION_OPTIONS.forEach(opt => {
        if (row[opt.key]) {
          userForm.permissions.push(opt.key)
        }
      })
    }
  } else {
    dialogType.value = 'create'
    userForm.id = null
    userForm.username = ''
    userForm.password = ''
    userForm.email = ''
    userForm.status = 1
    userForm.isAdmin = false
    userForm.permissions = []
  }
  dialogVisible.value = true
}

// 辅助函数：构造权限对象
const buildPermissionObject = (isAdmin, selectedPermissions) => {
  const perms = { isAdmin }
  // 遍历所有权限定义
  PERMISSION_OPTIONS.forEach(opt => {
    // 如果是管理员，默认给 true；否则看是否在选中数组里
    perms[opt.key] = isAdmin ? true : selectedPermissions.includes(opt.key)
  })
  return perms
}

const submitUserForm = async () => {
  if (!userFormRef.value) return

  if (dialogType.value === 'create' && !userForm.password) {
    ElMessage.warning('新增用户需设置密码')
    return
  }

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 构造权限对象
        const permissionData = buildPermissionObject(userForm.isAdmin, userForm.permissions)

        if (dialogType.value === 'create') {
          // 创建用户：合并基本信息和权限信息
          const createPayload = {
            realName: userForm.username,
            username: userForm.username,
            email: userForm.email,
            password: userForm.password,
            status: userForm.status,
            ...permissionData // 展开所有权限字段
          }
          await createUser(createPayload)
          ElMessage.success('创建成功')
        } else {
          // 编辑用户：分三步
          const userId = userForm.id;

          // 1. 更新基本信息
          await updateUser({
            id: userId,
            username: userForm.username,
            email: userForm.email,
            status: userForm.status
          })

          // 2. 更新权限
          await updateUserPermissions({
            id: userId,
            ...permissionData // 包含 isAdmin 和所有 booleans
          })

          // 3. 更新密码
          if (userForm.password && userForm.password.trim() !== '') {
            await resetUserPassword(userId, userForm.password.trim())
          }

          ElMessage.success('更新成功')
        }

        dialogVisible.value = false
        fetchUserList()
      } catch (error) {
        console.error(error)
        ElMessage.error('操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDeleteUser = (row) => {
  ElMessageBox.confirm(`确定删除用户 ${row.username}?`, '提示', { type: 'warning' })
    .then(async () => {
      try {
        await deleteUser(row.id)
        ElMessage.success('删除成功')
        fetchUserList()
      } catch (e) {
        ElMessage.error('删除失败')
      }
    }).catch(() => { })
}

const handleStatusChange = async (row) => {
  try {
    await updateUser({ id: row.id, status: row.status })
    ElMessage.success('状态已更新')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('更新失败')
  }
}
</script>

<style scoped>
.system-page {
  padding: 20px;
}

h2 {
  color: #0366d6;
  margin-bottom: 20px;
}

.settings-card {
  margin-bottom: 20px;
}

:deep(.settings-tabs .el-tabs__header) {
  margin-bottom: 0;
}

.settings-form {
  padding: 20px;
}

.logo-upload {
  width: 200px;
  height: 60px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-upload:hover {
  border-color: #0366d6;
}

.logo-upload-icon {
  font-size: 28px;
  color: #8c939d;
}

.logo-preview {
  max-width: 100%;
  max-height: 100%;
  display: block;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.setting-desc {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.backup-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.backup-info {
  margin: 0;
  font-size: 13px;
  color: #67c23a;
}

.header-actions {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

.permission-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.admin-tip {
  margin-left: 100px;
  /* 对齐 label-width */
}
</style>