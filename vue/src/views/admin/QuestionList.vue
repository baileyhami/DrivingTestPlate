<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <span>题库管理</span>
        <div>
          <el-upload :http-request="doImport" accept=".xls,.xlsx" :show-file-list="false">
            <el-button type="primary" size="small">导入题库（Excel）</el-button>
          </el-upload>
          <el-button type="primary" @click="openAdd">新增题目</el-button>
          <el-button type="default" @click="exportQuestions" size="small">导出题库</el-button>
        </div>
      </div>
    </template>
    <el-form inline>
      <el-form-item label="科目">
        <el-select v-model="query.subject" clearable placeholder="全部" style="width: 120px" @change="load">
          <el-option :value="1" label="科目一" />
          <el-option :value="4" label="科目四" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-table :data="table.records" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="subject" label="科目" width="80" />
      <el-table-column prop="chapter" label="类型" width="160" />
      <el-table-column prop="title" label="题干" min-width="200" show-overflow-tooltip />
      <el-table-column prop="answer" label="答案" width="80" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="edit(row)">编辑</el-button>
          <el-button link type="danger" @click="del(row)">删除</el-button>
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
    <el-dialog v-model="dlg" :title="form.id ? '编辑' : '新增'" width="640px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="科目"><el-input-number v-model="form.subject" :min="1" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.chapter" clearable placeholder="请选择章节">
            <el-option v-for="c in chapters" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="题干"><el-input v-model="form.title" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="A"><el-input v-model="form.optionA" /></el-form-item>
        <el-form-item label="B"><el-input v-model="form.optionB" /></el-form-item>
        <el-form-item label="C"><el-input v-model="form.optionC" /></el-form-item>
        <el-form-item label="D"><el-input v-model="form.optionD" /></el-form-item>
        <el-form-item label="答案"><el-input v-model="form.answer" placeholder="如 A 或 ABC" /></el-form-item>
        <el-form-item label="解析"><el-input v-model="form.analysis" type="textarea" /></el-form-item>
        <el-form-item label="章节"><el-input v-model="form.chapter" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionsPage, questionCreate, questionUpdate, questionDelete, questionImport, questionChapters, reportExportQuestions } from '@/api/biz'
import { downloadBlob } from '@/utils/download'

const loading = ref(false)
const query = reactive({ current: 1, size: 10, subject: null })
const table = reactive({ records: [], total: 0 })
const dlg = ref(false)
const form = reactive({
  id: null,
  subject: 1,
  qType: 1,
  title: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  answer: '',
  analysis: '',
  chapter: '',
})

const chapters = ref([])

async function load() {
  loading.value = true
  try {
    // load available chapters for the current subject filter
    try {
      const ch = await questionChapters({ subject: query.subject })
      chapters.value = ch || []
    } catch (e) {
      chapters.value = []
    }
    const params = { current: query.current, size: query.size }
    if (query.subject != null) params.subject = query.subject
    const res = await questionsPage(params)
    table.records = res.records
    table.total = res.total
  } finally {
    loading.value = false
  }
}

function openAdd() {
  Object.assign(form, {
    id: null,
    subject: 1,
    qType: 1,
    title: '',
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    answer: '',
    analysis: '',
    chapter: '',
  })
  dlg.value = true
}

function edit(row) {
  Object.assign(form, row)
  dlg.value = true
}

async function save() {
  if (form.id) {
    await questionUpdate(form.id, form)
  } else {
    await questionCreate(form)
  }
  ElMessage.success('已保存')
  dlg.value = false
  load()
}

async function del(row) {
  await ElMessageBox.confirm('确认删除？')
  await questionDelete(row.id)
  ElMessage.success('已删除')
  load()
}

async function doImport({ file, onSuccess, onError }) {
  try {
    const fd = new FormData()
    fd.append('file', file.raw || file)
    // 让浏览器自动生成 multipart boundary，避免后端解析失败
    await questionImport(fd)
    ElMessage.success('导入成功')
    load()
    onSuccess && onSuccess()
  } catch (err) {
    ElMessage.error(err.message || '导入失败')
    onError && onError(err)
  }
}

onMounted(load)

async function exportQuestions() {
  try {
    const blob = await reportExportQuestions()
    downloadBlob(blob, 'questions.csv')
    ElMessage.success('导出已开始')
  } catch (e) {
    ElMessage.error(e?.message || '导出失败')
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
