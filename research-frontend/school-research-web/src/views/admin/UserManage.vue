<template>
  <div class="user-manage-container">
    <el-card>
      <!-- 顶部操作栏 -->
      <div class="header-actions">
        <div class="left">
          <el-input 
            v-model="searchKey" 
            placeholder="搜索姓名或工号" 
            style="width: 200px; margin-right: 10px" 
            clearable
            @input="handleSearch"
          />
          <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
        </div>
        <div class="right">
          <el-button type="success" icon="Plus" @click="openAddDialog">新增用户</el-button>
        </div>
      </div>

      <!-- 用户表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="username" label="工号/账号" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        
        <el-table-column prop="roleKey" label="角色身份" width="150">
          <template #default="scope">
            <el-tag :type="getRoleTag(scope.row.roleKey)">
              {{ getRoleName(scope.row.roleKey) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- <el-table-column prop="collegeId" label="所属学院ID" width="120" align="center" /> -->
         <!-- 🟢 修改点1：显示学院名称，而不是 ID -->
        <el-table-column label="所属学院" min-width="180">
          <template #default="scope">
            {{ getCollegeName(scope.row.collegeId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isFirstLogin" type="warning" size="small">未激活</el-tag>
            <el-tag v-else type="success" size="small">正常</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" prop="createTime" width="180" />

        <!-- 操作列 -->
        <el-table-column label="操作" min-width="250" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑用户' : '新增用户'" 
      width="500px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        
        <el-form-item label="工号/账号" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="用于登录的唯一账号" />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="教师真实姓名" />
        </el-form-item>

        <el-form-item label="角色身份" prop="roleKey">
          <el-select v-model="form.roleKey" placeholder="请选择角色" style="width: 100%">
            <el-option label="系统管理员" value="ADMIN" />
            <el-option label="学院院长" value="DEAN" />
            <el-option label="教学秘书" value="SEC_TEACHING" />
            <el-option label="科研秘书" value="SEC_RESEARCH" />
            <el-option label="普通教师" value="TEACHER" />
          </el-select>
        </el-form-item>
        
        <!-- 这里暂时用数字输入，后续你可以改成学院下拉框 -->
        <!-- <el-form-item label="学院ID" prop="collegeId">
          <el-input-number v-model="form.collegeId" :min="1" style="width: 100%" placeholder="请输入学院ID" />
        </el-form-item> -->
        <!-- 🟢 修改点2：学院 ID 改为 下拉选择 -->
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择所属学院" style="width: 100%">
            <el-option 
              v-for="item in collegeList" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>

      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
// // ... 引入 Clipboard 用于复制 (可选，或者直接让用户手动复制) ...
// import useClipboard from 'vue-clipboard3' // 如果你没装这个库，用下面的原生方法
// --- 数据定义 ---
const tableData = ref([])
const allData = ref([]) // 用于本地搜索过滤
const loading = ref(false)
const searchKey = ref('')
const collegeList = ref([]) // 🟢 存学院列表

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  username: '',
  realName: '',
  roleKey: 'TEACHER', // 默认教师
  collegeId: 1
})

const rules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// --- 角色字典 ---
const roleMap = {
  'ADMIN': '管理员',
  'DEAN': '院长',
  'SEC_TEACHING': '教学秘书',
  'SEC_RESEARCH': '科研秘书',
  'TEACHER': '教师'
}
const getRoleName = (key) => roleMap[key] || key
const getRoleTag = (key) => {
  if(key === 'ADMIN') return 'danger'
  if(key.startsWith('SEC')) return 'warning'
  if(key === 'DEAN') return 'success'
  return 'info'
}
// 🟢 辅助函数：根据ID获取学院名称
const getCollegeName = (id) => {
  if (!id) return '未绑定'
  const found = collegeList.value.find(c => c.id === id)
  return found ? found.name : `未知学院(ID:${id})`
}
// --- 方法 ---

// 1. 获取用户列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/list')
    if(res.code === 200) {
      allData.value = res.data
      handleSearch() // 触发本地搜索过滤
    }
  } finally {
    loading.value = false
  }
}
// 🟢 2. 获取学院列表
const fetchColleges = async () => {
  try {
    const res = await request.get('/college/list')
    if(res.code === 200) {
      collegeList.value = res.data || []
    }
  } catch(e) {
    console.error("获取学院列表失败", e)
  }
}

// 2. 本地搜索过滤
const handleSearch = () => {
  if (!searchKey.value) {
    tableData.value = allData.value
  } else {
    const k = searchKey.value.toLowerCase()
    tableData.value = allData.value.filter(item => 
      (item.username && item.username.toLowerCase().includes(k)) || 
      (item.realName && item.realName.includes(k))
    )
  }
}

// 3. 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  resetForm() // 重置表单
  dialogVisible.value = true
}

