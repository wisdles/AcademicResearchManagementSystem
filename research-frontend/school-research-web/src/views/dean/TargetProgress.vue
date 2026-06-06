<template>
  <div class="target-progress">
    <el-card shadow="never">
      <template #header>
        <span>年度考核目标设定</span>
      </template>
      <el-form :model="form" label-width="120px" inline>
        <el-form-item label="年份">
          <el-select v-model="form.year" @change="loadTarget">
            <el-option v-for="y in years" :key="y" :label="String(y)" :value="y" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in fields" :key="item.key">
          <el-form-item :label="item.label">
            <el-input-number v-model="form[item.key]" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-button type="primary" @click="save">保存目标</el-button>
    </el-card>

    <!-- 完成进度 -->
    <el-card shadow="never" style="margin-top: 20px">
      <template #header><span>年度目标完成进度 ({{ form.year }}年)</span></template>
      <div v-for="item in progress" :key="item.name" style="margin-bottom: 15px">
        <div style="display: flex; justify-content: space-between; margin-bottom: 5px">
          <span>{{ item.name }}</span>
          <span>{{ item.actual }} / {{ item.target || '未设定' }}</span>
        </div>
        <el-progress
          :percentage="item.percent"
          :color="item.percent >= 100 ? '#67C23A' : item.percent >= 50 ? '#409EFF' : '#E6A23C'"
          :stroke-width="18"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const years = [2025, 2026, 2027]
const collegeId = ref(Number(localStorage.getItem('collegeId')) || 1)
const fields = [
  { key: 'projectTarget', label: '项目目标' },
  { key: 'paperTarget', label: '论文目标' },
  { key: 'patentTarget', label: '专利目标' },
  { key: 'softTarget', label: '软著目标' },
  { key: 'bookTarget', label: '专著目标' },
  { key: 'awardTarget', label: '获奖目标' },
  { key: 'competitionTarget', label: '竞赛目标' },
  { key: 'courseTarget', label: '课程目标' }
]

const form = reactive({
  year: new Date().getFullYear(),
  collegeId: 1,
  projectTarget: 0, paperTarget: 0, patentTarget: 0, softTarget: 0,
  bookTarget: 0, awardTarget: 0, competitionTarget: 0, courseTarget: 0
})

const progress = ref([])

const loadTarget = async () => {
  try {
    const res = await request.get(`/dean/target/${collegeId.value}/${form.year}`)
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data)
    } else {
      fields.forEach(f => form[f.key] = 0)
    }
  } catch (e) { console.error(e) }
  loadProgress()
}

const loadProgress = async () => {
  try {
    const res = await request.get(`/dean/target/progress/${collegeId.value}/${form.year}`)
    if (res.code === 200 && res.data) {
      progress.value = Object.values(res.data)
    }
  } catch (e) { console.error(e) }
}

const save = async () => {
  try {
    const res = await request.post('/dean/target/save', { ...form, collegeId: collegeId.value })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      loadProgress()
    }
  } catch (e) { console.error(e) }
}

onMounted(() => { loadTarget(); loadProgress() })
</script>
