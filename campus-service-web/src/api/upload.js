import request from './request'

export function uploadImageApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData)
}

export function uploadMediaApi(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/media', formData)
}
