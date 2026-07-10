<template>
  <div class="index-page">
    <!-- 顶部分类标签栏 -->
    <div class="category-bar">
      <div class="category-tabs">
        <div
          v-for="cat in categories"
          :key="cat"
          class="cat-tab"
          :class="{ active: activeCategory === cat }"
          @click="switchCategory(cat)"
        >
          <span class="cat-text">{{ cat }}</span>
        </div>
      </div>
    </div>

    <!-- 搜索结果提示 -->
    <div class="search-tip" v-if="searchKeyword">
      <el-icon><Search /></el-icon>
      <span class="tip-text">
        搜索 “<strong>{{ searchKeyword }}</strong>” 的结果，共
        <em>{{ filteredVideos.length }}</em> 个视频
      </span>
      <el-button text size="small" class="clear-btn" @click="clearSearch">
        <el-icon><Close /></el-icon>
        <span>清除搜索</span>
      </el-button>
    </div>

    <!-- 视频内容区域 -->
    <div class="content" v-loading="loading">
      <!-- 视频卡片网格 -->
      <div class="video-grid" v-if="filteredVideos.length">
        <div
          v-for="video in filteredVideos"
          :key="video.id"
          class="video-card"
          @click="goToVideo(video.id)"
        >
          <!-- 缩略图区域：使用渐变背景色代替 -->
          <div class="card-cover" :style="coverStyle(video)">
            <div class="cover-overlay">
              <div class="play-btn">
                <el-icon :size="46"><VideoPlay /></el-icon>
              </div>
            </div>
            <span class="cover-cat">{{ video.category }}</span>
          </div>

          <!-- 卡片信息区域 -->
          <div class="card-info">
            <h3 class="card-title" :title="video.title">{{ video.title }}</h3>

            <div class="card-meta">
              <el-icon class="meta-icon"><User /></el-icon>
              <span class="uploader">{{ video.uploader || '匿名用户' }}</span>
              <span class="meta-dot">·</span>
              <span class="time">{{ formatTime(video.createTime) }}</span>
            </div>

            <div class="card-footer">
              <el-tag
                size="small"
                :type="categoryTagType(video.category)"
                effect="light"
                round
              >
                {{ video.category }}
              </el-tag>
              <span class="status-tag" :class="statusClass(video.status)">
                <i class="status-dot"></i>
                {{ video.status }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态提示 -->
      <el-empty
        v-else-if="!loading"
        :description="emptyDescription"
        :image-size="160"
      >
        <el-button type="primary" plain @click="resetFilter">重置筛选</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { VideoPlay, User, Search, Close } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 视频数据类型定义
interface Video {
  id: number | string
  title: string
  category: string
  fileUrl?: string
  coverUrl?: string
  status: string
  uploader?: string
  createTime?: string
}

// 后端响应类型定义
interface VideoListResponse {
  code: number
  message: string
  data: Video[]
}

const router = useRouter()
const route = useRoute()

// 分类列表
const categories = ['全部', '技术分享', '生活日常', '会议录屏']

// 视频列表与加载状态
const videos = ref<Video[]>([])
const loading = ref(false)

// 当前选中的分类（从路由参数读取，兼容 /category/:cat）
const activeCategory = computed(() => {
  return (route.params.cat as string) || '全部'
})

// 搜索关键词（从 URL query 的 q 参数读取）
const searchKeyword = computed(() => {
  return (route.query.q as string) || ''
})

// 过滤后的视频列表：只显示已发布，并按分类与关键词筛选
const filteredVideos = computed(() => {
  let list = videos.value.filter((v) => v.status === '已发布')

  // 按分类筛选
  if (activeCategory.value !== '全部') {
    list = list.filter((v) => v.category === activeCategory.value)
  }

  // 按搜索关键词筛选（匹配标题）
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter((v) => v.title.toLowerCase().includes(kw))
  }

  return list
})

// 空状态提示文案
const emptyDescription = computed(() => {
  if (searchKeyword.value || activeCategory.value !== '全部') {
    return '没有找到相关视频，换个条件试试吧'
  }
  return '暂无视频内容，敬请期待'
})

// 缩略图渐变色调色板
const gradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
  'linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
  'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)'
]

// 根据视频 id 生成稳定的渐变背景
const coverStyle = (video: Video) => {
  const key = String(video.id ?? video.title)
  let hash = 0
  for (let i = 0; i < key.length; i++) {
    hash = (hash * 31 + key.charCodeAt(i)) >>> 0
  }
  const index = hash % gradients.length
  return { background: gradients[index] }
}

