import request from './request'

export function addCommentApi(data) {
  return request.post('/comment/add', data)
}

export function getCommentListApi(contentId) {
  return request.get(`/comment/list/${contentId}`)
}

export function getCommentPageApi(contentId, params) {
  return request.get(`/comment/page/${contentId}`, { params })
}

export function deleteCommentApi(id) {
  return request.delete(`/comment/delete/${id}`)
}

export function reactCommentApi(commentId, reactionType) {
  return request.post(`/comment/react/${commentId}/${reactionType}`)
}

export function toggleCommentTopApi(commentId) {
  return request.put(`/comment/toggleTop/${commentId}`)
}
