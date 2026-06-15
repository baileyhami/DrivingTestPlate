<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <span>账号管理</span>
        <div>
          <el-button v-if="store.isSchool || store.isAdmin" @click="openCoach">新增教练</el-button>
          <el-button v-if="store.isSchool || store.isAdmin" type="primary" @click="openStudent">新增学员</el-button>
          <el-button v-if="store.isSchool || store.isAdmin" type="default" @click="exportUsers">导出账号</el-button>
        </div>
      </div>
    </template>
    <el-form inline class="filter">
      <el-form-item label="角色">
        <el-select v-model="query.roleCode" clearable placeholder="全部" style="width: 140px" @change="load">
          <el-option value="COACH" label="教练" />
          <el-option value="STUDENT" label="学员" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-table :data="table.records" v-loading="loading" border>
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="roleCode" label="角色" width="100" />
      <el-table-column prop="schoolId" label="驾校ID" width="90" />
      <el-table-column prop="status" label="状态" width="80" />
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
    <el-dialog v-model="coachDlg" title="新增教练" width="520px">
      <el-form :model="coachForm" label-width="100px">
        <el-form-item v-if="store.isAdmin" label="驾校ID"><el-input-number v-model="coachForm.schoolId" :min="1" /></el-form-item>
        <el-form-item label="用户名"><el-input v-model="coachForm.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="coachForm.password" type="password" show-password /></el-form-item>
        <el-form-item label="手机"><el-input v-model="coachForm.phone" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="coachForm.nickname" /></el-form-item>
        <el-form-item label="教练证"><el-input v-model="coachForm.licenseNo" /></el-form-item>
        <el-form-item label="擅长"><el-input v-model="coachForm.specialty" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="coachDlg = false">取消</el-button>
        <el-button type="primary" @click="saveCoach">保存</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="stuDlg" title="新增学员" width="520px">
      <el-form :model="stuForm" label-width="100px">
        <el-form-item v-if="store.isAdmin" label="驾校ID"><el-input-number v-model="stuForm.schoolId" :min="1" /></el-form-item>
        <el-form-item label="绑定教练ID"><el-input-number v-model="stuForm.coachId" :min="0" /></el-form-item>
        <el-form-item label="用户名"><el-input v-model="stuForm.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="stuForm.password" type="password" show-password /></el-form-item>
        <el-form-item label="手机"><el-input v-model="stuForm.phone" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="stuForm.nickname" /></el-form-item>
        <el-form-item label="身份证"><el-input v-model="stuForm.idCardNo" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stuDlg = false">取消</el-button>
        <el-button type="primary" @click="saveStudent">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { usersPage, userCreateCoach, userCreateStudent, reportExportUsers } from '@/api/biz'
import { downloadBlob } from '@/utils/download'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const loading = ref(false)
const query = reactive({ current: 1, size: 10, roleCode: '' })
const table = reactive({ records: [], total: 0 })
const coachDlg = ref(false)
const stuDlg = ref(false)
const coachForm = reactive({
  schoolId: 1,
  username: '',
  password: '',
  phone: '',
  nickname: '',
  licenseNo: '',
  specialty: '',
})
const stuForm = reactive({
  schoolId: 1,
  coachId: null,
  username: '',
  password: '',
  phone: '',
  nickname: '',
  idCardNo: '',
})

async function load() {
  loading.value = true
  try {
    const params = { current: query.current, size: query.size }
    if (query.roleCode) params.roleCode = query.roleCode
    const res = await usersPage(params)
    table.records = res.records
    table.total = res.total
  } finally {
    loading.value = false
  }
}

function openCoach() {
  coachForm.schoolId = store.user?.schoolId || 1
  coachDlg.value = true
}

function openStudent() {
  stuForm.schoolId = store.user?.schoolId || 1
  stuDlg.value = true
}

async function saveCoach() {
  await userCreateCoach(coachForm)
  ElMessage.success('已创建')
  coachDlg.value = false
  load()
}

async function saveStudent() {
  await userCreateStudent(stuForm)
  ElMessage.success('已创建')
  stuDlg.value = false
  load()
}

onMounted(load)

async function exportUsers() {
  try {
    const blob = await reportExportUsers()
    downloadBlob(blob, 'users.csv')
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
.filter {
  margin-bottom: 12px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
