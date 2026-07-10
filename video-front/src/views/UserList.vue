<template>
  <div class="user-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>用户管理列表</h2>
        <p class="subtitle">管理系统用户，设置权限与角色</p>
      </div>
      <el-button type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon>
        新增用户
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-card-blue">
        <div class="stat-card-bg"></div>
        <div class="stat-item">
          <div class="stat-icon-wrap stat-icon-blue">
            <el-icon size="28" color="#fff"><User /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ userList.length }}</span>
            <span class="stat-label">用户总数</span>
          </div>
        </div>
      </div>
      <div class="stat-card stat-card-red">
        <div class="stat-card-bg"></div>
        <div class="stat-item">
          <div class="stat-icon-wrap stat-icon-red">
            <el-icon size="28" color="#fff"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ adminCount }}</span>
            <span class="stat-label">管理员</span>
          </div>
        </div>
      </div>
      <div class="stat-card stat-card-green">
        <div class="stat-card-bg"></div>
        <div class="stat-item">
          <div class="stat-icon-wrap stat-icon-green">
            <el-icon size="28" color="#fff"><UserFilled /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ normalCount }}</span>
            <span class="stat-label">普通用户</span>
          </div>
        </div>
      </div>
      <div class="stat-card stat-card-orange">
        <div class="stat-card-bg"></div>
        <div class="stat-item">
          <div class="stat-icon-wrap stat-icon-orange">
            <el-icon size="28" color="#fff"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <span class="stat-value">{{ activeUserCount }}</span>
            <span class="stat-label">正常账号</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 搜索筛选卡片 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true">
        <el-form-item label="搜索关键词">
          <el-input
            v-model="searchKeyword"
            placeholder="账号/昵称"
            clearable
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="角色筛选">
          <el-select v-model="roleFilter" placeholder="全部角色" clearable style="width: 150px;">
            <el-option label="管理员" value="管理员" />
            <el-option label="普通用户" value="普通用户" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态筛选">
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 150px;">
            <el-option label="正常" :value="0" />
            <el-option label="已禁用" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="paginatedUserList" border stripe v-loading="loading">
        <el-table-column label="用户ID" prop="id" width="100" align="center" />
        <el-table-column label="登录账号" prop="username" align="center">
          <template #default="scope">
            <span class="username-text">{{ scope.row.username }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户昵称" prop="nickname" align="center">
          <template #default="scope">
            <el-tag effect="plain">{{ scope.row.nickname }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" prop="role" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.role === '管理员' ? 'danger' : 'success'" effect="dark">
              {{ scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="180" align="center">
          <template #default="scope">
            <el-icon><Clock /></el-icon>
            {{ scope.row.createTime }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="deleted" width="120" align="center">
          <template #default="scope">
            <el-switch
              v-model="scope.row.deleted"
              :active-value="0"
              :inactive-value="1"
              active-text="正常"
              inactive-text="禁用"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="openEditDialog(scope.row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row)"
              :disabled="scope.row.deleted === 1"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="filteredUserList.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" title="用户信息" width="480px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="用户角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="普通用户" value="普通用户" />
            <el-option label="管理员" value="管理员" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Search, Refresh, Clock, Edit, Delete, User, UserFilled, CircleCheck, Star } from '@element-plus/icons-vue'

interface User {
  id: number
  username: string
  nickname: string
  role: string
  createTime?: string
  deleted: number
}

const userList = ref<User[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = ref<User>({
  id: 0,
  username: '',
  nickname: '',
  role: '普通用户',
  deleted: 0
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索相关
const searchKeyword = ref('')
const roleFilter = ref('')
const statusFilter = ref<number | null>(null)

// 表单校验规则
const formRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 筛选后的用户列表
const filteredUserList = computed(() => {
  let list = userList.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(user =>
      user.username.toLowerCase().includes(keyword) ||
      user.nickname.toLowerCase().includes(keyword)
    )
  }
  if (roleFilter.value) {
    list = list.filter(user => user.role === roleFilter.value)
  }
  if (statusFilter.value !== null) {
    list = list.filter(user => user.deleted === statusFilter.value)
  }
  return list
})

// 分页后的用户列表
const paginatedUserList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUserList.value.slice(start, end)
})

// 统计数量
const adminCount = computed(() => userList.value.filter(u => u.role === '管理员').length)
const normalCount = computed(() => userList.value.filter(u => u.role === '普通用户').length)
const activeUserCount = computed(() => userList.value.filter(u => u.deleted === 0).length)

// 获取用户列表
const getUserList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/user/list')
    userList.value = res.data || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  ElMessage.success(`找到 ${filteredUserList.value.length} 条记录`)
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  roleFilter.value = ''
  statusFilter.value = null
}

// 打开新增弹窗
const openAddDialog = () => {
  form.value = { id: 0, username: '', nickname: '', role: '普通用户', deleted: 0 }
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row: User) => {
  form.value = { ...row }
  dialogVisible.value = true
}

// 提交保存
const submitForm = async () => {
  await formRef.value?.validate()
  if (form.value.id === 0) {
    userList.value.unshift({
      ...form.value,
      id: Date.now(),
      createTime: new Date().toLocaleString()
    })
    ElMessage.success('用户添加成功')
  } else {
    const index = userList.value.findIndex(item => item.id === form.value.id)
    if (index !== -1) {
      userList.value[index] = { ...form.value }
    }
    ElMessage.success('用户信息更新成功')
  }
  dialogVisible.value = false
}

// 状态切换
const handleStatusChange = (row: User) => {
  ElMessage.success(`${row.nickname} 状态已更新`)
}

// 禁用用户
const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm(`确定禁用用户【${row.nickname}】？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const index = userList.value.findIndex(item => item.id === row.id)
    if (index !== -1) {
      userList.value[index].deleted = 1
      ElMessage.success('用户已禁用')
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => getUserList())
</script>

<style scoped>
.user-page {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.header-title h2 {
  margin: 0 0 8px 0;
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 0.5px;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.search-card {
  margin-bottom: 20px;
  position: relative;
  overflow: visible;
}

.search-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #409EFF, #67C23A, #E6A23C, #F56C6C);
  border-radius: 12px 12px 0 0;
}

.search-card .el-form-item {
  margin-bottom: 0;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  border-radius: 16px;
  padding: 24px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
}

.stat-card-bg {
  position: absolute;
  top: -40px;
  right: -40px;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  opacity: 0.15;
  transition: all 0.4s ease;
}

.stat-card:hover .stat-card-bg {
  transform: scale(1.2);
  opacity: 0.2;
}

.stat-card-blue .stat-card-bg {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
}

.stat-card-red .stat-card-bg {
  background: linear-gradient(135deg, #F56C6C, #fab6b6);
}

.stat-card-green .stat-card-bg {
  background: linear-gradient(135deg, #67C23A, #95d475);
}

.stat-card-orange .stat-card-bg {
  background: linear-gradient(135deg, #E6A23C, #eebe77);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  flex-shrink: 0;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon-wrap {
  transform: rotate(-8deg) scale(1.05);
}

.stat-icon-blue { background: linear-gradient(135deg, #409EFF, #2979ff); }
.stat-icon-red { background: linear-gradient(135deg, #F56C6C, #e64242); }
.stat-icon-green { background: linear-gradient(135deg, #67C23A, #3eaa15); }
.stat-icon-orange { background: linear-gradient(135deg, #E6A23C, #c98116); }

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin-top: 6px;
  font-weight: 500;
}

.table-card {
  border-radius: 12px;
  overflow: hidden;
}

.pagination-wrap {
  margin-top: 20px;
  padding: 16px 0 0;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e5e7eb;
}

.username-text {
  font-weight: 600;
  color: #1f2937;
}

.table-footer {
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
  display: flex;
  gap: 12px;
}
</style>