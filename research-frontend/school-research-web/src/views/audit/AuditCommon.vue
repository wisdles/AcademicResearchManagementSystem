<template>
  <div class="audit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span style="font-size:18px; font-weight:bold">待审核{{ config.label }}列表</span>
          <el-button icon="Refresh" circle @click="getList" />
        </div>
      </template>

      <el-table :key="type" :data="tableData" stripe border style="width:100%" v-loading="loading">
        
        <!-- 1. 动态渲染关键业务列 (根据配置文件) -->
        <el-table-column 
          v-for="col in config.columns" 
          :key="col.prop"
          :prop="col.prop" 
          :label="col.label" 
          :min-width="col.minWidth"
          :width="col.width"
          show-overflow-tooltip
        />

        <!-- 2. 申报人 (通用) -->
        <el-table-column prop="applicantName" label="申报人" width="100" />
          
        <!-- 3. 类别 (兼容 category 和 classification) -->
        <el-table-column label="归属" width="100">
          <template #default="{ row }">
             <!-- 优先显示 classification，没有则显示 category -->
             <el-tag :type="getCategoryType(row)">
               {{ row.classification || row.category || '未分类' }}
             </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申报时间" width="170" />
        
        <!-- 4. 申报材料下载 (动态获取文件字段) -->
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

// 1. 获取当前页面类型 (从路由参数获取 type)
const type = computed(() => route.meta.type || 'project')

// 2. 根据类型获取配置
const config = computed(() => AUDIT_CONFIG[type.value] || AUDIT_CONFIG.project)

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)

const auditForm = reactive({
  targetId: null, // 临时存ID
  isPass: true,
  comment: ''
})

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    // 动态请求: /project/audit-list, /paper/audit-list ...
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
  // 从配置中读取该类型对应的文件字段名 (appFileUrl 或 proofFile)
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

// 标签颜色逻辑
const getCategoryType = (row) => {
  const val = row.classification || row.category
  if (val === 'RESEARCH') return '' // 默认蓝色
  if (val === 'TEACHING') return 'success' // 绿色
  return 'info'
}

// 打开审核弹窗
const openAudit = (row) => {
  auditForm.targetId = row.id // 暂存 ID
  auditForm.isPass = true
  auditForm.comment = ''
  dialogVisible.value = true
}

// 提交审核
const submitAudit = () => {
  if (auditForm.isPass === false && !auditForm.comment) {
    return ElMessage.warning('驳回时必须填写审核意见')
  }
  
  // 构造后端需要的 DTO
  // 注意：这里需要确认后端 AuditDto 接收的 ID 字段名是 projectId 还是统一叫 id
  // 根据你之前的代码，似乎都叫 projectId (或者你可以修改后端 DTO 统一叫 id)
  const payload = {
    // 动态设置 ID 的 key，例如 projectId: 123
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

// 监听路由变化自动刷新 (防止同组件切换不刷新)
watch(() => route.path, () => {
  getList()
}, { immediate: true })

</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>