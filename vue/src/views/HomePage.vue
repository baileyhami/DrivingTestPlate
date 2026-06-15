<template>
  <div class="home">
    <header class="top">
      <div class="top-inner">
        <div class="brand">
          <span class="mark" />
          <span class="name">驾考一点通</span>
        </div>
        <nav class="nav">
          <el-button text type="primary" @click="scrollTo('flow')">学车流程</el-button>
          <el-button text type="primary" @click="scrollTo('exam')">模拟考试</el-button>
          <el-button v-if="store.token" text type="primary" @click="scrollTo('ai')">AI 对话</el-button>
          <el-button text type="primary" @click="scrollTo('schools')">驾校</el-button>
          <el-button v-if="store.isStudent" text type="primary" @click="scrollTo('enroll')">我的报名</el-button>
        </nav>
        <div class="actions">
          <template v-if="!store.token">
            <el-button @click="$router.push('/login')">登录</el-button>
            <el-dropdown trigger="click">
              <el-button type="primary">
                注册
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/register')">学员注册</el-dropdown-item>
                  <el-dropdown-item @click="$router.push('/register/coach')">教练注册</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="success" plain @click="$router.push('/register/school')">驾校入驻</el-button>
          </template>
          <template v-else>
            <span class="who">{{ store.user?.nickname }}（{{ roleLabel(store.user?.roleCode) }}）</span>
            <el-button type="primary" @click="goWorkspace">进入工作台</el-button>
            <el-button link type="danger" @click="logout">退出</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="content">
      <section class="hero reveal">
        <h1>学车 · 练题 · 选驾校，一站完成</h1>
        <p>科目一 / 科目四支持游客在线模拟；登录学员可保存成绩与错题本。</p>
        <div class="hero-meta">
          <div class="stats">
            <div class="stat-card reveal" data-stat="schools">
              <div class="stat-value">{{ statValues.schools }}</div>
              <div class="stat-label">入驻驾校</div>
            </div>
            <div class="stat-card reveal" data-stat="coaches">
              <div class="stat-value">{{ statValues.coaches }}</div>
              <div class="stat-label">在榜教练</div>
            </div>
            <div class="stat-card reveal" data-stat="reviews">
              <div class="stat-value">{{ statValues.reviews }}</div>
              <div class="stat-label">最新评价</div>
            </div>
          </div>
          <div class="scroll-hint" aria-hidden="true">
            <span class="scroll-dot" />
            下滑查看更多
          </div>
        </div>
      </section>

      <section id="flow" class="section reveal">
        <h2 class="section-title">学车流程</h2>
        <p class="muted mb">以下为标准学车阶段说明，点击各阶段可查看详细要求（与个人进度无关）。</p>
        <el-collapse v-model="flowOpen">
          <el-collapse-item title="科目一 · 道路交通安全法律、法规和相关知识" name="1">
            <p>学习交通法规、标志标线、安全常识；通过理论考试（100 题，90 分及格）后方可预约后续练车。</p>
          </el-collapse-item>
          <el-collapse-item title="科目二 · 场地驾驶技能" name="2">
            <p>在训练场完成倒车入库、侧方停车、坡道定点、曲线行驶、直角转弯等项目；由教练安排预约练车并记录学时。</p>
          </el-collapse-item>
          <el-collapse-item title="科目三 · 道路驾驶技能" name="3">
            <p>在实际道路上完成起步、变更车道、超车、会车、掉头等操作；注重安全意识与规范操作。</p>
          </el-collapse-item>
          <el-collapse-item title="科目四 · 安全文明驾驶常识" name="4">
            <p>理论考试（判断、单选、多选），考查紧急情况处置、恶劣天气驾驶等；通过后领取驾驶证。</p>
          </el-collapse-item>
        </el-collapse>
      </section>

      <section id="exam" class="section reveal">
        <h2 class="section-title">模拟考试（科目一 / 科目四）</h2>
        <el-alert
          v-if="!store.token"
          type="info"
          :closable="false"
          show-icon
          title="游客模式：交卷后展示分数与是否及格，成绩不会保存。"
          class="mb"
        />
        <el-alert
          v-else-if="store.token && !store.isStudent && !store.isAdmin && !store.isSchool && !store.isCoach"
          type="warning"
          :closable="false"
          show-icon
          title="当前角色不提供模拟考试与错题本，请使用学员账号体验完整功能。"
          class="mb"
        />
        <el-row v-if="!store.token || store.isStudent || store.isAdmin || store.isSchool || store.isCoach" :gutter="16">
          <el-col :xs="24" :md="12">
            <el-card shadow="never" class="exam-card">
              <template #header>科目一</template>
              <MockExamPanel :guest="!store.token || !store.isStudent" :subject="1" />
            </el-card>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-card shadow="never" class="exam-card">
              <template #header>科目四</template>
              <MockExamPanel :guest="!store.token || !store.isStudent" :subject="4" />
            </el-card>
          </el-col>
        </el-row>
      </section>

      <section id="ai" class="section reveal">
        <div class="ai-header">
          <div>
            <h2 class="section-title">AI 智能顾问</h2>
            <p class="muted">登录用户可与 AI 对话，咨询报名流程、练车安排或考试技巧。</p>
          </div>
          <el-tag type="success" effect="light" class="ai-badge">实时在线</el-tag>
        </div>
        <template v-if="store.token">
          <div class="ai-chat">
            <div class="ai-side">
              <div class="ai-side-card">
                <div class="ai-side-title">常见咨询</div>
                <div class="ai-chips">
                  <button class="ai-chip" type="button" @click="aiInput = '科目二练车如何安排更高效？'">科目二练车安排</button>
                  <button class="ai-chip" type="button" @click="aiInput = '报考需要准备哪些材料？'">报考材料清单</button>
                  <button class="ai-chip" type="button" @click="aiInput = '考试前如何快速复习？'">考前速记方法</button>
                  <button class="ai-chip" type="button" @click="aiInput = '预约练车一般提前多久？'">预约练车建议</button>
                </div>
              </div>
              <div class="ai-side-card ai-side-note">
                <div class="ai-side-title">服务说明</div>
                <p class="muted">AI 建议仅供参考，具体安排以驾校通知为准。</p>
              </div>
            </div>
            <div class="ai-main">
              <div ref="aiScrollRef" class="ai-messages">
                <div v-if="!aiMessages.length" class="ai-empty">输入问题，AI 会即时给出建议。</div>
                <div v-for="(m, idx) in aiMessages" :key="m.id || idx" :class="['ai-bubble', m.role]">
                  <span class="ai-role">{{ m.role === 'user' ? '我' : 'AI' }}</span>
                  <div class="ai-text">{{ m.content }}</div>
                </div>
                <div v-if="aiLoading" class="ai-bubble assistant">
                  <span class="ai-role">AI</span>
                  <div class="ai-text">正在思考...</div>
                </div>
              </div>
              <div class="ai-input">
                <el-input
                  v-model="aiInput"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                  placeholder="例如：科目二练车如何安排更高效？"
                  @keydown="onAiKeydown"
                />
                <div class="ai-actions">
                  <el-button type="primary" :loading="aiLoading" :disabled="!aiInput.trim()" @click="sendAi">
                    发送
                  </el-button>
                  <el-button v-if="aiMessages.length" text @click="clearAi">清空</el-button>
                </div>
              </div>
            </div>
          </div>
        </template>
        <el-alert v-else type="info" :closable="false" show-icon title="登录后即可使用 AI 对话功能。" />
      </section>

      <section id="schools" class="section reveal">
        <h2 class="section-title">驾校评分榜</h2>
        <el-collapse v-model="rankOpen" class="rank-collapse">
          <el-collapse-item title="驾校评分榜" name="schools">
            <el-table
              :data="pagedSchoolRank"
              v-loading="rankLoading"
              border
              size="small"
              class="mb table-reveal reveal"
              :row-style="rowStyle"
            >
              <el-table-column type="index" label="#" width="50">
                <template #default="{ $index }">{{ schoolPageOffset + $index + 1 }}</template>
              </el-table-column>
              <el-table-column prop="name" label="驾校" min-width="160" />
              <el-table-column prop="city" label="城市" width="100" />
              <el-table-column label="均分" width="140">
                <template #default="{ row }">
                  <el-rate :model-value="Number(row.ratingAvg || 0)" allow-half disabled />
                </template>
              </el-table-column>
              <el-table-column prop="ratingCount" label="评价数" width="90" />
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button type="primary" link @click="openSchool(row.id)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="schoolRankPage"
              :page-size="rankPageSize"
              layout="prev, pager, next"
              :total="schoolRank.length"
              class="rank-pagination"
            />
          </el-collapse-item>

          <el-collapse-item title="教练评分榜" name="coaches">
            <el-table
              :data="pagedCoachRank"
              v-loading="rankLoading"
              border
              size="small"
              class="table-reveal reveal"
              :row-style="rowStyle"
            >
              <el-table-column type="index" label="#" width="50">
                <template #default="{ $index }">{{ coachPageOffset + $index + 1 }}</template>
              </el-table-column>
              <el-table-column prop="coachName" label="教练" width="120" />
              <el-table-column prop="schoolName" label="所属驾校" min-width="160" />
              <el-table-column label="均分" width="140">
                <template #default="{ row }">
                  <el-rate :model-value="Number(row.ratingAvg || 0)" allow-half disabled />
                </template>
              </el-table-column>
              <el-table-column prop="ratingCount" label="评价数" width="90" />
            </el-table>
            <el-pagination
              v-model:current-page="coachRankPage"
              :page-size="rankPageSize"
              layout="prev, pager, next"
              :total="coachRank.length"
              class="rank-pagination"
            />
          </el-collapse-item>
        </el-collapse>

        <h2 class="mt-lg section-title">最新学员评价</h2>
        <el-table
          :data="recentReviews"
          v-loading="reviewFeedLoading"
          border
          size="small"
          empty-text="暂无评价"
          class="table-reveal reveal"
          :row-style="rowStyle"
        >
          <el-table-column prop="reviewerLabel" label="学员" width="80" />
          <el-table-column prop="schoolName" label="驾校" min-width="140" />
          <el-table-column label="驾校评分" width="120">
            <template #default="{ row }">
              <el-rate :model-value="row.schoolStars" disabled />
            </template>
          </el-table-column>
          <el-table-column prop="coachName" label="教练" width="100" />
          <el-table-column label="教练评分" width="120">
            <template #default="{ row }">
              <el-rate :model-value="row.coachStars" disabled />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="170" />
        </el-table>
      </section>

      <section v-if="store.isStudent" id="enroll" class="section reveal">
        <h2 class="section-title">我的报名</h2>
        <p v-if="!store.token" class="muted">注册并登录学员账号后，可在本页选择驾校、教练并留下联系方式完成报名。</p>
        <p v-else-if="!store.isStudent" class="muted">当前账号不是学员，请使用学员账号登录后进行报名。</p>
        <el-skeleton v-else-if="enrollLoading" :rows="3" animated />
        <template v-else-if="enrollment?.enrolled">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="驾校">{{ enrollment.schoolName }}</el-descriptions-item>
            <el-descriptions-item label="教练">{{ enrollment.coachName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="报名时间">{{ enrollment.enrollDate }}</el-descriptions-item>
            <el-descriptions-item label="手机">{{ enrollment.phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ enrollment.email }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="enrollment?.reviewSubmitted" class="review-box">
            <el-alert type="success" :closable="false" show-icon title="您已提交过评价，感谢反馈！" />
          </div>
          <div v-else-if="canReview" class="review-box">
            <h3>评价驾校与教练</h3>
            <p class="muted">报名成功后可为所报驾校与绑定教练打分（各 1–5 星），每人仅可评价一次。</p>
            <el-form inline>
              <el-form-item label="驾校">
                <el-rate v-model="review.schoolStars" />
              </el-form-item>
              <el-form-item label="教练">
                <el-rate v-model="review.coachStars" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="reviewLoading" @click="submitReview">提交评价</el-button>
              </el-form-item>
            </el-form>
          </div>
        </template>
        <template v-else-if="store.isStudent">
          <p class="muted">您尚未报名驾校。请选择驾校与教练并留下联系方式。</p>
          <el-form :model="enrollForm" label-width="100px" class="enroll-form" @submit.prevent>
            <el-form-item label="驾校" required>
              <el-select v-model="enrollForm.schoolId" placeholder="选择驾校" filterable style="width: 100%" @change="onSchoolChange">
                <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="教练" required>
              <el-select v-model="enrollForm.coachId" placeholder="先选驾校" filterable style="width: 100%" :disabled="!enrollForm.schoolId">
                <el-option v-for="c in coachOpts" :key="c.coachId" :label="c.nickname || c.username" :value="c.coachId" />
              </el-select>
            </el-form-item>
            <el-form-item label="手机" required>
              <el-input v-model="enrollForm.phone" />
            </el-form-item>
            <el-form-item label="邮箱" required>
              <el-input v-model="enrollForm.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="enrollSubmitting" @click="doEnroll">提交报名</el-button>
            </el-form-item>
          </el-form>
        </template>
      </section>

      <footer class="foot">驾考一点通 · 门户</footer>
    </main>

    <el-dialog v-model="detailVisible" title="驾校详情" width="560px" destroy-on-close>
      <template v-if="schoolDetail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="全称">{{ schoolDetail.name }}</el-descriptions-item>
          <el-descriptions-item label="简称">{{ schoolDetail.shortName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ schoolDetail.city || '—' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ schoolDetail.address || '—' }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ schoolDetail.contactPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ schoolDetail.contactEmail || '—' }}</el-descriptions-item>
          <el-descriptions-item label="工作时间">{{ schoolDetail.workHours || '—' }}</el-descriptions-item>
          <el-descriptions-item label="C1 价格">{{ schoolDetail.priceC1 != null ? `¥${schoolDetail.priceC1}` : '—' }}</el-descriptions-item>
          <el-descriptions-item label="C2 价格">{{ schoolDetail.priceC2 != null ? `¥${schoolDetail.priceC2}` : '—' }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            {{ Number(schoolDetail.ratingAvg || 0).toFixed(1) }}（{{ schoolDetail.ratingCount || 0 }} 条评价）
          </el-descriptions-item>
        </el-descriptions>
        <h4 class="mt">教练团队</h4>
        <el-table :data="schoolDetail.coaches || []" size="small" border>
          <el-table-column prop="nickname" label="教练" />
          <el-table-column prop="specialty" label="擅长" />
          <el-table-column label="评分" width="100">
            <template #default="{ row }">{{ Number(row.ratingAvg || 0).toFixed(1) }}</template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MockExamPanel from '@/components/MockExamPanel.vue'
import { useUserStore } from '@/stores/user'
import { aiChat } from '@/api/ai'
import {
  schoolsPublic,
  schoolsPublicRankings,
  coachesPublicRankings,
  schoolPublicDetail,
  coachesPublicBySchool,
  studentEnrollment,
  studentEnroll,
  studentReview,
  reviewsPublicRecent,
} from '@/api/biz'
import { defaultWorkspacePath } from '@/router'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const schoolRank = ref([])
const coachRank = ref([])
const rankLoading = ref(false)
const rankOpen = ref(['schools', 'coaches'])
const rankPageSize = 10
const schoolRankPage = ref(1)
const coachRankPage = ref(1)
const pagedSchoolRank = computed(() => {
  const start = (schoolRankPage.value - 1) * rankPageSize
  return schoolRank.value.slice(start, start + rankPageSize)
})
const pagedCoachRank = computed(() => {
  const start = (coachRankPage.value - 1) * rankPageSize
  return coachRank.value.slice(start, start + rankPageSize)
})
const schoolPageOffset = computed(() => (schoolRankPage.value - 1) * rankPageSize)
const coachPageOffset = computed(() => (coachRankPage.value - 1) * rankPageSize)
const schools = ref([])
const detailVisible = ref(false)
const schoolDetail = ref(null)
const enrollment = ref(null)
const enrollLoading = ref(false)
const enrollSubmitting = ref(false)
const coachOpts = ref([])
const reviewLoading = ref(false)
const reviewFeedLoading = ref(false)
const recentReviews = ref([])
const flowOpen = ref(['1'])
const review = reactive({ schoolStars: 5, coachStars: 5 })
const aiMessages = ref([])
const aiInput = ref('')
const aiLoading = ref(false)
const aiScrollRef = ref(null)
const enrollForm = reactive({
  schoolId: null,
  coachId: null,
  phone: '',
  email: '',
})
const statTargets = reactive({ schools: 0, coaches: 0, reviews: 0 })
const statValues = reactive({ schools: 0, coaches: 0, reviews: 0 })
const animatedStats = new Set()
let statSyncFrame = 0

const canReview = computed(
  () =>
  store.user?.canSubmitReview === true ||
  enrollment.value?.canSubmitReview === true,
)

function scrollTo(id) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function roleLabel(code) {
  const m = { ADMIN: '管理员', SCHOOL: '驾校', COACH: '教练', STUDENT: '学员' }
  return m[code] || code
}

function stageLabel(stage) {
  const m = { 1: '科目一阶段', 2: '科目二阶段', 3: '科目三阶段', 4: '科目四阶段', 5: '已结业（可评价）' }
  return m[stage] || '科目一阶段'
}

function goWorkspace() {
  router.push(defaultWorkspacePath(store.user?.roleCode))
}

function logout() {
  store.clearAuth()
  enrollment.value = null
  aiMessages.value = []
  aiInput.value = ''
  ElMessage.success('已退出')
}

async function loadRankings() {
  rankLoading.value = true
  try {
    const [s, c] = await Promise.all([schoolsPublicRankings(), coachesPublicRankings()])
    schoolRank.value = s
    coachRank.value = c
    if (schoolRankPage.value > Math.ceil((s.length || 1) / rankPageSize)) {
      schoolRankPage.value = 1
    }
    if (coachRankPage.value > Math.ceil((c.length || 1) / rankPageSize)) {
      coachRankPage.value = 1
    }
  } finally {
    rankLoading.value = false
    scheduleStatTargets()
  }
}

async function loadRecentReviews() {
  reviewFeedLoading.value = true
  try {
    recentReviews.value = await reviewsPublicRecent(15)
  } catch {
    recentReviews.value = []
  } finally {
    reviewFeedLoading.value = false
    scheduleStatTargets()
  }
}

async function openSchool(id) {
  schoolDetail.value = await schoolPublicDetail(id)
  detailVisible.value = true
}

async function loadEnrollment() {
  if (!store.token) {
    enrollment.value = null
    return
  }
  enrollLoading.value = true
  try {
    await store.loadProfile()
    if (!store.isStudent) {
      enrollment.value = null
      return
    }
    enrollment.value = await studentEnrollment()
  } finally {
    enrollLoading.value = false
  }
}

async function onSchoolChange(sid) {
  enrollForm.coachId = null
  if (!sid) {
    coachOpts.value = []
    return
  }
  coachOpts.value = await coachesPublicBySchool(sid)
}

async function doEnroll() {
  if (!enrollForm.schoolId || !enrollForm.coachId || !enrollForm.phone?.trim() || !enrollForm.email?.trim()) {
    ElMessage.warning('请完整填写报名信息')
    return
  }
  enrollSubmitting.value = true
  try {
    const res = await studentEnroll({
      schoolId: enrollForm.schoolId,
      coachId: enrollForm.coachId,
      phone: enrollForm.phone.trim(),
      email: enrollForm.email.trim(),
    })
    store.setAuth(res.token, res.user)
    ElMessage.success('报名成功')
    await loadEnrollment()
  } finally {
    enrollSubmitting.value = false
  }
}

async function submitReview() {
  if (!review.schoolStars || !review.coachStars) {
    ElMessage.warning('请完成打分')
    return
  }
  reviewLoading.value = true
  try {
    await studentReview({
      schoolStars: review.schoolStars,
      coachStars: review.coachStars,
    })
    ElMessage.success('感谢您的评价')
    await store.loadProfile()
    await loadEnrollment()
    await loadRankings()
    await loadRecentReviews()
  } finally {
    reviewLoading.value = false
  }
}

watch(
  () => store.token,
  () => {
    loadEnrollment()
  },
)

let revealObserver

function prefersReducedMotion() {
  return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

function updateStatTargets() {
  statTargets.schools = schoolRank.value?.length || 0
  statTargets.coaches = coachRank.value?.length || 0
  statTargets.reviews = recentReviews.value?.length || 0
  if (prefersReducedMotion()) {
    statValues.schools = statTargets.schools
    statValues.coaches = statTargets.coaches
    statValues.reviews = statTargets.reviews
    return
  }
  if (animatedStats.has('schools')) statValues.schools = statTargets.schools
  if (animatedStats.has('coaches')) statValues.coaches = statTargets.coaches
  if (animatedStats.has('reviews')) statValues.reviews = statTargets.reviews
}

function scheduleStatTargets() {
  if (statSyncFrame) return
  statSyncFrame = requestAnimationFrame(() => {
    statSyncFrame = 0
    updateStatTargets()
  })
}

function animateCount(key) {
  if (animatedStats.has(key)) return
  animatedStats.add(key)
  const to = Number(statTargets[key] || 0)
  const from = Number(statValues[key] || 0)
  if (prefersReducedMotion() || to === from) {
    statValues[key] = to
    return
  }
  const duration = 700
  const start = performance.now()
  const step = (now) => {
    const progress = Math.min((now - start) / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    statValues[key] = Math.round(from + (to - from) * eased)
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

function rowStyle({ rowIndex }) {
  return { '--row-index': rowIndex }
}

function initReveal() {
  const nodes = Array.from(document.querySelectorAll('.reveal'))
  if (!nodes.length) return
  if (prefersReducedMotion()) {
    document.documentElement.classList.add('reduced-motion')
    nodes.forEach((el) => el.classList.add('in-view'))
    return
  }
  revealObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('in-view')
          const statKey = entry.target.dataset?.stat
          if (statKey) animateCount(statKey)
          revealObserver.unobserve(entry.target)
        }
      })
    },
    { threshold: 0.15, rootMargin: '0px 0px -8% 0px' },
  )
  nodes.forEach((el) => revealObserver.observe(el))
}

let revealInitHandle = null
let revealUsesIdleCallback = false

function scheduleRevealInit() {
  const run = () => {
    revealInitHandle = null
    initReveal()
  }
  if (typeof window.requestIdleCallback === 'function') {
    revealUsesIdleCallback = true
    revealInitHandle = window.requestIdleCallback(run, { timeout: 800 })
  } else {
    revealUsesIdleCallback = false
    revealInitHandle = window.setTimeout(run, 0)
  }
}

function scrollChatToBottom() {
  const el = aiScrollRef.value
  if (!el) return
  el.scrollTop = el.scrollHeight
}

async function sendAi() {
  if (aiLoading.value) return
  if (!aiInput.value.trim()) return
  const content = aiInput.value.trim()
  aiMessages.value.push({ id: Date.now(), role: 'user', content })
  aiInput.value = ''
  aiLoading.value = true
  await nextTick()
  scrollChatToBottom()
  try {
    const history = aiMessages.value
      .filter((m) => m.role === 'user' || m.role === 'assistant')
      .slice(-6)
      .map((m) => ({ role: m.role, content: m.content }))
    const res = await aiChat({ message: content, history })
    aiMessages.value.push({ id: Date.now() + 1, role: 'assistant', content: res.reply })
  } catch (err) {
    ElMessage.error(err?.message || 'AI 服务暂时不可用')
  } finally {
    aiLoading.value = false
    await nextTick()
    scrollChatToBottom()
  }
}

function clearAi() {
  aiMessages.value = []
}

function onAiKeydown(e) {
  if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
    e.preventDefault()
    sendAi()
  }
}

onMounted(async () => {
  const [schoolsData] = await Promise.all([
    schoolsPublic(),
    loadRankings(),
    loadRecentReviews(),
    loadEnrollment(),
  ])
  schools.value = schoolsData
  scheduleStatTargets()
  await nextTick()
  scheduleRevealInit()
  if (route.query.scroll === 'enroll') {
    scrollTo('enroll')
  }
})

onBeforeUnmount(() => {
  if (statSyncFrame) {
    cancelAnimationFrame(statSyncFrame)
    statSyncFrame = 0
  }
  if (revealInitHandle != null) {
    if (revealUsesIdleCallback && typeof window.cancelIdleCallback === 'function') {
      window.cancelIdleCallback(revealInitHandle)
    } else {
      clearTimeout(revealInitHandle)
    }
    revealInitHandle = null
  }
  if (revealObserver) {
    revealObserver.disconnect()
    revealObserver = null
  }
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
  position: relative;
  overflow-x: hidden;
}
.home::before,
.home::after {
  content: '';
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.35;
  pointer-events: none;
  z-index: 0;
  animation: floatGlow 18s ease-in-out infinite;
}
.home::before {
  top: -140px;
  right: -120px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.6), transparent 60%);
}
.home::after {
  bottom: -180px;
  left: -120px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.45), transparent 60%);
  animation-delay: 4s;
}

