<template>
  <div>
    <el-form inline v-if="!started">
      <el-form-item v-if="!subjectLocked" label="科目">
        <el-select v-model="subject" style="width: 120px">
          <el-option :value="1" label="科目一" />
          <el-option :value="4" label="科目四" />
        </el-select>
      </el-form-item>
      <el-form-item label="模式">
        <el-radio-group v-model="mode">
          <el-radio-button label="RANDOM">随机做题</el-radio-button>
          <el-radio-button label="SEQUENTIAL">顺序做题</el-radio-button>
          <el-radio-button label="SIMULATION">实战模拟</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="isSequential" label="起始题号">
        <el-input-number v-model="startQuestionNo" :min="1" :max="9999" />
      </el-form-item>
      <el-form-item label="题量">
        <el-input-number v-model="count" :min="5" :max="100" :disabled="isSimulation" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="start">开始考试</el-button>
      </el-form-item>
    </el-form>
    <div v-else class="exam">
      <div class="meta">
        <span>
          进度 {{ idx + 1 }} / {{ questions.length }}
          <template v-if="startedMode === 'SIMULATION'"> · 剩余 {{ remainText }}</template>
        </span>
        <el-button type="danger" link @click="reset">放弃</el-button>
      </div>
      <el-progress :percentage="progress" :stroke-width="14" :show-text="true" class="progress" />
      <div class="q" v-if="current">
        <div class="title">{{ current.title }}</div>
        <el-image v-if="current.imageUrl" :src="current.imageUrl" style="max-height: 200px" fit="contain" />
        <div class="opts" v-if="current.qType !== 2">
          <el-checkbox-group v-if="current.qType === 3" v-model="multi">
            <el-checkbox v-if="current.optionA" label="A">{{ current.optionA }}</el-checkbox>
            <el-checkbox v-if="current.optionB" label="B">{{ current.optionB }}</el-checkbox>
            <el-checkbox v-if="current.optionC" label="C">{{ current.optionC }}</el-checkbox>
            <el-checkbox v-if="current.optionD" label="D">{{ current.optionD }}</el-checkbox>
          </el-checkbox-group>
          <el-radio-group v-else v-model="single">
            <el-radio v-if="current.optionA" label="A">{{ current.optionA }}</el-radio>
            <el-radio v-if="current.optionB" label="B">{{ current.optionB }}</el-radio>
            <el-radio v-if="current.optionC" label="C">{{ current.optionC }}</el-radio>
            <el-radio v-if="current.optionD" label="D">{{ current.optionD }}</el-radio>
          </el-radio-group>
        </div>
        <div class="opts" v-else>
          <el-radio-group v-model="single">
            <el-radio label="A">正确</el-radio>
            <el-radio label="B">错误</el-radio>
          </el-radio-group>
        </div>
        <div class="nav">
          <el-button :disabled="idx === 0" @click="prev">上一题</el-button>
          <el-button type="primary" @click="nextOrSubmit">{{ idx >= questions.length - 1 ? '交卷' : '下一题' }}</el-button>
        </div>
      </div>
    </div>
    <el-result
      v-if="result"
      :icon="result.passed ? 'success' : 'warning'"
      title="交卷完成"
      :sub-title="`得分 ${result.score}（${result.passed ? '及格' : '不及格'}），正确 ${result.correctCount}/${result.totalCount}`"
    />
  </div>
</template>

<script setup>
import { ref, computed, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  mockStart,
  mockSubmit,
  mockProgress,
  mockPublicStart,
  mockPublicSubmit,
  mockPublicProgress,
} from '@/api/biz'

const SIMULATION_LIMIT_SEC = 45 * 60

const props = defineProps({
  /** 游客模式：不落库、无错题本 */
  guest: { type: Boolean, default: false },
  /** 固定科目（首页双区块复用） */
  subject: { type: Number, default: null },
})

const subjectLocked = computed(() => props.subject === 1 || props.subject === 4)
const subject = ref(subjectLocked.value ? props.subject : 1)
const mode = ref('RANDOM')
const startedMode = ref('RANDOM')
const startQuestionNo = ref(1)
const count = ref(10)
const started = ref(false)
const questions = ref([])
const idx = ref(0)
const answers = ref({})
const single = ref('')
const multi = ref([])
const startedAt = ref(0)
const result = ref(null)
const submitting = ref(false)
const remainingSec = ref(0)
const failTriggered = ref(false)
let timerId = null

const current = computed(() => questions.value[idx.value])
const isSimulation = computed(() => mode.value === 'SIMULATION')
const isSequential = computed(() => mode.value === 'SEQUENTIAL')
const progress = computed(() => {
  if (!questions.value.length) return 0
  return Math.round(((idx.value + 1) / questions.value.length) * 100)
})
const remainText = computed(() => {
  const sec = Math.max(0, remainingSec.value)
  const mm = String(Math.floor(sec / 60)).padStart(2, '0')
  const ss = String(sec % 60).padStart(2, '0')
  return `${mm}:${ss}`
})

