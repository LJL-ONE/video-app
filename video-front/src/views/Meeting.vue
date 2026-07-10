<template>
  <div class="meeting-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>会议房间管理</h2>
        <p class="subtitle">创建和管理在线会议室</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon>
        创建会议房间
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card" shadow="never">
        <div class="stat-item">
          <el-icon size="32" color="#409EFF"><Calendar /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ roomList.length }}</span>
            <span class="stat-label">总房间数</span>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-item">
          <el-icon size="32" color="#67C23A"><VideoCamera /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ activeRoomCount }}</span>
            <span class="stat-label">使用中</span>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-item">
          <el-icon size="32" color="#909399"><HomeFilled /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ idleRoomCount }}</span>
            <span class="stat-label">空闲房间</span>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-item">
          <el-icon size="32" color="#E6A23C"><User /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ creatorCount }}</span>
            <span class="stat-label">创建人数</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true">
        <el-form-item label="房间名称">
          <el-input v-model="searchKeyword" placeholder="搜索房间" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="使用中" value="使用中" />
            <el-option label="空闲" value="空闲" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建人">
          <el-select v-model="creatorFilter" placeholder="全部创建人" clearable style="width: 150px">
            <el-option label="admin" value="admin" />
            <el-option label="test" value="test" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 房间表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="filteredRoomList" border stripe v-loading="loading">
        <el-table-column label="房间ID" prop="id" width="120" align="center" />
        <el-table-column label="房间名称" prop="roomName" align="center">
          <template #default="scope">
            <span class="room-name">{{ scope.row.roomName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="创建人" prop="creator" width="160" align="center">
          <template #default="scope">
            <el-tag effect="plain">{{ scope.row.creator }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="200" align="center">
          <template #default="scope">
            <el-icon><Clock /></el-icon>
            {{ scope.row.createTime }}
          </template>
        </el-table-column>
        <el-table-column label="房间状态" prop="status" width="140" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === '使用中' ? 'success' : 'info'"
              effect="dark"
              style="cursor: pointer"
              @click="toggleRoomStatus(scope.row)"
            >
              <el-icon v-if="scope.row.status === '使用中'"><VideoCamera /></el-icon>
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="enterRoom(scope.row)"
              :disabled="scope.row.status === '使用中'"
            >
              <el-icon><Check /></el-icon>
            </el-button>
            <el-button size="small" type="warning" @click="editRoom(scope.row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button size="small" type="danger" @click="deleteRoom(scope.row)">
              <el-icon><CircleClose /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建房间弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建会议房间" width="500px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="房间名称" prop="roomName">
          <el-input v-model="form.roomName" placeholder="请输入房间名称" />
        </el-form-item>
        <el-form-item label="房间描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入房间描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确认创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Calendar, VideoCamera, HomeFilled, User, Plus, Clock, Check, Edit, CircleClose } from '@element-plus/icons-vue'

interface MeetingRoom {
  id: number
  roomName: string
  creator: string
  createTime: string
  status: string
}

interface FormData {
  roomName: string
  description: string
}

// 房间列表数据
const roomList = ref<MeetingRoom[]>([])
const loading = ref(false)
// 弹窗控制
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
// 新建表单
const form = ref<FormData>({
  roomName: '',
  description: ''
})

// 搜索筛选
const searchKeyword = ref('')
const statusFilter = ref('')
const creatorFilter = ref('')

// 表单校验
const formRules = {
  roomName: [{ required: true, message: '请填写房间名称', trigger: 'blur' }]
}

// 统计数据
const activeRoomCount = computed(() => roomList.value.filter(r => r.status === '使用中').length)
const idleRoomCount = computed(() => roomList.value.filter(r => r.status === '空闲').length)
const creatorCount = computed(() => {
  const creators = new Set(roomList.value.map(r => r.creator))
  return creators.size
})

// 筛选列表
const filteredRoomList = computed(() => {
  let list = roomList.value
  if (searchKeyword.value) {
    list = list.filter(r => r.roomName.includes(searchKeyword.value))
  }
  if (statusFilter.value) {
    list = list.filter(r => r.status === statusFilter.value)
  }
  if (creatorFilter.value) {
    list = list.filter(r => r.creator === creatorFilter.value)
  }
  return list
})

// 加载房间列表
const getRoomList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/meeting/list')
    roomList.value = res.data || []
  } catch (error) {
    console.error('获取房间列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换房间状态
const toggleRoomStatus = (row: MeetingRoom) => {
  const newStatus = row.status === '使用中' ? '空闲' : '使用中'
  row.status = newStatus
  ElMessage.success(`${row.roomName} 状态已切换为 ${newStatus}`)
}

// 打开新建弹窗
const openCreateDialog = () => {
  form.value = { roomName: '', description: '' }
  dialogVisible.value = true
}

// 提交创建
const submitCreate = async () => {
  await formRef.value?.validate()
  roomList.value.unshift({
    id: Date.now(),
    roomName: form.value.roomName,
    creator: 'admin',
    createTime: new Date().toLocaleString(),
    status: '使用中'
  })
  ElMessage.success('会议房间创建成功')
  dialogVisible.value = false
}

// 进入房间
const enterRoom = (row: MeetingRoom) => {
  ElMessage.info(`正在进入房间：${row.roomName}`)
}

// 编辑房间
const editRoom = (row: MeetingRoom) => {
  ElMessage.info(`编辑房间：${row.roomName}`)
}

// 关闭房间
const deleteRoom = async (row: MeetingRoom) => {
  try {
    await ElMessageBox.confirm(`确定关闭【${row.roomName}】？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    roomList.value = roomList.value.filter(item => item.id !== row.id)
    ElMessage.success('房间已关闭')
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  getRoomList()
})
</script>

<style scoped>
.meeting-page {
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

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

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
  margin-top: 4px;
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
  background: linear-gradient(90deg, #E6A23C, #409EFF, #67C23A, #F56C6C);
  border-radius: 12px 12px 0 0;
}

.search-card .el-form-item {
  margin-bottom: 0;
}

.table-card {
  border-radius: 12px;
  overflow: hidden;
}

.room-name {
  font-weight: 600;
  color: #1f2937;
}
</style>