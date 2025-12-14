<template>
  <el-card class="achievement-card">
    <div class="form-header">
      <h2 class="form-title">{{ typeLabel }}申报</h2>
      <el-tag v-if="form.id" type="warning" effect="dark">
        正在编辑 (ID: {{ form.id }})
      </el-tag>
    </div>

    <el-form ref="formRef" :model="form" label-width="120px" class="achievement-form">
      
      <el-form-item 
        v-for="field in fields" 
        :key="field.key"
        :label="field.label" 
        :prop="field.key" 
        :required="field.required"
      >
          
        <!-- 输入框 -->
        <el-input 
          v-if="field.type==='input'" 
          v-model="form[field.key]" 
          :placeholder="'请输入' + field.label"
          clearable 
        />
        
        <!-- 数字框 -->
        <el-input-number 
          v-if="field.type==='number'" 
          v-model="form[field.key]" 
          :precision="2" 
          style="width: 100%" 
        />

        <!-- 多行文本 -->
        <el-input 
          v-if="field.type==='textarea'" 
          type="textarea" 
          v-model="form[field.key]" 
          :rows="3" 
        />
        
        <!-- 下拉选择 -->
        <el-select 
          v-if="field.type==='select'" 
          v-model="form[field.key]" 
          placeholder="请选择"
          style="width: 100%"
        >
          <el-option 
            v-for="opt in field.options" 
            :key="opt.value || opt" 
            :label="opt.label || opt" 
            :value="opt.value || opt" 
          />
        </el-select>
        
        <!-- 日期选择 -->
        <el-date-picker 
          v-if="field.type==='date'" 
          v-model="form[field.key]" 
          type="date" 
          value-format="YYYY-MM-DD"
          placeholder="选择日期" 
          style="width: 100%"
        />
        
        <!-- 文件上传 -->
        <div v-if="field.type==='upload'" class="upload-wrapper">
          <el-upload
            class="upload-area"
            drag
            action="http://localhost:8080/file/upload"
            name="file"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="(res)=>handleUpload(res, field.key)"
          >
            <!-- 未上传 -->
            <div v-if="!form[field.key]">
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">拖拽或点击上传 {{ field.label }}</div>
            </div>
            
            <!-- 已上传 -->
            <div v-else class="file-success-box">
                <el-icon size="40" color="#67C23A"><DocumentChecked /></el-icon>
                <div class="file-name">{{ getFileName(form[field.key]) }}</div>
                <div class="re-upload-tip">点击此处可替换文件</div>
            </div>
          </el-upload>

          <!-- 下载按钮 -->
          <div v-if="form[field.key]" class="download-area">
              <el-button 
                type="success" 
                size="small" 
                icon="Download"
                @click="downloadFile(form[field.key])"
              >
                下载 / 预览文件
              </el-button>
          </div>
        </div>
      </el-form-item>

      <!-- 按钮区 -->
      <el-form-item class="form-actions">
        <el-button type="primary" @click="submitForm(true)" :loading="loading">正式提交</el-button>
        <el-button type="warning" @click="submitForm(false)" :loading="loading">保存草稿</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, computed, ref, watch } from "vue"
import { FORM_FIELDS } from "@/data/achievement-fields.js"
import { ElMessage } from "element-plus"
import { UploadFilled, DocumentChecked, Download } from '@element-plus/icons-vue'
import request from "@/utils/request.js"

// 接收参数
const props = defineProps({ 
  type: String,
  initialData: Object // 列表传过来的行数据（后端实体结构）
})

const emit = defineEmits(['success'])
const formRef = ref(null)
const loading = ref(false)
const form = reactive({})

// 动态获取字段
const fields = computed(() => FORM_FIELDS[props.type] || [])

// 标题
const typeLabel = computed(() => {
  const map = { paper: "论文", project: "项目", software: "软著", patent: "专利", book: "专著" }
  return map[props.type] || "成果"
})

// 请求头
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token');
  return {
    Authorization: token ? (token.startsWith('Bearer') ? token : `Bearer ${token}`) : ''
  }
})


