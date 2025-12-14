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
            style="width: 200px" 
            clearable 
            @clear="fetchData"
            @keyup.enter="fetchData"
          >
            <template #append>
              <el-button icon="Search" @click="fetchData" />
            </template>
          </el-input>
          <el-button type="primary" icon="Refresh" circle @click="fetchData" />
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
            <div class="number">{{ dashboardData.activeTeachers || '-' }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card gradient-green">
          <div class="icon-wrapper"><el-icon><DataLine /></el-icon></div>
          <div class="content">
            <div class="label">本月新增</div>
             <!-- 🟢 修改这里：绑定 currentMonthNew -->
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
            <template #header><div class="card-title">学院科研趋势 (模拟)</div></template>
            <div ref="lineChartRef" class="chart-box" style="height: 300px"></div>
         </el-card>
       </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
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

// 1. 加载学院
const fetchColleges = async () => {
  const res = await request.get('/college/list')
  if(res.code === 200) collegeList.value = res.data
}

// 2. 加载统计数据
const fetchData = async () => {
  try {
    const res = await request.post('/stats/dashboard', query)
    if(res.code === 200) {
      console.log('仪表盘数据：', res.data)
      dashboardData.value = res.data
      updateCharts(res.data)
    }
  } catch(e) { console.error(e) }
}

// 3. 初始化并更新图表
const updateCharts = (data) => {
  if (!data) return

  // === 饼图 ===
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  const pieOption = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'],
    series: [{
      name: '成果分布',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
      data: Object.entries(data.classificationDistribution || {}).map(([k, v]) => ({ value: v, name: k }))
    }]
  }
  pieChart.setOption(pieOption)

  // === 柱状图 ===
  if (!barChart) barChart = echarts.init(barChartRef.value)
  const teachers = data.topTeachers || []
  const barOption = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'type', data: teachers.map(t => t.name), axisTick: { alignWithLabel: true } },
    yAxis: { type: 'value' },
    series: [{
      name: '成果数',
      type: 'bar',
      barWidth: '40%',
      data: teachers.map(t => t.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#83bff6' },
          { offset: 0.5, color: '#188df0' },
          { offset: 1, color: '#188df0' }
        ])
      }
    }]
  }
  barChart.setOption(barOption)

  // === 折线图 (模拟数据) ===
  if (!lineChart) lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['项目', '论文'] },
    xAxis: { type: 'classification', boundaryGap: false, data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
    yAxis: { type: 'value' },
    series: [
      { name: '项目', type: 'line', smooth: true, data: [120, 132, 101, 134, 90, 230] },
      { name: '论文', type: 'line', smooth: true, data: [220, 182, 191, 234, 290, 330] }
    ]
  })
   // === 折线图 (改为：项目经费 Top 5 教师对比) ===
  // 既然趋势图SQL比较难写，我们把下面这个大图改成“经费排行”或者“成果详情堆叠图”
  if (!lineChart) lineChart = echarts.init(lineChartRef.value)
  
  // 我们利用饼图的数据，做一个简单的柱状展示作为“趋势/分布”替代
  const categories = Object.keys(data.classificationDistribution || {})
  const values = Object.values(data.classificationDistribution || {})

  lineChart.setOption({
    title: { text: '各类成果数量对比' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'classification', data: categories },
    yAxis: { type: 'value' },
    series: [{
      data: values,
      type: 'line',
      smooth: true,
      areaStyle: {}, // 面积图效果，更炫酷
      itemStyle: { color: '#8e44ad' }
    }]
  })
}

// 窗口大小改变时重绘
window.addEventListener('resize', () => {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
})

onMounted(() => {
    pieChart = echarts.init(pieChartRef.value)
  barChart = echarts.init(barChartRef.value)
  lineChart = echarts.init(lineChartRef.value)

  fetchColleges()
  fetchData()
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa; /* 浅灰底色 */
  min-height: 100vh;
}

.mt-20 { margin-top: 20px; }

/* 筛选栏 */
.filter-header {
 display: flex;
  justify-content: space-between;
  align-items: center;
  height: 50px; /* 增加高度 */
}
.title {
  font-size: 20px;
  font-weight: 800;
  color: #303133;
  /* 炫酷的渐变文字 */
  background: linear-gradient(45deg, #409EFF, #67C23A);

  /* 标准属性，定义背景绘制区域 */
  background-clip: text;

  /* WebKit 前缀，兼容 Chrome/Safari */
  -webkit-background-clip: text;

  /* 让文字透明，显示背景渐变 */
  color: transparent;
}

.filters {
  display: flex;
  gap: 15px;
  align-items: center;
}
/*  强制增加输入框宽度，防止被压缩 */
/* 使用 :deep() 穿透 Element Plus 的内部样式 */
.filters :deep(.el-input) {
  width: 280px !important; /* 加宽搜索框 */
}
.filters :deep(.el-select) {
  width: 180px !important; /* 加宽下拉框 */
}
/* 统计卡片 (炫酷核心) */
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
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.2);
}

/* 渐变背景 */
.gradient-blue { background: linear-gradient(135deg, #36d1dc, #5b86e5); }
.gradient-red { background: linear-gradient(135deg, #ff9966, #ff5e62); }
.gradient-purple { background: linear-gradient(135deg, #654ea3, #eaafc8); }
.gradient-green { background: linear-gradient(135deg, #11998e, #38ef7d); }

.icon-wrapper {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  margin-right: 20px;
}
.content .label { font-size: 14px; opacity: 0.9; margin-bottom: 5px; }
.content .number { font-size: 28px; font-weight: bold; }

/* 图表卡片 */
.chart-card {
  border-radius: 8px;
}
.card-title {
  font-weight: bold;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
}
.chart-box {
  width: 100%;
  height: 300px;
}
</style>