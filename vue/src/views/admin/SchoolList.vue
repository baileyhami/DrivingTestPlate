<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <span>驾校管理</span>
        <el-button type="primary" @click="openAdd">新增驾校</el-button>
      </div>
    </template>
    <el-table :data="table.records" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="licenseNo" label="许可证号" />
      <el-table-column prop="address" label="地址" show-overflow-tooltip />
      <el-table-column prop="contactPhone" label="电话" />
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
    <el-dialog v-model="dlg" title="新增驾校" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="驾校名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="城市"><el-input v-model="form.city" placeholder="可选" /></el-form-item>
        <el-form-item label="简称"><el-input v-model="form.shortName" placeholder="可选" /></el-form-item>
        <el-form-item label="许可证号"><el-input v-model="form.licenseNo" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.contactEmail" placeholder="可选" /></el-form-item>
        <el-form-item label="工作时间"><el-input v-model="form.workHours" placeholder="可选" /></el-form-item>
        <el-form-item label="C1价格"><el-input-number v-model="form.priceC1" :min="0" :step="100" controls-position="right" /></el-form-item>
        <el-form-item label="C2价格"><el-input-number v-model="form.priceC2" :min="0" :step="100" controls-position="right" /></el-form-item>
        <el-form-item label="登录名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="登录密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
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
import { schoolsPage, schoolCreate } from '@/api/biz'

const loading = ref(false)
const query = reactive({ current: 1, size: 10 })
const table = reactive({ records: [], total: 0 })
const dlg = ref(false)
const form = reactive({
  name: '',
  city: '',
  shortName: '',
  licenseNo: '',
  address: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  workHours: '',
  priceC1: undefined,
  priceC2: undefined,
  username: '',
  password: '',
})

async function load() {
  loading.value = true
  try {
    const res = await schoolsPage(query)
    table.records = res.records
    table.total = res.total
  } finally {
    loading.value = false
  }
}

function openAdd() {
  Object.assign(form, {
    name: '',
    city: '',
    shortName: '',
    licenseNo: '',
    address: '',
    contactName: '',
    contactPhone: '',
    contactEmail: '',
    workHours: '',
    priceC1: undefined,
    priceC2: undefined,
    username: '',
    password: '',
  })
  dlg.value = true
}

async function save() {
  await schoolCreate(form)
  ElMessage.success('已创建')
  dlg.value = false
  load()
}

onMounted(load)
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
