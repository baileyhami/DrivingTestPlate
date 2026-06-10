<template>
  <div class="exam-reg-page">
    <el-card v-if="!store.isCoach" class="ydt-card official-card" shadow="hover" v-loading="infoLoading">
      <template #header>
        <div class="official-head">
          <div>
            <div class="page-title">{{ official.title || '官方考试能力 / 时间查询' }}</div>
            <div class="page-desc">{{ official.description }}</div>
          </div>
          <div class="official-actions">
            <el-tag v-if="official.remoteReachable === true" type="success" effect="plain">官方站点可访问</el-tag>
            <el-button type="primary" :disabled="!official.officialPageUrl" @click="openOfficial">在交管平台打开</el-button>
          </div>
        </div>
      </template>
      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="考试计划与预约能力以湖北省交通安全综合服务管理平台实时公布为准；本系统仅管理驾校内部报考流程。"
        class="official-alert"
      />
    </el-card>

    <el-card class="ydt-card inner-card" shadow="hover">
      <template #header>
        <div class="page-head">
          <div>
            <div class="page-title">{{ store.isCoach ? '学员考场报考' : '考场报考' }}</div>
            <div class="page-desc">
              <template v-if="store.isCoach">
                实时查看本人名下学员的报考记录与审核状态（约每 30 秒自动刷新）。
              </template>
              <template v-else>
                提交或审核驾校内部报考记录；考试日期请结合上方官方公布能力合理填报。
              </template>
            </div>
          </div>
          <div class="head-actions">
            <el-button v-if="store.isCoach" :loading="loading" @click="load">刷新</el-button>
            <el-button v-if="store.isStudent" type="primary" @click="openDlg">提交报考</el-button>
            <el-button v-if="store.isAdmin || store.isSchool" type="default" @click="exportExamRegs">导出报考</el-button>
          </div>
        </div>
      </template>
      <div v-if="store.isCoach" class="coach-toolbar">
        <el-select v-model="coachFilter.status" clearable placeholder="报考状态" style="width: 140px" @change="applyCoachFilter">
          <el-option label="已提交" value="SUBMITTED" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="考试通过" value="PASSED" />
        </el-select>
        <el-input
          v-model="coachFilter.studentName"
          clearable
          placeholder="按学员姓名筛选"
          style="width: 180px"
          @input="applyCoachFilter"
        />
        <span class="coach-hint">共 {{ table.total }} 条 · 上次刷新 {{ lastRefreshText }}</span>
      </div>
      <el-table :data="displayRecords" v-loading="loading" border stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="学员" min-width="120">
          <template #default="{ row }">{{ row.studentNickname || '—' }}</template>
        </el-table-column>
        <el-table-column label="考场" min-width="140">
          <template #default="{ row }">{{ row.venueName || '—' }}</template>
        </el-table-column>
        <el-table-column label="科目" width="90">
          <template #default="{ row }">{{ formatSubject(row.examSubject) }}</template>
        </el-table-column>
        <el-table-column prop="examDate" label="考试日" width="110" />
        <el-table-column label="报考状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" effect="plain" size="small">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="store.isCoach" prop="updateTime" label="状态更新时间" width="170" />
        <el-table-column v-if="store.isCoach" prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column v-if="store.isAdmin || store.isSchool" label="审核" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="setS(row, 'APPROVED')">通过</el-button>
            <el-button link type="danger" @click="setS(row, 'REJECTED')">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="store.isCoach && !loading && displayRecords.length === 0" description="暂无学员报考记录" />
      <el-pagination
        class="pager"
        background
        layout="prev, pager, next, total"
        :total="table.total"
        v-model:current-page="query.current"
        :page-size="query.size"
        @current-change="load"
      />
      <el-dialog v-model="dlg" title="提交报考" width="480px">
        <el-form :model="form" label-width="100px">
          <el-form-item label="考场" required>
            <el-select v-model="form.venueName" filterable placeholder="选择考场" style="width: 100%">
              <el-option v-for="v in venueOpts" :key="v.id" :label="v.name" :value="v.name" />
            </el-select>
          </el-form-item>
          <el-form-item label="科目">
            <el-select v-model="form.examSubject" style="width: 100%">
              <el-option label="科目一" value="SUBJECT1" />
              <el-option label="科目二" value="SUBJECT2" />
              <el-option label="科目三" value="SUBJECT3" />
              <el-option label="科目四" value="SUBJECT4" />
            </el-select>
          </el-form-item>
          <el-form-item label="考试日">
            <el-date-picker v-model="form.examDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dlg = false">取消</el-button>
          <el-button type="primary" @click="save">提交</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { examRegPage, examRegCreate, examRegStatus, officialExamScheduleInfo, reportExportExamRegs, venuesPage } from '@/api/biz'
