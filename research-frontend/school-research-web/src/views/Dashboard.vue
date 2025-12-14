<template>
  <div class="dashboard-container">
    
    <!-- 1. 顶部动态通知条 -->
    <div class="notice-bar" v-if="latestNotice">
      <div class="notice-icon">
        <el-icon :size="20"><BellFilled /></el-icon> <span style="margin-left:5px">最新公告：</span>
      </div>
      <div class="scroll-wrapper" @click="viewDetail(latestNotice)">
        <div class="scroll-content">
          <span class="scroll-date">[{{ formatDate(latestNotice.createTime) }}]</span>
          <span class="scroll-title">{{ latestNotice.title }}</span>
        </div>
      </div>
    </div>

    <el-row :gutter="20" style="margin-top: 25px;">
      <!-- 左侧：公告列表 -->
      <el-col :span="16">
        <el-card class="box-card notice-card">
          <template #header>
            <div class="card-header">
              <span class="header-title">
                <el-icon :size="20"><DataBoard /></el-icon> 通知公告栏
              </span>
              <el-button v-if="isSecretary" type="primary" size="default" icon="Plus" @click="showPublishDialog">
                发布通知
              </el-button>
            </div>
          </template>
          
          <ul class="notice-list">
            <li v-for="item in noticeList" :key="item.id" class="notice-item" @click="viewDetail(item)">
              <div class="item-left">
                <!-- 🔴 修改点 1：字段由 classification 改为 classification -->
                <el-tag :type="getclassificationType(item.classification)" effect="dark" class="classification-tag">
                  {{ getclassificationText(item.classification) }}
                </el-tag>
                
                <span class="item-title">{{ item.title }}</span>
                <span v-if="isNew(item.createTime)" class="new-icon">NEW</span>
              </div>
              <div class="item-right">
                <span class="item-publisher">{{ item.publisherName }}</span>
                <span class="item-date">{{ formatDate(item.createTime) }}</span>

                <div class="action-btns" v-if="item.publisherId === currentUserId || role === 'ADMIN'">
                  <el-button type="primary" link icon="Edit" @click.stop="handleEdit(item)"></el-button>
                  <el-button type="danger" link icon="Delete" @click.stop="handleDelete(item)"></el-button>
                </div>
              </div>
            </li>
            <div v-if="noticeList.length === 0" class="empty-text">暂无通知公告</div>
          </ul>
        </el-card>
      </el-col>

      <!-- 右侧：工作日历 -->
      <el-col :span="8">
        <el-card class="box-card quick-card">
          <template #header>
            <div class="card-header">
              <span class="header-title"><el-icon :size="20"><Calendar /></el-icon> 工作日历</span>
            </div>
          </template>
          <el-calendar v-model="currentDate" class="mini-calendar" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 发布弹窗 -->
    <el-dialog v-model="publishVisible" title="发布新通知" width="600px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>
        
        <!-- 🔴 修改点 2：绑定 classification -->
        <el-form-item label="分类">
          <el-radio-group v-model="form.classification">
            <el-radio-button label="ALL">全校</el-radio-button>
            <el-radio-button label="TEACHING">教学</el-radio-button>
            <el-radio-button label="RESEARCH">科研</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入正文..." />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            class="upload-demo"
            action="http://localhost:8080/file/upload"
            :headers="headers"
            :limit="1"
            :on-success="handleUploadSuccess"
            :file-list="fileList"
          >
            <el-button type="primary" icon="Upload">点击上传 Word/PDF</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPublish">立即发布</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" width="70%" top="5vh">
      <template #header>
        <div class="detail-header">
          <h2>{{ currentNotice.title }}</h2>
          <div class="detail-meta">
            <span>发布部门：{{ currentNotice.publisherName }}</span>
            <span>发布时间：{{ currentNotice.createTime?.replace('T', ' ') }}</span>
            <!-- 🔴 修改点 3：显示 classification -->
            <el-tag size="small">{{ getclassificationText(currentNotice.classification) }}</el-tag>
          </div>
        </div>
      </template>

      <div class="detail-body">
        <div class="text-content">{{ currentNotice.content }}</div>
        
        <div v-if="currentNotice.attachmentUrl" class="attachment-box">
          <div class="att-title"><el-icon><Paperclip /></el-icon> 附件下载</div>
          <div class="att-link">
            <a :href="'http://localhost:8080' + currentNotice.attachmentUrl" target="_blank" class="download-link">
              {{ currentNotice.attachmentName || '点击下载文档' }}
            </a>
            <el-button type="warning" link size="default" @click="previewAttachment(currentNotice.id)" style="margin-left: 15px;">
              <el-icon><View /></el-icon> 在线转文本预览
            </el-button>
          </div>
          <div v-if="previewText" class="doc-preview">
            <pre>{{ previewText }}</pre>
          </div>
        </div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const role = localStorage.getItem('role')
