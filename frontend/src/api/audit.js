import request from './request'

export function getWaitAuditListApi() {
  return request.get('/audit/waitList')
}

export function getWaitAuditPageApi(params) {
  return request.get('/audit/waitPage', { params })
}

export function handleAuditApi(data) {
  return request.post('/audit/handle', data)
}

export function getAuditRecordApi(contentId) {
  return request.get(`/audit/record/${contentId}`)
}
