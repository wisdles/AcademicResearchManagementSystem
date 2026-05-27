<template>
   
  <el-card class="achievement-card">
    <div class="form-header">
      <h2 class="form-title">{{ typeLabel }}申报</h2>
      <el-tag v-if="form.id" type="warning" effect="dark">
        正在编辑 (ID: {{ form.id }})
      </el-tag>
    </div>

    <!-- === 修改：ref 名称统一为 formRef（脚本中定义） === -->
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
          <!-- === 修改：on-success 与 @change 写法，确保传入 field.key === -->
          <el-upload
            class="upload-area"
            drag
            action="http://localhost:8080/file/upload"
            name="file"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="(res,file,fileList) => handleUpload(res, file, fileList, field.key)"
           
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
      <!-- === 新增：OCR 识别内容编辑区 开始 === -->
<!-- === 新增：简洁 OCR 文本区域（与备注栏样式一致） === -->
<el-form-item label="OCR 识别内容" prop="ocrText">
  <el-input
    type="textarea"
    v-model="ocrText"
    :rows="3"
    placeholder="OCR 识别的原始文本会显示在这里，识别不完整可手动编辑后复制到对应字段"
  />
</el-form-item>
<!-- === 新增结束 === -->

<!-- === 新增：OCR 识别内容编辑区 结束 === -->

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
/* ============================
   基础依赖与已有逻辑（保留/略微调整）
   ============================ */
import { reactive, computed, ref, watch, onBeforeUnmount  } from "vue"
import { FORM_FIELDS } from "@/data/achievement-fields.js"
import { ElMessage } from "element-plus"
import { UploadFilled, DocumentChecked, Download } from '@element-plus/icons-vue'
import request from "@/utils/request.js"
// === 新增：OCR 文本编辑与操作相关状态 ===

const ocrText = ref('') 
const isOcrLoading = ref(false)
const formRef = ref(null)
const loading = ref(false)
const form = reactive({})
const props = defineProps({ 
  type: String,
  initialData: Object
})
const emit = defineEmits(['success'])





// === 新增：解析 ocrText 并尝试填充表单字段 ===
const applyOcrToForm = () => {
  try {
    const text = ocrText.value || ''
    if (!text.trim()) {
      ElMessage.warning('OCR 文本为空，请先粘贴或识别文本')
      return
    }
    // 使用已有的 mapOcrToFields 函数解析
    const mapped = mapOcrToFields(props.type, text)
    // 只覆盖非空字段
    Object.keys(mapped).forEach(k => {
      if (mapped[k]) {
        form[k] = mapped[k]
      }
    })
    ElMessage.success('已尝试解析并填充表单，请核对')
  } catch (e) {
    console.error('applyOcrToForm 错误', e)
    ElMessage.error('解析失败，请检查 OCR 文本格式')
  }
}

// === 新增：复制 OCR 文本到剪贴板 ===
const copyOcrText = async () => {
  try {
    await navigator.clipboard.writeText(ocrText.value || '')
    ElMessage.success('已复制到剪贴板')
  } catch (e) {
    console.error('复制失败', e)
    ElMessage.error('复制失败，请手动复制')
  }
}

// === 新增：清空 OCR 文本 ===
const clearOcrText = () => {
  ocrText.value = ''
  ElMessage.info('已清空 OCR 文本')
}


/* 动态字段与标题 */
const fields = computed(() => FORM_FIELDS[props.type] || [])
const typeLabel = computed(() => {
  const map = { paper: "论文", project: "项目", software: "软著", patent: "专利", book: "专著", award: "获奖", competition: "竞赛", course: "课程" }
  return map[props.type] || "成果"
})

/* 上传头 */
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token');
  return {
    Authorization: token ? (token.startsWith('Bearer') ? token : `Bearer ${token}`) : ''
  }
})

/* 重置表单（保持原逻辑） */
const resetForm = () => {
  if(formRef.value) formRef.value.resetFields()
  Object.keys(form).forEach(key => delete form[key])
}

/* 后端实体到前端字段映射（保留并兼容） */
const mapEntityToForm = (type, entity) => {
  if (!entity) return {}
  const data = { ...entity }
  data.classification = entity.classification 
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
    data.grantDate = entity.grantDate || entity.developDate
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
  else if (type === 'award') {
    data.awardName = entity.awardName
    data.awardDate = entity.awardDate
    data.proofFile = entity.proofFile
  }
  else if (type === 'competition') {
    data.name = entity.name
    data.awardDate = entity.awardDate
    data.certFileUrl = entity.certFileUrl
  }
  else if (type === 'course') {
    data.courseName = entity.courseName
    data.startDate = entity.startDate
    data.proofFile = entity.proofFile
  }
  return data
}

