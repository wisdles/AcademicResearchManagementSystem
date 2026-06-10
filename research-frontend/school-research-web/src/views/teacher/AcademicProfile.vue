<template>
  <div class="academic-profile">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="profile-header">
            <el-avatar :size="80" style="background: #409EFF; font-size: 32px">
              {{ profile.realName ? profile.realName.charAt(0) : '?' }}
            </el-avatar>
            <h2>{{ profile.realName }}</h2>
            <p>{{ profile.collegeName }} · {{ profile.username }}</p>
          </div>
          <el-divider />
          <div class="stats">
            <el-statistic title="已通过成果总数" :value="profile.totalCount || 0" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span>成果类型分布 <small style="color:#909399;font-weight:400">(点击扇形筛选)</small></span>
            <el-button v-if="activeFilter" size="small" style="float:right" @click="clearFilter">清除筛选</el-button>
          </template>
          <div ref="chartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span>{{ activeFilter ? `「${activeFilter}」类成果明细` : '全部成果明细' }}</span>
        <el-button type="primary" size="small" style="float: right" @click="exportCSV">导出 CSV</el-button>
      </template>
      <el-table :data="detailList" border stripe v-loading="detailLoading" max-height="400" @row-click="()=>{}">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="typeLabel" label="类型" width="80" />
        <el-table-column prop="name" label="名称" min-width="240" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="120" />
      </el-table>
      <div style="margin-top:8px;color:#909399;font-size:13px">共 {{ detailList.length }} 条</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const profile = ref({})
const detailList = ref([])
const detailLoading = ref(false)
const activeFilter = ref('')
const chartRef = ref(null)
let chart = null

const typeMap = { '项目': 'project', '论文': 'paper', '专利': 'patent', '软著': 'software', '专著': 'book', '获奖': 'award', '竞赛': 'competition', '课程': 'course' }

const fetchProfile = async () => {
  const res = await request.get('/teacher/profile')
  if (res.code === 200) {
    profile.value = res.data
    await nextTick()
    renderChart()
  }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
    chart.on('click', (params) => {
      activeFilter.value = params.name
      fetchFiltered()
    })
  }
  const dist = profile.value.distribution || {}
  const data = Object.entries(dist).filter(([, v]) => v > 0).map(([k, v]) => ({ name: k, value: v }))
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{ type: 'pie', radius: ['40%', '70%'], data, label: { formatter: '{b}: {c}' } }]
  })
}

const fetchFiltered = async () => {
  detailLoading.value = true
  try {
    const params = { status: '3' }
    if (activeFilter.value) {
      params.type = typeMap[activeFilter.value] || ''
    }
    const res = await request.post('/stats/my-achievements', params)
    if (res.code === 200) detailList.value = res.data || []
  } finally { detailLoading.value = false }
}

const clearFilter = () => { activeFilter.value = ''; fetchFiltered() }

const exportCSV = () => {
  if (!detailList.value.length) return ElMessage.warning('无数据')
  let csv = '﻿类型,名称,创建时间\n'
  detailList.value.forEach(r => { csv += `${r.typeLabel},${r.name || ''},${r.createTime}\n` })
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob); const a = document.createElement('a')
  a.href = url; a.download = '我的成果.csv'; a.click(); URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(() => { fetchProfile(); fetchFiltered() })
</script>

<style scoped>
.profile-header { text-align: center; padding: 10px 0; }
.profile-header h2 { margin: 10px 0 5px; }
.profile-header p { color: #909399; margin: 0; }
.stats { text-align: center; }
</style>
