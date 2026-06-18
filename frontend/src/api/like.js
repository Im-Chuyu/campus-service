import request from './request'

export function addLikeApi(contentId) {
  return request.post(`/like/add/${contentId}`)
}

export function deleteLikeApi(contentId) {
  return request.delete(`/like/delete/${contentId}`)
}

export function getMyLikeListApi() {
  return request.get('/like/myList')
}
