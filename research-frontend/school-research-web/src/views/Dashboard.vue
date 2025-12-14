<template>
  <div class="dashboard-container">
    
    <!-- 1. 顶部动态通知条 (改版：字体变大，日期放前面) -->
    <div class="notice-bar" v-if="latestNotice">
      <div class="notice-icon">
        <el-icon :size="20"><BellFilled /></el-icon> <span style="margin-left:5px">最新公告：</span>
      </div>
      <div class="scroll-wrapper" @click="viewDetail(latestNotice)">
        <div class="scroll-content">
          <!-- 改成：[2024-12-08] 标题内容 -->
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
                <!-- 标签稍微变大 -->
                <el-tag :type="getCategoryType(item.category)" effect="dark" class="category-tag">
                  {{ getCategoryText(item.category) }}
                </el-tag>
                
                <!-- 标题变大、加粗 -->
                <span class="item-title">{{ item.title }}</span>
                
                <!-- NEW 图标 -->
                <span v-if="isNew(item.createTime)" class="new-icon">NEW</span>
              </div>
              <div class="item-right">
                <span class="item-publisher">{{ item.publisherName }}</span>
                <span class="item-date">{{ formatDate(item.createTime) }}</span>

                <!-- 操作按钮 -->
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

    <!-- 弹窗部分保持不变，省略代码以节省篇幅，原来的逻辑不用动 -->
    <!-- ... 发布弹窗 ... -->
    <!-- ... 详情弹窗 ... -->
    
    <!-- 为了完整性，我把原来的发布和详情弹窗代码贴在这里，确保你覆盖不会出错 -->
    <!-- 发布弹窗 -->
    <el-dialog v-model="publishVisible" title="发布新通知" width="600px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入通知标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-radio-group v-model="form.category">
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
            <el-tag size="small">{{ getCategoryText(currentNotice.category) }}</el-tag>
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
// 新增：需要获取当前用户的 ID，以便判断哪个通知是“我”发的
// 简单做法：登录时 localStorage 存了 token，我们可以解析 token 或者调 user info 接口
// 这里为了最快实现，我们假设 localStorage 里存了 userId (你需要去 Login.vue 加一下，或者这里调个接口)
// 👇 建议修改 Login.vue 把 userId 存进去。如果没有，暂时用 role 判断能否操作也行，后端会拦截的。
const currentUserId = Number(localStorage.getItem('userId') || 0) 
// 动态 Header (解决 CSRF 403)
const headers = computed(() => {
  return { Authorization: `Bearer ${localStorage.getItem('token')}` }
})

// 数据
const noticeList = ref([])
const latestNotice = ref(null) // 用于顶部滚动
const fileList = ref([]) // 用于发布时回显
//  修改：submitPublish 支持“新增”和“修改”
const isEditMode = ref(false) // 标记是否为编辑模式
const currentEditId = ref(null)
// 点击“发布通知”按钮
const showPublishDialog = () => {
  isEditMode.value = false
  form.title = ''; form.content = ''; form.category = 'ALL'; form.attachmentUrl = ''; form.attachmentName = '';
  fileList.value = []
  publishVisible.value = true
}

// 点击列表里的“编辑”按钮
const handleEdit = (item) => {
  isEditMode.value = true
  currentEditId.value = item.id
  // 回填数据
  form.title = item.title
  form.category = item.category
  form.content = item.content
  form.attachmentUrl = item.attachmentUrl
  form.attachmentName = item.attachmentName
  
  // 回显文件列表
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

// 提交表单（区分新增/修改）
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

// 辅助函数
const getCategoryText = (val) => {
  const map = { 'TEACHING': '教学', 'RESEARCH': '科研', 'ALL': '通知' }
  return map[val] || '公告'
}
const getCategoryType = (val) => {
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
  return diffDays <= 7 // 7天内算新
}

// --- 发布逻辑 ---
const publishVisible = ref(false)
const form = reactive({
  title: '', category: 'ALL', content: '', attachmentUrl: '', attachmentName: ''
})



const handleUploadSuccess = (res, file) => {
  if (res.code === 200) {
    form.attachmentUrl = res.data
    form.attachmentName = file.name
    ElMessage.success('附件上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

// const submitPublish = () => {
//   if (!form.title) return ElMessage.warning('请输入标题')
//   request.post('/notice/add', form).then(() => {
//     ElMessage.success('发布成功')
//     publishVisible.value = false
//     getList()
//   })
// }

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

/* 1. 顶部滚动通知条 - 样式重写 */
.notice-bar {
  display: flex;
  align-items: center;
  background: #fff;
  height: 50px; /* 增高 */
  border-radius: 6px;
  padding: 0 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.08);
  margin-bottom: 15px;
  border-left: 5px solid #409EFF;
}
.notice-icon {
  font-size: 18px; /* 图标文字变大 */
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
  animation: scrollText 20s linear infinite; /* 调慢一点 */
  color: #333;
}
/* 顶部滚动的字体设定 */
.scroll-date { 
  font-size: 18px; 
  color: #409EFF; 
  font-weight: bold; 
  margin-right: 10px; 
}
.scroll-title {
  font-size: 18px; /* 标题变大 */
  font-weight: bold;
}
.scroll-content:hover {
  animation-play-state: paused;
  color: #409EFF;
}

/* 2. 列表样式 - 样式重写 */
.notice-card { min-height: 550px; }
.header-title { font-size: 20px; font-weight: bold; display: flex; align-items: center; gap: 8px; }

.notice-list { list-style: none; padding: 0; margin: 0; }
.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 10px; /* 增加行间距 */
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

/* 列表内字体设定 */
.category-tag { font-size: 14px; width: 50px; text-align: center; }
.item-title { 
  font-size: 18px; /* 列表标题显著变大 */
  color: #2c3e50; 
  font-weight: 500;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 450px; 
}
.new-icon { 
  background: #ff4d4f; color: white; font-size: 12px; padding: 1px 6px; border-radius: 4px; 
  font-weight: bold; animation: blink 2s infinite; 
}

.item-right { 
  font-size: 15px; /* 右侧信息也稍微变大 */
  color: #888; display: flex; gap: 20px; white-space: nowrap; align-items: center;
}
.action-btns { opacity: 0; transition: opacity 0.3s; margin-left: 10px; background: #fff; border: 1px solid #eee; padding: 0 5px; border-radius: 4px; }
.notice-item:hover .action-btns { opacity: 1; }

/* 3. 详情页样式 */
.detail-header h2 { font-size: 24px; margin: 10px 0; } /* 详情标题超大 */
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