.top {
  position: sticky;
  top: 0;
  z-index: 20;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--el-border-color-lighter);
}
.top-inner {
  max-width: 1100px;
  margin: 0 auto;
  padding: 10px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  color: #0f172a;
  position: relative;
}
.mark {
  width: 8px;
  height: 26px;
  border-radius: 4px;
  background: linear-gradient(180deg, #38bdf8, #1450aa);
  box-shadow: 0 0 12px rgba(56, 189, 248, 0.6);
  animation: pulseMark 2.6s ease-in-out infinite;
}

.nav {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.who {
  font-size: 13px;
  color: #64748b;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.content {
  max-width: 1100px;
  margin: 0 auto;
  padding: 36px 20px 56px;
  position: relative;
  z-index: 1;
}
.hero {
  text-align: center;
  padding: 28px 0 36px;
  position: relative;
}
.hero h1 {
  font-size: clamp(28px, 3.4vw, 40px);
  letter-spacing: 0.5px;
  margin-bottom: 10px;
}
.hero p {
  font-size: 15px;
  color: #475569;
  line-height: 1.7;
}
.hero::before {
  content: '';
  position: absolute;
  inset: -10px 10% auto 10%;
  height: 140px;
  background: radial-gradient(circle at top, rgba(56, 189, 248, 0.2), transparent 70%);
  pointer-events: none;
  z-index: -1;
}

.hero-meta {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: center;
}
.stats {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}
.stat-card {
  min-width: 120px;
  padding: 10px 16px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(6px);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}
.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}
.scroll-hint {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #94a3b8;
}
.scroll-dot {
  width: 22px;
  height: 34px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.7);
  position: relative;
}
.scroll-dot::after {
  content: '';
  position: absolute;
  top: 6px;
  left: 50%;
  width: 4px;
  height: 4px;
  border-radius: 999px;
  background: #94a3b8;
  transform: translateX(-50%);
  animation: scrollPulse 1.6s ease-in-out infinite;
}

