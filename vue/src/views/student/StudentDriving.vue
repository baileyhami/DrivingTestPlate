<template>
  <el-card>
    <template #header>我的学车</template>
    <el-skeleton v-if="loading" :rows="4" animated />
    <template v-else>
      <template v-if="info?.enrolled">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="驾校">{{ info.schoolName }}</el-descriptions-item>
          <el-descriptions-item label="教练">{{ info.coachName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="报名时间">{{ info.enrollDate }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ info.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ info.email }}</el-descriptions-item>
        </el-descriptions>
        <p class="mt">当前阶段：{{ stageLabel(store.user?.learningStage) }}</p>

        <div v-if="info.reviewSubmitted" class="review-box">
          <el-alert type="success" :closable="false" show-icon title="您已评价过驾校与教练" />
        </div>
        <div v-else-if="info.canSubmitReview || store.user?.canSubmitReview" class="review-box">
          <h3>评价驾校与教练</h3>
          <p class="hint">每人仅可评价一次，提交后榜单实时更新。</p>
          <el-form label-width="80px" class="review-form">
            <el-form-item label="驾校">
              <el-rate v-model="review.schoolStars" />
            </el-form-item>
            <el-form-item label="教练">
              <el-rate v-model="review.coachStars" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="reviewLoading" @click="submitReview">提交评价</el-button>
            </el-form-item>
          </el-form>
        </div>

        <el-button type="primary" link class="mt" @click="$router.push('/app/appointments')">预约练车</el-button>
      </template>
      <template v-else>
        <el-empty description="尚未报名驾校">
          <el-button type="primary" @click="$router.push({ path: '/', query: { scroll: 'enroll' } })">去首页报名</el-button>
        </el-empty>
      </template>
    </template>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { studentEnrollment, studentReview } from '@/api/biz'

const store = useUserStore()
const route = useRoute()
const loading = ref(true)
const reviewLoading = ref(false)
const info = ref(null)
const review = reactive({ schoolStars: 5, coachStars: 5 })

function stageLabel(stage) {
  const m = { 1: '科目一', 2: '科目二', 3: '科目三', 4: '科目四', 5: '已结业' }
  return m[stage] || '科目一'
}

async function submitReview() {
  if (!review.schoolStars || !review.coachStars) {
    ElMessage.warning('请完成打分')
    return
  }
  reviewLoading.value = true
  try {
    await studentReview({
      schoolStars: review.schoolStars,
      coachStars: review.coachStars,
    })
    ElMessage.success('感谢您的评价')
    await store.loadProfile()
    info.value = await studentEnrollment()
  } finally {
    reviewLoading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await store.loadProfile()
    info.value = await studentEnrollment()
    if (route.query.scroll === 'enroll') {
      /* no-op */
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.mt {
  margin-top: 16px;
}
.review-box {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px dashed var(--el-border-color);
}
.review-box h3 {
  margin: 0 0 8px;
  font-size: 15px;
}
.hint {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  margin: 0 0 12px;
}
.review-form {
  max-width: 360px;
}
</style>
