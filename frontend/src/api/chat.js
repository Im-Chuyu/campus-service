import request from './request'

export function getPrivateChatPeerApi(userId) {
  return request.get(`/private-chat/peer/${userId}`)
}

export function getPrivateChatSessionsApi() {
  return request.get('/private-chat/sessions')
}

export function getPrivateChatHistoryApi(userId) {
  return request.get(`/private-chat/history/${userId}`)
}

export function syncPrivateChatApi(userId, afterId = 0) {
  return request.get(`/private-chat/sync/${userId}`, { params: { afterId } })
}

export function sendPrivateChatApi(data) {
  return request.post('/private-chat/send', data)
}

export function blockUserApi(userId) {
  return request.post(`/private-chat/block/${userId}`)
}

export function unblockUserApi(userId) {
  return request.delete(`/private-chat/block/${userId}`)
}

export function getPrivateChatUnreadCountApi() {
  return request.get('/private-chat/unreadCount')
}

export function markAllPrivateChatReadApi() {
  return request.put('/private-chat/markAllRead')
}

export function markPrivateChatReadApi(userId) {
  return request.put(`/private-chat/markRead/${userId}`)
}
