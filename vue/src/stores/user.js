import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { fetchMe } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('ydt_token') || '')
  const user = ref(null)

  const roleCode = computed(() => user.value?.roleCode || '')
  const isAdmin = computed(() => roleCode.value === 'ADMIN')
  const isSchool = computed(() => roleCode.value === 'SCHOOL')
  const isCoach = computed(() => roleCode.value === 'COACH')
  const isStudent = computed(() => roleCode.value === 'STUDENT')

  function setAuth(t, u) {
    token.value = t
    user.value = u
    localStorage.setItem('ydt_token', t)
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem('ydt_token')
  }

  async function loadProfile() {
    if (!token.value) return
    user.value = await fetchMe()
  }

  return { token, user, roleCode, isAdmin, isSchool, isCoach, isStudent, setAuth, clearAuth, loadProfile }
})
