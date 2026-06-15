<template>
  <div class="page">
    <el-card class="card">
      <h2>教练注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="所属驾校" prop="schoolId">
          <el-select v-model="form.schoolId" placeholder="请选择驾校" filterable style="width: 100%">
            <el-option v-for="s in schools" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">注册</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
          <el-button link type="primary" @click="$router.push('/register')">学员注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { schoolsPublic } from '@/api/biz'
import { registerCoach } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const schools = ref([])
const loading = ref(false)
const form = reactive({
  schoolId: null,
  username: '',
  password: '',
})
const rules = {
  schoolId: [{ required: true, message: '请选择驾校', trigger: 'change' }],
  username: [{ required: true, message: '必填', trigger: 'blur' }],
  password: [{ required: true, message: '必填', trigger: 'blur' }],
}

onMounted(async () => {
  schools.value = await schoolsPublic()
})

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await registerCoach(form)
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
  margin-bottom: 24px;
}
</style>