import { useUserStore } from '@/stores/user'
import { formatStatus, formatSubject } from '@/utils/labelMaps'
import { downloadBlob } from '@/utils/download'

const store = useUserStore()
const loading = ref(false)
const infoLoading = ref(false)
const query = reactive({ current: 1, size: 10 })
const table = reactive({ records: [], total: 0 })
const filteredRecords = ref([])
const dlg = ref(false)
const venueOpts = ref([])
const lastRefreshAt = ref(null)
const coachFilter = reactive({ status: '', studentName: '' })
const form = reactive({ venueName: '', examSubject: 'SUBJECT2', examDate: '', remark: '' })

let refreshTimer = null

const official = reactive({
  title: '',
  description: '',
  officialPageUrl: '',
  remoteReachable: null,
})

const displayRecords = computed(() => (store.isCoach ? filteredRecords.value : table.records))

const lastRefreshText = computed(() => {
  if (!lastRefreshAt.value) return '—'
  return lastRefreshAt.value.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
})

function statusTagType(status) {
  const m = { SUBMITTED: 'info', APPROVED: 'success', REJECTED: 'danger', PASSED: 'success' }
  return m[status] || 'info'
}

function applyCoachFilter() {
  let rows = [...table.records]
  if (coachFilter.status) {
    rows = rows.filter((r) => r.status === coachFilter.status)
  }
  const name = coachFilter.studentName?.trim()
  if (name) {
    rows = rows.filter((r) => (r.studentNickname || '').includes(name))
  }
  filteredRecords.value = rows
}

function openOfficial() {
  if (!official.officialPageUrl) return
  window.open(official.officialPageUrl, '_blank', 'noopener,noreferrer')
}

async function loadOfficial() {
  if (store.isCoach) return
  infoLoading.value = true
  try {
    const data = await officialExamScheduleInfo()
    Object.assign(official, data)
  } catch {
    official.title = '官方考试能力 / 时间查询'
    official.description = '暂时无法获取官方链接配置，请联系管理员检查后端 ydt.integration.official-exam-schedule-url。'
    official.officialPageUrl = ''
  } finally {
    infoLoading.value = false
  }
}

async function load() {
  loading.value = true
  try {
    const res = await examRegPage(query)
    table.records = res.records || []
    table.total = res.total || 0
    lastRefreshAt.value = new Date()
    if (store.isCoach) {
      applyCoachFilter()
    }
  } finally {
    loading.value = false
  }
}

async function openDlg() {
  const res = await venuesPage({ current: 1, size: 200 })
  venueOpts.value = res.records || []
  dlg.value = true
}

async function save() {
  if (!form.venueName) {
    ElMessage.warning('请选择考场')
    return
  }
  await examRegCreate(form)
  ElMessage.success('已提交')
  dlg.value = false
  load()
}

async function setS(row, status) {
  await examRegStatus(row.id, status)
  ElMessage.success('已更新')
  load()
}

function startAutoRefresh() {
  if (!store.isCoach) return
  stopAutoRefresh()
  refreshTimer = setInterval(() => {
    if (document.visibilityState === 'visible') {
      load()
    }
  }, 30000)
}

function stopAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  if (store.isCoach) {
    query.size = 50
  }
  loadOfficial()
  load()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})

async function exportExamRegs() {
  try {
    const blob = await reportExportExamRegs()
    downloadBlob(blob, 'exam_registrations.csv')
    ElMessage.success('导出已开始')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}
</script>

<style scoped>
.exam-reg-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.official-card :deep(.el-card__body) {
  padding-top: 8px;
}
.official-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}
.official-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}
.head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.coach-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.coach-hint {
  font-size: 13px;
  color: var(--ydt-text-secondary);
}
.page-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--ydt-text);
}
.page-desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--ydt-text-secondary);
  line-height: 1.5;
  max-width: 560px;
}
.official-alert {
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