/* 监听 initialData 回显 */
watch(() => props.initialData, (newVal) => {
  if (newVal) {
    console.log('接收到编辑数据(Raw):', newVal)
    Object.keys(form).forEach(key => delete form[key])
    const mappedData = mapEntityToForm(props.type, newVal)
    console.log('映射后数据(Mapped):', mappedData)
    Object.assign(form, mappedData)
  } else {
    resetForm()
  }
}, { immediate: true })

/* 文件名与下载（保留） */
const getFileName = (url) => {
  if(!url) return ''
  return url.substring(url.lastIndexOf('/') + 1)
}
const downloadFile = (url) => {
  if (!url) return
  const fullUrl = `http://localhost:8080${url}`
  window.open(fullUrl, '_blank')
}

/* 原有上传成功处理（已修改签名以兼容模板传参） */
/* 修改上传成功的回调 */
const handleUpload = async (res, file, fileList, fieldKey) => {
  try {
    // 1. 常规逻辑：处理文件保存后的 URL 回显
    if (res && res.code === 200) {
      if (fieldKey) form[fieldKey] = res.data // 这里保存的是 /files/xxx.pdf
      ElMessage.success('文件上传成功')
    } else {
      ElMessage.error(res?.msg || '上传失败')
      return // 如果上传都失败了，就别做 OCR 了
    }

    // 2. === 新增逻辑：智能识别填充 ===
    // 只有当上传的是"证明文件"（通常包含核心信息）时，才触发 AI 识别
    // 你的字段里 proofFile, openFileUrl, closeFileUrl 都是 upload 类型
    // 我们假设 proofFile 是主要的识别源，或者只要是 upload 都试一下
    const isProofFile = ['proofFile', 'openFileUrl', 'closeFileUrl'].includes(fieldKey)
    
    if (isProofFile) {
      // 获取用户刚刚上传的那个原始文件对象 (raw file)
      // 注意：element-plus 的 onSuccess 回调里，第二个参数 file 包含了 raw
      const rawFile = file.raw 
      if (!rawFile) return

      // 显示识别中的提示
      const loadingInstance = ElMessage.info({
        message: '正在利用 AI 智能提取文件信息，请稍候...',
        duration: 0 // 不自动关闭
      })

      // 构造 OCR 请求
      const formData = new FormData()
      formData.append('file', rawFile)
      formData.append('type', props.type) // 告诉后端这是 'software' 还是 'paper'

      try {
        // 调用我们写的 OCR 接口
        const aiRes = await request.post('/ocr/predict', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
          // === 🔍 调试代码：请务必加上这一行 ===
        console.log('后端返回的完整数据结构:', aiRes) 
        // ====================================
        if (aiRes && aiRes.code === 200) {
            // 2. === 关键修复：解析 JSON 字符串 ===
          let aiData = aiRes.data
           // 如果后端返回的是字符串，需要 parse 一下转成对象
          if (typeof aiData === 'string') {
            try {
              aiData = JSON.parse(aiData)
            } catch (err) {
              console.error('JSON解析失败', err)
              ElMessage.error('AI 返回的数据格式有误')
              loadingInstance.close()
              return
            }
          }
          console.log('最终提取的对象:', aiData)
         // 3. 自动填充
          let fillCount = 0
          Object.keys(aiData).forEach(key => {
            // 排除不需要覆盖的字段
            if (['proofFile', 'openFileUrl', 'closeFileUrl', 'remark', 'classification'].includes(key)) return

            // 只有有值才填充
            if (aiData[key]) {
               form[key] = aiData[key]
               fillCount++
            }
          })
          
          loadingInstance.close()
          if (fillCount > 0) {
             ElMessage.success(`AI 已填充 ${fillCount} 项信息`)
          } else {
             ElMessage.warning('AI 识别成功，但未匹配到有效字段')
          }
        } else {
          loadingInstance.close()
          ElMessage.error(aiRes?.msg || 'AI 识别失败，请手动填写')
        }
      } catch (ocrError) {
        loadingInstance.close()
        console.error('OCR 请求失败', ocrError)
        // OCR 失败不影响文件上传成功的状态，所以只打日志或者轻微提示
        ElMessage.warning('文件上传成功，但 AI 识别超时或失败，请手动填写。')
      }
    }

  } catch (e) {
    console.error('handleUpload 错误', e)
  }
}

/* 提交表单（增加校验示例） */
const submitForm = async (isSubmit) => {
  if (!formRef.value) {
    ElMessage.error('表单未就绪')
    return
  }
  try {
    await formRef.value.validate()
  } catch (err) {
    ElMessage.warning('请先修正表单校验错误')
    return
  }

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
    ElMessage.error('提交失败')
  } finally {
    loading.value = false
  }
}

