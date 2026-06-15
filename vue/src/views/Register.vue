<template>
  <div class="page">
    <el-card class="card">
      <h2>学员注册</h2>
      <p class="hint">注册后可在首页选择驾校与教练完成报名。</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">注册</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
          <el-button link type="primary" @click="$router.push('/register/coach')">教练注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerStudent } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  username: '',
  password: '',
})
const rules = {
  username: [{ required: true, message: '必填', trigger: 'blur' }],
  password: [{ required: true, message: '必填', trigger: 'blur' }],
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await registerStudent(form)
    ElMessage.success('注册成功，请登录')
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
  width: 480px;
}
h2 {
  text-align: center;
  margin-bottom: 8px;
}
.hint {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin-bottom: 20px;
}
</style>
