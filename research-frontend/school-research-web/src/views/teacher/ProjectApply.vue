<template>
  <div class="apply-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">项目申报</span>
          <span class="tips">请如实填写项目信息，带 * 为必填项</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" class="apply-form">
        
        <!-- 基础信息 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入项目全称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目类别" prop="category">
              <el-select v-model="form.category" placeholder="请选择类别" style="width: 100%">
                <el-option label="科研项目" value="RESEARCH" />
                <el-option label="教学项目" value="TEACHING" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="项目级别" prop="level">
              <el-select v-model="form.level" placeholder="请选择级别" style="width: 100%">
                <el-option label="国家级" value="NATIONAL" />
                <el-option label="省部级" value="PROVINCIAL" />
                <el-option label="市厅级" value="CITY" />
                <el-option label="校级" value="SCHOOL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="子类型" prop="subType">
              <el-input v-model="form.subType" placeholder="例如：纵向、横向、教改重点" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经费预算" prop="funds">
              <el-input-number v-model="form.funds" :min="0" :precision="2" :step="0.1" style="width: 100%" />
              <span class="unit">万元</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="起止时间" prop="dateRange">
              <el-date-picker
                v-model="form.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结项日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 附件上传 -->
        <el-divider content-position="left">附件材料</el-divider>
        
        <el-form-item label="申报书" prop="appFileUrl">
          <el-upload
            action="http://localhost:8080/file/upload"
            :headers="headers"
            :limit="1"
            :on-success="(res) => handleUploadSuccess(res, 'appFileUrl')"
            :file-list="appFileList"
          >
            <el-button type="primary" icon="Upload">点击上传申报书 (PDF/Word)</el-button>
            <template #tip>
            <div v-if="form.appFileUrl">
            <el-button type="success" @click="downloadFile(form.appFileUrl)">下载申报书</el-button>
            </div>
        </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="合同/协议">
          <el-upload
            action="http://localhost:8080/file/upload"
            :headers="headers"
            :limit="1"
            :on-success="(res) => handleUploadSuccess(res, 'contractFileUrl')"
            :file-list="contractFileList"
          >
            <el-button icon="Upload">点击上传合同 (选填)</el-button>
            <template #tip>
              <div v-if="form.contractFileUrl">
                <el-button type="success" @click="downloadFile(form.contractFileUrl)">下载申报书</el-button>
              </div></template>
          </el-upload>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" size="large" @click="submitForm(true)">立即提交审核</el-button>
          <el-button size="large" @click="submitForm(false)">保存为草稿</el-button>
        </el-form-item>

      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { onMounted } from 'vue'

const router = useRouter()
const route = useRoute()
const projectId = route.query.id
const formRef = ref(null)
const headers = computed(() => ({ Authorization: `Bearer ${localStorage.getItem('token')}` }))

const appFileList = ref([])
const contractFileList = ref([])
onMounted(() => {
   console.log('项目ID:', projectId)
  if (projectId) {
    request.get(`/project/detail/${projectId}`).then(res => {
        console.log(res)
      if (res.code === 200) {
        // 把返回的数据覆盖到 form
        const project = res.data   // 如果后端返回单个对象
        form.id = project.id
        form.name = project.name
        form.category = project.category
        form.level = project.level
        form.subType = project.subType
        form.funds = project.funds
        form.dateRange = [project.startDate, project.endDate]
        form.appFileUrl = project.appFileUrl
        form.contractFileUrl = project.contractFileUrl
        // 初始化 file-list
        if (project.appFileUrl) {
          appFileList.value = [{ name: '申报书', url: project.appFileUrl }]
        }
        if (project.contractFileUrl) {
          contractFileList.value = [{ name: '合同', url: project.contractFileUrl }]
        }
      }
    })
  }
})
const downloadFile = (url) => {
  if (url) {
    window.open(`http://localhost:8080${url}`, '_blank')
  } else {
    ElMessage.warning('暂无文件可下载')
  }
}
const form = reactive({
  id: null,    
  name: '',
  category: '',
  level: '',
  subType: '',
  funds: 0,
  dateRange: [],
  appFileUrl: '',
  contractFileUrl: ''
})

const rules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择项目类别', trigger: 'change' }],
  level: [{ required: true, message: '请选择项目级别', trigger: 'change' }],
  funds: [{ required: true, message: '请输入经费预算', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择起止时间', trigger: 'change' }],
  appFileUrl: [{ required: true, message: '请上传申报书', trigger: 'change' }]
}

// 通用上传回调
const handleUploadSuccess = (res, field) => {
  if (res.code === 200) {
    form[field] = res.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

const submitForm = (isSubmit) => {
  formRef.value.validate((valid) => {
    if (valid) {
      const payload = {
        ...form,
        startDate: form.dateRange[0],
        endDate: form.dateRange[1],
        isSubmit: isSubmit
      }
      console.log(payload)

      
      const actionText = isSubmit ? '提交审核' : '保存草稿'
      
      ElMessageBox.confirm(`确认${actionText}吗？`, '提示', { type: 'warning' }).then(() => {
        request.post('/project/add', payload).then(() => {
          ElMessage.success(`${actionText}成功`)
          router.push('/project/my') // 跳转到列表页
        })
      })
    }
  })
}
</script>

<style scoped>
.apply-container { max-width: 1000px; margin: 0 auto; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 20px; font-weight: bold; }
.tips { font-size: 14px; color: #999; }
.unit { margin-left: 10px; color: #666; }
</style>