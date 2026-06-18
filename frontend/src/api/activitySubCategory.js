import request from './request'

export function getActivitySubCategoryListApi(params) {
  return request.get('/activitySubCategory/list', { params })
}

export function getAdminActivitySubCategoryListApi() {
  return request.get('/activitySubCategory/admin/list')
}

export function addActivitySubCategoryApi(data) {
  return request.post('/activitySubCategory/add', data)
}

export function updateActivitySubCategoryApi(data) {
  return request.put('/activitySubCategory/update', data)
}

export function deleteActivitySubCategoryApi(id) {
  return request.delete(`/activitySubCategory/delete/${id}`)
}
