import request from '@/api/request'

export function aiChat(payload) {
  return request.post('/ai/chat', payload)
}