.section {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.95));
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 6px 22px rgba(15, 23, 42, 0.06);
  border: 1px solid rgba(148, 163, 184, 0.2);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  content-visibility: auto;
  contain-intrinsic-size: 1px 520px;
  position: relative;
  overflow: hidden;
}
.section::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: linear-gradient(90deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px),
    linear-gradient(0deg, rgba(148, 163, 184, 0.06) 1px, transparent 1px);
  background-size: 40px 40px;
  opacity: 0.4;
  pointer-events: none;
  mask-image: linear-gradient(180deg, rgba(255, 255, 255, 0.6), transparent 70%);
}
.section:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
}

.section-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  position: relative;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 10px;
  padding-bottom: 8px;
}
.section-title::before {
  content: '';
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #38bdf8, #1d4ed8);
  box-shadow: 0 0 10px rgba(56, 189, 248, 0.45);
  transform: translateY(1px);
  animation: titlePulse 2.6s ease-in-out infinite;
}
.section-title::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: linear-gradient(90deg, rgba(148, 163, 184, 0.4), rgba(148, 163, 184, 0));
}
.section-title:hover::before {
  transform: translateY(1px) scale(1.05);
}

.reveal {
  opacity: 0;
  transform: translateY(14px);
  transition: opacity 0.6s ease, transform 0.6s ease;
  will-change: opacity, transform;
}
.reveal.in-view {
  opacity: 1;
  transform: translateY(0);
}

