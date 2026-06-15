<template>
  <el-card>
    <template #header>报表导出</template>
    <p class="tip">导出当前权限范围内的招生线索为 CSV（UTF-8）。</p>
    <el-button type="primary" @click="exportCsv">下载 enrollment 报表</el-button>
  </el-card>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { reportExportEnrollments } from '@/api/biz'

async function exportCsv() {
  const blob = await reportExportEnrollments()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'enrollments.csv'
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已开始下载')
}
</script>

<style scoped>
.tip {
  color: #606266;
  margin-bottom: 16px;
}
</style>
