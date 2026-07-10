<template>
  <el-container style="height: 100vh;">
    <!-- 侧边导航 -->
    <el-aside width="240px" class="sidebar">
      <!-- 装饰光斑 -->
      <div class="sidebar-glow sidebar-glow-1"></div>
      <div class="sidebar-glow sidebar-glow-2"></div>

      <!-- Logo区域 -->
      <div class="logo-area">
        <div class="logo-icon-box">
          <el-icon size="22" color="#fff"><VideoCameraFilled /></el-icon>
        </div>
        <div class="logo-text">
          <h1 class="logo-title">视频社区后台</h1>
          <p class="logo-subtitle">Management System</p>
        </div>
      </div>

      <!-- 用户信息 -->
      <div class="user-info">
        <el-avatar :size="48" class="user-avatar">
          <el-icon><UserFilled /></el-icon>
        </el-avatar>
        <div class="user-detail">
          <p class="username">admin</p>
          <p class="role-tag">管理员</p>
        </div>
        <el-icon class="user-action"><Setting /></el-icon>
      </div>

      <!-- 导航菜单 -->
      <el-menu
        router
        :default-active="route.path"
        :collapse-transition="false"
        class="nav-menu"
      >
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/video">
          <el-icon><VideoCamera /></el-icon>
          <template #title>视频上传</template>
        </el-menu-item>
        <el-menu-item index="/meeting">
          <el-icon><Calendar /></el-icon>
          <template #title>会议房间</template>
        </el-menu-item>
      </el-menu>

      <!-- 退出登录 -->
      <div class="logout-area">
        <el-button type="danger" @click="handleLogout" class="logout-btn">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-button>
      </div>
    </el-aside>

    <!-- 主内容区域 -->
    <el-container direction="vertical" style="flex: 1;">
      <!-- 顶部面包屑 -->
      <el-header class="main-header">
        <div class="header-left">
          <h2 class="page-title">{{ currentPageTitle }}</h2>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/user' }">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-badge :value="3" class="header-icon-badge">
            <el-icon size="20" class="header-icon"><Bell /></el-icon>
          </el-badge>
          <el-tooltip content="帮助文档" placement="bottom">
            <el-icon size="20" class="header-icon"><QuestionFilled /></el-icon>
          </el-tooltip>
        </div>
      </el-header>

      <!-- 路由内容出口 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  VideoCameraFilled,
  UserFilled,
  User,
  VideoCamera,
  Calendar,
  SwitchButton,
  Setting,
  HomeFilled,
  Bell,
  QuestionFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 当前页面标题
const currentPageTitle = computed(() => {
  const titleMap: Record<string, string> = {
    '/user': '用户管理',
    '/video': '视频上传',
    '/meeting': '会议房间'
  }
  return titleMap[route.path] || '首页'
})

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    localStorage.removeItem('token')
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.sidebar {
  position: relative;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
}

.sidebar-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;
  pointer-events: none;
}

.sidebar-glow-1 {
  width: 200px;
  height: 200px;
  background: #409EFF;
  top: -50px;
  right: -50px;
  animation: glowFloat 8s ease-in-out infinite;
}

.sidebar-glow-2 {
  width: 180px;
  height: 180px;
  background: #764ba2;
  bottom: 50px;
  left: -60px;
  animation: glowFloat 10s ease-in-out infinite reverse;
}

@keyframes glowFloat {
  0%, 100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(20px, -20px);
  }
}

.logo-area {
  position: relative;
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.logo-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #409EFF 0%, #764ba2 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.logo-icon-box:hover {
  transform: rotate(-10deg) scale(1.1);
}

.logo-text {
  flex: 1;
  min-width: 0;
}

.logo-title {
  color: #fff;
  font-size: 17px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 0.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logo-subtitle {
  color: #94a3b8;
  font-size: 11px;
  margin: 2px 0 0;
  letter-spacing: 1px;
}

.user-info {
  position: relative;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.user-avatar {
  background: linear-gradient(135deg, #409EFF 0%, #764ba2 100%) !important;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.2);
  flex-shrink: 0;
}

.user-detail {
  flex: 1;
  min-width: 0;
}

.username {
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-tag {
  color: #94a3b8;
  font-size: 11px;
  margin: 0;
  background: rgba(64, 158, 255, 0.2);
  padding: 2px 8px;
  border-radius: 8px;
  display: inline-block;
  border: 1px solid rgba(64, 158, 255, 0.3);
}

.user-action {
  color: #64748b;
  cursor: pointer;
  transition: all 0.3s ease;
  padding: 6px;
  border-radius: 6px;
}

.user-action:hover {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
  transform: rotate(30deg);
}

.nav-menu {
  border-right: none;
  flex: 1;
  padding: 12px 0;
  background: transparent;
}

.nav-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 4px 12px;
  border-radius: 10px;
  color: #d1d5db;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-menu .el-menu-item:hover {
  background: rgba(64, 158, 255, 0.1);
  color: #fff;
  transform: translateX(4px);
}

.nav-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.3) 0%, rgba(64, 158, 255, 0.1) 100%);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.logout-area {
  position: relative;
  padding: 16px 20px;
  border-top: 1px solid rgba(255,255,255,0.08);
  background: linear-gradient(180deg, transparent 0%, rgba(0, 0, 0, 0.2) 100%);
}

.logout-btn {
  width: 100%;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(135deg, #F56C6C 0%, #e64242 100%) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  transition: all 0.3s ease;
}

.logout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.5);
}

.main-header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  height: 64px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.breadcrumb {
  font-size: 12px;
}

.breadcrumb :deep(.el-breadcrumb__inner) {
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon-badge {
  display: flex;
  align-items: center;
}

.header-icon {
  color: #6b7280;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.header-icon:hover {
  color: #409EFF;
  background: rgba(64, 158, 255, 0.1);
  transform: scale(1.1);
}

.main-content {
  background:
    radial-gradient(circle at 20% 30%, rgba(64, 158, 255, 0.04) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(118, 75, 162, 0.04) 0%, transparent 50%),
    linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 24px;
  overflow-y: auto;
  min-height: calc(100vh - 64px);
}

/* 子路由过渡动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}
</style>