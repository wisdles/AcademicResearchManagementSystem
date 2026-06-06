<template>
  <div class="decision-support">
    <!-- 异常预警 -->
    <el-card shadow="never" style="margin-bottom: 20px" v-if="anomalies.length > 0">
      <template #header>
        <span style="color: #F56C6C">⚠ 数据异常预警 ({{ anomalies.length }})</span>
      </template>
      <el-table :data="anomalies" border stripe>
        <el-table-column prop="type" label="异常类型" width="140">
          <template #default="{ row }">
            <el-tag :type="row.level === 'danger' ? 'danger' : 'warning'" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="对象" min-width="200" show-overflow-tooltip />
        <el-table-column prop="count" label="数量" width="80" align="center" />
        <el-table-column prop="desc" label="说明" min-width="300" show-overflow-tooltip />
      </el-table>
    </el-card>
    <el-empty v-else-if="loaded" description="暂无异常数据" :image-size="80" style="margin-bottom: 20px" />

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>研究方向关键词分析 (Top 20)</template>
          <div ref="keywordChartRef" style="height: 400px"></div>
          <el-empty v-if="loaded && noKeywords" description="暂无足够数据进行分析" :image-size="80" />
        </el-card>
      </el-col>
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

    <!-- 资源分配建议 -->
    <el-card shadow="never" style="margin-top: 20px" v-if="resources.length > 0">
      <template #header>资源分配建议</template>
      <el-table :data="resources" border stripe>
        <el-table-column prop="category" label="方向/维度" width="120" />
        <el-table-column label="优先级" width="120">
          <template #default="{ row }">
            <el-tag :type="row.priority === '高优先级' ? 'danger' : row.priority === '中优先级' ? 'warning' : 'success'">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="current" label="当前数" width="100" />
        <el-table-column prop="percent" label="占比/指数" width="120">
          <template #default="{ row }">{{ row.percent }}%</template>
        </el-table-column>
        <el-table-column prop="suggestion" label="建议" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const keywordChartRef = ref(null)
const talentList = ref([])
const anomalies = ref([])
const resources = ref([])
const loaded = ref(false)
const noKeywords = ref(false)
const collegeId = ref(Number(localStorage.getItem('collegeId')) || 1)

const loadData = async () => {
  try {
    const [kwRes, talentRes, anomalyRes, resRes] = await Promise.all([
      request.get(`/dean/analysis/keywords/${collegeId.value}`),
      request.get(`/dean/analysis/talent/${collegeId.value}`),
      request.get(`/dean/analysis/anomaly/${collegeId.value}`),
      request.get(`/dean/analysis/resource/${collegeId.value}`)
    ])
    if (kwRes.code === 200 && kwRes.data) {
      const kws = kwRes.data.keywords || []
      noKeywords.value = kws.length === 0
      if (kws.length > 0) renderKeywordChart(kws)
    }
    if (talentRes.code === 200) talentList.value = talentRes.data || []
    if (anomalyRes.code === 200) anomalies.value = anomalyRes.data || []
    if (resRes.code === 200) resources.value = resRes.data || []
  } catch (e) { console.error(e) }
  finally { loaded.value = true }
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
