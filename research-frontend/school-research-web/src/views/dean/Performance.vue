<template>
  <div class="performance">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>教师绩效考核排名</span>
          <div class="header-actions">
            <el-select v-model="collegeId" placeholder="选择学院" clearable style="width: 200px; margin-right: 10px" @change="fetchData">
              <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
            <el-button type="primary" @click="fetchData">刷新数据</el-button>
          </div>
        </div>
      </template>

      <el-table :data="performanceList" border stripe v-loading="loading" style="width: 100%">
        <el-table-column label="排名" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.rank <= 3" :type="scope.row.rank === 1 ? 'danger' : scope.row.rank === 2 ? 'warning' : 'success'" size="small">
              {{ scope.row.rank }}
            </el-tag>
            <span v-else>{{ scope.row.rank }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="姓名" width="120" align="center" />
        <el-table-column prop="username" label="工号" width="120" align="center" />
        <el-table-column prop="collegeName" label="学院" width="150" align="center" />
        <el-table-column prop="score" label="绩效总分" width="100" align="center" sortable>
          <template #default="scope">
            <el-tag type="primary">{{ scope.row.score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAchievements" label="成果总数" width="100" align="center" />
        <el-table-column prop="projectCount" label="项目" width="70" align="center" />
        <el-table-column prop="paperCount" label="论文" width="70" align="center" />
        <el-table-column prop="patentCount" label="专利" width="70" align="center" />
        <el-table-column prop="softCount" label="软著" width="70" align="center" />
        <el-table-column prop="bookCount" label="专著" width="70" align="center" />
        <el-table-column prop="awardCount" label="获奖" width="70" align="center" />
        <el-table-column prop="competitionCount" label="竞赛" width="70" align="center" />
        <el-table-column prop="courseCount" label="课程" width="70" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const performanceList = ref([])
const colleges = ref([])
const collegeId = ref(null)
const loading = ref(false)

const fetchColleges = async () => {
  try {
    const res = await request.get('/college/list')
    if (res.code === 200) colleges.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {}
    if (collegeId.value) params.collegeId = collegeId.value
    const res = await request.post('/stats/performance', params)
    if (res.code === 200) performanceList.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchColleges()
  fetchData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}
</style>
