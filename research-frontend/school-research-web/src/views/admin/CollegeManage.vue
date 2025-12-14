<template>
  <div class="college-manage-container">
    <el-card>
      <div class="header">
        <h3>学院管理</h3>
        <el-button type="primary" icon="Plus" @click="openAdd">新增学院</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="学院名称" />
        <el-table-column prop="createTime" label="创建时间" />
        
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button link type="primary" @click="openEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑学院' : '新增学院'" 
      width="400px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="学院名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入学院名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: ''
})

const rules = {
  name: [{ required: true, message: '请输入学院名称', trigger: 'blur' }]
}

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/college/list')
    if (res.code === 200) {
      tableData.value = res.data
    }
  } finally {
    loading.value = false
  }
}

// 打开新增
const openAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑
const openEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  dialogVisible.value = true
}

// 提交
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await request.post('/college/update', form)
          ElMessage.success('修改成功')
        } else {
          await request.post('/college/add', form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        fetchList()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除 [${row.name}] 吗？`, '警告', {
    type: 'warning'
  }).then(async () => {
    await request.post(`/college/delete/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  })
}

const resetForm = () => {
  form.id = null
  form.name = ''
  if(formRef.value) formRef.value.resetFields()
}

onMounted(fetchList)
</script>

<style scoped>
.college-manage-container { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; }
</style>