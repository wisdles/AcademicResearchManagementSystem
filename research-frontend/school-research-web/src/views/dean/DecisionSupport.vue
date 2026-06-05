<template>
  <div class="decision-support">
    <el-row :gutter="20">
      <!-- 研究方向关键词 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>研究方向关键词分析</template>
          <div ref="keywordChartRef" style="height: 400px"></div>
        </el-card>
      </el-col>

      <!-- 人才梯队 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>人才梯队分析</template>
          <el-table :data="talentList" border stripe max-height="400">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="name" label="教师" width="100" />
            <el-table-column prop="total" label="成果总数" width="100" />
            <el-table-column prop="level" label="梯队等级" width="100">
              <template #default="{ row }">
                <el-tag :type="row.level === '骨干' ? 'danger' : row.level === '中坚' ? 'warning' : row.level === '新锐' ? 'success' : 'info'">
                  {{ row.level }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const keywordChartRef = ref(null)
const talentList = ref([])

const collegeId = ref(1) // 从登录信息获取

const loadData = async () => {
  try {
    const kwRes = await request.get(`/dean/analysis/keywords/${collegeId.value}`)
    if (kwRes.code === 200 && kwRes.data) {
      renderKeywordChart(kwRes.data.keywords || [])
    }
    const talentRes = await request.get(`/dean/analysis/talent/${collegeId.value}`)
    if (talentRes.code === 200) talentList.value = talentRes.data || []
  } catch (e) { console.error(e) }
}

const renderKeywordChart = (data) => {
  if (!keywordChartRef.value) return
  const chart = echarts.init(keywordChartRef.value)
  chart.setOption({
    tooltip: {},
    xAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { rotate: 45 } },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: data.map(d => d.value), itemStyle: { color: '#409EFF' } }],
    grid: { bottom: 100 }
  })
}

onMounted(() => { nextTick(loadData) })
</script>