// 4. 打开编辑弹窗
const openEditDialog = (row) => {
  isEdit.value = true
  // 填充数据
  form.id = row.id
  form.username = row.username
  form.realName = row.realName
  form.roleKey = row.roleKey
  form.collegeId = row.collegeId
  dialogVisible.value = true
}

// 5. 提交表单 (新增或修改)
const submitForm = async () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          // 修改接口 /user/update
          await request.post('/user/update', form)
          ElMessage.success('修改成功')
        } else {
          // 新增接口 /user/add
          await request.post('/user/add', form)
          ElMessage.success('添加成功，初始密码 abc123')
        }
        dialogVisible.value = false
        fetchList() // 刷新列表
      } catch (e) {
        console.error(e)
      }
    }
  })
}

// 6. 重置密码
// 修改重置密码逻辑
const handleResetPwd = (row) => {
  ElMessageBox.confirm(
    `确定要重置用户 [${row.realName}] 的密码吗?`, 
    '重置确认', 
    {
      confirmButtonText: '确定重置',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 1. 调用接口
      const res = await request.post(`/user/reset-pwd/${row.id}`)
      
      if (res.code === 200) {
        // res.data 里现在是后端生成的随机密码
        const newPassword = res.data 
        
        // 2. 弹窗展示密码
        // 使用 HTML 片段让密码更显眼
        ElMessageBox.alert(
          `
            <div style="text-align: center;">
              <p>重置成功！新密码为：</p>
              <h2 style="color: #409EFF; margin: 10px 0;">${newPassword}</h2>
              <p style="color: #909399; font-size: 12px;">请复制此密码并发送给用户，提醒用户登录后立即修改。</p>
            </div>
          `,
          '重置成功',
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: '我已复制',
            callback: () => {
              // 点击确定后的回调，可以在这里尝试自动复制到剪贴板
              copyToClipboard(newPassword)
            }
          }
        )
      }
    } catch (e) {
      console.error(e)
    }
  })
}
// 辅助函数：原生 JS 复制到剪贴板
const copyToClipboard = (text) => {
  if (navigator.clipboard && window.isSecureContext) {
    // 安全域下使用新 API
    navigator.clipboard.writeText(text).then(() => {
      ElMessage.success('密码已自动复制到剪贴板')
    })
  } else {
    // 降级处理 (非 HTTPS 或旧浏览器)
    const textArea = document.createElement("textarea");
    textArea.value = text;
    textArea.style.position = "absolute";
    textArea.style.left = "-999999px";
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
    ElMessage.success('密码已自动复制到剪贴板')
  }
}
// 7. 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 [${row.realName}] 吗? 此操作不可恢复。`, '危险', {
    type: 'error',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  }).then(async () => {
    await request.post(`/user/delete/${row.id}`)
    ElMessage.success('删除成功')
    fetchList()
  })
}

// 表单重置
const resetForm = () => {
  form.id = null
  form.username = ''
  form.realName = ''
  form.roleKey = 'TEACHER'
  form.collegeId = 1
  if(formRef.value) formRef.value.resetFields()
}

onMounted(() => {
  fetchList()
  fetchColleges()
})
</script>

<style scoped>
.user-manage-container {
  padding: 20px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>