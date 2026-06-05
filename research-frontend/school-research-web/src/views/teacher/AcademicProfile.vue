<template>
  <div class="academic-profile">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
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

      <!-- 成果分布 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>成果类型分布</template>
          <div ref="chartRef" style="height: 350px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 个人数据导出 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header>
        <span>个人成果导出</span>
        <el-button type="primary" style="float: right" @click="exportData">导出 Excel</el-button>
      </template>
      <el-table :data="exportList" border stripe v-loading="loading" max-height="400">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="type" label="成果类型" width="100" />
        <el-table-column prop="name" label="成果名称" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const profile = ref({})
const exportList = ref([])
const loading = ref(false)
const chartRef = ref(null)

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
  const chart = echarts.init(chartRef.value)
  const dist = profile.value.distribution || {}
  const data = Object.entries(dist).filter(([, v]) => v > 0).map(([k, v]) => ({ name: k, value: v }))
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data,
      label: { formatter: '{b}: {c}' }
    }]
  })
}

const exportData = async () => {
  loading.value = true
  try {
    const res = await request.get('/teacher/export')
    if (res.code === 200) {
      exportList.value = res.data || []
      if (exportList.value.length === 0) {
        ElMessage.warning('暂无已通过成果可导出')
      } else {
        // 生成CSV并下载
        let csv = '﻿成果类型,成果名称\n'
        exportList.value.forEach(e => { csv += `${e.type},${e.name}\n` })
        const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url; a.download = '我的成果.csv'; a.click()
        URL.revokeObjectURL(url)
        ElMessage.success('导出成功')
      }
    }
  } finally { loading.value = false }
}

onMounted(fetchProfile)
</script>

<style scoped>
.profile-header { text-align: center; padding: 10px 0; }
.profile-header h2 { margin: 10px 0 5px; }
.profile-header p { color: #909399; margin: 0; }
.stats { text-align: center; }
</style>
