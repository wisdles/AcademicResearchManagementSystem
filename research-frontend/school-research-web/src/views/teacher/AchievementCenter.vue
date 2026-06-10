<script setup>
import { useRoute } from 'vue-router'
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import AchievementForm from '@/components/AchievementForm.vue'
import AchievementList from '@/components/AchievementList.vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'

const route = useRoute()
const type = computed(() => route.query.type || 'project')  // 响应式读取 query.type

const activeTab = ref('form')
const editData = ref(null)    // 用来存要修改的数据
const listRef = ref(null)     // 列表组件实例引用

// 监听路由变化 (比如从项目切换到论文)，重置状态
watch(() => route.query.type, () => {
  activeTab.value = 'form'
  editData.value = null
})

// 🟢 核心逻辑：处理列表点击“修改”
const handleEdit = (row) => {
  console.log('父组件收到编辑请求:', row)
  
  // 1. 将行数据深拷贝一份存入 editData
  // (必须拷贝，否则表单修改会直接影响列表显示，虽然切tab了看不见，但逻辑上不严谨)
  editData.value = { ...row }
  
  // 2. 切换 Tab 到表单页
  activeTab.value = 'form'
}

// 处理手动点击 Tab
const handleTabClick = (tab) => {
  if (tab.paneName === 'form') {
    // 如果是用户手动点“成果申报”，说明是想新增，不是修改
    // 如果此时 activeTab 已经是 form 且有 editData (也就是正在修改中)，就不清空
    // 但通常逻辑是：手动点标签页 = 新增，点击列表修改 = 修改
    // 这里我们可以做一个简单的判断：如果是从列表点过来的，editData 会有值
    // 如果直接点 Tab，我们清空 editData 变成新增模式
    if (activeTab.value === 'form' && !editData.value) {
       // 已经是新增模式，不做啥
    } else {
       // 这里策略：只要手动点 Tab，就视为新增，清空编辑数据
       // (注意：这取决于你的 editData 什么时候被赋值，上面 handleEdit 会赋值并切 Tab，不会触发这个点击事件)
       editData.value = null 
    }
  }
}
// --- 共同作者功能 ---
const shareVisible = ref(false)
const shareTarget = ref(null)
const shareUserIds = ref([])
const shareCandidates = ref([])
const shareLoading = ref(false)
const participationList = ref([])

const fetchParticipation = async () => {
  try {
    const res = await request.get('/share/my-participation')
    if (res.code === 200) participationList.value = res.data || []
  } catch (e) { console.error(e) }
}

const handleShare = async (row) => {
  shareTarget.value = row
  shareUserIds.value = []
  try {
    const res = await request.get('/notice/urge/candidates')
    if (res.code === 200) shareCandidates.value = res.data || []
  } catch (e) { console.error(e) }
  shareVisible.value = true
}

const confirmShare = async () => {
  if (shareUserIds.value.length === 0) {
    ElMessage.warning('请至少选择一个共同作者')
    return
  }
  shareLoading.value = true
  try {
    const res = await request.post('/share/add', {
      achievementType: type.value,
      achievementId: shareTarget.value.id,
      sharedUserIds: shareUserIds.value,
      roleType: 'CO_AUTHOR'
    })
    if (res.code === 200) {
      ElMessage.success(res.data)
      shareVisible.value = false
      fetchParticipation()
    }
  } finally { shareLoading.value = false }
}

// 页面加载时获取参与成果
onMounted(fetchParticipation)

// 处理表单提交成功
const handleFormSuccess = () => {
  // 1. 切回列表
  activeTab.value = 'list'
  // 2. 清空编辑数据
  editData.value = null
  // 3. 刷新列表数据 (需配合 nextTick 等待组件显示)
  nextTick(() => {
    if (listRef.value) {
      listRef.value.fetchData()
    }
  })
}
</script>

<template>
  <el-card>
       <!-- 这里的 activeTab 控制当前显示哪一页 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="成果申报" name="form">
          <!-- 
           1. 传入 editData 给表单回显 
           2. 监听 success 事件，提交成功后自动切回列表
        -->
        <AchievementForm 
          :key="type"
          :type="type" 
          :initial-data="editData" 
          @success="handleFormSuccess"
        />
      </el-tab-pane>
      <el-tab-pane label="我的成果" name="list">
         <!-- 
           3. 监听 @edit 事件，接收子组件传来的 row 数据
           4. ref="listRef" 用于提交成功后调用子组件刷新方法
        -->
        <AchievementList
          ref="listRef"
          :key="type"
          :type="type"
          @edit="handleEdit"
          @share="handleShare"
        />
      </el-tab-pane>

      <!-- 我参与的成果 -->
      <el-tab-pane label="我参与的成果" name="participation">
        <el-empty v-if="!participationList.length" description="暂无参与成果" :image-size="80" />
        <el-table v-else :data="participationList" border stripe>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="typeLabel" label="成果类型" width="100" />
          <el-table-column prop="name" label="成果名称" show-overflow-tooltip />
          <el-table-column prop="ownerName" label="录入人" width="120" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <!-- 共同作者弹窗 -->
  <el-dialog v-model="shareVisible" title="管理共同作者" width="500px" destroy-on-close>
    <p style="color:#6B7280;margin-bottom:12px">为「{{ shareTarget?.name || shareTarget?.title || '(未命名)' }}」添加共同作者，对方将看到此成果</p>
    <el-select v-model="shareUserIds" multiple filterable placeholder="搜索并选择共同作者" style="width:100%">
      <el-option v-for="t in shareCandidates" :key="t.id" :label="`${t.realName} (${t.username})`" :value="t.id" />
    </el-select>
    <template #footer>
      <el-button @click="shareVisible=false">取消</el-button>
      <el-button type="primary" :loading="shareLoading" @click="confirmShare">确认</el-button>
    </template>
  </el-dialog>
</template>
