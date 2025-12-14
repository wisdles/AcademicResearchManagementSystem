<template>
  <div class="audit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span style="font-size:18px; font-weight:bold">待审核{{ config.label }}列表</span>
          <el-button icon="Refresh" circle @click="getList" />
        </div>
      </template>

      <!-- 加上 key 强制刷新，border 美化 -->
      <el-table 
        :key="type"
        :data="tableData" 
        stripe 
        border 
        style="width:100%" 
        v-loading="loading"
      >
        
        <!-- 1. 动态渲染关键业务列 -->
        <el-table-column 
          v-for="col in config.columns" 
          :key="col.prop"
          :prop="col.prop" 
          :label="col.label" 
          :min-width="col.minWidth"
          :width="col.width"
          show-overflow-tooltip
        />

        <!-- 2. 申报人 -->
        <el-table-column prop="applicantName" label="申报人" width="100" />
          
        <!-- 3. 归属 (核心修改) -->
        <el-table-column label="归属" width="100" align="center">
          <template #default="{ row }">
             <!-- 使用 getclassificationType 获取颜色，使用 getclassificationText 获取中文 -->
             <el-tag :type="getclassificationType(row)" effect="plain">
               {{ getclassificationText(row) }}
             </el-tag> 
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申报时间" width="170" />
        
        <!-- 4. 申报材料下载 -->
        <el-table-column label="申报材料" width="120" align="center">
           <template #default="{ row }">
             <el-button 
               v-if="getFileUrl(row)" 
               link 
               type="primary" 
               icon="Download"
               @click="downloadFile(row)"
             >
               下载
             </el-button>
             <span v-else style="color:#999">无附件</span>
           </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openAudit(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog v-model="dialogVisible" :title="config.label + '审核'" width="500px">
      <el-form :model="auditForm">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.isPass">
             <el-radio :label="true" border>通过</el-radio>
             <el-radio :label="false" border style="color:red">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="审核意见">
          <el-input 
            v-model="auditForm.comment" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入审核意见（驳回时必填）" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Download, Refresh } from '@element-plus/icons-vue'
import { AUDIT_CONFIG } from '@/data/audit-config.js'

const route = useRoute()

// 1. 获取当前页面类型
const type = computed(() => route.meta.type || 'project')

// 2. 根据类型获取配置
const config = computed(() => AUDIT_CONFIG[type.value] || AUDIT_CONFIG.project)

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)

const auditForm = reactive({
  targetId: null, 
  isPass: true,
  comment: ''
})

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    const res = await request.get(`/${type.value}/audit-list`)
    if (res.code === 200) {
      tableData.value = res.data
    }
  } finally {
    loading.value = false
  }
}

// 获取文件地址
const getFileUrl = (row) => {
  const field = config.value.fileField
  return row[field]
}

// 下载文件
const downloadFile = (row) => {
  const url = getFileUrl(row)
  if (url) {
    window.open('http://localhost:8080' + url, '_blank')
  }
}

// 🟢 核心修改 1：获取中文显示
const getclassificationText = (row) => {
  // 优先取 classification，如果没有则取 classification (兼容旧逻辑)
  const val = row.classification || row.classification
  const map = {
    'RESEARCH': '科研',
    'TEACHING': '教学'
  }
  return map[val] || val || '未分类'
}

// 🟢 核心修改 2：获取标签颜色
const getclassificationType = (row) => {
  const val = row.classification || row.classification
  // 必须判断英文代码，因为数据库存的是英文
  if (val === 'RESEARCH') return '' // 默认蓝色
  if (val === 'TEACHING') return 'success' // 绿色
  return 'info' // 灰色
}

// 打开审核弹窗
const openAudit = (row) => {
  auditForm.targetId = row.id 
  auditForm.isPass = true
  auditForm.comment = ''
  dialogVisible.value = true
}

// 提交审核
const submitAudit = () => {
  if (auditForm.isPass === false && !auditForm.comment) {
    return ElMessage.warning('驳回时必须填写审核意见')
  }
  
  // 动态构造 payload
  const payload = {
    [config.value.idKey]: auditForm.targetId, 
    isPass: auditForm.isPass,
    comment: auditForm.comment
  }
  
  request.post(`/${type.value}/audit`, payload).then(res => {
    ElMessage.success('审核完成')
    dialogVisible.value = false
    getList()
  })
}

// 监听路由变化自动刷新
watch(() => route.path, () => {
  getList()
}, { immediate: true })

</script>

<style scoped>
.audit-container {
  min-height: 500px; /* 防止闪烁 */
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>