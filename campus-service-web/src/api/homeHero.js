import request from './request'

export function getPublicHomeHeroConfigApi() {
  return request.get('/home/hero/public')
}

export function getHomeHeroConfigApi() {
  return request.get('/home/hero/config')
}

export function updateHomeHeroConfigApi(data) {
  return request.put('/home/hero/config', data)
}
