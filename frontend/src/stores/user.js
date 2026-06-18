import { defineStore } from 'pinia'
import { loginApi, getUserInfoApi } from '../api/user'

const DEVICE_ID_KEY = 'campus_device_id'

const getDeviceId = () => {
  let deviceId = localStorage.getItem(DEVICE_ID_KEY)
  if (!deviceId) {
    deviceId = window.crypto?.randomUUID
      ? window.crypto.randomUUID()
      : `device-${Date.now()}-${Math.random().toString(36).slice(2)}`
    localStorage.setItem(DEVICE_ID_KEY, deviceId)
  }
  return deviceId
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: sessionStorage.getItem('token') || '',
    userInfo: JSON.parse(sessionStorage.getItem('userInfo') || 'null')
  }),

  getters: {
    isLogin: state => Boolean(state.token),
    isAdmin: state => state.userInfo?.roleId === 1,
    isSuperAdmin: state => state.userInfo?.username === 'admin'
  },

  actions: {
    async login(form) {
      const res = await loginApi({
        ...form,
        deviceId: getDeviceId()
      })
      const token = res.data

      this.token = token
      sessionStorage.setItem('token', token)

      await this.getUserInfo()
    },

    async getUserInfo() {
      const res = await getUserInfoApi()
      const userInfo = res.data

      this.userInfo = userInfo
      sessionStorage.setItem('userInfo', JSON.stringify(userInfo))
      return userInfo
    },

    logout() {
      this.token = ''
      this.userInfo = null
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
    }
  }
})