.table-reveal .el-table__body-wrapper tbody tr {
  opacity: 0;
  transform: translateY(6px);
  will-change: opacity, transform;
}
.table-reveal.in-view .el-table__body-wrapper tbody tr {
  animation: rowRise 0.45s ease forwards;
  animation-delay: calc(var(--row-index) * 45ms);
}

.reduced-motion .scroll-dot::after {
  animation: none;
}
.reduced-motion .table-reveal .el-table__body-wrapper tbody tr {
  opacity: 1;
  transform: none;
  animation: none;
}

.ai-chat {
  display: grid;
  gap: 16px;
  grid-template-columns: minmax(0, 220px) minmax(0, 1fr);
}
.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.ai-badge {
  font-weight: 600;
}
.ai-side {
  display: grid;
  gap: 12px;
}
.ai-side-card {
  border-radius: 14px;
  padding: 14px;
  background: linear-gradient(160deg, rgba(248, 250, 252, 0.9), rgba(226, 232, 240, 0.7));
  border: 1px solid rgba(148, 163, 184, 0.25);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}
.ai-side-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 10px;
}
.ai-side-note {
  background: #f1f5f9;
}
.ai-chips {
  display: grid;
  gap: 8px;
}
.ai-chip {
  border: none;
  background: #fff;
  border-radius: 10px;
  padding: 8px 10px;
  font-size: 12px;
  color: #1e293b;
  cursor: pointer;
  text-align: left;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.ai-chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}
