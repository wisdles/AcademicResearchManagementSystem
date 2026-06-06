<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-deco bg-deco-1"></div>
    <div class="bg-deco bg-deco-2"></div>
    <div class="bg-deco bg-deco-3"></div>

    <div class="login-box">
      <!-- 左侧品牌区 -->
      <div class="left">
        <div class="brand">
          <div class="logo-icon">
            <el-icon :size="42"><School /></el-icon>
          </div>
          <h1>高校教师科研管理系统</h1>
          <p class="brand-sub">Research Management System</p>
        </div>
        <div class="features">
          <div class="feature-item">
            <el-icon><CircleCheckFilled /></el-icon>
            <span>智能成果管理 · OCR 自动识别</span>
          </div>
          <div class="feature-item">
            <el-icon><CircleCheckFilled /></el-icon>
            <span>三级审核流程 · 全程留痕可追溯</span>
          </div>
          <div class="feature-item">
            <el-icon><CircleCheckFilled /></el-icon>
            <span>数据可视化看板 · 决策支持分析</span>
          </div>
        </div>
        <div class="copyright">© 2026 Research Management System</div>
      </div>

      <!-- 右侧登录 -->
      <div class="right">
        <div class="login-header">
          <h2>欢迎回来 👋</h2>
          <p>请使用您的账号登录系统</p>
        </div>

        <el-form :model="form" :rules="rules" ref="loginFormRef" label-width="0" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入工号 / 账号" clearable>
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password @keyup.enter="handleLogin">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin" size="large">
            登 录
          </el-button>
        </el-form>

        <div class="login-tip">
          <el-divider>测试账号</el-divider>
          <div class="tip-row">
            <el-tag size="small" type="info">管理员</el-tag>
            <span>admin / abc123</span>
          </div>
          <div class="tip-row">
            <el-tag size="small" type="success">教师</el-tag>
            <span>teacher_new_01 / abc123</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, School, CircleCheckFilled } from '@element-plus/icons-vue'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  loginFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      request.post('/auth/login', form).then(res => {
        const { token, role, realName, isFirstLogin, userId, collegeId } = res.data
        localStorage.setItem('token', token)
        localStorage.setItem('role', role)
        localStorage.setItem('realName', realName)
        localStorage.setItem('userId', userId)
        localStorage.setItem('collegeId', collegeId || '')
        ElMessage.success(`欢迎回来，${realName}`)
        if (isFirstLogin) {
          ElMessage.warning('检测到您是首次登录，建议尽快修改密码')
        }
        router.push('/')
      }).finally(() => {
        loading.value = false
      })
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
  position: relative;
}

/* 浮动装饰球 */
.bg-deco {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.5;
  pointer-events: none;
}
.bg-deco-1 {
  width: 400px; height: 400px;
  background: #5B8DEF;
  top: -100px; left: -100px;
  animation: float 8s ease-in-out infinite;
}
.bg-deco-2 {
  width: 300px; height: 300px;
  background: #6C5CE7;
  bottom: -50px; right: -50px;
  animation: float 10s ease-in-out infinite reverse;
}
.bg-deco-3 {
  width: 200px; height: 200px;
  background: #00C896;
  top: 50%; right: 25%;
  opacity: 0.3;
  animation: float 12s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -30px); }
}

.login-box {
  width: 920px;
  height: 560px;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25);
  display: flex;
  overflow: hidden;
  z-index: 1;
}

/* 左侧 */
.left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #5B8DEF 50%, #6C5CE7 100%);
  color: white;
  display: flex;
  flex-direction: column;
  padding: 50px 40px;
  position: relative;
}
.brand {
  text-align: left;
}
.logo-icon {
  width: 70px;
  height: 70px;
  background: rgba(255,255,255,0.2);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}
.left h1 {
  font-size: 24px;
  margin: 0 0 8px 0;
  font-weight: 700;
  line-height: 1.4;
}
.brand-sub {
  font-size: 13px;
  opacity: 0.85;
  margin: 0;
}
.features {
  margin-top: 60px;
  flex: 1;
}
.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
  font-size: 14px;
  opacity: 0.95;
}
.feature-item .el-icon {
  font-size: 18px;
  color: #00E5A8;
}
.copyright {
  font-size: 12px;
  opacity: 0.6;
}

/* 右侧 */
.right {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.login-header {
  margin-bottom: 32px;
}
.login-header h2 {
  font-size: 26px;
  font-weight: 600;
  color: #1F2937;
  margin: 0 0 8px 0;
}
.login-header p {
  color: #6B7280;
  margin: 0;
  font-size: 14px;
}
.login-btn {
  width: 100%;
  margin-top: 12px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
}
.login-tip {
  margin-top: 30px;
}
.tip-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #6B7280;
}
.tip-row .el-tag {
  width: 60px;
  text-align: center;
}
</style>
