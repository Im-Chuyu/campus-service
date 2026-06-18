import request from './request'

export function getDashboardStatApi() {
  return request.get('/statistics/dashboard')
}
