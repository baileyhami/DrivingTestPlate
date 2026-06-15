import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

function defaultWorkspacePath(role) {
  const m = {
    ADMIN: '/app/dashboard',
    SCHOOL: '/app/dashboard',
    COACH: '/app/my-students',
    STUDENT: '/app/appointments',
  }
  return m[role] || '/app/appointments'
}

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/HomePage.vue'), meta: { public: true } },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { public: true } },
  {
    path: '/register/coach',
    name: 'RegisterCoach',
    component: () => import('@/views/RegisterCoach.vue'),
    meta: { public: true },
  },
  {
    path: '/register/school',
    name: 'RegisterSchool',
    component: () => import('@/views/RegisterSchool.vue'),
    meta: { public: true },
  },
  {
    path: '/app',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/app/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '数据看板', roles: ['ADMIN', 'SCHOOL'] } },
      { path: 'schools', name: 'Schools', component: () => import('@/views/admin/SchoolList.vue'), meta: { title: '驾校管理', roles: ['ADMIN'] } },
      { path: 'users', name: 'Users', component: () => import('@/views/admin/UserList.vue'), meta: { title: '账号管理', roles: ['ADMIN', 'SCHOOL'] } },
      { path: 'questions', name: 'Questions', component: () => import('@/views/admin/QuestionList.vue'), meta: { title: '题库管理', roles: ['ADMIN'] } },
      { path: 'enrollments', name: 'Enrollments', component: () => import('@/views/enrollment/EnrollmentList.vue'), meta: { title: '招生管理', roles: ['ADMIN', 'SCHOOL'] } },
      { path: 'my-driving', name: 'MyDriving', component: () => import('@/views/student/StudentDriving.vue'), meta: { title: '我的学车', roles: ['STUDENT'] } },
      { path: 'my-students', name: 'CoachStudents', component: () => import('@/views/teaching/CoachStudents.vue'), meta: { title: '我的学员', roles: ['COACH'] } },
      { path: 'appointments', name: 'Appointments', component: () => import('@/views/teaching/AppointmentList.vue'), meta: { title: '练车预约', roles: ['ADMIN', 'SCHOOL', 'COACH', 'STUDENT'] } },
      { path: 'trainings', name: 'Trainings', component: () => import('@/views/teaching/TrainingList.vue'), meta: { title: '培训签到', roles: ['ADMIN', 'SCHOOL', 'COACH', 'STUDENT'] } },
      { path: 'venues', name: 'Venues', component: () => import('@/views/exam/VenueList.vue'), meta: { title: '考场与线路', roles: ['ADMIN', 'SCHOOL', 'COACH', 'STUDENT'] } },
      { path: 'exam-reg', name: 'ExamReg', component: () => import('@/views/exam/ExamRegistration.vue'), meta: { title: '考场报考', roles: ['ADMIN', 'SCHOOL', 'COACH', 'STUDENT'] } },
      { path: 'mock-exam', name: 'MockExam', component: () => import('@/views/mock/MockExam.vue'), meta: { title: '模拟考试', roles: ['STUDENT'] } },
      { path: 'mock-records', name: 'MockRecords', component: () => import('@/views/mock/MockRecords.vue'), meta: { title: '考试记录', roles: ['STUDENT', 'COACH'] } },
      { path: 'wrong-book', name: 'WrongBook', component: () => import('@/views/mock/WrongBook.vue'), meta: { title: '错题本', roles: ['STUDENT'] } },
      { path: 'reports', name: 'Reports', component: () => import('@/views/report/ReportExport.vue'), meta: { title: '报表导出', roles: ['ADMIN', 'SCHOOL'] } },
    ],
  },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach(async (to, from, next) => {
  const store = useUserStore()
  if (to.meta.public) {
    if (store.token && (to.name === 'Login' || to.name === 'Register' || to.name === 'RegisterCoach' || to.name === 'RegisterSchool')) {
      if (!store.user) {
        try {
          await store.loadProfile()
        } catch {
          store.clearAuth()
          return next()
        }
      }
      return next(defaultWorkspacePath(store.user?.roleCode))
    }
    return next()
  }
  if (!store.token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }
  if (!store.user) {
    try {
      await store.loadProfile()
    } catch {
      store.clearAuth()
      return next({ name: 'Login' })
    }
  }
  const need = to.meta.roles
  if (need?.length && !need.includes(store.user.roleCode)) {
    return next({ path: defaultWorkspacePath(store.user.roleCode) })
  }
  next()
})

export default router
export { defaultWorkspacePath }
