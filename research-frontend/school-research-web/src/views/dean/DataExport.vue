<template>
  <div class="data-export-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-title-bar">
          <span class="main-title">数据明细与导出</span>
          <el-button type="success" icon="Download" @click="exportToExcel">导出当前结果</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="detailQuery" class="detail-form">
        <el-form-item label="学院">
          <el-select v-model="detailQuery.collegeId" placeholder="全部学院" clearable style="width: 150px">
            <el-option v-for="c in collegeList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教师">
          <el-input v-model="detailQuery.teacherName" placeholder="姓名" style="width: 120px" clearable />
        </el-form-item>
        <el-form-item label="归属">
          <el-select v-model="detailQuery.classification" placeholder="全部" clearable style="width: 120px">
            <el-option label="科研" value="科研" />
            <el-option label="教学" value="教学" />
          </el-select>
        </el-form-item>
        <el-form-item label="成果类型">
          <el-select v-model="detailQuery.types" multiple collapse-tags placeholder="全部类型" style="width: 200px">
            <el-option label="项目" value="project" />
            <el-option label="论文" value="paper" />
            <el-option label="专利" value="patent" />
            <el-option label="软著" value="software" />
            <el-option label="专著" value="book" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="detailQuery.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="fetchDetailData">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 结果表格 -->
      <el-table :data="detailList" border stripe height="550" v-loading="detailLoading">
        <el-table-column type="index" label="#" width="50" align="center" />
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称/标题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="applicantName" label="申报人" width="100" />
        <el-table-column prop="collegeName" label="所属学院" width="150" />
        <el-table-column prop="classification" label="归属" width="80" align="center">
           <template #default="{ row }">
             <el-tag effect="plain" :type="row.classification==='科研'?'':'success'">{{ row.classification }}</el-tag>
           </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
           <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import * as XLSX from 'xlsx' 
import request from '@/utils/request'
import { Download, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const collegeList = ref([])
const detailList = ref([])
const detailLoading = ref(false)

const detailQuery = reactive({
  collegeId: null,
  teacherName: '',
  classification: '',
  types: [], 
  dateRange: []
})

// 获取学院列表 (必须单独获取，因为筛选条件需要)
const fetchColleges = async () => {
  const res = await request.get('/college/list')
  if(res.code === 200) collegeList.value = res.data
}

// 获取明细数据
const fetchDetailData = async () => {
  detailLoading.value = true
  try {
    const res = await request.post('/stats/detail-list', detailQuery)
    if (res.code === 200) {
      detailList.value = res.data
    }
  } finally {
    detailLoading.value = false
  }
}

// 导出 Excel
const exportToExcel = () => {
  if (detailList.value.length === 0) return ElMessage.warning('暂无数据可导出')
  
  const data = detailList.value.map(item => ({
    '成果类型': item.type,
    '名称': item.name,
    '申报人': item.applicantName,
    '所属学院': item.collegeName,
    '教研归属': item.classification,
    '提交时间': formatTime(item.createTime),
    '状态': '已通过'
  }))

  const ws = XLSX.utils.json_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, "科研统计明细")
  XLSX.writeFile(wb, `科研统计导出_${new Date().getTime()}.xlsx`)
}

const getTypeTag = (type) => {
  const map = { '项目': '', '论文': 'success', '专利': 'warning', '软著': 'danger', '专著': 'info' }
  return map[type] || ''
}

const formatTime = (t) => {
  return t ? t.replace('T', ' ') : ''
}

onMounted(() => {
  fetchColleges()
  fetchDetailData()
})
</script>

<style scoped>
.data-export-container { padding: 20px; background-color: #f5f7fa; min-height: 100vh; }
.card-title-bar { display: flex; justify-content: space-between; align-items: center; }
.main-title { font-size: 18px; font-weight: bold; border-left: 4px solid #409EFF; padding-left: 10px; }
.detail-form .el-form-item { margin-bottom: 15px; }
</style>