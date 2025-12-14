<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        
        <!-- 标签页1: 科研档案 (仅教师可见, 管理员也能看但不需要填) -->
        <el-tab-pane label="科研档案" name="resume" v-if="role === 'TEACHER'">
          <el-form :model="resumeForm" label-width="100px" style="max-width: 600px; margin-top: 20px">
            <el-form-item label="职称">
              <el-select v-model="resumeForm.titleLevel" placeholder="请选择职称">
                <el-option label="教授" value="教授" />
                <el-option label="副教授" value="副教授" />
                <el-option label="讲师" value="讲师" />
                <el-option label="助教" value="助教" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="最高学历">
              <el-select v-model="resumeForm.education" placeholder="请选择学历">
                <el-option label="博士研究生" value="博士" />
                <el-option label="硕士研究生" value="硕士" />
                <el-option label="本科" value="本科" />
              </el-select>
            </el-form-item>

            <el-form-item label="研究方向">
              <el-input v-model="resumeForm.researchDirection" placeholder="例如：人工智能、大数据分析"></el-input>
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input v-model="resumeForm.bio" type="textarea" :rows="5" placeholder="请输入个人简介..."></el-input>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveResume">保存档案</el-button>
            </el-form-item>
         
          </el-form>
        </el-tab-pane>

        <!-- 标签页2: 安全设置 (所有用户可见) -->
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="100px" style="max-width: 500px; margin-top: 20px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password></el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password></el-input>
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="danger" @click="updatePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const role = localStorage.getItem('role')
const activeTab = ref(role === 'TEACHER' ? 'resume' : 'security')

// --- 简历相关 ---
const resumeForm = reactive({
  titleLevel: '',
  education: '',
  researchDirection: '',
  bio: ''
})

const getResume = () => {
  if (role !== 'TEACHER') return
  request.get('/resume/my').then(res => {
    if (res.data) {
      Object.assign(resumeForm, res.data)
    }
  })
}

const saveResume = () => {
  request.post('/resume/save', resumeForm).then(res => {
    ElMessage.success('档案保存成功')
  })
}

// --- 修改密码相关 ---
const pwdRef = ref(null)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }]
}

const updatePassword = () => {
  pwdRef.value.validate(valid => {
    if (valid) {
      request.post('/user/update-password', {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      }).then(res => {
        ElMessage.success('密码修改成功，请重新登录')
        localStorage.clear()
        setTimeout(() => {
            window.location.href = '/login'
        }, 1000)
      })
    }
  })
}

onMounted(() => {
  getResume()
})
</script>