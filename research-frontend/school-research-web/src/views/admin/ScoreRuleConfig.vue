<template>
  <div class="score-rule">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:10px">
          <span>审核评分规则配置</span>
          <div style="display:flex;gap:8px;align-items:center">
            <!-- 年份切换 -->
            <el-select v-model="currentYear" @change="onYearChange" style="width:120px">
              <el-option v-for="y in yearList" :key="y" :label="String(y)" :value="y" />
            </el-select>
            <el-button v-if="!yearList.includes(currentYear)" type="success" @click="handleInit">初始化 {{ currentYear }}年规则</el-button>
            <el-button type="warning" @click="handleCopy">从上年复制</el-button>
            <el-button type="primary" @click="handleSave" :loading="loading">保存</el-button>
          </div>
        </div>
      </template>

      <el-alert v-if="emptyYear" title="该年份无评分规则，请点击「初始化」或「从上年复制」" type="warning" show-icon :closable="false" style="margin-bottom:15px" />

      <el-tabs v-model="activeType">
        <el-tab-pane v-for="t in types" :key="t.key" :label="t.label" :name="t.key">
          <el-table :data="groupedRules[t.key] || []" border stripe>
            <el-table-column prop="ruleLabel" label="规则名称" min-width="200" />
            <el-table-column label="分值" width="180">
              <template #default="{ row }">
                <el-input-number v-model="row.score" :min="0" :max="100" />
              </template>
            </el-table-column>
            <el-table-column label="启用" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" />
              </template>
            </el-table-column>
            <el-table-column prop="ruleKey" label="规则键" width="150" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 历史记录弹窗 -->
    <el-dialog v-model="historyVisible" title="往年评分规则" width="800px">
      <el-table :data="historyRules" border stripe>
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="ruleType" label="类型" width="100" />
        <el-table-column prop="ruleLabel" label="规则名" />
        <el-table-column prop="score" label="分值" width="80" />
        <el-table-column prop="enabled" label="启用" width="80">
          <template #default="{ row }">{{ row.enabled ? '是' : '否' }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const currentYear = ref(new Date().getFullYear())
const yearList = ref([])
const rules = ref([])
const activeType = ref('project')
const emptyYear = ref(false)
const historyVisible = ref(false)
const historyRules = ref([])

const types = [
  { key: 'project', label: '项目' }, { key: 'paper', label: '论文' },
  { key: 'patent', label: '专利' }, { key: 'software', label: '软著' },
  { key: 'book', label: '专著' }, { key: 'award', label: '获奖' },
  { key: 'competition', label: '竞赛' }, { key: 'course', label: '课程' }
]

const groupedRules = computed(() => {
  const map = {}
  rules.value.forEach(r => { (map[r.ruleType] ||= []).push(r) })
  return map
})

const fetchYears = async () => {
  try {
    const res = await request.get('/preference/rules/years')
    if (res.code === 200) {
      yearList.value = res.data || []
      if (yearList.value.length > 0 && !yearList.value.includes(currentYear.value)) {
        // 有历史但当前年份无规则，默认选最近一年
        currentYear.value = yearList.value[0]
      }
      if (yearList.value.length === 0) yearList.value = [currentYear.value]
    }
  } catch (e) { console.error(e) }
}

const loadRules = async () => {
  try {
    const res = await request.get(`/preference/rules/${currentYear.value}`)
    if (res.code === 200) {
      rules.value = res.data || []
      emptyYear.value = rules.value.length === 0
    }
  } catch (e) { console.error(e) }
}

const onYearChange = () => loadRules()

const handleInit = async () => {
  try {
    await ElMessageBox.confirm(`将为 ${currentYear.value} 年创建默认评分规则，确认？`, '初始化')
  } catch { return }
  loading.value = true
  try {
    const res = await request.post(`/preference/rules/init/${currentYear.value}`)
    if (res.code === 200) ElMessage.success(res.data)
    await fetchYears()
    await loadRules()
  } finally { loading.value = false }
}

const handleCopy = async () => {
  const fromYear = yearList.value.length > 0 ? Math.max(...yearList.value) : currentYear.value - 1
  if (fromYear >= currentYear.value) {
    ElMessage.warning('没有更早的年份可复制')
    return
  }
  try {
    await ElMessageBox.confirm(`将 ${fromYear} 年规则复制到 ${currentYear.value} 年？`, '复制规则')
  } catch { return }
  loading.value = true
  try {
    const res = await request.post(`/preference/rules/copy/${fromYear}/${currentYear.value}`)
    if (res.code === 200) ElMessage.success(res.data)
    await fetchYears()
    await loadRules()
  } finally { loading.value = false }
}

const handleSave = async () => {
  loading.value = true
  try {
    const res = await request.post('/preference/rules/save', {
      year: currentYear.value,
      rules: rules.value
    })
    if (res.code === 200) ElMessage.success('保存成功')
    await fetchYears()
  } finally { loading.value = false }
}

onMounted(async () => {
  await fetchYears()
  if (yearList.value.length === 0 || !yearList.value.includes(currentYear.value)) {
    rules.value = []
    emptyYear.value = true
  } else {
    await loadRules()
  }
})
</script>
