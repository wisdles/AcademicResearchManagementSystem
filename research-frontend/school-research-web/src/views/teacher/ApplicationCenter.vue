<template>
  <div class="application-center">
    <el-card shadow="never">
      <template #header>
        <span>项目申报中心</span>
      </template>

      <el-alert
        title="本功能将根据您已通过审核的成果（论文、项目、专利、专著），自动生成规范化的项目申报书 Word 模板。"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px"
      />

      <!-- 已有成果概览 -->
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="8">
          <el-statistic title="已通过项目" :value="achievements.totalProject || 0">
            <template #suffix>项</template>
          </el-statistic>
        </el-col>
        <el-col :span="8">
          <el-statistic title="已通过论文" :value="achievements.totalPaper || 0">
            <template #suffix>篇</template>
          </el-statistic>
        </el-col>
        <el-col :span="8">
          <el-statistic title="已通过专利" :value="achievements.totalPatent || 0">
            <template #suffix>项</template>
          </el-statistic>
        </el-col>
      </el-row>

      <el-divider />

      <!-- 生成申报书 -->
      <el-form :model="form" label-width="120px" style="max-width: 600px">
        <el-form-item label="拟申报项目名称" required>
          <el-input v-model="form.projectName" placeholder="请输入要申报的项目名称" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="generateDoc">
            <el-icon style="margin-right: 5px"><Download /></el-icon>
            生成并下载申报书
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 申报进度（按已申报项目展示） -->
      <el-divider content-position="left">我的申报进度</el-divider>
      <el-table :data="myProjects" border stripe v-loading="listLoading">
        <el-table-column prop="name" label="项目名称" min-width="240" show-overflow-tooltip />
        <el-table-column prop="level" label="级别" width="100" />
        <el-table-column prop="funds" label="经费(万)" width="100" />
        <el-table-column label="申报状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="申报时间" width="130" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import request from '@/utils/request'

const achievements = ref({})
const myProjects = ref([])
const loading = ref(false)
const listLoading = ref(false)

const form = reactive({ projectName: '' })

const fetchData = async () => {
  try {
    const res = await request.get('/teacher/my-achievements')
    if (res.code === 200) achievements.value = res.data
  } catch (e) { console.error(e) }
}

const fetchMyProjects = async () => {
  listLoading.value = true
  try {
    const res = await request.get('/project/my-list')
    if (res.code === 200) myProjects.value = res.data || []
  } catch (e) { /* 接口可能不存在 */ }
  finally { listLoading.value = false }
}

const generateDoc = async () => {
  if (!form.projectName.trim()) {
    ElMessage.warning('请输入项目名称')
    return
  }
  loading.value = true
  try {
    // 用 fetch 拿二进制文件
    const token = localStorage.getItem('token')
    const resp = await fetch(`/api/application/template?projectName=${encodeURIComponent(form.projectName)}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (!resp.ok) {
      ElMessage.error('生成失败：' + resp.status)
      return
    }
    const blob = await resp.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${form.projectName}_申报书.docx`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('申报书已生成')
  } catch (e) {
    console.error(e)
    ElMessage.error('生成失败')
  } finally { loading.value = false }
}

const statusText = (s) => ({ 0:'草稿', 1:'待秘书审核', 2:'待院长审核', 3:'已通过', '-1':'秘书驳回', '-2':'院长驳回' }[s] || '未知')
const statusTag = (s) => ({ 0:'info', 1:'warning', 2:'warning', 3:'success', '-1':'danger', '-2':'danger' }[s] || '')

onMounted(() => { fetchData(); fetchMyProjects() })
</script>
