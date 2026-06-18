import request from './request'

export function getContentListApi(params) {
  return request.get('/content/list', { params })
}

export function getContentPageApi(params) {
  return request.get('/content/page', { params })
}

export function getContentDetailApi(id) {
  return request.get(`/content/${id}`)
}

export function getPublicContentDetailApi(id) {
  return request.get(`/content/public/${id}`)
}

export function addContentApi(data) {
  return request.post('/content/add', data)
}

export function updateContentApi(data) {
  return request.put('/content/update', data)
}

export function deleteContentApi(id) {
  return request.delete(`/content/delete/${id}`)
}

export function toggleContentTopApi(id) {
  return request.put(`/content/toggleTop/${id}`)
}

export function returnContentAuditApi(data) {
  return request.put('/content/returnAudit', data)
}

export function getMyContentListApi() {
  return request.get('/content/myList')
}
