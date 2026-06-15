import request from './request'

export const schoolsPublic = () => request.get('/schools/public/list')
export const schoolsPublicRankings = () => request.get('/schools/public/rankings')
export const schoolPublicDetail = (id) => request.get(`/schools/public/${id}/detail`)
export const coachesPublicRankings = () => request.get('/coaches/public/rankings')
export const reviewsPublicRecent = (limit = 20) =>
  request.get('/reviews/public/recent', { params: { limit } })
export const coachMyStudents = () => request.get('/coaches/my-students')
export const coachesPublicBySchool = (schoolId) =>
  request.get('/coaches/public/by-school', { params: { schoolId } })

export const mockPublicStart = (data) => request.post('/mock-exams/public/start', data)
export const mockPublicSubmit = (data) => request.post('/mock-exams/public/submit', data)
export const mockPublicProgress = (data) => request.post('/mock-exams/public/progress', data)

export const studentEnroll = (data) => request.post('/student/enroll', data)
export const studentEnrollment = () => request.get('/student/enrollment')
export const studentReview = (data) => request.post('/student/review', data)

export const rosterLearningStage = (studentRowId, stage) =>
  request.patch(`/roster/students/${studentRowId}/learning-stage`, null, { params: { stage } })
export const schoolsPage = (params) => request.get('/schools', { params })
export const schoolCreate = (data) => request.post('/schools', data)
export const schoolUpdate = (id, data) => request.put(`/schools/${id}`, data)

export const usersPage = (params) => request.get('/users', { params })
export const userCreateCoach = (data) => request.post('/users/coach', data)
export const userCreateStudent = (data) => request.post('/users/student', data)
export const userStatus = (id, status) => request.put(`/users/${id}/status`, null, { params: { status } })

export const enrollmentsPage = (params) => request.get('/enrollments', { params })
export const enrollmentCreate = (data) => request.post('/enrollments', data)
export const enrollmentStatus = (id, status) => request.put(`/enrollments/${id}/status`, null, { params: { status } })

export const appointmentsPage = (params) => request.get('/appointments', { params })
export const appointmentCreate = (data) => request.post('/appointments', data)
export const appointmentStatus = (id, status) => request.put(`/appointments/${id}/status`, null, { params: { status } })

export const trainingsPage = (params) => request.get('/trainings', { params })
export const trainingCreate = (data) => request.post('/trainings', data)

export const venuesPage = (params) => request.get('/exam-venues', { params })
export const venueGeocode = (address) => request.get('/exam-venues/address-geocode', { params: { address } })
export const venueRoutes = (venueId) => request.get(`/exam-venues/${venueId}/routes`)
export const venueCreate = (data) => request.post('/exam-venues', data)
export const venueUpdate = (id, data) => request.put(`/exam-venues/${id}`, data)
export const routeCreate = (venueId, data) => request.post(`/exam-venues/${venueId}/routes`, data)
export const routeUpdate = (routeId, data) => request.put(`/exam-venues/routes/${routeId}`, data)
export const routeDelete = (routeId) => request.delete(`/exam-venues/routes/${routeId}`)
/** 高德 Web 路径规划后的折线（驾车/步行） */
export const routePlannedPath = (routeId) => request.get(`/exam-venues/routes/${routeId}/planned-path`)

export const examRegPage = (params) => request.get('/exam-registrations', { params })
export const examRegCreate = (data) => request.post('/exam-registrations', data)
export const examRegStatus = (id, status) => request.put(`/exam-registrations/${id}/status`, null, { params: { status } })

export const officialExamScheduleInfo = () => request.get('/exam-schedule/official-info')

export const questionsPage = (params) => request.get('/questions', { params })
export const questionCreate = (data) => request.post('/questions', data)
export const questionUpdate = (id, data) => request.put(`/questions/${id}`, data)
export const questionDelete = (id) => request.delete(`/questions/${id}`)
export const questionImport = (formData) => request.post('/questions/import', formData)
export const questionChapters = (params) => request.get('/questions/chapters', { params })

export const mockStart = (data) => request.post('/mock-exams/start', data)
export const mockSubmit = (data) => request.post('/mock-exams/submit', data)
export const mockProgress = (data) => request.post('/mock-exams/progress', data)
export const mockRecords = (params) => request.get('/mock-exams/records', { params })

export const wrongBookPage = (params) => request.get('/wrong-book', { params })
export const wrongBookDelete = (id) => request.delete(`/wrong-book/${id}`)

export const reportDashboard = () => request.get('/reports/dashboard')
export const reportExportEnrollments = () =>
  request.get('/reports/export/enrollments', { responseType: 'blob' })
export const reportExportAppointments = () =>
  request.get('/reports/export/appointments', { responseType: 'blob' })
export const reportExportTrainings = () =>
  request.get('/reports/export/trainings', { responseType: 'blob' })
export const reportExportExamRegs = () =>
  request.get('/reports/export/exam-registrations', { responseType: 'blob' })
export const reportExportQuestions = () =>
  request.get('/reports/export/questions', { responseType: 'blob' })
export const reportExportUsers = () =>
  request.get('/reports/export/users', { responseType: 'blob' })

export const coachOptions = () => request.get('/coaches/options')
export const studentsOptions = (params) => request.get('/students/options', { params })
