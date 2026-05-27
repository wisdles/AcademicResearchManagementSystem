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
          <template #header>成果类型分布</template>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

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

const fetchData = async () => {
  const res = await request.get('/stats/teacher-dashboard')
  if (res.code !== 200) return
  const data = res.data

  // 更新统计卡片
  statCards[0].value = data.totalCount || 0
  statCards[1].value = data.approvedCount || 0
  statCards[2].value = data.pendingCount || 0
  statCards[3].value = data.rejectedCount || 0

  // 饼图 - 成果类型分布
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  const pieData = data.achievementDistribution || {}
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: Object.entries(pieData).map(([name, value]) => ({ name, value }))
    }]
  })

  // 柱状图 - 状态分布
  if (!barChart) barChart = echarts.init(barChartRef.value)
  const statusData = data.statusBreakdown || {}
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Object.keys(statusData) },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: Object.values(statusData),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 1, color: '#188df0' }
        ])
      }
    }]
  })
}

const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
})
</script>

<style scoped>
.stat-card .card-content {
  display: flex;
  align-items: center;
}
.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}
.card-label {
  font-size: 14px;
  color: #909399;
}
.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
</style>