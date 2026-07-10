<template>
  <div class="upload-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>视频上传管理</h2>
        <p class="subtitle">上传、管理和发布视频内容</p>
      </div>
      <el-button type="success" @click="openUploadDialog">
        <el-icon><Upload /></el-icon>
        上传新视频
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-card class="stat-card" shadow="never">
        <div class="stat-item">
          <el-icon size="32" color="#409EFF"><VideoCamera /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ videoList.length }}</span>
            <span class="stat-label">总视频数</span>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-item">
          <el-icon size="32" color="#67C23A"><CircleCheck /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ publishedCount }}</span>
            <span class="stat-label">已发布</span>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-item">
          <el-icon size="32" color="#E6A23C"><Clock /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ pendingCount }}</span>
            <span class="stat-label">待审核</span>
          </div>
        </div>
      </el-card>
      <el-card class="stat-card" shadow="never">
        <div class="stat-item">
          <el-icon size="32" color="#909399"><Folder /></el-icon>
          <div class="stat-content">
            <span class="stat-value">{{ categoryCount }}</span>
            <span class="stat-label">分类数</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true">
        <el-form-item label="视频标题">
          <el-input v-model="searchKeyword" placeholder="搜索标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="categoryFilter" placeholder="全部分类" clearable style="width: 150px">
            <el-option label="生活日常" value="生活日常" />
            <el-option label="技术分享" value="技术分享" />
            <el-option label="会议录屏" value="会议录屏" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="已发布" value="已发布" />
            <el-option label="待审核" value="待审核" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 视频列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="filteredVideoList" border stripe>
        <el-table-column label="视频ID" prop="id" width="100" align="center" />
        <el-table-column label="视频标题" prop="title" align="center">
          <template #default="scope">
            <span class="video-title">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分类" prop="category" width="130" align="center">
          <template #default="scope">
            <el-tag effect="plain" :type="getCategoryType(scope.row.category)">
              {{ scope.row.category }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" prop="uploadTime" width="180" align="center">
          <template #default="scope">
            <el-icon><Clock /></el-icon>
            {{ scope.row.uploadTime }}
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '已发布' ? 'success' : 'warning'" effect="dark">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="previewVideo(scope.row)">
              <el-icon><View /></el-icon>
            </el-button>
            <el-button size="small" type="warning" @click="editVideo(scope.row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button size="small" type="danger" @click="delVideo(scope.row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 上传弹窗 -->
    <el-dialog v-model="uploadDialog" title="视频上传" width="620px" destroy-on-close>
      <el-form :model="videoForm" :rules="videoRules" ref="formRef" label-width="100px">
        <el-form-item label="视频标题" prop="title">
          <el-input v-model="videoForm.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-form-item label="视频分类" prop="category">
          <el-select v-model="videoForm.category" placeholder="选择分类">
            <el-option label="生活日常" value="生活日常" />
            <el-option label="技术分享" value="技术分享" />
            <el-option label="会议录屏" value="会议录屏" />
          </el-select>
        </el-form-item>
        <el-form-item label="视频文件">
          <el-upload
            ref="uploadRef"
            action="/api/upload"
            drag
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-progress="handleUploadProgress"
            :limit="1"
            accept=".mp4"
            :auto-upload="false"
            style="width: 100%"
          >
            <div class="upload-area">
              <el-icon size="48" color="#409EFF"><UploadFilled /></el-icon>
              <p class="upload-text">将视频拖入此处，或点击选择文件</p>
            </div>
            <template #tip>
              <div class="upload-tip">仅支持 MP4 格式，单文件最大 200MB</div>
            </template>
          </el-upload>
          <!-- 上传进度条 -->
          <el-progress
            v-if="uploadProgress > 0"
            :percentage="uploadProgress"
            :status="uploadStatus"
            style="margin-top: 12px"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialog = false">取消</el-button>
        <el-button type="primary" @click="submitUpload" :loading="submitting">
          {{ submitting ? '发布中...' : '提交发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  UploadFilled,
  Upload,
  VideoCamera,
  CircleCheck,
  Clock,
  Folder,
  View,
  Edit,
  Delete
} from '@element-plus/icons-vue'

interface Video {
  id: number
  title: string
  category: string
  uploadTime: string
  status: string
}

const uploadDialog = ref(false)
const formRef = ref<FormInstance>()
const uploadRef = ref()
const videoList = ref<Video[]>([])
const videoForm = ref({
  title: '',
  category: ''
})

// 上传进度相关
const uploadProgress = ref(0)
const uploadStatus = ref<'success' | 'warning' | 'exception' | ''>('')
const submitting = ref(false)
const hasFile = ref(false)

// 搜索筛选
const searchKeyword = ref('')
const categoryFilter = ref('')
const statusFilter = ref('')

// 表单校验
const videoRules = {
  title: [{ required: true, message: '请输入视频标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

// 统计数据
const publishedCount = computed(() => videoList.value.filter(v => v.status === '已发布').length)
const pendingCount = computed(() => videoList.value.filter(v => v.status === '待审核').length)
const categoryCount = computed(() => {
  const categories = new Set(videoList.value.map(v => v.category))
  return categories.size
})

// 筛选列表
const filteredVideoList = computed(() => {
  let list = videoList.value
  if (searchKeyword.value) {
    list = list.filter(v => v.title.includes(searchKeyword.value))
  }
  if (categoryFilter.value) {
    list = list.filter(v => v.category === categoryFilter.value)
  }
  if (statusFilter.value) {
    list = list.filter(v => v.status === statusFilter.value)
  }
  return list
})

// 分类颜色映射
const getCategoryType = (category: string) => {
  const typeMap: Record<string, string> = {
    '生活日常': '',
    '技术分享': 'success',
    '会议录屏': 'warning'
  }
  return typeMap[category] || ''
}

// 打开上传弹窗
const openUploadDialog = () => {
  videoForm.value = { title: '', category: '' }
  uploadProgress.value = 0
  uploadStatus.value = ''
  hasFile.value = false
  uploadDialog.value = true
}

// 文件上传成功
const handleUploadSuccess = () => {
  uploadStatus.value = 'success'
  hasFile.value = true
  ElMessage.success('文件上传完成')
}

// 文件上传失败
const handleUploadError = () => {
  uploadStatus.value = 'exception'
  ElMessage.error('文件上传失败，请重试')
}

// 文件上传进度
const handleUploadProgress = (event: any) => {
  uploadProgress.value = Math.round(event.percent)
}

// 提交发布
const submitUpload = async () => {
  await formRef.value?.validate()

  // 触发 el-upload 的手动上传
  uploadRef.value?.submit()

  submitting.value = true
  uploadProgress.value = 0
  uploadStatus.value = ''

  // 模拟上传过程
  const timer = setInterval(() => {
    uploadProgress.value += 10
    if (uploadProgress.value >= 100) {
      clearInterval(timer)
      videoList.value.unshift({
        id: Date.now(),
        title: videoForm.value.title,
        category: videoForm.value.category,
        uploadTime: new Date().toLocaleString(),
        status: '已发布'
      })
      uploadStatus.value = 'success'
      ElMessage.success('视频发布成功')
      submitting.value = false
      uploadDialog.value = false
    }
  }, 200)
}

// 预览视频
const previewVideo = (row: Video) => {
  ElMessage.info(`正在预览：${row.title}`)
}

// 编辑视频
const editVideo = (row: Video) => {
  ElMessage.info(`编辑：${row.title}`)
}

// 删除视频
const delVideo = async (row: Video) => {
  try {
    await ElMessageBox.confirm(`确定删除视频【${row.title}】？删除后无法恢复`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    videoList.value = videoList.value.filter(item => item.id !== row.id)
    ElMessage.success('视频已删除')
  } catch {
    // 用户取消
  }
}

// 加载视频列表
const getVideoList = async () => {
  const res = await request.get('/video/list')
  videoList.value = res.data
}

onMounted(() => getVideoList())
</script>

<style scoped>
.upload-page {
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
  background: linear-gradient(90deg, #67C23A, #409EFF, #E6A23C, #F56C6C);
  border-radius: 12px 12px 0 0;
}

.search-card .el-form-item {
  margin-bottom: 0;
}

.table-card {
  border-radius: 12px;
  overflow: hidden;
}

.video-title {
  font-weight: 600;
  color: #1f2937;
}

.upload-area {
  padding: 50px 20px;
  text-align: center;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.upload-area:hover {
  background: linear-gradient(180deg, #ecf5ff 0%, #e1f0ff 100%);
}

.upload-text {
  margin-top: 16px;
  color: #4b5563;
  font-size: 14px;
}

.upload-tip {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 8px;
}
</style>