<template>
  <div class="my-project-container">
    <el-card>
      <template #header>
        <span style="font-size: 18px; font-weight: bold;">我的项目列表</span>
      </template>

      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="name" label="项目名称" min-width="200" />
        
        <el-table-column label="分类/级别" width="220">
          <template #default="{ row }">
             <el-tag size="small">{{ row.category === 'TEACHING' ? '教学' : '科研' }}</el-tag>
             <el-tag size="small" type="info"  style="margin-left:8px">{{ getLevelText(row.level) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="funds" label="经费(万)" width="220" />
        
        <el-table-column label="状态" width="220">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="申报时间" width="220">
           <template #default="{ row }">
             <span v-if="row.status > 0">
      {{ row.createTime ? row.createTime.replace('T', ' ') : '未记录' }}
    </span>
    <span v-else style="color: #999;">草稿未申报</span>
           </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
             <!-- 只有草稿(0)或被驳回(-1, -2)的才能编辑，这里暂时只做展示，后面可以加编辑功能 -->
             <el-button  type="info" link :disabled="!(row.status <= 0)"  @click="handleEdit(row)">修改</el-button>
             
             <el-button type="primary" link @click="showTimeline(row)">查看进度</el-button>
             <el-button type="danger" size="small" @click="deleteProject(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 进度详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="项目审核进度" width="500px">
       <el-timeline>
         <el-timeline-item timestamp="提交申请" placement="top" :color="timelineData.status >= 0 ? '#0bbd87' : ''">
           <h4>老师提交申报</h4>
         </el-timeline-item>
         
         <el-timeline-item timestamp="秘书审核" placement="top" :color="getSecColor(timelineData.status)">
           <h4>秘书审核</h4>
           <p v-if="timelineData.status === -1" style="color:red">已驳回</p>
         </el-timeline-item>
         
         <el-timeline-item timestamp="院长终审" placement="top" :color="getDeanColor(timelineData.status)">
           <h4>院长审核</h4>
           <p v-if="timelineData.status === -2" style="color:red">已驳回</p>
         </el-timeline-item>
         
         <el-timeline-item v-if="timelineData.status === 3" timestamp="立项归档" placement="top" color="#0bbd87">
           <h4>审核通过，项目已立项</h4>
         </el-timeline-item>
       </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
const tableData = ref([])
const dialogVisible = ref(false)
const timelineData = ref({})
const router = useRouter()
const getList = () => {
  request.get('/project/my-list').then(res => {
    tableData.value = res.data
  })
}

// 辅助函数
const getLevelText = (val) => {
  const map = { 'NATIONAL': '国家级', 'PROVINCIAL': '省部级', 'CITY': '市厅级', 'SCHOOL': '校级' }
  return map[val] || val
}
const getStatusText = (status) => {
  const map = { 0:'草稿', 1:'待秘书审', 2:'待院长审', 3:'已立项', '-1':'秘书驳回', '-2':'院长驳回' }
  return map[status] || status
}
const getStatusType = (status) => {
  if (status === 3) return 'success'
  if (status < 0) return 'danger'
  if (status === 0) return 'info'
  return 'warning'
}

// 简单的进度条颜色判断逻辑
const getSecColor = (s) => (s >= 2 || s === 3) ? '#0bbd87' : (s === -1 ? '#f56c6c' : '#e4e7ed')
const getDeanColor = (s) => (s === 3) ? '#0bbd87' : (s === -2 ? '#f56c6c' : '#e4e7ed')

const showTimeline = (row) => {
  timelineData.value = row
  dialogVisible.value = true
}

const handleEdit = (row) => {
  //ElMessage.info('修改功能与申报页面类似，请自行补充路由跳转')
  // TODO: 跳转到申报页面，并传入项目id
  router.push({ path: '/project/apply', query: { id: row.id } })

}
const deleteProject = (id) => {
  ElMessageBox.confirm('确认删除该草稿吗？', '提示', { type: 'warning' }).then(() => {
    request.delete(`/project/delete/${id}`).then(res => {
      if (res.code === 200) {
        ElMessage.success('删除成功')
        getList() // 重新加载列表
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    })
  })
}

onMounted(() => {
  getList()
})
</script>
<style scoped>
.el-tag {
  font-size: 14px;
  padding: 4px 8px;
}

.el-button {
  font-size: 14px;
}

.el-table .cell {
  font-size: 15px;
}
.el-tag {
  font-size: 15px !important;
  line-height: 22px !important;
  padding: 4px 8px !important;
}
.el-tag__content {
  font-size: 15px !important;
}

</style>
