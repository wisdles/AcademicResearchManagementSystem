<template>
  <div class="message-center">
    <el-card shadow="never">
      <template #header>
        <div class="msg-header">
          <span>消息中心</span>
          <el-button type="primary" size="small" @click="handleMarkAllRead">全部标为已读</el-button>
        </div>
      </template>

      <el-table :data="messages" border stripe v-loading="loading" style="width: 100%">
        <el-table-column label="类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getTypeTag(scope.row.type)" size="small">
              {{ getTypeLabel(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="发送时间" width="180" align="center">
          <template #default="scope">
            {{ scope.row.createTime ? scope.row.createTime.substring(0, 16) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isRead ? 'info' : 'danger'" size="small">
              {{ scope.row.isRead ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button v-if="!scope.row.isRead" link type="primary" size="small" @click="handleRead(scope.row)">
              标为已读
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const messages = ref([])
const loading = ref(false)

const getTypeLabel = (type) => {
  const map = { AUDIT_RESULT: '审核结果', URGE: '催报', NOTICE: '通知', SYSTEM: '系统' }
  return map[type] || '消息'
}

const getTypeTag = (type) => {
  const map = { AUDIT_RESULT: 'warning', URGE: 'danger', NOTICE: 'primary', SYSTEM: 'info' }
  return map[type] || 'info'
}

const fetchMessages = async () => {
  loading.value = true
  try {
    const res = await request.get('/message/my-list')
    if (res.code === 200) messages.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleRead = async (row) => {
  await request.put(`/message/read/${row.id}`)
  row.isRead = 1
  ElMessage.success('已标记为已读')
}

const handleMarkAllRead = async () => {
  await request.put('/message/read-all')
  messages.value.forEach(m => m.isRead = 1)
  ElMessage.success('全部已读')
}

onMounted(fetchMessages)
</script>

<style scoped>
.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>