const isSecretary = computed(() => role && role.startsWith('SEC_'))
const currentDate = ref(new Date())
const currentUserId = Number(localStorage.getItem('userId') || 0) 

const headers = computed(() => {
  return { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

// 数据
const noticeList = ref([])
const latestNotice = ref(null) 
const fileList = ref([]) 
const isEditMode = ref(false) 
const currentEditId = ref(null)

// 🔴 修改点 4：form 结构中的 classification 改为 classification
const form = reactive({
  title: '', 
  classification: 'ALL', // 默认为全校通知
  content: '', 
  attachmentUrl: '', 
  attachmentName: ''
})

// 点击“发布通知”按钮
const showPublishDialog = () => {
  isEditMode.value = false
  // 重置表单
  form.title = ''; 
  form.content = ''; 
  form.classification = 'ALL'; // 重置为默认
  form.attachmentUrl = ''; 
  form.attachmentName = '';
  fileList.value = []
  publishVisible.value = true
}

// 点击列表里的“编辑”按钮
const handleEdit = (item) => {
  isEditMode.value = true
  currentEditId.value = item.id
  // 回填数据
  form.title = item.title
  form.classification = item.classification // 🔴 回显 classification
  form.content = item.content
  form.attachmentUrl = item.attachmentUrl
  form.attachmentName = item.attachmentName
  
  if (item.attachmentName) {
    fileList.value = [{ name: item.attachmentName, url: item.attachmentUrl }]
  } else {
    fileList.value = []
  }
  
  publishVisible.value = true
}

// 点击“删除”按钮
const handleDelete = (item) => {
  ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
    type: 'warning'
  }).then(() => {
    request.delete(`/notice/delete/${item.id}`).then(res => {
      ElMessage.success('删除成功')
      getList()
    })
  })
}

// 提交表单
const submitPublish = () => {
  if (!form.title) return ElMessage.warning('请输入标题')
  
  if (isEditMode.value) {
    // 修改
    request.post('/notice/update', { id: currentEditId.value, ...form }).then(() => {
      ElMessage.success('修改成功')
      publishVisible.value = false
      getList()
    })
  } else {
    // 新增
    request.post('/notice/add', form).then(() => {
      ElMessage.success('发布成功')
      publishVisible.value = false
      getList()
    })
  }
}

// 获取列表
const getList = () => {
  request.get('/notice/list').then(res => {
    noticeList.value = res.data
    if (res.data.length > 0) {
      latestNotice.value = res.data[0]
    }
  })
}

// 🔴 修改点 5：辅助函数适配 classification
const getclassificationText = (val) => {
  const map = { 'TEACHING': '教学', 'RESEARCH': '科研', 'ALL': '通知' }
  return map[val] || '公告'
}
const getclassificationType = (val) => {
  const map = { 'TEACHING': 'warning', 'RESEARCH': 'success', 'ALL': 'primary' }
  return map[val] || 'info'
}

const formatDate = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.split('T')[0]
}
const isNew = (timeStr) => {
  if (!timeStr) return false
  const date = new Date(timeStr)
  const now = new Date()
  const diffTime = Math.abs(now - date)
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return diffDays <= 7 
}

