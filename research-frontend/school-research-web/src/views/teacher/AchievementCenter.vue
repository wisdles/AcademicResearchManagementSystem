<script setup>
import { useRoute } from 'vue-router'
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import AchievementForm from '@/components/AchievementForm.vue'
import AchievementList from '@/components/AchievementList.vue'
import request from '@/utils/request.js'
import { ElMessage } from 'element-plus'

const route = useRoute()
const type = computed(() => route.query.type || 'project')

const activeTab = ref('form')
const editData = ref(null)
const listRef = ref(null)

// 当切换成果类型时（例如从项目切到论文），重置为新增模式
watch(type, () => {
  activeTab.value = 'form'
  editData.value = null
})

const handleEdit = (row) => {
  editData.value = { ...row }
  activeTab.value = 'form'
}

const handleFormSuccess = () => {
  activeTab.value = 'list'
  editData.value = null
  nextTick(() => listRef.value?.fetchData())
}

// --- 共同作者 ---
const shareVisible = ref(false)
const shareTarget = ref(null)
const shareUserIds = ref([])
const shareCandidates = ref([])
const shareLoading = ref(false)
const participationList = ref([])

onMounted(async () => {
  try {
    const res = await request.get('/share/my-participation')
    if (res.code === 200) participationList.value = res.data || []
  } catch (e) { console.error(e) }
})

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
  if (!shareUserIds.value.length) return ElMessage.warning('请至少选择一个共同作者')
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
      // refresh participation
      const r = await request.get('/share/my-participation')
      if (r.code === 200) participationList.value = r.data || []
    }
  } finally { shareLoading.value = false }
}
</script>

<template>
  <el-card>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="成果申报" name="form">
        <AchievementForm
          :type="type"
          :initial-data="editData"
          @success="handleFormSuccess"
        />
      </el-tab-pane>

      <el-tab-pane label="我的成果" name="list">
        <AchievementList
          ref="listRef"
          :type="type"
          @edit="handleEdit"
          @share="handleShare"
        />
      </el-tab-pane>

      <el-tab-pane label="我参与的成果" name="participation">
        <el-empty v-if="!participationList.length" description="暂无参与的成果" :image-size="80" />
        <el-table v-else :data="participationList" border stripe>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="typeLabel" label="成果类型" width="100" />
          <el-table-column prop="name" label="成果名称" show-overflow-tooltip />
          <el-table-column prop="ownerName" label="录入人" width="120" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <el-dialog v-model="shareVisible" title="管理共同作者" width="500px" destroy-on-close>
    <p style="color:#6B7280;margin-bottom:12px">
      为「{{ shareTarget?.name || shareTarget?.title || '(未命名)' }}」添加共同作者
    </p>
    <el-select v-model="shareUserIds" multiple filterable placeholder="搜索并选择共同作者" style="width:100%">
      <el-option v-for="t in shareCandidates" :key="t.id" :label="`${t.realName} (${t.username})`" :value="t.id" />
    </el-select>
    <template #footer>
      <el-button @click="shareVisible = false">取消</el-button>
      <el-button type="primary" :loading="shareLoading" @click="confirmShare">确认</el-button>
    </template>
  </el-dialog>
</template>
