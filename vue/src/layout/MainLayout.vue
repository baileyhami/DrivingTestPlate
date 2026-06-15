<template>
  <el-container class="layout-root">
    <el-aside width="232px" class="aside">
      <div class="brand" @click="$router.push('/')" title="返回首页">
        <span class="brand-mark" aria-hidden="true" />
        <span class="brand-text">驾考一点通</span>
      </div>
      <el-menu
        class="side-menu"
        :default-active="active"
        router
        :background-color="'transparent'"
        text-color="#cbd5e1"
        active-text-color="#38bdf8"
      >
        <template v-for="item in menus" :key="item.path">
          <el-menu-item v-if="visible(item)" :index="item.path">
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
      <div class="aside-foot">工作台</div>
    </el-aside>
    <el-container class="right-col">
      <el-header class="header" height="56px">
        <div class="header-title">{{ currentTitle }}</div>
        <div class="header-right">
          <el-button type="primary" link @click="$router.push('/')">门户首页</el-button>
          <el-divider direction="vertical" />
          <span class="nick">{{ user?.nickname }}（{{ roleLabel(user?.roleCode) }}）</span>
          <el-divider direction="vertical" />
          <el-button type="primary" link @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <div class="main-inner">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const store = useUserStore()
const user = computed(() => store.user)

const menus = [
  { path: '/app/dashboard', title: '数据看板', roles: ['ADMIN', 'SCHOOL'] },
  { path: '/app/schools', title: '驾校管理', roles: ['ADMIN'] },
  { path: '/app/users', title: '账号管理', roles: ['ADMIN', 'SCHOOL'] },
  { path: '/app/questions', title: '题库管理', roles: ['ADMIN'] },
  { path: '/app/enrollments', title: '招生管理', roles: ['ADMIN', 'SCHOOL'] },
  { path: '/app/my-students', title: '我的学员', roles: ['COACH'] },
  { path: '/app/my-driving', title: '我的学车', roles: ['STUDENT'] },
  { path: '/app/appointments', title: '练车预约', roles: ['ADMIN', 'SCHOOL', 'COACH', 'STUDENT'] },
  { path: '/app/trainings', title: '培训签到', roles: ['ADMIN', 'SCHOOL', 'COACH', 'STUDENT'] },
  { path: '/app/venues', title: '考场与线路', roles: ['ADMIN', 'SCHOOL', 'COACH', 'STUDENT'] },
  { path: '/app/exam-reg', title: '考场报考', roles: ['ADMIN', 'SCHOOL', 'COACH', 'STUDENT'] },
  { path: '/app/mock-exam', title: '模拟考试', roles: ['STUDENT'] },
  { path: '/app/mock-records', title: '考试记录', roles: ['STUDENT', 'COACH'] },
  { path: '/app/wrong-book', title: '错题本', roles: ['STUDENT'] },
  { path: '/app/reports', title: '报表导出', roles: ['ADMIN', 'SCHOOL'] },
]

function visible(item) {
  return item.roles.includes(store.user?.roleCode)
}

const active = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')

function roleLabel(code) {
  const m = { ADMIN: '管理员', SCHOOL: '驾校', COACH: '教练', STUDENT: '学员' }
  return m[code] || code
}

function logout() {
  store.clearAuth()
  router.push({ name: 'Home' })
}
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
}
.aside {
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  border-right: 1px solid rgba(148, 163, 184, 0.15);
}
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  cursor: pointer;
}
.brand-mark {
  width: 8px;
  height: 28px;
  border-radius: 4px;
  background: linear-gradient(180deg, #38bdf8, #1450aa);
}
.brand-text {
  color: #f8fafc;
  font-weight: 700;
  font-size: 16px;
  letter-spacing: 0.02em;
}
.side-menu {
  flex: 1;
  border-right: none;
  padding: 8px 0 12px;
}
.side-menu :deep(.el-menu-item) {
  margin: 2px 10px;
  border-radius: 8px;
}
.side-menu :deep(.el-menu-item.is-active) {
  background: rgba(56, 189, 248, 0.12) !important;
}
.aside-foot {
  padding: 12px 16px 16px;
  font-size: 12px;
  color: #64748b;
  text-align: center;
  border-top: 1px solid rgba(148, 163, 184, 0.1);
}
.right-col {
  min-width: 0;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid var(--ydt-border);
  background: var(--ydt-surface);
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.04);
}
.header-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--ydt-text);
  letter-spacing: 0.01em;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.nick {
  color: var(--ydt-text-secondary);
  font-size: 14px;
}
.main {
  background: var(--ydt-page-bg);
  padding: 20px 24px 28px;
}
.main-inner {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
