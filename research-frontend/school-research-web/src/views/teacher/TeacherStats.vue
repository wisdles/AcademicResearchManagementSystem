<template>
  <div class="teacher-stats">
    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card :body-style="{ padding: '20px' }" class="stat-card" shadow="hover">
          <div class="card-content">
            <div class="card-icon" :style="{ backgroundColor: card.color }">
              <el-icon size="28" color="#fff"><DataAnalysis /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-label">{{ card.label }}</div>
              <div class="card-value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span>成果类型分布 <small style="color:#909399;font-weight:400">(点击扇形筛选)</small></span>
          </template>
          <div ref="pieChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>审核状态分布</template>
          <div ref="barChartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选 + 明细列表 + 导出 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px">
          <span>成果明细</span>
          <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center">
            <el-select v-model="filterType" placeholder="成果类型" clearable style="width:120px" @change="fetchList">
              <el-option label="全部" value="" />
              <el-option v-for="t in types" :key="t.v" :label="t.l" :value="t.v" />
            </el-select>
            <el-input v-model="filterKeyword" placeholder="名称关键词" clearable style="width:140px" @clear="fetchList" @keyup.enter="fetchList" />
            <el-input v-model="filterTag" placeholder="标签" clearable style="width:100px" @clear="fetchList" @keyup.enter="fetchList" />
            <el-select v-model="filterYearFrom" placeholder="起" clearable style="width:80px" @change="fetchList">
              <el-option v-for="y in years" :key="y" :label="String(y)" :value="y" />
            </el-select>
            <span style="color:#909399">—</span>
            <el-select v-model="filterYearTo" placeholder="止" clearable style="width:80px" @change="fetchList">
              <el-option v-for="y in years" :key="y" :label="String(y)" :value="y" />
            </el-select>
            <el-button type="primary" @click="exportCSV" icon="Download">导出 Excel</el-button>
          </div>
        </div>
      </template>

      <el-table :data="achievementList" border stripe v-loading="listLoading" max-height="500">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="typeLabel" label="类型" width="80" />
        <el-table-column prop="name" label="名称" min-width="240" show-overflow-tooltip />
        <el-table-column prop="classification" label="分类" width="80" />
        <el-table-column label="标签" width="180">
          <template #default="{ row }">
            <div v-if="row.editingTag" style="display:flex;gap:4px">
              <el-input v-model="row.editTagValue" size="small" style="width:100px" @keyup.enter="saveTag(row)" @blur="saveTag(row)" ref="tagInput" />
            </div>
            <div v-else @click="startEditTag(row)" style="cursor:pointer;min-height:22px;display:flex;align-items:center" :title="row.tags || '点击编辑标签'">
              <el-tag v-if="row.tags" size="small" type="success" style="cursor:pointer">{{ row.tags }}</el-tag>
              <span v-else style="color:#c0c4cc;font-size:12px">点击添加</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="120" />
      </el-table>
      <div style="margin-top:10px;color:#909399;font-size:13px">共 {{ achievementList.length }} 条</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { DataAnalysis, Download } from '@element-plus/icons-vue'

const pieChartRef = ref(null)
const barChartRef = ref(null)
let pieChart = null
let barChart = null

const statCards = reactive([
  { label: '成果总数', value: 0, color: '#409EFF' },
  { label: '已通过', value: 0, color: '#67C23A' },
  { label: '审核中', value: 0, color: '#E6A23C' },
  { label: '已驳回', value: 0, color: '#F56C6C' }
])

const types = [
  { v: 'project', l: '项目' }, { v: 'paper', l: '论文' }, { v: 'patent', l: '专利' },
  { v: 'software', l: '软著' }, { v: 'book', l: '专著' }, { v: 'award', l: '获奖' },
  { v: 'competition', l: '竞赛' }, { v: 'course', l: '课程' }
]
const years = [2020, 2021, 2022, 2023, 2024, 2025, 2026, 2027]

