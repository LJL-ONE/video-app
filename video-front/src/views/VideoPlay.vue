<template>
  <div class="play-page" v-loading="loading">
    <!-- 视频不存在 / 加载错误提示 -->
    <el-result
      v-if="notFound"
      icon="error"
      title="视频不存在"
      sub-title="该视频可能已被删除或链接有误"
    >
      <template #extra>
        <el-button type="primary" @click="router.push('/')">返回首页</el-button>
      </template>
    </el-result>

    <!-- 正常播放页布局 -->
    <div v-else-if="video" class="play-layout">
      <!-- 左侧主体区域 -->
      <div class="play-main">
        <!-- 视频播放器 -->
        <div class="player-wrap">
          <video
            v-if="video.fileUrl"
            class="video-player"
            :src="video.fileUrl"
            :poster="video.coverUrl"
            controls
            autoplay
            playsinline
          />
          <!-- 视频URL为空时的占位 -->
          <div v-else class="player-placeholder">
            <el-icon size="64" color="#909399"><VideoPlay /></el-icon>
            <p class="placeholder-text">视频准备中</p>
            <p class="placeholder-sub">资源正在处理，请稍后回来观看</p>
          </div>
        </div>

        <!-- 视频标题 -->
        <h1 class="video-title">{{ video.title }}</h1>

        <!-- 视频信息栏 -->
        <div class="video-info">
          <div class="info-left">
            <el-avatar :size="36" class="uploader-avatar">
              {{ video.uploader?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="uploader-name">{{ video.uploader || '匿名用户' }}</span>
          </div>
          <div class="info-right">
            <el-tag effect="plain" :type="getCategoryType(video.category)">
              {{ video.category }}
            </el-tag>
            <span class="info-item">
              <el-icon><Clock /></el-icon>
              {{ video.createTime }}
            </span>
            <span class="info-item" v-if="video.duration">
              <el-icon><VideoPlay /></el-icon>
              {{ formatDuration(video.duration) }}
            </span>
            <span class="info-item" v-if="video.size">
              <el-icon><Film /></el-icon>
              {{ formatSize(video.size) }}
            </span>
          </div>
        </div>

        <!-- 操作按钮：点赞 / 收藏 / 分享 -->
        <div class="action-bar">
          <el-button
            round
            :type="liked ? 'primary' : 'default'"
            @click="handleLike"
          >
            <el-icon><Pointer /></el-icon>
            <span>{{ liked ? '已点赞' : '点赞' }}</span>
            <span class="action-count">{{ likeCount }}</span>
          </el-button>

          <el-button
            round
            :type="favorited ? 'warning' : 'default'"
            @click="handleFavorite"
          >
            <el-icon><StarFilled v-if="favorited" /><Star v-else /></el-icon>
            <span>{{ favorited ? '已收藏' : '收藏' }}</span>
            <span class="action-count">{{ favoriteCount }}</span>
          </el-button>

          <el-button round @click="handleShare">
            <el-icon><Share /></el-icon>
            <span>分享</span>
          </el-button>
        </div>

        <!-- 视频简介 -->
        <el-card class="desc-card" shadow="never">
          <div class="desc-header">
            <span class="desc-title">视频简介</span>
            <el-tag size="small" :type="video.status === '已发布' ? 'success' : 'warning'">
              {{ video.status }}
            </el-tag>
          </div>
          <p class="desc-content">
            {{ video.description || '该视频暂无简介内容。' }}
          </p>
        </el-card>
      </div>

      <!-- 右侧推荐列表 -->
      <div class="play-side">
        <div class="side-header">
          <el-icon color="#409EFF"><Film /></el-icon>
          <span>相关推荐</span>
        </div>
        <div class="rec-list" v-loading="recLoading">
          <div
            v-for="item in recommendList"
            :key="item.id"
            class="rec-item"
            @click="goVideo(item.id)"
          >
            <div class="rec-cover">
              <el-image
                v-if="item.coverUrl"
                :src="item.coverUrl"
                fit="cover"
                class="rec-cover-img"
              />
              <div v-else class="rec-cover-placeholder">
                <el-icon size="28" color="#fff"><VideoPlay /></el-icon>
              </div>
              <span class="rec-duration" v-if="item.duration">
                {{ formatDuration(item.duration) }}
              </span>
            </div>
            <div class="rec-info">
              <p class="rec-title">{{ item.title }}</p>
              <p class="rec-uploader">{{ item.uploader || '匿名用户' }}</p>
              <div class="rec-meta">
                <el-tag size="small" effect="plain">{{ item.category }}</el-tag>
                <span class="rec-time">{{ item.uploadTime || item.createTime }}</span>
              </div>
            </div>
          </div>
          <el-empty
            v-if="!recLoading && recommendList.length === 0"
            description="暂无相关推荐"
            :image-size="80"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  VideoPlay,
  Clock,
  Film,
  Pointer,
  Star,
  StarFilled,
  Share
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 视频信息类型
interface VideoInfo {
  id: number
  title: string
  category: string
  fileUrl?: string
  coverUrl?: string
  status: string
  uploader?: string
  createTime?: string
  size?: number
  duration?: number
  description?: string
}

// 推荐列表项类型
interface RecommendItem {
  id: number
  title: string
  category: string
  coverUrl?: string
  uploader?: string
  uploadTime?: string
  createTime?: string
  duration?: number
  status?: string
}

const route = useRoute()
const router = useRouter()

// 当前视频ID
const videoId = computed(() => Number(route.params.id))

// 页面状态
const loading = ref(false)
const recLoading = ref(false)
const notFound = ref(false)
const video = ref<VideoInfo | null>(null)
const recommendList = ref<RecommendItem[]>([])

// 点赞 / 收藏 状态（仅UI）
const liked = ref(false)
const favorited = ref(false)
const likeCount = ref(0)
const favoriteCount = ref(0)

// 分类标签颜色映射
const getCategoryType = (category: string) => {
  const typeMap: Record<string, string> = {
    '生活日常': '',
    '技术分享': 'success',
    '会议录屏': 'warning'
  }
  return typeMap[category] || ''
}

// 时长格式化：秒 -> mm:ss
const formatDuration = (duration: number) => {
  const sec = Math.max(0, Math.floor(Number(duration) || 0))
  const m = Math.floor(sec / 60)
  const s = sec % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

// 文件大小格式化：字节 -> MB
const formatSize = (size: number) => {
  const mb = Number(size) / (1024 * 1024)
  if (mb >= 1024) return (mb / 1024).toFixed(2) + ' GB'
  return mb.toFixed(2) + ' MB'
}

// 获取单个视频信息
const loadVideo = async () => {
  if (!videoId.value) {
    notFound.value = true
    return
  }
  loading.value = true
  notFound.value = false
  video.value = null
  try {
    const res: any = await request.get(`/video/${videoId.value}`)
    // 兼容直接返回对象或 { data } 包裹
    const data = res?.data ?? res
    if (!data) {
      notFound.value = true
      return
    }
    video.value = data
    // 重置交互状态（点赞数模拟）
    liked.value = false
    favorited.value = false
    likeCount.value = Math.floor(Math.random() * 500) + 10
    favoriteCount.value = Math.floor(Math.random() * 200) + 5
    // 加载推荐列表
    loadRecommendList(data.category)
  } catch (e) {
    notFound.value = true
  } finally {
    loading.value = false
  }
}

// 获取推荐列表（同分类的其他视频）
const loadRecommendList = async (category: string) => {
  recLoading.value = true
  try {
    const res: any = await request.get('/video/list')
    const list: RecommendItem[] = res?.data ?? res ?? []
    recommendList.value = list.filter(
      (item) => item.id !== videoId.value && (!category || item.category === category)
    )
    // 若同分类不足，补充其他视频
    if (recommendList.value.length < 4) {
      const others = list.filter(
        (item) => item.id !== videoId.value && item.category !== category
      )
      recommendList.value = [...recommendList.value, ...others]
    }
  } catch (e) {
    recommendList.value = []
  } finally {
    recLoading.value = false
  }
}

// 跳转到推荐视频
const goVideo = (id: number) => {
  router.push(`/video/${id}`)
}

// 点赞（仅UI提示）
const handleLike = () => {
  liked.value = !liked.value
  likeCount.value += liked.value ? 1 : -1
  ElMessage.success(liked.value ? '点赞成功' : '已取消点赞')
}

// 收藏（仅UI提示）
const handleFavorite = () => {
  favorited.value = !favorited.value
  favoriteCount.value += favorited.value ? 1 : -1
  ElMessage.success(favorited.value ? '已加入收藏' : '已取消收藏')
}

// 分享（仅UI提示）
const handleShare = () => {
  ElMessage.success('分享链接已复制到剪贴板')
}

// 监听路由参数变化，重新加载视频
watch(() => route.params.id, () => {
  if (route.name === 'VideoPlay') loadVideo()
})

onMounted(loadVideo)
</script>

<style scoped>
.play-page {
  min-height: 400px;
}

.play-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

/* 左侧主体 */
.play-main {
  flex: 1;
  min-width: 0;
}

/* 播放器 */
.player-wrap {
  width: 100%;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 16 / 9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

/* 视频准备中占位 */
.player-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  text-align: center;
  padding: 24px;
}

.placeholder-text {
  margin-top: 16px;
  font-size: 20px;
  font-weight: 600;
}

.placeholder-sub {
  margin-top: 8px;
  font-size: 13px;
  color: #9ca3af;
}

/* 标题 */
.video-title {
  margin: 20px 0 12px;
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
}

/* 信息栏 */
.video-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.info-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.uploader-avatar {
  background: linear-gradient(135deg, #409EFF 0%, #764ba2 100%) !important;
  color: #fff !important;
  font-weight: 600;
}

.uploader-name {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.info-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.info-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
}

/* 操作按钮 */
.action-bar {
  display: flex;
  gap: 12px;
  margin: 16px 0;
  flex-wrap: wrap;
}

.action-count {
  margin-left: 4px;
  font-size: 13px;
  opacity: 0.85;
}

/* 简介 */
.desc-card {
  margin-top: 8px;
}

.desc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.desc-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.desc-content {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.8;
  white-space: pre-wrap;
}

/* 右侧推荐 */
.play-side {
  width: 340px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 80px;
}

.side-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.rec-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-height: 70vh;
  overflow-y: auto;
}

.rec-item {
  display: flex;
  gap: 10px;
  cursor: pointer;
  padding: 6px;
  border-radius: 8px;
  transition: background 0.2s;
}

.rec-item:hover {
  background: #f5f7fa;
}

.rec-item:hover .rec-title {
  color: #409EFF;
}

.rec-cover {
  position: relative;
  width: 140px;
  height: 80px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  background: #000;
}

.rec-cover-img {
  width: 100%;
  height: 100%;
}

.rec-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.rec-duration {
  position: absolute;
  right: 4px;
  bottom: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-size: 11px;
  padding: 1px 5px;
  border-radius: 3px;
}

.rec-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rec-title {
  font-size: 13px;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.rec-uploader {
  font-size: 12px;
  color: #6b7280;
}

.rec-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
  flex-wrap: wrap;
}

.rec-time {
  font-size: 11px;
  color: #9ca3af;
}

/* 响应式 */
@media (max-width: 992px) {
  .play-layout {
    flex-direction: column;
  }
  .play-side {
    width: 100%;
    position: static;
  }
  .rec-list {
    max-height: none;
  }
}
</style>
