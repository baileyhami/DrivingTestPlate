<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <span>练车预约</span>
        <el-button v-if="store.isStudent" type="primary" @click="openDlg">预约练车</el-button>
        <el-button v-if="store.isAdmin || store.isSchool" type="default" @click="exportAppointments">导出预约</el-button>
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
      <el-table-column label="科目" width="100">
        <template #default="{ row }">{{ formatSubject(row.subjectType) }}</template>
      </el-table-column>
      <el-table-column prop="appointmentTime" label="预约时间" width="170" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">{{ formatStatus(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280" v-if="!store.isStudent">
        <template #default="{ row }">
          <el-button link type="primary" @click="chg(row, 'CHECKED_IN')">签到</el-button>
          <el-button link type="success" @click="chg(row, 'COMPLETED')">完成</el-button>
          <el-button link type="danger" @click="chg(row, 'CANCELLED')">取消</el-button>
        </template>
      </el-table-column>
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
    <el-dialog v-model="dlg" title="预约练车" width="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="教练">
          <el-select v-model="form.coachId" placeholder="选择教练" style="width: 100%">
            <el-option v-for="c in coaches" :key="c.coachId" :label="`${c.nickname}(${c.specialty || '-'})`" :value="c.coachId" />
          </el-select>
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="form.subjectType" style="width: 100%">
            <el-option label="科目二" value="SUBJECT2" />
            <el-option label="科目三" value="SUBJECT3" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker v-model="form.appointmentTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="saveAppt">提交</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>

import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { appointmentsPage, appointmentCreate, appointmentStatus, coachOptions, reportExportAppointments } from '@/api/biz'
import { useUserStore } from '@/stores/user'
import { formatStatus, formatSubject } from '@/utils/labelMaps'
import { downloadBlob } from '@/utils/download'

const store = useUserStore()
const loading = ref(false)
const query = reactive({ current: 1, size: 10 })
const table = reactive({ records: [], total: 0 })
const dlg = ref(false)
const coaches = ref([])
const form = reactive({
  coachId: null,
  subjectType: 'SUBJECT2',
  appointmentTime: '',
  remark: '',
})

async function load() {
  loading.value = true
  try {
    const res = await appointmentsPage(query)
    table.records = res.records
    table.total = res.total
  } finally {
    loading.value = false
  }
}

async function openDlg() {
  coaches.value = await coachOptions()
  dlg.value = true
}

async function saveAppt() {
  try {
    if (!form.coachId) {
      ElMessage.error('请选择教练')
      return
    }
    if (!form.appointmentTime) {
      ElMessage.error('请选择预约时间')
      return
    }
    const payload = {
      ...form,
      appointmentTime: String(form.appointmentTime).replace(' ', 'T'),
    }
    await appointmentCreate(payload)
    ElMessage.success('预约成功')
    dlg.value = false
    load()
  } catch (err) {
    console.error(err)
  }
}

async function chg(row, status) {
  await appointmentStatus(row.id, status)
  ElMessage.success('已更新')
  load()
}

onMounted(load)

async function exportAppointments() {
  try {
    const blob = await reportExportAppointments()
    downloadBlob(blob, 'appointments.csv')
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
