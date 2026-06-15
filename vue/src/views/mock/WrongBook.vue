<template>
  <el-card>
    <template #header>错题本</template>
    <el-table :data="table.records" v-loading="loading" border>
      <el-table-column prop="wrong.questionId" label="题目ID" width="90" />
      <el-table-column prop="wrong.wrongTimes" label="错次" width="80" />
      <el-table-column prop="wrong.lastWrongAt" label="最近错误" width="170" />
      <el-table-column label="题干" min-width="240">
        <template #default="{ row }">{{ row.question?.title }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row.wrong.id)">移除</el-button>
        </template>
      </el-table-column>
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
import { ElMessage } from 'element-plus'
import { wrongBookPage, wrongBookDelete } from '@/api/biz'

const loading = ref(false)
const query = reactive({ current: 1, size: 10 })
const table = reactive({ records: [], total: 0 })

async function load() {
  loading.value = true
  try {
    const res = await wrongBookPage(query)
    table.records = res.records
    table.total = res.total
  } finally {
    loading.value = false
  }
}

async function remove(id) {
  await wrongBookDelete(id)
  ElMessage.success('已移除')
  load()
}

onMounted(load)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
