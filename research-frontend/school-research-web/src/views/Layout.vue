<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '230px'" class="aside">
      <div class="logo" :class="{ collapsed }">
        <div class="logo-icon"><el-icon :size="22"><School /></el-icon></div>
        <span v-if="!collapsed" class="logo-text">科研管理系统</span>
      </div>
      <el-scrollbar class="menu-scroll">
        <el-menu
          :default-active="route.path"
          background-color="transparent"
          text-color="#CBD5E1"
          active-text-color="#FFFFFF"
          :collapse="collapsed"
          :collapse-transition="false"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataLine /></el-icon><template #title>首页 / 公告</template>
          </el-menu-item>

          <!-- 教师 -->
          <template v-if="role === 'TEACHER'">
            <div class="menu-group-title" v-if="!collapsed">成果管理</div>
            <el-menu-item index="/achievement?type=project"><el-icon><Files /></el-icon><template #title>项目管理</template></el-menu-item>
            <el-menu-item index="/achievement?type=paper"><el-icon><EditPen /></el-icon><template #title>论文管理</template></el-menu-item>
            <el-menu-item index="/achievement?type=patent"><el-icon><Postcard /></el-icon><template #title>专利管理</template></el-menu-item>
            <el-menu-item index="/achievement?type=software"><el-icon><Cpu /></el-icon><template #title>软著管理</template></el-menu-item>
            <el-menu-item index="/achievement?type=book"><el-icon><Reading /></el-icon><template #title>专著管理</template></el-menu-item>
            <el-menu-item index="/achievement?type=award"><el-icon><Medal /></el-icon><template #title>获奖管理</template></el-menu-item>
            <el-menu-item index="/achievement?type=competition"><el-icon><TrophyBase /></el-icon><template #title>竞赛管理</template></el-menu-item>
            <el-menu-item index="/achievement?type=course"><el-icon><Notebook /></el-icon><template #title>课程管理</template></el-menu-item>
            <div class="menu-group-title" v-if="!collapsed">数据分析</div>
            <el-menu-item index="/teacher/stats"><el-icon><DataAnalysis /></el-icon><template #title>业绩看板</template></el-menu-item>
            <el-menu-item index="/teacher/profile"><el-icon><User /></el-icon><template #title>学术主页</template></el-menu-item>
            <el-menu-item index="/teacher/application"><el-icon><DocumentAdd /></el-icon><template #title>项目申报</template></el-menu-item>
          </template>

          <!-- 秘书 -->
          <template v-if="role.startsWith('SEC_')">
            <div class="menu-group-title" v-if="!collapsed">成果审核</div>
            <el-menu-item index="/audit/project"><el-icon><Files /></el-icon><template #title>项目审核</template></el-menu-item>
            <el-menu-item index="/audit/paper"><el-icon><EditPen /></el-icon><template #title>论文审核</template></el-menu-item>
            <el-menu-item index="/audit/patent"><el-icon><Postcard /></el-icon><template #title>专利审核</template></el-menu-item>
            <el-menu-item index="/audit/software"><el-icon><Cpu /></el-icon><template #title>软著审核</template></el-menu-item>
            <el-menu-item index="/audit/book"><el-icon><Reading /></el-icon><template #title>专著审核</template></el-menu-item>
            <el-menu-item index="/audit/award"><el-icon><Medal /></el-icon><template #title>获奖审核</template></el-menu-item>
            <el-menu-item index="/audit/competition"><el-icon><TrophyBase /></el-icon><template #title>竞赛审核</template></el-menu-item>
            <el-menu-item index="/audit/course"><el-icon><Notebook /></el-icon><template #title>课程审核</template></el-menu-item>
            <div class="menu-group-title" v-if="!collapsed">日常管理</div>
            <el-menu-item index="/secretary/urge"><el-icon><BellFilled /></el-icon><template #title>催报管理</template></el-menu-item>
          </template>

          <!-- 院长 -->
          <template v-if="role === 'DEAN'">
            <div class="menu-group-title" v-if="!collapsed">终审管理</div>
            <el-menu-item index="/audit/project"><el-icon><Files /></el-icon><template #title>终审项目</template></el-menu-item>
            <el-menu-item index="/audit/paper"><el-icon><EditPen /></el-icon><template #title>终审论文</template></el-menu-item>
            <el-menu-item index="/audit/patent"><el-icon><Postcard /></el-icon><template #title>终审专利</template></el-menu-item>
            <el-menu-item index="/audit/software"><el-icon><Cpu /></el-icon><template #title>终审软著</template></el-menu-item>
            <el-menu-item index="/audit/book"><el-icon><Reading /></el-icon><template #title>终审专著</template></el-menu-item>
            <el-menu-item index="/audit/award"><el-icon><Medal /></el-icon><template #title>终审获奖</template></el-menu-item>
            <el-menu-item index="/audit/competition"><el-icon><TrophyBase /></el-icon><template #title>终审竞赛</template></el-menu-item>
            <el-menu-item index="/audit/course"><el-icon><Notebook /></el-icon><template #title>终审课程</template></el-menu-item>
            <div class="menu-group-title" v-if="!collapsed">数据分析</div>
            <el-menu-item index="/dean/stats"><el-icon><PieChart /></el-icon><template #title>科研统计</template></el-menu-item>
            <el-menu-item index="/dean/performance"><el-icon><DataAnalysis /></el-icon><template #title>绩效考核</template></el-menu-item>
            <el-menu-item index="/dean/target"><el-icon><Aim /></el-icon><template #title>年度目标</template></el-menu-item>
            <el-menu-item index="/dean/analysis"><el-icon><TrendCharts /></el-icon><template #title>决策分析</template></el-menu-item>
            <el-menu-item index="/dean/data-export"><el-icon><Download /></el-icon><template #title>数据导出</template></el-menu-item>
          </template>

          <!-- 管理员 -->
          <template v-if="role === 'ADMIN'">
            <div class="menu-group-title" v-if="!collapsed">系统管理</div>
            <el-menu-item index="/user/manage"><el-icon><UserFilled /></el-icon><template #title>用户管理</template></el-menu-item>
            <el-menu-item index="/admin/college"><el-icon><OfficeBuilding /></el-icon><template #title>学院管理</template></el-menu-item>
            <el-menu-item index="/admin/score-rules"><el-icon><Operation /></el-icon><template #title>评分规则</template></el-menu-item>
            <el-menu-item index="/dean/stats"><el-icon><PieChart /></el-icon><template #title>科研统计</template></el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-button text class="collapse-btn" @click="collapsed = !collapsed">
            <el-icon :size="20"><Expand v-if="collapsed" /><Fold v-else /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.path !== '/dashboard'">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-tooltip content="刷新页面" placement="bottom">
            <el-button text class="header-icon-btn" @click="reload">
              <el-icon :size="18"><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="消息中心" placement="bottom">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="msg-badge">
              <el-button text class="header-icon-btn" @click="router.push('/messages')">
                <el-icon :size="18"><Bell /></el-icon>
              </el-button>
            </el-badge>
          </el-tooltip>
          <el-divider direction="vertical" />
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="34" class="user-avatar">{{ realName?.charAt(0) || 'U' }}</el-avatar>
              <div class="user-text">
                <div class="user-name">{{ realName }}</div>
                <div class="user-role">{{ roleText }}</div>
              </div>
              <el-icon class="dropdown-arrow"><arrow-down /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="User">个人中心</el-dropdown-item>
                <el-dropdown-item command="preference" :icon="Setting">消息偏好</el-dropdown-item>
                <el-dropdown-item command="logout" divided :icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import {
  DataLine, DocumentAdd, Reading, Bell, BellFilled, PieChart, Download, DataAnalysis,
  User, UserFilled, School, ArrowDown, Aim, TrendCharts, Files, EditPen, Postcard, Cpu,
  Medal, TrophyBase, Notebook, OfficeBuilding, Operation, Expand, Fold, Refresh, Setting, SwitchButton
} from '@element-plus/icons-vue'

