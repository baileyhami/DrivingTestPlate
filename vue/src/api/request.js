import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  const store = useUserStore()
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    if (res.config.responseType === 'blob') {
      return res.data
    }
    const body = res.data
    if (body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      if (body.code === 401) {
        const store = useUserStore()
        store.clearAuth()
        router.push({ name: 'Login' })
      }
      return Promise.reject(new Error(body.message))
    }
    return body.data
  },
  (err) => {
    ElMessage.error(err.message || '网络错误')
    return Promise.reject(err)
  }
)

export default request
