<template>
  <div class="page">
    <el-card class="card">
      <h2>驾校入驻</h2>
      <p class="hint">请如实填写与营业执照一致的驾校全称，审核与展示均以该名称为准。</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="所属城市" prop="city">
          <el-input v-model="form.city" placeholder="例如：武汉市" />
        </el-form-item>
        <el-form-item label="驾校全称" prop="name">
          <el-input v-model="form.name" placeholder="与营业执照相同" />
        </el-form-item>
        <el-form-item label="驾校简称" prop="shortName">
          <el-input v-model="form.shortName" />
        </el-form-item>
        <el-form-item label="管理员账号" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="管理员密码" prop="password">
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">提交入驻</el-button>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerSchool } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  city: '',
  name: '',
  shortName: '',
  username: '',
  password: '',
})
const rules = {
  city: [{ required: true, message: '必填', trigger: 'blur' }],
  name: [{ required: true, message: '必填', trigger: 'blur' }],
  shortName: [{ required: true, message: '必填', trigger: 'blur' }],
  username: [{ required: true, message: '必填', trigger: 'blur' }],
  password: [{ required: true, message: '必填', trigger: 'blur' }],
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await registerSchool(form)
    ElMessage.success('入驻成功，请使用管理员账号登录工作台')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.card {
  width: 520px;
}
h2 {
  text-align: center;
  margin-bottom: 12px;
}
.hint {
  font-size: 13px;
  color: #909399;
  margin: 0 0 20px;
  text-align: center;
}
</style>
