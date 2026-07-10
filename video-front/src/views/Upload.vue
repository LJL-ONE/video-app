<template>
  <div class="upload-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">投稿视频</h2>
      <p class="page-subtitle">分享你的精彩内容，让更多人看到</p>
    </div>

    <!-- 投稿表单 -->
    <el-card class="upload-card" shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="upload-form"
      >
        <!-- 视频标题 -->
        <el-form-item label="视频标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入视频标题"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>

        <!-- 视频分类 -->
        <el-form-item label="视频分类" prop="category">
          <el-select
            v-model="form.category"
            placeholder="请选择视频分类"
            style="width: 100%"
          >
            <el-option label="技术分享" value="技术分享" />
            <el-option label="生活日常" value="生活日常" />
            <el-option label="会议录屏" value="会议录屏" />
          </el-select>
        </el-form-item>

        <!-- 视频简介 -->
        <el-form-item label="视频简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="简单介绍一下你的视频（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 视频文件上传 -->
        <el-form-item label="视频文件" required>
          <div class="upload-wrap">
            <el-upload
              ref="uploadRef"
              drag
              :auto-upload="false"
              :limit="1"
              accept=".mp4"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :on-exceed="handleExceed"
              :file-list="fileList"
            >
              <div class="upload-area">
                <el-icon size="48" color="#409EFF"><UploadFilled /></el-icon>
                <p class="upload-text">将视频拖拽到此处，或点击选择文件</p>
                <p class="upload-hint">仅支持 MP4 格式，单文件最大 200MB</p>
              </div>
            </el-upload>

            <!-- 上传进度条 -->
            <el-progress
              v-if="uploadProgress > 0"
              :percentage="uploadProgress"
              :status="uploadStatus"
              :stroke-width="10"
              class="upload-progress"
            />
          </div>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="submitUpload"
          >
            {{ submitting ? '投稿中...' : '提交投稿' }}
          </el-button>
          <el-button size="large" @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type UploadFile, type UploadFiles } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref<FormInstance>()
const uploadRef = ref()

// 表单数据
const form = ref({
  title: '',
  category: '',
  description: ''
})

// 选中的视频文件
const selectedFile = ref<File | null>(null)
const fileList = ref<UploadFiles>([])

// 上传进度与状态
const uploadProgress = ref(0)
const uploadStatus = ref<'' | 'success' | 'exception'>('')
const submitting = ref(false)

// 文件大小上限：200MB
const MAX_SIZE = 200 * 1024 * 1024

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度为 2-50 个字符', trigger: 'blur' }
  ],
  category: [{ required: true, message: '请选择视频分类', trigger: 'change' }]
}

// 检查登录状态，未登录则提示并跳转登录页
const checkLogin = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }
  return true
}

// 文件选择变化时校验
const handleFileChange = (file: UploadFile, files: UploadFiles) => {
  const raw = file.raw as File
  if (!raw) return

  // 校验文件格式
  const isMp4 = raw.type === 'video/mp4' || raw.name.toLowerCase().endsWith('.mp4')
  if (!isMp4) {
    ElMessage.error('仅支持 MP4 格式的视频文件')
    clearFiles()
    return
  }

  // 校验文件大小
  if (raw.size > MAX_SIZE) {
    ElMessage.error('视频文件大小不能超过 200MB')
    clearFiles()
    return
  }

  selectedFile.value = raw
  fileList.value = files
}

// 文件移除
const handleFileRemove = () => {
  selectedFile.value = null
  fileList.value = []
}

// 超出文件数量限制
const handleExceed = () => {
  ElMessage.warning('每次只能上传一个视频文件，请先移除已选文件')
}

// 清空已选文件
const clearFiles = () => {
  selectedFile.value = null
  fileList.value = []
  uploadRef.value?.clearFiles()
}

// 获取上传者用户名
const getUploader = (): string => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return ''
  try {
    const info = JSON.parse(userInfoStr)
    return info?.username || ''
  } catch {
    return ''
  }
}

// 提交投稿
const submitUpload = async () => {
  // 校验表单
  await formRef.value?.validate()

  // 校验文件是否已选择
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要上传的视频文件')
    return
  }

  const uploader = getUploader()
  if (!uploader) {
    ElMessage.warning('登录信息已失效，请重新登录')
    router.push('/login')
    return
  }

  submitting.value = true
  uploadProgress.value = 0
  uploadStatus.value = ''

  // 构建 multipart/form-data 表单
  const formData = new FormData()
  formData.append('file', selectedFile.value)
  formData.append('title', form.value.title)
  formData.append('category', form.value.category)
  formData.append('description', form.value.description)
  formData.append('uploader', uploader)

  try {
    const res: any = await request.post('/video/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      // 大文件上传需要更长的超时时间
      timeout: 0,
      onUploadProgress: (progressEvent: any) => {
        if (progressEvent.total) {
          uploadProgress.value = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          )
        }
      }
    })

    // 响应拦截器已对 code !== 200 的情况抛错，到这里即为成功
    if (res.code === 200 || res.code === undefined) {
      uploadProgress.value = 100
      uploadStatus.value = 'success'
      ElMessage.success('视频投稿成功！')
      // 上传成功后跳转到首页
      router.push('/')
    } else {
      uploadStatus.value = 'exception'
      ElMessage.error(res.msg || '投稿失败，请重试')
      submitting.value = false
    }
  } catch {
    uploadStatus.value = 'exception'
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  clearFiles()
  uploadProgress.value = 0
  uploadStatus.value = ''
}

onMounted(() => {
  checkLogin()
})
</script>

<style scoped>
.upload-page {
  max-width: 860px;
  margin: 0 auto;
  padding: 24px 16px;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 0.5px;
}

.page-subtitle {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.upload-card {
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.upload-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #409EFF, #667eea, #764ba2);
  z-index: 1;
}

.upload-form {
  padding: 12px 8px 0;
}

.upload-wrap {
  width: 100%;
}

.upload-area {
  padding: 40px 20px;
  text-align: center;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.upload-area:hover {
  background: linear-gradient(180deg, #ecf5ff 0%, #e1f0ff 100%);
}

.upload-text {
  margin: 16px 0 6px;
  color: #4b5563;
  font-size: 15px;
  font-weight: 500;
}

.upload-hint {
  margin: 0;
  color: #9ca3af;
  font-size: 12px;
}

.upload-progress {
  margin-top: 16px;
}
</style>
