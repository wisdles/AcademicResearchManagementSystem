<template>
  <div class="score-rule">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>审核评分规则配置</span>
          <div>
            <el-button v-if="rules.length === 0" type="warning" @click="init">初始化默认规则</el-button>
            <el-button type="primary" @click="save" :loading="loading">保存修改</el-button>
          </div>
        </div>
      </template>

      <el-alert title="此处配置的分值将用于教师绩效考核计算。修改后立即生效。"
                type="info" show-icon :closable="false" style="margin-bottom: 15px" />

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
            <el-table-column prop="description" label="说明" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const rules = ref([])
const activeType = ref('project')
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

const load = async () => {
  const res = await request.get('/preference/rules')
  if (res.code === 200) rules.value = res.data || []
}

const init = async () => {
  await request.post('/preference/rules/init')
  ElMessage.success('已初始化默认规则')
  load()
}

const save = async () => {
  loading.value = true
  try {
    const res = await request.post('/preference/rules/save', rules.value)
    if (res.code === 200) ElMessage.success('保存成功')
  } finally { loading.value = false }
}

onMounted(load)
</script>
