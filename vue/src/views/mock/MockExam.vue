<template>
  <el-card>
    <template #header>模拟考试</template>
    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="exam-card">
          <template #header>科目一</template>
          <MockExamPanel :guest="false" :subject="1" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never" class="exam-card">
          <template #header>科目四</template>
          <MockExamPanel :guest="false" :subject="4" />
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import MockExamPanel from '@/components/MockExamPanel.vue'
</script>

<style scoped>
.exam-card {
  margin-bottom: 12px;
}
</style>

