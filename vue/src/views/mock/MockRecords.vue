<template>
  <el-card>
    <template #header>模拟考试记录</template>
    <el-table :data="table.records" v-loading="loading" border>
      <el-table-column v-if="store.isCoach" label="学员" min-width="100">
        <template #default="{ row }">{{ row.studentNickname || '—' }}</template>
      </el-table-column>
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="subject" label="科目" width="90" />
      <el-table-column prop="score" label="得分" width="80" />
      <el-table-column label="结果" width="90">
        <template #default="{ row }">
          {{ row.score >= 90 ? '及格' : '不及格' }}
        </template>
      </el-table-column>
      <el-table-column prop="correctCount" label="正确数" width="90" />
      <el-table-column prop="totalCount" label="总题数" width="90" />
      <el-table-column prop="durationSec" label="用时(秒)" width="100" />
      <el-table-column prop="createTime" label="时间" width="170" />
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
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { mockRecords } from '@/api/biz'
import { useUserStore } from '@/stores/user'

const store = useUserStore()

const loading = ref(false)
const query = reactive({ current: 1, size: 10 })
const table = reactive({ records: [], total: 0 })

async function load() {
  loading.value = true
  try {
    const res = await mockRecords(query)
    table.records = res.records
    table.total = res.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
