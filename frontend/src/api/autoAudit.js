import request from './request'

export function getAutoAuditConfigApi() {
  return request.get('/audit/auto/config')
}

export function updateAutoAuditConfigApi(enabled) {
  return request.put('/audit/auto/config', { enabled })
}
