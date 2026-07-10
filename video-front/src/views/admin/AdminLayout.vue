<template>
  <el-container style="height: 100vh;">
    <el-aside width="220px" class="sidebar">
      <div class="logo-area">
        <el-icon size="22" color="#fff"><VideoCameraFilled /></el-icon>
        <span class="logo-text">管理后台</span>
      </div>
      <el-menu router :default-active="route.path" class="nav-menu">
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/video">
          <el-icon><VideoCamera /></el-icon>
          <template #title>视频管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/meeting">
          <el-icon><Calendar /></el-icon>
          <template #title>会议房间</template>
        </el-menu-item>
      </el-menu>
      <div class="back-site">
        <el-button @click="router.push('/')" text>
          <el-icon><Back /></el-icon>
          返回主站
        </el-button>
      </div>
    </el-aside>
    <el-main class="admin-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { VideoCameraFilled, User, VideoCamera, Calendar, Back } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
</script>

<style scoped>
.sidebar {
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}

.logo-text {
  color: #fff;
  font-size: 17px;
  font-weight: 700;
}

.nav-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: 12px 0;
}

.nav-menu :deep(.el-menu-item) {
  color: #d1d5db;
  margin: 4px 12px;
  border-radius: 10px;
}

.nav-menu :deep(.el-menu-item:hover) {
  background: rgba(64, 158, 255, 0.1);
  color: #fff;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background: rgba(64, 158, 255, 0.3);
  color: #fff;
}

.back-site {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,0.08);
}

.back-site :deep(.el-button) {
  color: #94a3b8;
  width: 100%;
}

.admin-main {
  background: #f5f5f5;
  padding: 24px;
  overflow-y: auto;
}
</style>