const filterType = ref('')
const filterKeyword = ref('')
const filterTag = ref('')
const filterStatus = ref('')
const filterYearFrom = ref(null)
const filterYearTo = ref(null)

const achievementList = ref([])
const listLoading = ref(false)

const statusTag = (s) => ({ 0:'info', 1:'warning', 2:'warning', 3:'success', '-1':'danger', '-2':'danger' }[s] || 'info')

const fetchDashboard = async () => {
  const res = await request.get('/stats/teacher-dashboard')
  if (res.code !== 200) return
  const data = res.data
  statCards[0].value = data.totalCount || 0
  statCards[1].value = data.approvedCount || 0
  statCards[2].value = data.pendingCount || 0
  statCards[3].value = data.rejectedCount || 0

  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.on('click', (params) => {
      const typeMap = { '项目': 'project', '论文': 'paper', '专利': 'patent', '软著': 'software', '专著': 'book', '获奖': 'award', '竞赛': 'competition', '课程': 'course' }
      filterType.value = typeMap[params.name] || ''
      fetchList()
    })
  }
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      data: Object.entries(data.achievementDistribution || {}).filter(([,v]) => v > 0).map(([name, value]) => ({ name, value }))
    }]
  })

  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
    barChart.on('click', (params) => {
      const statusMap = { '草稿': '0', '审核中': '1,2', '已通过': '3', '已驳回': '-1,-2' }
      filterStatus.value = statusMap[params.name] || ''
      fetchList()
    })
  }
  const sd = data.statusBreakdown || {}
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Object.keys(sd) },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar', data: Object.values(sd),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#83bff6' }, { offset: 1, color: '#188df0' }]) }
    }]
  })
}

const fetchList = async () => {
  listLoading.value = true
  try {
    const params = {}
    if (filterType.value) params.type = filterType.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterTag.value) params.tag = filterTag.value
    if (filterYearFrom.value) params.yearFrom = filterYearFrom.value
    if (filterYearTo.value) params.yearTo = filterYearTo.value
    const res = await request.post('/stats/my-achievements', params)
    if (res.code === 200) achievementList.value = res.data || []
  } finally { listLoading.value = false }
}

const startEditTag = (row) => {
  achievementList.value.forEach(r => { r.editingTag = false })
  row.editingTag = true
  row.editTagValue = row.tags || ''
  setTimeout(() => {
    const inputs = document.querySelectorAll('.el-input__inner')
    const last = inputs[inputs.length - 1]
    if (last) last.focus()
  }, 50)
}

const saveTag = async (row) => {
  row.editingTag = false
  const newVal = (row.editTagValue || '').trim()
  if (newVal === (row.tags || '')) return
  try {
    await request.put('/stats/update-tags', { type: row.type, id: row.id, tags: newVal })
    row.tags = newVal
    ElMessage.success('标签已更新')
  } catch (e) { row.tags = row.tags || '' }
}

const exportCSV = () => {
  if (!achievementList.value.length) return ElMessage.warning('无数据可导出')
  // BOM + CSV header + rows
  let csv = '﻿类型,名称,分类,标签,状态,创建时间\n'
  achievementList.value.forEach(r => {
    csv += `${r.typeLabel},${r.name || ''},${r.classification || ''},${r.tags || ''},${r.statusText},${r.createTime}\n`
  })
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `我的成果明细_${new Date().toISOString().slice(0,10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const handleResize = () => { pieChart?.resize(); barChart?.resize() }

onMounted(() => {
  fetchDashboard()
  fetchList()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose(); barChart?.dispose()
})
</script>

<style scoped>
.stat-card .card-content { display: flex; align-items: center; }
.card-icon {
  width: 56px; height: 56px; border-radius: 8px;
  display: flex; align-items: center; justify-content: center; margin-right: 16px;
}
.card-label { font-size: 14px; color: #909399; }
.card-value { font-size: 28px; font-weight: bold; color: #303133; }
</style>
