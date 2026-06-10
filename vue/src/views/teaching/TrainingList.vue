<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <span>培训 / 签到记录</span>
        <el-button v-if="canAdd" type="primary" @click="openDlg">登记培训</el-button>
        <el-button v-if="store.isAdmin || store.isSchool" type="default" @click="exportTrainings">导出培训</el-button>
      </div>
    </template>
    <el-table :data="table.records" v-loading="loading" border>
      <el-table-column type="index" label="#" width="50" />
      <el-table-column label="学员" min-width="120">
        <template #default="{ row }">{{ row.studentNickname || '—' }}</template>
      </el-table-column>
      <el-table-column label="教练" min-width="120">
        <template #default="{ row }">{{ row.coachNickname || '—' }}</template>
      </el-table-column>
      <el-table-column label="科目">
        <template #default="{ row }">{{ formatSubject(row.subjectType) }}</template>
      </el-table-column>
      <el-table-column prop="trainingDate" label="日期" />
      <el-table-column prop="checkInTime" label="签到时间" width="170" />
      <el-table-column prop="durationHours" label="学时" width="80" />
      <el-table-column prop="score" label="评分" width="80" />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
    </el-table>
    <el-pagination
      class="pager"
      background
      layout="prev, pager, next, total"
      :total="table.total"
      v-model:current-page="query.current"
      :page-size="query.size"
      @current-change="load"
    />
    <el-dialog v-model="dlg" title="登记培训" width="520px" @close="resetForm">
      <el-form :model="form" label-width="100px">
        <el-form-item label="学员" required>
          <el-select
            v-if="store.isCoach || store.isAdmin || store.isSchool"
            v-model="form.studentId"
            filterable
            placeholder="选择学员"
            style="width: 100%"
          >
            <el-option
              v-for="s in studentOpts"
              :key="s.id"
              :label="s.nickname"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="store.isAdmin || store.isSchool" label="教练">
          <el-select v-model="form.coachId" filterable placeholder="选择教练" style="width: 100%">
            <el-option
              v-for="c in coachOpts"
              :key="c.coachId"
              :label="c.nickname || c.username"
              :value="c.coachId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="form.subjectType" style="width: 100%">
            <el-option label="科目二" value="SUBJECT2" />
            <el-option label="科目三" value="SUBJECT3" />
          </el-select>
        </el-form-item>
        <el-form-item label="培训日期">
          <el-date-picker v-model="form.trainingDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="学时">
          <el-input-number v-model="form.durationHours" :min="0" :step="0.5" />
        </el-form-item>
        <el-form-item label="评分"><el-input-number v-model="form.score" :min="0" :max="100" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { trainingsPage, trainingCreate, reportExportTrainings, studentsOptions, coachOptions } from '@/api/biz'
import { useUserStore } from '@/stores/user'
import { formatSubject } from '@/utils/labelMaps'
import { downloadBlob } from '@/utils/download'

const store = useUserStore()
const canAdd = computed(() => store.isAdmin || store.isSchool || store.isCoach)
const loading = ref(false)
const query = reactive({ current: 1, size: 10 })
const table = reactive({ records: [], total: 0 })
const dlg = ref(false)
const studentOpts = ref([])
const coachOpts = ref([])
const form = reactive({
  studentId: null,
  coachId: null,
  subjectType: 'SUBJECT2',
  trainingDate: '',
  durationHours: 2,
  score: 100,
  content: '',
})

async function loadStudents() {
  studentOpts.value = await studentsOptions({ limit: 100 })
}

async function loadCoaches() {
  if (store.isAdmin || store.isSchool) {
    coachOpts.value = await coachOptions()
  }
}

async function openDlg() {
  await loadStudents()
  await loadCoaches()
  if (store.isCoach && studentOpts.value.length === 1) {
    form.studentId = studentOpts.value[0].id
  }
  dlg.value = true
}

function resetForm() {
  Object.assign(form, {
    studentId: null,
    coachId: null,
    subjectType: 'SUBJECT2',
    trainingDate: '',
    durationHours: 2,
    score: 100,
    content: '',
  })
}

async function load() {
  loading.value = true
  try {
    const res = await trainingsPage(query)
    table.records = res.records
    table.total = res.total
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.studentId) {
    ElMessage.warning('请选择学员')
    return
  }
  if (!form.trainingDate) {
    ElMessage.warning('请选择培训日期')
    return
  }
  const payload = { ...form }
  if (store.isCoach) {
    delete payload.coachId
  }
  await trainingCreate(payload)
  ElMessage.success('已保存')
  dlg.value = false
  load()
}

onMounted(load)

async function exportTrainings() {
  try {
    const blob = await reportExportTrainings()
    downloadBlob(blob, 'trainings.csv')
    ElMessage.success('导出已开始')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