const publishVisible = ref(false)

const handleUploadSuccess = (res, file) => {
  if (res.code === 200) {
    form.attachmentUrl = res.data
    form.attachmentName = file.name
    ElMessage.success('附件上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// --- 详情逻辑 ---
const detailVisible = ref(false)
const currentNotice = ref({})
const previewText = ref('')

const viewDetail = (row) => {
  currentNotice.value = row
  previewText.value = ''
  detailVisible.value = true
}

const previewAttachment = (id) => {
  previewText.value = '正在解析文档内容，请稍候...'
  request.get(`/notice/preview/${id}`).then(res => {
    previewText.value = res.data
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.dashboard-container { padding: 0 10px; }

/* 1. 顶部滚动通知条 */
.notice-bar {
  display: flex;
  align-items: center;
  background: #fff;
  height: 50px;
  border-radius: 6px;
  padding: 0 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.08);
  margin-bottom: 15px;
  border-left: 5px solid #409EFF;
}
.notice-icon {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  display: flex;
  align-items: center;
  margin-right: 15px;
  white-space: nowrap;
}
.scroll-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
  height: 50px;
  cursor: pointer;
}
.scroll-content {
  position: absolute;
  top: 0;
  height: 50px;
  line-height: 50px;
  white-space: nowrap;
  animation: scrollText 20s linear infinite;
  color: #333;
}
.scroll-date { 
  font-size: 18px; 
  color: #409EFF; 
  font-weight: bold; 
  margin-right: 10px; 
}
.scroll-title {
  font-size: 18px; 
  font-weight: bold;
}
.scroll-content:hover {
  animation-play-state: paused;
  color: #409EFF;
}

/* 2. 列表样式 */
.notice-card { min-height: 550px; }
.header-title { font-size: 20px; font-weight: bold; display: flex; align-items: center; gap: 8px; }

.notice-list { list-style: none; padding: 0; margin: 0; }
.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 10px;
  border-bottom: 1px dashed #e0e0e0;
  cursor: pointer;
  transition: all 0.3s;
}
.notice-item:hover {
  background-color: #f5f7fa;
  transform: translateX(8px);
  border-left: 4px solid #409EFF;
  padding-left: 15px;
}
.item-left { display: flex; align-items: center; gap: 12px; overflow: hidden; }

.classification-tag { font-size: 14px; width: 50px; text-align: center; }
.item-title { 
  font-size: 18px;
  color: #2c3e50; 
  font-weight: 500;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 450px; 
}
.new-icon { 
  background: #ff4d4f; color: white; font-size: 12px; padding: 1px 6px; border-radius: 4px; 
  font-weight: bold; animation: blink 2s infinite; 
}

.item-right { 
  font-size: 15px;
  color: #888; display: flex; gap: 20px; white-space: nowrap; align-items: center;
}
.action-btns { opacity: 0; transition: opacity 0.3s; margin-left: 10px; background: #fff; border: 1px solid #eee; padding: 0 5px; border-radius: 4px; }
.notice-item:hover .action-btns { opacity: 1; }

/* 3. 详情页样式 */
.detail-header h2 { font-size: 24px; margin: 10px 0; }
.detail-meta { font-size: 14px; }
.text-content { font-size: 16px; line-height: 1.8; color: #333; }
.att-title { font-size: 16px; }

@keyframes scrollText {
  0% { transform: translateX(100%); }
  100% { transform: translateX(-100%); }
}
@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.6; } 100% { opacity: 1; } }

.quick-card :deep(.el-calendar-table .el-calendar-day) { height: 45px; font-size: 16px; }
</style>