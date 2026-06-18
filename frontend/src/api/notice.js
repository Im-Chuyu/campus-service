import request from './request'

export function getPublishedNoticeListApi() {
  return request.get('/notice/publishedList')
}

export function getNoticeListApi(params) {
  return request.get('/notice/list', { params })
}

export function getNoticePageApi(params) {
  return request.get('/notice/page', { params })
}

export function getNoticeDetailApi(id) {
  return request.get(`/notice/${id}`)
}

export function addNoticeApi(data) {
  return request.post('/notice/add', data)
}

export function updateNoticeApi(data) {
  return request.put('/notice/update', data)
}

export function deleteNoticeApi(id) {
  return request.delete(`/notice/delete/${id}`)
}

export function toggleNoticeTopApi(id) {
  return request.put(`/notice/toggleTop/${id}`)
}
