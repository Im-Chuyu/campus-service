import request from './request'

export function addFavoriteApi(contentId) {
  return request.post(`/favorite/add/${contentId}`)
}

export function deleteFavoriteApi(contentId) {
  return request.delete(`/favorite/delete/${contentId}`)
}

export function getMyFavoriteListApi() {
  return request.get('/favorite/myList')
}
