<template>
  <header class="navbar">
    <div class="navbar-inner">
      <!-- Logo -->
      <div class="logo" @click="router.push('/')">
        <el-icon size="28" color="#409EFF"><VideoCameraFilled /></el-icon>
        <span class="logo-text">视界</span>
      </div>

      <!-- 导航链接 -->
      <nav class="nav-links">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/category/技术分享" class="nav-link">技术</router-link>
        <router-link to="/category/生活日常" class="nav-link">生活</router-link>
        <router-link to="/category/会议录屏" class="nav-link">会议</router-link>
      </nav>

      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索视频..."
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        />
      </div>

      <!-- 右侧操作 -->
      <div class="nav-actions">
        <el-button type="primary" @click="handleUpload" v-if="isLoggedIn">
          <el-icon><Upload /></el-icon>
          <span>投稿</span>
        </el-button>

        <template v-if="isLoggedIn">
          <el-dropdown @command="handleCommand">
            <div class="user-avatar-box">
              <el-avatar :size="36" class="user-avatar">
                {{ userInfo.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo.nickname || '用户' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="upload">投稿视频</el-dropdown-item>
                <el-dropdown-item command="admin" v-if="userInfo.role === '管理员'">管理后台</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button @click="router.push('/login')">登录</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { VideoCameraFilled, Search, Upload } from '@element-plus/icons-vue'

const router = useRouter()

const searchKeyword = ref('')

// 用户信息
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const userInfo = computed(() => {
  try {
    const stored = localStorage.getItem('userInfo')
    return stored ? JSON.parse(stored) : {}
  } catch {
    return {}
  }
})

// 搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/', query: { q: searchKeyword.value.trim() } })
  }
}

// 投稿
const handleUpload = () => {
  router.push('/upload')
}

// 下拉菜单
const handleCommand = (command: string) => {
  if (command === 'upload') {
    router.push('/upload')
  } else if (command === 'admin') {
    router.push('/admin/user')
  } else if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    ElMessage.success('已退出登录')
    router.push('/')
  }
}
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.navbar-inner {
  max-width: 1400px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: 1px;
}

.nav-links {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.nav-link {
  padding: 6px 14px;
  border-radius: 8px;
  color: #4b5563;
  text-decoration: none;
  font-size: 15px;
  transition: all 0.2s;
}

.nav-link:hover {
  background: #f0f5ff;
  color: #409EFF;
}

.nav-link.router-link-exact-active {
  color: #409EFF;
  font-weight: 600;
}

.search-box {
  flex: 1;
  max-width: 400px;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.user-avatar-box {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-avatar-box:hover {
  background: #f5f5f5;
}

.user-avatar {
  background: linear-gradient(135deg, #409EFF 0%, #764ba2 100%) !important;
  color: #fff !important;
  font-weight: 600;
}

.username {
  font-size: 14px;
  color: #1f2937;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .nav-links {
    display: none;
  }
  .search-box {
    max-width: 200px;
  }
  .username {
    display: none;
  }
}
</style>