// 分类对应的标签类型
type TagType = 'primary' | 'success' | 'info' | 'warning' | 'danger'
const categoryTagType = (cat: string): TagType => {
  const map: Record<string, TagType> = {
    技术分享: 'primary',
    生活日常: 'success',
    会议录屏: 'warning'
  }
  return map[cat] || 'info'
}

// 状态样式类
const statusClass = (status: string) => {
  if (status === '已发布') return 'status-published'
  if (status === '待审核') return 'status-pending'
  return 'status-other'
}

// 格式化时间显示
const formatTime = (time?: string) => {
  if (!time) return ''
  // 仅保留日期部分
  return time.length > 10 ? time.substring(0, 10) : time
}

// 切换分类：通过路由同步，保持与导航栏一致
const switchCategory = (cat: string) => {
  if (cat === '全部') {
    router.push('/')
  } else {
    router.push(`/category/${cat}`)
  }
}

// 清除搜索
const clearSearch = () => {
  router.push('/')
}

// 重置所有筛选条件
const resetFilter = () => {
  router.push('/')
}

// 跳转到视频播放页
const goToVideo = (id: number | string) => {
  router.push(`/video/${id}`)
}

// 从后端获取视频列表
const fetchVideos = async () => {
  loading.value = true
  try {
    const res = (await request.get('/video/list')) as unknown as VideoListResponse
    videos.value = res?.data ?? []
  } catch (error) {
    // 错误提示已由 request 拦截器统一处理
    videos.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchVideos()
})
</script>

<style scoped>
.index-page {
  width: 100%;
}

/* ===== 分类标签栏 ===== */
.category-bar {
  background: #fff;
  border-radius: 14px;
  padding: 14px 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 68px;
  z-index: 10;
}

.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.cat-tab {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  color: #4b5563;
  cursor: pointer;
  user-select: none;
  transition: all 0.25s ease;
  background: #f3f4f6;
}

.cat-tab:hover {
  color: #409eff;
  background: #ecf5ff;
  transform: translateY(-1px);
}

.cat-tab.active {
  color: #fff;
  background: linear-gradient(135deg, #409eff 0%, #6a8dff 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.35);
  font-weight: 600;
}

/* ===== 搜索结果提示 ===== */
.search-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0e6ff 100%);
  border-radius: 12px;
  padding: 12px 18px;
  margin-bottom: 18px;
  color: #4b5563;
  font-size: 14px;
}

.search-tip .tip-text {
  flex: 1;
}

.search-tip strong {
  color: #409eff;
}

.search-tip em {
  color: #f5576c;
  font-style: normal;
  font-weight: 700;
  margin: 0 2px;
}

.clear-btn {
  color: #6b7280;
}

/* ===== 内容区域 ===== */
.content {
  min-height: 320px;
}

/* ===== 视频卡片网格 ===== */
.video-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
}

/* ===== 视频卡片 ===== */
.video-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

.video-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
}

/* 缩略图区域 */
.card-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%; /* 16:9 宽高比 */
  overflow: hidden;
}

.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s ease;
}

.video-card:hover .cover-overlay {
  background: rgba(0, 0, 0, 0.32);
}

.play-btn {
  color: #fff;
  opacity: 0;
  transform: scale(0.7);
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.3));
}

.video-card:hover .play-btn {
  opacity: 1;
  transform: scale(1);
}

.cover-cat {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
}

/* 卡片信息区域 */
.card-info {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

.card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 43px;
  transition: color 0.2s;
}

.video-card:hover .card-title {
  color: #409eff;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #9ca3af;
}

.meta-icon {
  font-size: 13px;
}

.uploader {
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-dot {
  margin: 0 2px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 4px;
}

/* 状态标签 */
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.status-published {
  color: #67c23a;
}

.status-pending {
  color: #e6a23c;
}

.status-other {
  color: #909399;
}

/* ===== 响应式布局 ===== */
@media (max-width: 1200px) {
  .video-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 992px) {
  .video-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .category-bar {
    top: 60px;
  }
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
  }
  .category-bar {
    padding: 10px 14px;
    top: 56px;
  }
  .cat-tab {
    padding: 6px 14px;
    font-size: 13px;
  }
  .card-title {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .video-grid {
    grid-template-columns: 1fr;
  }
}
</style>
