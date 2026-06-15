<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <div>数据看板</div>
        <div>
          <el-dropdown>
            <el-button type="primary" size="small">导出报表</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="exportEnrollments">导出招生</el-dropdown-item>
                <el-dropdown-item @click="exportAppointments">导出预约</el-dropdown-item>
                <el-dropdown-item @click="exportTrainings">导出培训</el-dropdown-item>
                <el-dropdown-item @click="exportExamRegs">导出报考</el-dropdown-item>
                <el-dropdown-item @click="exportUsers">导出账号</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </template>
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="stat">学员总数：<b>{{ data.studentTotal ?? 0 }}</b></div>
      </el-col>
      <el-col :span="8">
        <div class="stat">模拟考试均分：<b>{{ (data.mockAvgScore ?? 0).toFixed(1) }}</b></div>
      </el-col>
    </el-row>
    <div ref="chartRef" class="chart" />
  </el-card>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import * as echarts from 'echarts'
import { reportDashboard, reportExportEnrollments, reportExportAppointments, reportExportTrainings, reportExportExamRegs, reportExportUsers } from '@/api/biz'
import { formatStatus } from '@/utils/labelMaps'
import { downloadBlob } from '@/utils/download'
import { ElMessage } from 'element-plus'

const chartRef = ref()
const data = reactive({
  studentTotal: 0,
  mockAvgScore: 0,
  enrollmentByStatus: {},
  appointmentByStatus: {},
  examRegByStatus: {},
})

onMounted(async () => {
  Object.assign(data, await reportDashboard())
  const keys = mergeKeys()
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    title: { text: '招生 / 预约 / 报考状态分布' },
    tooltip: { trigger: 'axis' },
    legend: { data: ['招生', '预约', '报考'] },
    xAxis: { type: 'category', data: keys.map((k) => formatStatus(k)) },
    yAxis: { type: 'value' },
    series: [
      { name: '招生', type: 'bar', data: keys.map((k) => data.enrollmentByStatus[k] || 0) },
      { name: '预约', type: 'bar', data: keys.map((k) => data.appointmentByStatus[k] || 0) },
      { name: '报考', type: 'bar', data: keys.map((k) => data.examRegByStatus[k] || 0) },
    ],
  })
})

function mergeKeys() {
  const s = new Set([
    ...Object.keys(data.enrollmentByStatus || {}),
    ...Object.keys(data.appointmentByStatus || {}),
    ...Object.keys(data.examRegByStatus || {}),
  ])
  return [...s]
}

async function exportEnrollments() {
  try {
    const blob = await reportExportEnrollments()
    downloadBlob(blob, 'enrollments.csv')
    ElMessage.success('导出已开始')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}

async function exportAppointments() {
  try {
    const blob = await reportExportAppointments()
    downloadBlob(blob, 'appointments.csv')
    ElMessage.success('导出已开始')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}

async function exportTrainings() {
  try {
    const blob = await reportExportTrainings()
    downloadBlob(blob, 'trainings.csv')
    ElMessage.success('导出已开始')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}

async function exportExamRegs() {
  try {
    const blob = await reportExportExamRegs()
    downloadBlob(blob, 'exam_registrations.csv')
    ElMessage.success('导出已开始')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}

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
.chart {
  height: 360px;
  margin-top: 24px;
}
.stat {
  padding: 16px;
  background: #f0f9ff;
  border-radius: 8px;
}
</style>
