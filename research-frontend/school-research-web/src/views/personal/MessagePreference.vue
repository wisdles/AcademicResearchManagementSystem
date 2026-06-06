<template>
  <div class="message-preference">
    <el-card shadow="never">
      <template #header>
        <span>消息偏好设置</span>
      </template>
      <el-alert
        title="勾选要接收的消息类型，取消勾选后将不再收到对应类别的通知。"
        type="info" show-icon :closable="false" style="margin-bottom: 20px"
      />
      <el-form label-width="180px">
        <el-form-item label="审核结果通知">
          <el-switch v-model="form.auditResultEnabled" :active-value="1" :inactive-value="0" />
          <span class="hint">提交成果被审核通过/驳回时通知</span>
        </el-form-item>
        <el-form-item label="催报提醒">
          <el-switch v-model="form.urgeEnabled" :active-value="1" :inactive-value="0" />
          <span class="hint">秘书发起催报时通知</span>
        </el-form-item>
        <el-form-item label="系统公告">
          <el-switch v-model="form.noticeEnabled" :active-value="1" :inactive-value="0" />
          <span class="hint">学院/管理员发布公告时通知</span>
        </el-form-item>
        <el-form-item label="系统消息">
          <el-switch v-model="form.systemEnabled" :active-value="1" :inactive-value="0" />
          <span class="hint">账户安全、密码修改等系统消息</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save" :loading="loading">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const form = reactive({
  auditResultEnabled: 1, urgeEnabled: 1, noticeEnabled: 1, systemEnabled: 1
})

const load = async () => {
  const res = await request.get('/preference/message')
  if (res.code === 200 && res.data) Object.assign(form, res.data)
}

const save = async () => {
  loading.value = true
  try {
    const res = await request.post('/preference/message', form)
    if (res.code === 200) ElMessage.success('保存成功')
  } finally { loading.value = false }
}

onMounted(load)
</script>

<style scoped>
.hint { margin-left: 15px; color: #909399; font-size: 13px; }
</style>
