<template>
  <div class="login-page">
    <el-card class="card">
      <h2>驾考一点通</h2>
      <p class="sub">驾考服务管理系统</p>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="0" @submit.prevent>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="onSubmit">登录</el-button>
        </el-form-item>
        <div class="links">
          <router-link to="/register">学员注册</router-link>
          <span class="sep">|</span>
          <router-link to="/register/coach">教练注册</router-link>
          <span class="sep">|</span>
          <router-link to="/register/school">驾校入驻</router-link>
          <span class="sep">|</span>
          <router-link to="/">首页</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { defaultWorkspacePath } from '@/router'

const router = useRouter()
const route = useRoute()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    store.setAuth(res.token, res.user)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || defaultWorkspacePath(res.user?.roleCode)
    router.push(redirect)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1d2b3a 0%, #2c5282 100%);
}
.card {
  width: 400px;
  padding: 12px 8px 8px;
}
h2 {
  text-align: center;
  margin: 0;
  color: #303133;
}
.sub {
  text-align: center;
  color: #909399;
  margin: 8px 0 24px;
  font-size: 14px;
}
.links {
  text-align: center;
  font-size: 13px;
}
.sep {
  margin: 0 8px;
  color: #dcdfe6;
}
</style>
