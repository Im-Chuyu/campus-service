import request from './request'

export function getFriendListApi() {
  return request.get('/friend/list')
}

export function getFriendStatusApi(friendId) {
  return request.get(`/friend/status/${friendId}`)
}

export function searchFriendByUsernameApi(username) {
  return request.get('/friend/search', { params: { username } })
}

export function addFriendApi(friendId) {
  return request.post(`/friend/${friendId}`)
}

export function deleteFriendApi(friendId) {
  return request.delete(`/friend/${friendId}`)
}

export function getFriendCategoryListApi() {
  return request.get('/friend/category/list')
}

export function saveFriendCategoryApi(data) {
  return request.post('/friend/category', data)
}

export function deleteFriendCategoryApi(id) {
  return request.delete(`/friend/category/${id}`)
}

export function assignFriendCategoryApi(data) {
  return request.put('/friend/category/assign', data)
}
