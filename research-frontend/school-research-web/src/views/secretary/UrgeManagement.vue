<template>
  <div class="urge-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span><el-icon><BellFilled /></el-icon> 催报管理</span>
        </div>
      </template>

      <el-alert
        title="精准催报：可选择特定教师或学院，并自定义催报内容"
        type="info"
        show-icon
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-form :model="form" label-width="120px" size="default" class="urge-form">
        <!-- 1. 催报对象 -->
        <el-form-item label="催报范围">
          <el-radio-group v-model="form.targetType" @change="onTargetTypeChange">
            <el-radio-button label="ALL">本院全部教师</el-radio-button>
            <el-radio-button label="USERS">指定教师</el-radio-button>
            <el-radio-button label="COLLEGE" v-if="isAdmin">指定学院</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 指定教师 -->
        <el-form-item label="选择教师" v-if="form.targetType === 'USERS'">
          <el-select
            v-model="form.userIds"
            multiple
            filterable
            collapse-tags
            collapse-tags-tooltip
            placeholder="搜索并选择教师（支持姓名/工号）"
            style="width: 100%"
            :loading="loadingCandidates"
          >
            <el-option
              v-for="t in candidates"
              :key="t.id"
              :label="`${t.realName} (${t.username})`"
              :value="t.id"
            />
          </el-select>
          <div class="hint">已选 {{ form.userIds.length }} 位教师</div>
        </el-form-item>

        <!-- 指定学院 -->
        <el-form-item label="选择学院" v-if="form.targetType === 'COLLEGE'">
          <el-select v-model="form.collegeIds" multiple placeholder="选择学院" style="width: 100%">
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>

        <!-- 2. 成果类型 -->
        <el-form-item label="催报成果类型">
          <el-select v-model="form.achievementType" placeholder="选择成果类型" @change="onTypeChange" style="width: 240px">
            <el-option v-for="t in achievementTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>

        <!-- 3. 催报标题 -->
        <el-form-item label="催报标题">
          <el-input v-model="form.title" placeholder="自定义标题，例如「2026春季论文催报」" maxlength="60" show-word-limit />
        </el-form-item>

        <!-- 4. 催报内容 -->
        <el-form-item label="催报内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            :placeholder="defaultContent"
            maxlength="500"
            show-word-limit
          />
          <div class="hint">留空则使用默认提醒：{{ defaultContent }}</div>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleUrge" size="large">
            <el-icon style="margin-right: 5px"><Promotion /></el-icon>
            发送催报
          </el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 快捷模板 -->
      <el-divider content-position="left">快捷模板</el-divider>
      <el-space wrap>
        <el-button v-for="tpl in templates" :key="tpl.label" @click="applyTemplate(tpl)" plain>
          {{ tpl.label }}
        </el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { BellFilled, Promotion } from '@element-plus/icons-vue'
import request from '@/utils/request'

const role = localStorage.getItem('role') || ''
const isAdmin = computed(() => role === 'ADMIN')

const loading = ref(false)
const loadingCandidates = ref(false)
const candidates = ref([])
const colleges = ref([])

const achievementTypes = [
  { value: 'project', label: '项目' },
  { value: 'paper', label: '论文' },
  { value: 'patent', label: '专利' },
  { value: 'software', label: '软著' },
  { value: 'book', label: '专著' },
  { value: 'award', label: '获奖' },
  { value: 'competition', label: '竞赛' },
  { value: 'course', label: '课程' }
]

const form = reactive({
  targetType: 'ALL',
  userIds: [],
  collegeIds: [],
  achievementType: 'paper',
  title: '催报通知',
  content: ''
})

const defaultContent = computed(() => {
  const label = achievementTypes.find(t => t.value === form.achievementType)?.label || '成果'
  return `请您尽快提交${label}成果，谢谢配合！`
})

const templates = [
  { label: '年终冲刺', title: '年终成果催报', content: '年终考核临近，请各位教师在本周内完成全部待提交成果的录入，谢谢配合！' },
  { label: '项目结题提醒', title: '项目结题催报', content: '请已结项的项目负责人尽快上传结项证明并提交审核，逾期将影响下一周期申报。' },
  { label: 'SCI 论文催报', title: 'SCI 论文成果催报', content: '请有 SCI 收录论文的老师尽快录入系统，并上传期刊检索证明。' },
  { label: '专利申报提醒', title: '专利成果催报', content: '近期请已获专利授权的老师及时录入系统，便于学院统计科研产出。' }
]

const fetchCandidates = async () => {
  loadingCandidates.value = true
  try {
    const res = await request.get('/notice/urge/candidates')
    if (res.code === 200) candidates.value = res.data || []
  } catch (e) { console.error(e) }
  finally { loadingCandidates.value = false }
}

const fetchColleges = async () => {
  try {
    const res = await request.get('/college/list')
    if (res.code === 200) colleges.value = res.data || []
  } catch (e) { console.error(e) }
}

const onTargetTypeChange = () => {
  form.userIds = []
  form.collegeIds = []
}

const onTypeChange = () => { /* 切换类型时，标题/内容保持手动控制 */ }

const applyTemplate = (tpl) => {
  form.title = tpl.title
  form.content = tpl.content
}

const reset = () => {
  form.targetType = 'ALL'
  form.userIds = []
  form.collegeIds = []
  form.achievementType = 'paper'
  form.title = '催报通知'
  form.content = ''
}

const handleUrge = async () => {
  // 校验
  if (form.targetType === 'USERS' && form.userIds.length === 0) {
    ElMessage.warning('请至少选择一位教师')
    return
  }
  if (form.targetType === 'COLLEGE' && form.collegeIds.length === 0) {
    ElMessage.warning('请至少选择一个学院')
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请填写催报标题')
    return
  }

  // 二次确认
  const targetDesc = form.targetType === 'ALL' ? '本院全部教师'
      : form.targetType === 'USERS' ? `已选 ${form.userIds.length} 位教师`
      : `已选 ${form.collegeIds.length} 个学院`
  try {
    await ElMessageBox.confirm(
      `将向 ${targetDesc} 发送「${form.title}」催报通知，确认发送？`,
      '确认催报',
      { type: 'warning', confirmButtonText: '确认发送', cancelButtonText: '取消' }
    )
  } catch { return }

  loading.value = true
  try {
    const typeLabel = achievementTypes.find(t => t.value === form.achievementType)?.label || '成果'
    const payload = {
      targetType: form.targetType,
      userIds: form.userIds,
      collegeIds: form.collegeIds,
      achievementType: form.achievementType,
      typeLabel,
      title: form.title,
      content: form.content
    }
    const res = await request.post('/notice/urge', payload)
    if (res.code === 200) {
      ElMessage.success(res.data || '催报已发送')
    }
  } finally { loading.value = false }
}

onMounted(() => {
  fetchCandidates()
  if (isAdmin.value) fetchColleges()
})
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.urge-form { max-width: 800px; }
.hint {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 4px;
}
</style>
