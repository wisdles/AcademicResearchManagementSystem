import { createRouter, createWebHistory } from 'vue-router'
 import Login from '../views/Login.vue' 
 import Layout from '../views/Layout.vue'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { title: '首页' }
        },
        {
          path: 'profile',
          component: () => import('../views/personal/Profile.vue'),
          meta: { title: '个人中心' }
        },
        {
          path: 'achievement',
          component: () => import('../views/teacher/AchievementCenter.vue'),
          meta: { title: '成果管理' }
        },
        {
          path: 'audit/project',
          component: () => import('../views/audit/ProjectAudit.vue'),
          meta: { title: '项目审核' }
        },
        {
          path: 'dean/stats',
          component: () => import('../views/dean/Stats.vue'),
          meta: { title: '统计' }
        },
        {
          path: 'teacher/stats',
          component: () => import('../views/teacher/TeacherStats.vue'),
          meta: { title: '个人业绩看板' }
        },
        {
          path: 'messages',
          component: () => import('../views/MessageCenter.vue'),
          meta: { title: '消息中心' }
        },
        {//数据导出的
          path: 'dean/data-export',
          component: () => import('../views/dean/DataExport.vue'),
          meta: { title: '数据导出' }
        },
        {
          path: 'dean/performance',
          component: () => import('@/views/dean/Performance.vue'),
          meta: { title: '绩效考核' }
        },
        {
          path: 'user/manage',
          component: () => import('../views/admin/UserManage.vue'),
          meta: { title: '用户管理' }
        },
        {
          path: 'secretary/urge',
          component: () => import('@/views/secretary/UrgeManagement.vue'),
          meta: { title: '催报管理' }
        },
        {
          path: 'admin/college',
          name: 'CollegeManage',
          component: () => import('@/views/admin/CollegeManage.vue'),
          meta: { title: '学院管理' }
        }
      ]
    },
    {
      path: '/audit',
      component: Layout,
      meta: { title: '审核管理' },
      children: [
        { path: 'project', name: 'AuditProject', component: () => import('@/views/audit/AuditCommon.vue'), meta: { title: '项目审核', type: 'project' } },
        { path: 'paper', name: 'AuditPaper', component: () => import('@/views/audit/AuditCommon.vue'), meta: { title: '论文审核', type: 'paper' } },
        { path: 'patent', name: 'AuditPatent', component: () => import('@/views/audit/AuditCommon.vue'), meta: { title: '专利审核', type: 'patent' } },
        { path: 'software', name: 'AuditSoftware', component: () => import('@/views/audit/AuditCommon.vue'), meta: { title: '软著审核', type: 'software' } },
        { path: 'book', name: 'AuditBook', component: () => import('@/views/audit/AuditCommon.vue'), meta: { title: '专著审核', type: 'book' } },
        { path: 'award', name: 'AuditAward', component: () => import('@/views/audit/AuditCommon.vue'), meta: { title: '获奖审核', type: 'award' } },
        { path: 'competition', name: 'AuditCompetition', component: () => import('@/views/audit/AuditCommon.vue'), meta: { title: '竞赛审核', type: 'competition' } },
        { path: 'course', name: 'AuditCourse', component: () => import('@/views/audit/AuditCommon.vue'), meta: { title: '课程审核', type: 'course' } }
      ]
    }
  ]
})
export default router