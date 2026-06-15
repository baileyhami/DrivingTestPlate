<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <span>我的学员</span>
        <el-button type="primary" link @click="load">刷新</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="nickname" label="学员姓名" min-width="100" />
      <el-table-column prop="phone" label="手机" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="140" show-overflow-tooltip />
      <el-table-column label="学车阶段" width="100">
        <template #default="{ row }">{{ stageLabel(row.learningStage) }}</template>
      </el-table-column>
      <el-table-column label="预约(待练/签到/完成)" width="160">
        <template #default="{ row }">
          {{ row.appointmentBooked }} / {{ row.appointmentCheckedIn }} / {{ row.appointmentCompleted }}
        </template>
      </el-table-column>
      <el-table-column prop="trainingCount" label="培训次数" width="90" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && list.length === 0" description="暂无绑定学员" />

    <el-dialog v-model="detailDlg" :title="detailTitle" width="640px" destroy-on-close>
      <template v-if="current">
        <el-descriptions :column="2" border size="small" class="mb">
          <el-descriptions-item label="手机">{{ current.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ current.email || '—' }}</el-descriptions-item>
        </el-descriptions>
        <h4>近期预约</h4>
        <el-table :data="current.recentAppointments || []" size="small" border empty-text="暂无预约">
          <el-table-column label="科目" width="90">
            <template #default="{ row }">{{ formatSubject(row.subjectType) }}</template>
          </el-table-column>
          <el-table-column prop="appointmentTime" label="时间" min-width="150" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">{{ formatStatus(row.status) }}</template>
          </el-table-column>
        </el-table>
        <h4 class="mt">近期培训</h4>
        <el-table :data="current.recentTrainings || []" size="small" border empty-text="暂无培训">
          <el-table-column label="科目" width="90">
            <template #default="{ row }">{{ formatSubject(row.subjectType) }}</template>
          </el-table-column>
          <el-table-column prop="trainingDate" label="日期" width="110" />
          <el-table-column prop="durationHours" label="学时" width="70" />
          <el-table-column prop="score" label="评分" width="70" />
        </el-table>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { coachMyStudents } from '@/api/biz'
import { formatStatus, formatSubject } from '@/utils/labelMaps'

const loading = ref(false)
const list = ref([])
const detailDlg = ref(false)
const current = ref(null)

const detailTitle = computed(() =>
  current.value?.nickname ? `${current.value.nickname} · 学车详情` : '学员详情',
)

function stageLabel(stage) {
  const m = { 1: '科目一', 2: '科目二', 3: '科目三', 4: '科目四', 5: '已结业' }
  return m[stage] || '科目一'
}

async function load() {
  loading.value = true
  try {
    list.value = await coachMyStudents()
  } finally {
    loading.value = false
  }
}

function openDetail(row) {
  current.value = row
  detailDlg.value = true
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mb {
  margin-bottom: 12px;
}
.mt {
  margin: 16px 0 8px;
  font-size: 14px;
}
</style>
