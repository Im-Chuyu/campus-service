import request from './request'

export function getCategoryListApi() {
  return request.get('/category/list')
}

export function getCategoryDetailApi(id) {
  return request.get(`/category/${id}`)
}

export function addCategoryApi(data) {
  return request.post('/category/add', data)
}

export function updateCategoryApi(data) {
  return request.put('/category/update', data)
}

export function deleteCategoryApi(id) {
  return request.delete(`/category/delete/${id}`)
}
