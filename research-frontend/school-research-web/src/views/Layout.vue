<template>
  <div class="layout">
    <!-- 左侧菜单 -->
    <el-aside width="220px" class="aside">
      <div class="logo">科研管理系统</div>
      <el-menu
        :default-active="route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <!-- 公共菜单 -->
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon> <span>首页/公告</span>
        </el-menu-item>

        <!-- 老师菜单
        <template v-if="role === 'TEACHER'">
          <el-menu-item index="/project/apply">
            <el-icon><DocumentAdd /></el-icon> <span>项目申报</span>
          </el-menu-item>
          <el-menu-item index="/project/my">
            <el-icon><List /></el-icon> <span>我的项目</span>
          </el-menu-item>
          <el-menu-item index="/paper/add">
            <el-icon><Picture /></el-icon> <span>论文录入(OCR)</span>
          </el-menu-item>
           <el-menu-item index="/patent/add">专利申报</el-menu-item>
         <el-menu-item index="/software/add">软著申报</el-menu-item>
          <el-menu-item index="/book/add">专著申报</el-menu-item>
        </template> -->
        <!-- 老师菜单 -->
        <template v-if="role === 'TEACHER'">
          <el-menu-item index="/achievement?type=project">
            <el-icon><DocumentAdd /></el-icon> <span>项目管理</span>
          </el-menu-item>
          <el-menu-item index="/achievement?type=paper">
            <el-icon><Picture /></el-icon> <span>论文管理</span>
          </el-menu-item>
          <el-menu-item index="/achievement?type=patent">
            <el-icon><Document /></el-icon> <span>专利管理</span>
          </el-menu-item>
          <el-menu-item index="/achievement?type=software">
            <el-icon><Document /></el-icon> <span>软著管理</span>
          </el-menu-item>
          <el-menu-item index="/achievement?type=book">
            <el-icon><Document /></el-icon> <span>专著管理</span>
          </el-menu-item>
          <el-menu-item index="/achievement?type=award">
            <el-icon><TrophyBase /></el-icon> <span>获奖管理</span>
          </el-menu-item>
          <el-menu-item index="/achievement?type=competition">
            <el-icon><TrophyBase /></el-icon> <span>竞赛管理</span>
          </el-menu-item>
          <el-menu-item index="/achievement?type=course">
            <el-icon><Reading /></el-icon> <span>课程管理</span>
          </el-menu-item>
          <el-menu-item index="/teacher/stats">
            <el-icon><DataAnalysis /></el-icon> <span>业绩看板</span>
          </el-menu-item>
        </template>
        <!-- 秘书菜单 -->
        <template v-if="role.startsWith('SEC_')">
          <el-menu-item index="/audit/project">
            <el-icon><Stamp /></el-icon> <span>项目审核</span>
          </el-menu-item>
          <el-menu-item index="/audit/paper">
            <el-icon><Stamp /></el-icon> <span>论文审核</span>
          </el-menu-item>
          <el-menu-item index="/audit/patent">
            <el-icon><Stamp /></el-icon> <span>专利审核</span>
          </el-menu-item>
          <el-menu-item index="/audit/software">
            <el-icon><Stamp /></el-icon> <span>软著审核</span>
          </el-menu-item>
          <el-menu-item index="/audit/book">
            <el-icon><Stamp /></el-icon> <span>专著审核</span>
          </el-menu-item>
          <el-menu-item index="/audit/award">
            <el-icon><Stamp /></el-icon> <span>获奖审核</span>
          </el-menu-item>
          <el-menu-item index="/audit/competition">
            <el-icon><Stamp /></el-icon> <span>竞赛审核</span>
          </el-menu-item>
          <el-menu-item index="/audit/course">
            <el-icon><Stamp /></el-icon> <span>课程审核</span>
          </el-menu-item>
          <el-menu-item index="/secretary/urge">
            <el-icon><Bell /></el-icon> <span>催报管理</span>
          </el-menu-item>
        </template>

        <!-- 院长菜单 -->
        <template v-if="role === 'DEAN'">
          <el-menu-item index="/audit/project">
            <el-icon><Stamp /></el-icon> <span>终审项目</span>
          </el-menu-item>
          <el-menu-item index="/audit/paper">
            <el-icon><Stamp /></el-icon> <span>终审论文</span>
          </el-menu-item>
          <el-menu-item index="/audit/patent">
            <el-icon><Stamp /></el-icon> <span>终审专利</span>
          </el-menu-item>
          <el-menu-item index="/audit/software">
            <el-icon><Stamp /></el-icon> <span>终审软著</span>
          </el-menu-item>
          <el-menu-item index="/audit/book">
            <el-icon><Stamp /></el-icon> <span>终审专著</span>
          </el-menu-item>
          <el-menu-item index="/audit/award">
            <el-icon><Stamp /></el-icon> <span>终审获奖</span>
          </el-menu-item>
          <el-menu-item index="/audit/competition">
            <el-icon><Stamp /></el-icon> <span>终审竞赛</span>
          </el-menu-item>
          <el-menu-item index="/audit/course">
            <el-icon><Stamp /></el-icon> <span>终审课程</span>
          </el-menu-item>
          <el-menu-item index="/dean/stats">
            <el-icon><PieChart /></el-icon> <span>科研统计</span>
          </el-menu-item>
          <el-menu-item index="/dean/data-export">
            <el-icon><Download /></el-icon> <span>数据导出</span>
          </el-menu-item>
          <el-menu-item index="/dean/performance">
            <el-icon><DataAnalysis /></el-icon> <span>绩效考核</span>
          </el-menu-item>
        </template>

        <!-- 审核管理菜单 -->
        <template v-if="role === 'ADMIN'">
          <el-menu-item index="/dean/stats">
            <el-icon><PieChart /></el-icon> <span>科研统计</span>
          </el-menu-item>
        </template>

         <!-- 管理员菜单 -->
        <template v-if="role === 'ADMIN'">
          <el-menu-item index="/user/manage">
            <el-icon><User /></el-icon> <span>用户管理</span>
          </el-menu-item>
           <!-- 新增这一项 -->
            <el-menu-item index="/admin/college">
              <el-icon><School /></el-icon> <!-- 需要引入 School 图标 -->
              <span>学院管理</span>
            </el-menu-item>
        </template>

      </el-menu>
    </el-aside>

    <!-- 右侧内容 -->
    <el-container>
      <el-header class="header">
        <!-- 面包屑或其他内容 -->
        <div class="header-left">
             <el-breadcrumb separator="/">
            <!-- 始终显示首页 -->
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            
            <!-- 如果当前不是首页，显示当前页标题 -->
            <el-breadcrumb-item v-if="route.path !== '/dashboard'">
              {{ route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
           <!-- 可以放面包屑，暂时留空 -->
        </div>

        <!-- 右侧用户信息下拉 -->
        <div class="header-right">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" style="margin-right: 20px">
            <el-button circle @click="router.push('/messages')">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              {{ realName }} ({{ roleText }})
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <!-- 子路由出口 -->
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
// 处理下拉菜单点击
const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
const router = useRouter()
const route = useRoute()

const role = localStorage.getItem('role') || ''
const realName = localStorage.getItem('realName') || '用户'

const roleText = computed(() => {
  const map = {
    'TEACHER': '教师',
    'SEC_TEACHING': '教学秘书',
    'SEC_RESEARCH': '科研秘书',
    'DEAN': '院长',
    'ADMIN': '管理员'
  }
  return map[role] || role
})

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

// 消息未读数
const unreadCount = ref(0)
const fetchUnreadCount = async () => {
  try {
    const res = await request.get('/message/unread-count')
    if (res.code === 200 && res.data) unreadCount.value = res.data.count || res.data
  } catch(e) {}
}
onMounted(() => {
  fetchUnreadCount()
  setInterval(fetchUnreadCount, 30000)
})
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #333;
}
.layout { height: 100vh; display: flex; }
.aside { background-color: #304156; color: white; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; font-size: 20px; font-weight: bold; background-color: #2b2f3a; }
.header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ddd; }
</style>