/* ============================
   === 新增 OCR 相关代码 START ===
   兼容 tesseract.js@6.x 的实现（动态导入 + worker/direct 回退）
   ============================ */











/* ============================
   === 简单的按类型抽取器（示例） ===
   说明：你可以基于样本不断扩充正则与上下文解析
   ============================ */
function mapOcrToFields(type, text) {
  const t = (text || '').replace(/\r/g, '\n')
  const res = {}

  const findFirst = (regex) => {
    const m = t.match(regex)
    return m ? (m[1] || '').trim() : ''
  }

  const findDate = (txt) => {
    const m1 = txt.match(/(\d{4}[-\/\.年]\d{1,2}[-\/\.月]\d{1,2}日?)/)
    const m2 = txt.match(/(\d{4}[-\/\.]\d{1,2})/)
    const m = m1 || m2
    if (!m) return ''
    return m[1].replace(/[年月\.]/g, '-').replace('日','')
  }

  if (type === 'paper') {
    res.title = findFirst(/论文题目[:：\s]*([^\n]{5,200})/i) || findFirst(/Title[:：\s]*([^\n]{5,200})/i) || ''
    res.journalName = findFirst(/刊名[:：\s]*([^\n]{2,100})/i) || findFirst(/Journal[:：\s]*([^\n]{2,100})/i) || ''
    res.issn = findFirst(/ISSN[:：\s]*([0-9Xx\-]{4,20})/i) || ''
    res.impactFactor = findFirst(/影响因子[:：\s]*([0-9\.]+)/) || ''
    res.pages = findFirst(/页码[:：\s]*([0-9\-–—]{1,20})/) || findFirst(/Pages[:：\s]*([0-9\-–—]{1,20})/) || ''
    res.publishDate = findDate(t)
    res.authors = findFirst(/第一作者[:：\s]*([^\n]{2,80})/) || findFirst(/作者[:：\s]*([^\n]{2,80})/) || ''
    res.correspondingAuthor = findFirst(/通讯作者[:：\s]*([^\n]{2,80})/) || ''
  }
  else if (type === 'patent') {
    res.name = findFirst(/专利名称[:：\s]*([^\n]{2,200})/) || findFirst(/Patent Title[:：\s]*([^\n]{2,200})/) || ''
    res.patentNo = findFirst(/专利号[:：\s]*([A-Za-z0-9\-]{5,50})/) || findFirst(/Patent No[:：\s]*([A-Za-z0-9\-]{5,50})/) || ''
    res.patentType = findFirst(/专利类型[:：\s]*(发明专利|实用新型|外观设计)/) || ''
    res.applyDate = findDate(t)
    res.grantDate = findDate(t)
    res.owner = findFirst(/专利权人[:：\s]*([^\n]{2,100})/) || ''
  }
  else if (type === 'software') {
    res.name = findFirst(/软件名称[:：\s]*([^\n]{2,200})/) || ''
    res.registerNo = findFirst(/登记号[:：\s]*([A-Za-z0-9\-]{3,50})/) || ''
    res.grantDate = findDate(t)
    res.authors = findFirst(/作者[:：\s]*([^\n]{2,100})/) || ''
  }
  else if (type === 'book') {
    res.name = findFirst(/书名[:：\s]*([^\n]{2,200})/) || ''
    res.isbn = findFirst(/ISBN[:：\s]*([0-9\-Xx]{8,20})/) || ''
    res.publisher = findFirst(/出版社[:：\s]*([^\n]{2,100})/) || ''
    res.publishDate = findDate(t)
    res.authors = findFirst(/作者[:：\s]*([^\n]{2,100})/) || ''
  }

  return res
}
</script>

<style scoped>
.achievement-card { padding: 16px; }
.form-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px; }
.form-title { margin:0; }
.upload-wrapper { display:flex; gap:12px; align-items:center; }
.file-success-box { display:flex; align-items:center; gap:12px; }
.file-name { font-weight:500; }
.download-area { margin-top:8px; }
.form-actions { text-align:right; }
</style>
