<template>
  <div class="login-wrap">
    <div class="login-background">
      <div class="bg-gradient bg-gradient-1"></div>
      <div class="bg-gradient bg-gradient-2"></div>
      <div class="bg-gradient bg-gradient-3"></div>
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
      <div class="bg-shape shape-4"></div>
      <div class="bg-particles">
        <div
          class="particle"
          v-for="(style, i) in particleStyles"
          :key="i"
          :style="style"
        ></div>
      </div>
    </div>

    <div class="login-card">
      <div class="card-glow"></div>
      <div class="card-header">
        <div class="logo-icon">
          <el-icon size="40" color="#fff"><VideoCameraFilled /></el-icon>
        </div>
        <h1 class="title">视界</h1>
        <p class="subtitle">分享你的精彩世界</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入账号"
            size="large"
            class="login-input"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            show-password
            placeholder="请输入密码"
            size="large"
            class="login-input"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="login"
            class="login-btn"
            :loading="loading"
          >
            <span v-if="!loading">登 录 系 统</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>

        <div class="form-options">
          <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
          <a class="forgot-link">忘记密码？</a>
        </div>
      </el-form>

      <div class="card-footer">
        <el-alert type="info" :closable="false" show-icon class="tip-alert">
          <template #default>
            <span style="font-size: 13px;">测试账号：admin / 密码：123456</span>
          </template>
        </el-alert>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { VideoCameraFilled, User, Lock } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)
const form = ref({
  username: '',
  password: ''
})

// 粒子动画样式 - 预计算避免重新渲染
const particleStyles = ref<string[]>([])

onMounted(() => {
  const styles: string[] = []
  for (let i = 0; i < 20; i++) {
    const size = Math.random() * 6 + 2
    const left = Math.random() * 100
    const duration = Math.random() * 20 + 10
    const delay = Math.random() * 5
    styles.push(
      `width:${size}px;height:${size}px;left:${left}%;animation-duration:${duration}s;animation-delay:${delay}s`
    )
  }
  particleStyles.value = styles
})

// 表单校验规则
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const login = async () => {
  await formRef.value?.validate()
  loading.value = true

  try {
    const res: any = await request.post('/login', form.value)
    if (res.code === 200 && res.data?.token) {
      localStorage.setItem('token', res.data.token)
      // 保存用户信息供导航栏使用
      localStorage.setItem('userInfo', JSON.stringify({
        userId: res.data.userId,
        username: res.data.username,
        nickname: res.data.nickname,
        role: res.data.role
      }))
      ElMessage.success('登录成功，欢迎回来！')
      router.push('/')
    } else {
      ElMessage.error(res.msg || '登录失败，请重试')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败，请检查网络')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  background-size: 200% 200%;
  animation: gradientShift 15s ease infinite;
  overflow: hidden;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.login-background {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

/* 大型渐变光斑 */
.bg-gradient {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
}

.bg-gradient-1 {
  width: 500px;
  height: 500px;
  background: #ff6b9d;
  top: -150px;
  left: -150px;
  animation: gradientFloat 20s ease-in-out infinite;
}

.bg-gradient-2 {
  width: 600px;
  height: 600px;
  background: #4facfe;
  bottom: -200px;
  right: -200px;
  animation: gradientFloat 25s ease-in-out infinite reverse;
}

.bg-gradient-3 {
  width: 400px;
  height: 400px;
  background: #43e97b;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: gradientFloat 18s ease-in-out infinite;
}

@keyframes gradientFloat {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(50px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-50px, 50px) scale(0.9);
  }
}

/* 圆形装饰 */
.bg-shape {
  position: absolute;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.15);
  background: transparent;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 15%;
  animation: rotate 30s linear infinite;
}

.shape-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  left: 80%;
  animation: rotate 20s linear infinite reverse;
}

.shape-3 {
  width: 150px;
  height: 150px;
  top: 70%;
  left: 10%;
  animation: rotate 25s linear infinite;
}

.shape-4 {
  width: 250px;
  height: 250px;
  top: 5%;
  right: 10%;
  animation: rotate 35s linear infinite reverse;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 粒子效果 */
.bg-particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  bottom: -20px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: rise linear infinite;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

@keyframes rise {
  0% {
    transform: translateY(0) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) translateX(50px);
    opacity: 0;
  }
}

.login-card {
  position: relative;
  width: 460px;
  background: rgba(255, 255, 255, 0.98);
  padding: 48px 40px;
  border-radius: 24px;
  box-shadow:
    0 25px 70px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  z-index: 10;
  animation: slideUp 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.card-glow {
  position: absolute;
  top: -100px;
  left: 50%;
  transform: translateX(-50%);
  width: 300px;
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  filter: blur(60px);
  opacity: 0.3;
  border-radius: 50%;
  z-index: 0;
}

.login-card > * {
  position: relative;
  z-index: 1;
}

.card-header {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.5);
  margin-bottom: 16px;
  transition: transform 0.3s ease;
  animation: logoFloat 3s ease-in-out infinite;
}

.logo-icon:hover {
  transform: scale(1.05) rotate(5deg);
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.title {
  margin: 0 0 8px;
  color: #1f2937;
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: #6b7280;
  font-size: 13px;
  margin: 0;
  letter-spacing: 1px;
}

.login-form {
  margin-top: 24px;
}

.login-form .el-input {
  height: 48px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  background: #f8fafc;
  box-shadow: 0 0 0 1px #e5e7eb;
  transition: all 0.3s ease;
}

.login-input :deep(.el-input__wrapper:hover) {
  background: #fff;
  box-shadow: 0 0 0 1px #c0c4cc, 0 2px 8px rgba(0, 0, 0, 0.04);
}

.login-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px #667eea, 0 4px 12px rgba(102, 126, 234, 0.2);
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0 0;
  font-size: 13px;
}

.forgot-link {
  color: #667eea;
  cursor: pointer;
  transition: all 0.3s ease;
}

.forgot-link:hover {
  color: #764ba2;
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.5);
}

.login-btn:active {
  transform: translateY(0);
}

.card-footer {
  margin-top: 24px;
}

.tip-alert {
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #e0e7ff 0%, #f3e8ff 100%);
}

.tip-alert :deep(.el-alert__content) {
  color: #6366f1;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>