function clearTimer() {
  if (timerId) {
    clearInterval(timerId)
    timerId = null
  }
}

function reset() {
  clearTimer()
  started.value = false
  questions.value = []
  idx.value = 0
  answers.value = {}
  result.value = null
  submitting.value = false
  remainingSec.value = 0
  failTriggered.value = false
}

async function start() {
  result.value = null
  clearTimer()
  const apiStart = props.guest ? mockPublicStart : mockStart
  const payload = {
    subject: subject.value,
    mode: mode.value,
    questionCount: isSimulation.value ? (subject.value === 1 ? 100 : 50) : count.value,
    startQuestionNo: isSequential.value ? startQuestionNo.value : 1,
  }
  questions.value = await apiStart(payload)
  if (!questions.value.length) {
    ElMessage.warning('未获取到可用题目')
    return
  }
  idx.value = 0
  answers.value = {}
  started.value = true
  startedMode.value = mode.value
  startedAt.value = Date.now()
  remainingSec.value = startedMode.value === 'SIMULATION' ? SIMULATION_LIMIT_SEC : 0
  failTriggered.value = false
  if (startedMode.value === 'SIMULATION') {
    timerId = setInterval(() => {
      remainingSec.value -= 1
      if (remainingSec.value <= 0) {
        remainingSec.value = 0
        clearTimer()
        submitPaper({ byTimeout: true })
      }
    }, 1000)
  }
  syncChoice()
}

function syncChoice() {
  const q = current.value
  if (!q) return
  const saved = answers.value[q.id]
  if (q.qType === 3) {
    multi.value = saved ? saved.split('') : []
  } else {
    single.value = saved || ''
  }
}

function saveCurrent() {
  const q = current.value
  if (!q) return
  if (q.qType === 3) {
    answers.value[q.id] = [...multi.value].sort().join('')
  } else {
    answers.value[q.id] = single.value
  }
}

function prev() {
  saveCurrent()
  idx.value--
  syncChoice()
}

async function nextOrSubmit() {
  saveCurrent()
  if (await checkSimulationFail()) {
    return
  }
  if (idx.value >= questions.value.length - 1) {
    await submitPaper()
    return
  }
  idx.value++
  syncChoice()
}

async function submitPaper(options = {}) {
  if (submitting.value) {
    return
  }
  const { byTimeout = false, toast } = options
  submitting.value = true
  try {
    clearTimer()
    const list = questions.value.map((q) => ({
      questionId: q.id,
      userAnswer: answers.value[q.id] || '',
    }))
    const durationSec = Math.round((Date.now() - startedAt.value) / 1000)
    const apiSubmit = props.guest ? mockPublicSubmit : mockSubmit
    result.value = await apiSubmit({ subject: subject.value, durationSec, answers: list })
    const message = toast?.message || (byTimeout ? '时间到，已自动交卷' : '交卷成功')
    const type = toast?.type || 'success'
    ElMessage({ type, message })
    started.value = false
  } finally {
    submitting.value = false
  }
}

async function checkSimulationFail() {
  if (!isSimulation.value || failTriggered.value) {
    return false
  }
  if (subject.value !== 1 && subject.value !== 4) {
    return false
  }
  const progressAnswers = questions.value
    .map((q) => {
      const userAnswer = answers.value[q.id]
      if (!userAnswer) return null
      return { questionId: q.id, userAnswer }
    })
    .filter(Boolean)
  if (!progressAnswers.length) {
    return false
  }
  const apiProgress = props.guest ? mockPublicProgress : mockProgress
  const progress = await apiProgress({ subject: subject.value, answers: progressAnswers })
  if (!progress.failed) {
    return false
  }
  failTriggered.value = true
  try {
    await ElMessageBox.confirm(
      `当前已错 ${progress.wrongCount} 题，已低于90分及格线。是否继续答题？`,
      '实战演练判负',
      {
        confirmButtonText: '继续答题',
        cancelButtonText: '终止考试',
        type: 'warning',
        distinguishCancelAndClose: true,
      },
    )
    ElMessage.info('已低于及格线，可继续作答')
  } catch (err) {
    reset()
    ElMessage.info('已终止考试')
  }
  return true
}

onBeforeUnmount(() => {
  clearTimer()
})
</script>

<style scoped>
.exam {
  max-width: 720px;
}
.meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.progress {
  margin-bottom: 14px;
}
.title {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 12px;
}
.opts {
  margin: 12px 0;
}
.nav {
  margin-top: 24px;
}
</style>
