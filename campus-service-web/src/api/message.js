import request from './request'

export function sendMessageApi(data) {
  return request.post('/message/send', data)
}

export function submitFeedbackApi(data) {
  return request.post('/message/feedback', data)
}

export function getMyMessageListApi() {
  return request.get('/message/myList')
}

export function getUnreadMessageCountApi() {
  return request.get('/message/unreadCount')
}

export function markMessageReadApi(id) {
  return request.put(`/message/markRead/${id}`)
}

export function markAllMessageReadApi() {
  return request.put('/message/markAllRead')
}

export function deleteMessageApi(id) {
  return request.delete(`/message/delete/${id}`)
}

export function getMessageListApi(params) {
  return request.get('/message/list', { params })
}

export function getMessagePageApi(params) {
  return request.get('/message/page', { params })
}
