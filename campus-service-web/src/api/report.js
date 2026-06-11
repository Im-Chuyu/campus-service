import request from './request'

export function getReportListApi(params) {
  return request.get('/report/list', { params })
}

export function getReportPageApi(params) {
  return request.get('/report/page', { params })
}

export function getPendingReportListApi() {
  return request.get('/report/pendingList')
}

export function handleReportApi(data) {
  return request.post('/report/handle', data)
}

export function addReportApi(data) {
  return request.post('/report/add', data)
}

export function getMyReportListApi() {
  return request.get('/report/myList')
}
