<template>
  <div class="audit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span style="font-size:18px; font-weight:bold">待审核项目列表</span>
          <el-button icon="Refresh" circle @click="getList" />
        </div>
      </template>

      <el-table :data="tableData" stripe>
        <el-table-column prop="name" label="项目名称" min-width="200" />
        <el-table-column prop="applicantName" label="申报人" width="120" />
          
        <el-table-column label="类别" width="100">
          <template #default="{ row }">
             <el-tag>{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="funds" label="经费" width="100" />
        <el-table-column prop="createTime" label="申报时间" width="180" />
        
        <el-table-column label="申报材料" width="150">
           <template #default="{ row }">
             <el-link type="primary" :href="'http://localhost:8080' + row.closeFileUrl" target="_blank">下载申报书</el-link>
           </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openAudit(row)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog v-model="dialogVisible" title="项目审核" width="500px">
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
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const tableData = ref([])
const dialogVisible = ref(false)
const auditForm = reactive({
  projectId: null,
  isPass: true,
  comment: ''
})

const getList = () => {
  request.get('/project/audit-list').then(res => {
    tableData.value = res.data
  })
}

const openAudit = (row) => {
  auditForm.projectId = row.id
  auditForm.isPass = true
  auditForm.comment = '' // 默认意见
  dialogVisible.value = true
}

const submitAudit = () => {
  if (auditForm.isPass === false && !auditForm.comment) {
    return ElMessage.warning('驳回时必须填写审核意见')
  }
  
  request.post('/project/audit', auditForm).then(res => {
    ElMessage.success('审核完成')
    dialogVisible.value = false
    getList() // 刷新列表
  })
}

onMounted(() => {
  getList()
})
</script>