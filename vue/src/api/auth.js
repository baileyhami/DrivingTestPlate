import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function registerStudent(data) {
  return request.post('/auth/register/student', data)
}

export function registerCoach(data) {
  return request.post('/auth/register/coach', data)
}

export function registerSchool(data) {
  return request.post('/auth/register/school', data)
}

export function fetchMe() {
  return request.get('/auth/me')
}