.ai-main {
  display: grid;
  gap: 12px;
}
.ai-messages {
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 12px;
  padding: 14px;
  background: radial-gradient(circle at top, #f8fafc, #eef2ff 70%);
  max-height: 320px;
  overflow-y: auto;
}
.ai-empty {
  font-size: 13px;
  color: #94a3b8;
  text-align: center;
  padding: 18px 0;
}
.ai-bubble {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.2);
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  max-width: 88%;
}
.ai-bubble.user {
  align-self: flex-end;
  background: linear-gradient(160deg, #eff6ff, #dbeafe);
  border-color: rgba(59, 130, 246, 0.25);
}
.ai-role {
  font-size: 12px;
  color: #64748b;
}
.ai-text {
  font-size: 14px;
  color: #0f172a;
  line-height: 1.6;
  white-space: pre-wrap;
}
.ai-input {
  display: grid;
  gap: 10px;
}
.ai-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 900px) {
  .ai-chat {
    grid-template-columns: 1fr;
  }
}

@keyframes floatGlow {
  0%,
  100% {
    transform: translateY(0) translateX(0);
  }
  50% {
    transform: translateY(16px) translateX(-12px);
  }
}

@keyframes pulseMark {
  0%,
  100% {
    transform: scaleY(1);
    opacity: 1;
  }
  50% {
    transform: scaleY(1.08);
    opacity: 0.85;
  }
}

@keyframes scrollPulse {
  0%,
  100% {
    transform: translateX(-50%) translateY(0);
    opacity: 0.6;
  }
  50% {
    transform: translateX(-50%) translateY(10px);
    opacity: 1;
  }
}

@keyframes titlePulse {
  0%,
  100% {
    opacity: 0.85;
    transform: translateY(1px) scale(1);
  }
  50% {
    opacity: 1;
    transform: translateY(1px) scale(1.1);
  }
}

@keyframes rowRise {
  0% {
    opacity: 0;
    transform: translateY(8px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
