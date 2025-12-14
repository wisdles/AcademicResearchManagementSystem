<template>
  <div class="login-container">
    <div class="login-box">
      <div class="left">
        <h2>高校教师科研管理系统</h2>
        <p>Research Management System</p>
      </div>
      <div class="right">
        <h3>用户登录</h3>
        <el-form :model="form" :rules="rules" ref="loginFormRef" label-width="0">
          <el-form-item prop="username">
            <el-input v-model="form.username" prefix-icon="User" placeholder="请输入工号/账号" size="large"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" prefix-icon="Lock" type="password" placeholder="请输入密码" show-password size="large"></el-input>
          </el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin" size="large">登 录</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})


const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  loginFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      request.post('/auth/login', form).then(res => {
        // 登录成功
        const { token, role, realName, isFirstLogin,userId } = res.data
        
        // 1. 存储信息
        localStorage.setItem('token', token)
        localStorage.setItem('role', role)
        localStorage.setItem('realName', realName)
        localStorage.setItem('userId',userId) // 存储 userId
        ElMessage.success(`欢迎回来，${realName}`)

        // 2. 如果是首次登录，跳转改密 (这里先不处理，直接跳主页)
        if (isFirstLogin) {
           ElMessage.warning('检测到您是首次登录，建议尽快修改密码')
        }
        
        // 3. 跳转主页
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
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #1c92d2, #f2fcfe);
}
.login-box {
  width: 800px;
  height: 400px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  display: flex;
  overflow: hidden;
}
.left {
  flex: 1;
  background: #409EFF;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.right {
  flex: 1;
  padding: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.login-btn { width: 100%; margin-top: 20px; }
</style>