const handleCommand = (command) => {
  if (command === 'logout') { localStorage.clear(); router.push('/login') }
  else if (command === 'profile') router.push('/profile')
  else if (command === 'preference') router.push('/preference')
}

const reload = () => window.location.reload()

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const role = localStorage.getItem('role') || ''
const realName = localStorage.getItem('realName') || '用户'

const roleText = computed(() => {
  const map = { 'TEACHER': '教师', 'SEC_TEACHING': '教学秘书', 'SEC_RESEARCH': '科研秘书', 'DEAN': '院长', 'ADMIN': '管理员' }
  return map[role] || role
})

const unreadCount = ref(0)
const fetchUnreadCount = async () => {
  try {
    const res = await request.get('/message/unread-count')
    if (res.code === 200 && res.data) unreadCount.value = res.data.count || res.data
  } catch (e) {}
}
onMounted(() => {
  fetchUnreadCount()
  setInterval(fetchUnreadCount, 30000)
})
</script>

<style scoped>
.layout { height: 100vh; display: flex; background: #F5F7FA; }

/* 侧边栏 */
.aside {
  background: linear-gradient(180deg, #1E293B 0%, #0F172A 100%);
  transition: width 0.25s;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 12px rgba(0,0,0,0.05);
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
}
.logo.collapsed { padding: 0; justify-content: center; }
.logo-icon {
  width: 36px; height: 36px;
  background: linear-gradient(135deg, #5B8DEF, #6C5CE7);
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.logo-text { font-size: 16px; font-weight: 600; }

.menu-scroll { flex: 1; }
.menu-scroll :deep(.el-scrollbar__view) { padding: 8px 0; }

.menu-group-title {
  padding: 16px 24px 8px;
  font-size: 11px;
  color: #64748B;
  letter-spacing: 1px;
  font-weight: 600;
  text-transform: uppercase;
}

:deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 12px;
  border-radius: 8px;
  padding-left: 16px !important;
  font-size: 14px;
}
:deep(.el-menu-item:hover) {
  background: rgba(91,141,239,0.12) !important;
  color: #fff !important;
}
:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #5B8DEF, #6C5CE7) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(91,141,239,0.3);
}

/* 顶栏 */
.main-container { display: flex; flex-direction: column; flex: 1; min-width: 0; }
.header {
  height: 60px;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #EAEDF3;
  box-shadow: 0 1px 4px rgba(0,0,0,0.02);
}
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { padding: 8px; }
.collapse-btn:hover { background: #F3F4F6; }

.header-right { display: flex; align-items: center; gap: 4px; }
.header-icon-btn {
  width: 38px; height: 38px; padding: 0;
  border-radius: 50%;
}
.header-icon-btn:hover { background: #F3F4F6; }
.msg-badge :deep(.el-badge__content) { top: 8px; right: 8px; }

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 12px 4px 4px;
  border-radius: 22px;
  cursor: pointer;
  transition: background 0.2s;
}
.user-info:hover { background: #F3F4F6; }
.user-avatar {
  background: linear-gradient(135deg, #5B8DEF, #6C5CE7);
  color: #fff;
  font-weight: 600;
}
.user-text { display: flex; flex-direction: column; line-height: 1.2; }
.user-name { font-size: 13px; font-weight: 600; color: #1F2937; }
.user-role { font-size: 11px; color: #6B7280; }
.dropdown-arrow { color: #9CA3AF; }

/* 主内容 */
.main-content {
  padding: 20px;
  background: #F5F7FA;
  overflow-y: auto;
}

/* 路由过渡 */
.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.25s ease;
}
.fade-slide-enter-from { opacity: 0; transform: translateY(8px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-8px); }
</style>