// 重置
const resetForm = () => {
  if(formRef.value) formRef.value.resetFields()
  Object.keys(form).forEach(key => delete form[key])
}
// 🟢 核心修复：数据适配器（将后端的驼峰字段 映射到 前端的下划线字段）
const mapEntityToForm = (type, entity) => {
  if (!entity) return {}
  
  // 1. 先把所有字段浅拷贝（为了获取 id, status, remark 等通用字段）
  const data = { ...entity }
 // 2. 统一处理 classification (无论后端叫 category 还是 classification)
  data.classification = entity.classification 

  // 3 针对不同类型，做特殊字段映射
 
  // 逻辑：前端字段名 = 后端字段名 || 备选后端字段名
  if (type === 'patent') {
    data.name = entity.name || entity.patentName
    data.patentNumber = entity.patentNo || entity.patentNumber
    data.patentType = entity.type || entity.patentType
    data.applyDate = entity.applyDate
    data.grantDate = entity.grantDate
    data.proofFile = entity.fileUrl || entity.proofFile
  } 
  else if (type === 'project') {
    data.name = entity.name || entity.projectName
    data.projectSource = entity.projectSource
    data.projectLevel = entity.level || entity.projectLevel
    data.projectNumber = entity.projectNumber
    data.startDate = entity.startDate
    data.endDate = entity.endDate
    // 兼容不同的附件字段名
    data.openFileUrl =  entity.openFileUrl
    data.closeFileUrl = entity.closeFileUrl 
    
  }
  else if (type === 'paper') {
    data.journalName = entity.journalName
    data.impactFactor = entity.impactFactor
    data.publishDate = entity.publishDate
    data.correspondingAuthor = entity.correspondingAuthor
    data.proofFile = entity.fileUrl || entity.proofFile
  }
  else if (type === 'software') {
    data.name = entity.name || entity.softwareName
    data.registerNumber = entity.registerNo || entity.registerNumber
    data.grantDate = entity.grantDate || entity.developDate // 视具体业务兼容
    data.proofFile = entity.fileUrl || entity.proofFile
    data.softwareType = entity.type || entity.softwareType
  }
  else if (type === 'book') {
    data.bookTitle = entity.title || entity.bookTitle
    data.publishDate = entity.publishDate
    data.proofFile = entity.fileUrl || entity.proofFile
    data.bookType = entity.type || entity.bookType
    data.isbn = entity.isbn
  }

  return data
}
// 🟢 监听 initialData 变化（核心回显逻辑）
watch(() => props.initialData, (newVal) => {
  if (newVal) {
    console.log('接收到编辑数据(Raw):', newVal)
    
    // 清空旧数据
    Object.keys(form).forEach(key => delete form[key])
    
    // 进行字段映射
    const mappedData = mapEntityToForm(props.type, newVal)
    console.log('映射后数据(Mapped):', mappedData)

    // 赋值给表单
    Object.assign(form, mappedData)
  } else {
    resetForm()
  }
}, { immediate: true })


// 获取文件名
const getFileName = (url) => {
  if(!url) return ''
  return url.substring(url.lastIndexOf('/') + 1)
}

// 文件上传成功
const handleUpload = (res, key) => {
  if (res.code === 200) {
    form[key] = res.data 
    ElMessage.success("上传成功")
  } else {
    ElMessage.error(res.msg || "上传失败")
  }
}

// 下载
const downloadFile = (url) => {
  if (!url) return
  const fullUrl = `http://localhost:8080${url}`
  window.open(fullUrl, '_blank')
}

// 提交
const submitForm = async (isSubmit) => {
  loading.value = true
  try {
    const payload = { ...form, isSubmit: isSubmit }
    console.log('提交数据(Payload):', payload)
    await request.post(`/${props.type}/add`, payload)
    ElMessage.success(isSubmit ? `${typeLabel.value}已提交审核` : '草稿已保存')
    emit('success')
    if(isSubmit) resetForm()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}


</script>

<style scoped>
.achievement-card { margin: 20px; }
.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
.achievement-form { max-width: 800px; }
.upload-wrapper { width: 100%; }
.file-success-box { padding: 20px 0; text-align: center; color: #67C23A; }
.file-name { margin-top: 10px; font-weight: bold; font-size: 14px; word-break: break-all; }
.re-upload-tip { font-size: 12px; color: #909399; margin-top: 5px; }
.download-area { margin-top: 10px; text-align: center; }
.form-actions { margin-top: 40px; text-align: center; }
.el-icon--upload { font-size: 60px; color: #c0c4cc; }
</style>