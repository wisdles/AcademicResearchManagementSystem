<template>
  <div class="urge-management">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>催报管理</span>
        </div>
      </template>

      <el-alert
        title="催报功能将向本院所有教师发送消息提醒，请谨慎操作"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-row :gutter="20">
        <el-col :span="8" v-for="item in urgeTypes" :key="item.type">
          <el-card shadow="hover" class="urge-card">
            <div class="urge-item">
              <div class="urge-icon">
                <el-icon :size="40"><component :is="item.icon" /></el-icon>
              </div>
              <div class="urge-info">
                <h3>{{ item.label }}</h3>
                <p>{{ item.desc }}</p>
              </div>
            </div>
            <el-button
              type="primary"
              :loading="item.loading"
              @click="handleUrge(item)"
              style="width: 100%; margin-top: 15px"
            >
              发送催报
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentAdd, Picture, Document, TrophyBase, Reading } from '@element-plus/icons-vue'
import request from '@/utils/request'

const urgeTypes = reactive([
  { type: 'project', label: '项目', desc: '催报科研项目成果', icon: DocumentAdd, loading: false },
  { type: 'paper', label: '论文', desc: '催报学术论文成果', icon: Picture, loading: false },
  { type: 'patent', label: '专利', desc: '催报专利成果', icon: Document, loading: false },
  { type: 'software', label: '软著', desc: '催报软件著作权', icon: Document, loading: false },
  { type: 'book', label: '专著', desc: '催报专著/教材', icon: Document, loading: false },
  { type: 'award', label: '获奖', desc: '催报获奖成果', icon: TrophyBase, loading: false },
  { type: 'competition', label: '竞赛', desc: '催报竞赛成果', icon: TrophyBase, loading: false },
  { type: 'course', label: '课程', desc: '催报课程建设', icon: Reading, loading: false }
])

const handleUrge = async (item) => {
  item.loading = true
  try {
    const res = await request.post('/notice/urge', {
      achievementType: item.type,
      typeLabel: item.label
    })
    if (res.code === 200) {
      ElMessage.success(res.data || '催报成功')
    } else {
      ElMessage.error(res.msg || '催报失败')
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('催报失败，请稍后再试')
  } finally {
    item.loading = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.urge-card {
  margin-bottom: 15px;
}

.urge-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.urge-icon {
  color: #409EFF;
}

.urge-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.urge-info p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
</style>
