<template>
  <el-card shadow="never">
    <div class="list-header">
      <h3>我的{{ typeLabel }}列表</h3>
      <el-button @click="fetchData" icon="Refresh" circle></el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe style="width: 100%">
      <!-- 序号 -->
      <el-table-column type="index" label="#" width="50" align="center" />
      
      <!-- 动态列渲染 -->
      <el-table-column
        v-for="col in columns.filter(c => c.prop !== 'status')"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        show-overflow-tooltip
      />

      <!-- 状态列 -->
      <el-table-column label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <!-- 只有 草稿(0) 或 驳回(-1,-2) 状态可以修改/删除 -->
          <template v-if="[0, -1, -2].includes(scope.row.status)">
            <el-button link type="primary" size="small" @click="$emit('edit', scope.row)">修改</el-button>
            <el-button link type="success" size="small" @click="handleSubmit(scope.row)">提交</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
          <!-- 审核中或已通过 -->
          <template v-else>
             <el-button v-if="scope.row.status === 1" link type="warning" size="small" @click="handleWithdraw(scope.row)">撤回</el-button>
             <el-button link type="info" size="small" disabled>查看详情</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ACHIEVEMENT_COLUMNS } from '@/utils/achievement.js' // 确保路径正确
import request from '@/utils/request.js' // 确保路径正确
// 如果没有引入图标库，可以删掉 icon="Refresh"

const props = defineProps({ type: String })
const emit = defineEmits(['edit'])

const list = ref([])
const loading = ref(false)

// 动态列配置
const columns = computed(() => ACHIEVEMENT_COLUMNS[props.type] || [])

// 标题
const typeLabel = computed(() => {
  const map = { paper: "论文", project: "项目", software: "软著", patent: "专利", book: "专著", award: "获奖", competition: "竞赛", course: "课程" }
  return map[props.type] || "成果"
})

// 状态字典
const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '待秘书审', 2: '待院长审', 3: '已通过', '-1': '秘书驳回', '-2': '院长驳回' }
  return map[status] || '未知'
}
const getStatusTag = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', '-1': 'danger', '-2': 'danger' }
  return map[status] || ''
}

// 核心：加载数据
const fetchData = async () => {
  loading.value = true
  try {
    // 🔴 修正点1：接口地址改成 /my-list (对应后端 Controller)
    const res = await request.get(`/${props.type}/my-list`)
    // request.js 响应拦截器通常会返回 res.data 或者 res，根据你的封装调整
    // 假设你的 request.js 返回的是后端 Result 对象
    if (res.code === 200) {
      list.value = res.data || []
    }
  } catch (e) {
    console.error(e)
    // request.js 应该处理了错误提示
  } finally {
    loading.value = false
  }
}

// 核心：提交审核
const handleSubmit = async (row) => {
  try {
    // 🔴 修正点2：接口改成 /add，并携带 isSubmit=true
    // 我们把整行数据发回去，确保字段完整
    const payload = { ...row, isSubmit: true }
    
    await request.post(`/${props.type}/add`, payload)
    
    ElMessage.success("已提交审核")
    fetchData() // 刷新列表
  } catch (e) {
    console.error(e)
  }
}

// 核心：删除
const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该条草稿吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/${props.type}/delete/${row.id}`)
      ElMessage.success("删除成功")
      fetchData() // 刷新列表
    } catch (e) {
      console.error(e)
    }
  })
}

// 撤回：将待秘书审核状态的成果撤回为草稿
const handleWithdraw = (row) => {
  ElMessageBox.confirm('确认撤回该条申报吗？撤回后状态将恢复为草稿。', '提示', {
    type: 'warning'
  }).then(async () => {
    await request.put(`/${props.type}/withdraw/${row.id}`)
    ElMessage.success("已撤回")
    fetchData()
  }).catch(() => {})
}

// 初始化
onMounted(fetchData)

// 监听 tab 切换 (type 变化时自动刷新)
watch(() => props.type, () => {
  list.value = []
  fetchData()
})

// 暴露给父组件，以便父组件提交表单成功后调用刷新
defineExpose({ fetchData })
</script>

<style scoped>
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
</style>