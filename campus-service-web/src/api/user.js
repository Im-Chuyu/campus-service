import request from './request'

export function loginApi(data) {
  return request.post('/user/login', data)
}

export function registerApi(data) {
  return request.post('/user/register', data)
}

export function sendCodeApi(data) {
  return request.post('/user/sendCode', data)
}

export function resetPasswordApi(data) {
  return request.post('/user/resetPassword', data)
}

export function getUserInfoApi() {
  return request.get('/user/info')
}

export function getUserInfoByIdApi(userId) {
  return request.get(`/user/info/${userId}`)
}

export function updateUserApi(data) {
  return request.put('/user/update', data)
}

export function updatePasswordApi(data) {
  return request.post('/user/updatePassword', data)
}

export function getUserListApi(params) {
  return request.get('/user/list', { params })
}

export function getUserPageApi(params) {
  return request.get('/user/page', { params })
}

export function updateUserStatusApi(id, status) {
  return request.put(`/user/status/${id}/${status}`)
}

export function deleteUserApi(id) {
  return request.delete(`/user/delete/${id}`)
}
