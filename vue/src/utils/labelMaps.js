const STATUS_LABELS = {
  PENDING: '待审核',
  SUBMITTED: '已提交',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  PASSED: '考试通过',
  BOOKED: '已预约',
  CHECKED_IN: '已签到',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

const SUBJECT_LABELS = {
  SUBJECT1: '科目一',
  SUBJECT2: '科目二',
  SUBJECT3: '科目三',
  SUBJECT4: '科目四',
}

export function formatStatus(status) {
  return STATUS_LABELS[status] || status || '—'
}

export function formatSubject(subject) {
  return SUBJECT_LABELS[subject] || subject || '—'
}

export function statusLabelMap() {
  return STATUS_LABELS
}

export function subjectLabelMap() {
  return SUBJECT_LABELS
}

