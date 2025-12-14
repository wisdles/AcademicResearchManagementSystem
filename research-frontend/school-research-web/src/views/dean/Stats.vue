<template>
  <div class="dashboard-container">
    <!-- 顶部：筛选栏 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-header">
        <span class="title">科研数据驾驶舱</span>
        <div class="filters">
          <el-select v-model="query.collegeId" placeholder="选择学院" clearable @change="fetchData">
            <el-option v-for="c in collegeList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-input 
            v-model="query.teacherName" 
            placeholder="搜索教师姓名/工号" 
            clearable 
            @clear="fetchData"
            @keyup.enter="fetchData"
          >
            <template #append>
              <el-button :icon="Search" @click="fetchData" />
            </template>
          </el-input>
          <el-button type="primary" :icon="Refresh" circle @click="fetchData" />
        </div>
      </div>
    </el-card>

    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="mt-20">
      <el-col :span="6">
        <div class="stat-card gradient-blue">
          <div class="icon-wrapper"><el-icon><Trophy /></el-icon></div>
          <div class="content">
            <div class="label">成果总数</div>
            <div class="number">{{ dashboardData.totalAchievements || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card gradient-red">
          <div class="icon-wrapper"><el-icon><Money /></el-icon></div>
          <div class="content">
            <div class="label">科研经费 (万元)</div>
            <div class="number">{{ dashboardData.totalFunds || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card gradient-purple">
          <div class="icon-wrapper"><el-icon><UserFilled /></el-icon></div>
          <div class="content">
            <div class="label">活跃教师</div>
            <div class="number">{{ dashboardData.activeTeachers || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card gradient-green">
          <div class="icon-wrapper"><el-icon><DataLine /></el-icon></div>
          <div class="content">
            <div class="label">本月新增</div>
            <div class="number">{{ dashboardData.currentMonthNew || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="mt-20">
      <!-- 左侧：饼图 -->
      <el-col :span="8">
        <el-card shadow="hover" class="chart-card">
          <template #header><div class="card-title">成果类型分布</div></template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      
      <!-- 中间：柱状图 -->
      <el-col :span="16">
        <el-card shadow="hover" class="chart-card">
          <template #header><div class="card-title">教师科研贡献 Top 10</div></template>
          <div ref="barChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
       <el-col :span="24">
         <el-card shadow="hover">
            <template #header><div class="card-title">学院科研趋势</div></template>
            <div ref="lineChartRef" class="chart-box" style="height: 350px"></div>
         </el-card>
       </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { Trophy, Money, UserFilled, DataLine, Search, Refresh } from '@element-plus/icons-vue'

// --- 数据 ---
const query = reactive({
  collegeId: null,
  teacherName: ''
})
const collegeList = ref([])
const dashboardData = ref({})

// --- ECharts 实例 ---
const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let barChart = null
let lineChart = null

// --- 方法 ---
const fetchColleges = async () => {
  const res = await request.get('/college/list')
  if(res.code === 200) collegeList.value = res.data
}

const fetchData = async () => {
  try {
    const res = await request.post('/stats/dashboard', query)
    if(res.code === 200) {
      dashboardData.value = res.data
      updateCharts(res.data)
    }
  } catch(e) { console.error(e) }
}

const updateCharts = (data) => {
  if (!data) return

  // 1. 饼图
  if (pieChartRef.value) {
    if (!pieChart) pieChart = echarts.init(pieChartRef.value)
    const distMap = data.classificationDistribution || data.categoryDistribution || {}
    const pieData = Object.entries(distMap).map(([k, v]) => ({ value: v, name: k }))

    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: '0%' },
      color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'],
      series: [{
        name: '成果分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 18, fontWeight: 'bold' } },
        data: pieData
      }]
    })
  }

  // 2. 柱状图
  if (barChartRef.value) {
    if (!barChart) barChart = echarts.init(barChartRef.value)
    const teachers = data.topTeachers || []
    const xData = teachers.map(t => t.name)
    const yData = teachers.map(t => t.count)

    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { 
        type: 'category', 
        data: xData, 
        axisTick: { alignWithLabel: true },
        axisLabel: { interval: 0, rotate: 30 }
      },
      yAxis: { type: 'value' },
      series: [{
        name: '成果数',
        type: 'bar',
        barWidth: '40%',
        data: yData,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }]
    })
  }

  // 3. 折线图
  if (lineChartRef.value) {
    if (!lineChart) lineChart = echarts.init(lineChartRef.value)
    const trend = data.trendData || []
    const months = trend.map(item => item.month)
    
    // 动态获取各类数据
    const projData = trend.map(item => item['项目'] || 0)
    const paperData = trend.map(item => item['论文'] || 0)
    const patentData = trend.map(item => item['专利'] || 0)
    const softData = trend.map(item => item['软著'] || 0)
    const bookData = trend.map(item => item['专著'] || 0)

    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['项目', '论文', '专利', '软著', '专著'], bottom: 0 },
      grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
      xAxis: { type: 'category', boundaryGap: false, data: months },
      yAxis: { type: 'value' },
      series: [
        { name: '项目', type: 'line', smooth: true, data: projData, itemStyle: { color: '#409EFF' } },
        { name: '论文', type: 'line', smooth: true, data: paperData, itemStyle: { color: '#67C23A' } },
        { name: '专利', type: 'line', smooth: true, data: patentData, itemStyle: { color: '#E6A23C' } },
        { name: '软著', type: 'line', smooth: true, data: softData, itemStyle: { color: '#F56C6C' } },
        { name: '专著', type: 'line', smooth: true, data: bookData, itemStyle: { color: '#909399' } }
      ]
    })
  }
}

// 窗口自适应
const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
}

onMounted(() => {
  fetchColleges()
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}
.mt-20 { margin-top: 20px; }

/* 筛选栏 */
.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px;
}
.title {
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(45deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  color: transparent;
}
.filters { display: flex; gap: 15px; align-items: center; }
.filters :deep(.el-input) { width: 280px !important; }
.filters :deep(.el-select) { width: 180px !important; }

/* 统计卡片 */
.stat-card {
  height: 120px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  padding: 0 25px;
  color: #fff;
  transition: transform 0.3s, box-shadow 0.3s;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}
.stat-card:hover { transform: translateY(-5px); box-shadow: 0 8px 25px rgba(0,0,0,0.2); }

.gradient-blue { background: linear-gradient(135deg, #36d1dc, #5b86e5); }
.gradient-red { background: linear-gradient(135deg, #ff9966, #ff5e62); }
.gradient-purple { background: linear-gradient(135deg, #654ea3, #eaafc8); }
.gradient-green { background: linear-gradient(135deg, #11998e, #38ef7d); }

.icon-wrapper {
  width: 60px; height: 60px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 30px; margin-right: 20px;
}
.content .label { font-size: 14px; opacity: 0.9; margin-bottom: 5px; }
.content .number { font-size: 28px; font-weight: bold; }

/* 图表容器 */
.chart-card { border-radius: 8px; }
.card-title { font-weight: bold; border-left: 4px solid #409EFF; padding-left: 10px; }
.chart-box { width: 100%; height: 300px; }
</style>