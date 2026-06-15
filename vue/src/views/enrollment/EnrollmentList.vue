<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <span>招生管理</span>
        <el-button v-if="store.isAdmin || store.isSchool" type="primary" @click="openAdd">新增线索</el-button>
        <el-button v-if="store.isAdmin || store.isSchool" type="default" @click="exportEnrollments">导出招生</el-button>
      </div>
    </template>
    <el-table :data="table.records" v-loading="loading" border>
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="applicantName" label="姓名" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column prop="source" label="来源" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ formatStatus(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column v-if="store.isAdmin || store.isSchool" label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="setStatus(row, 'APPROVED')">通过</el-button>
          <el-button link type="danger" @click="setStatus(row, 'REJECTED')">拒绝</el-button>
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
    <el-dialog v-model="dlg" title="新增招生线索" width="480px" @close="reset">
      <el-form :model="form" label-width="100px">
        <el-form-item v-if="store.isAdmin" label="驾校ID">
          <el-input-number v-model="form.schoolId" :min="1" />
        </el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.applicantName" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="身份证"><el-input v-model="form.idCard" /></el-form-item>
        <el-form-item label="来源"><el-input v-model="form.source" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { enrollmentsPage, enrollmentCreate, enrollmentStatus, reportExportEnrollments } from '@/api/biz'
import { useUserStore } from '@/stores/user'
import { formatStatus } from '@/utils/labelMaps'
import { downloadBlob } from '@/utils/download'

const store = useUserStore()
const loading = ref(false)
const query = reactive({ current: 1, size: 10 })
const table = reactive({ records: [], total: 0 })
const dlg = ref(false)
const form = reactive({
  schoolId: 1,
  applicantName: '',
  phone: '',
  idCard: '',
  source: '',
  remark: '',
})

async function load() {
  loading.value = true
  try {
    const res = await enrollmentsPage(query)
    table.records = res.records
    table.total = res.total
  } finally {
    loading.value = false
  }
}

function openAdd() {
  dlg.value = true
}

function reset() {
  Object.assign(form, { schoolId: store.user?.schoolId || 1, applicantName: '', phone: '', idCard: '', source: '', remark: '' })
}

async function save() {
  await enrollmentCreate(form)
  ElMessage.success('已保存')
  dlg.value = false
  load()
}

async function setStatus(row, status) {
  await enrollmentStatus(row.id, status)
  ElMessage.success('已更新')
  load()
}

onMounted(load)

async function exportEnrollments() {
  try {
    const blob = await reportExportEnrollments()
    downloadBlob(blob, 'enrollments.csv')
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
