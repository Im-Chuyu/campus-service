import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code !== undefined && res.code !== 200) {
      ElMessage.error(res.message || res.msg || '请求失败')
      return Promise.reject(res)
    }

    return res
  },
  error => {
    const data = error.response?.data
    const message =
      data?.message ||
      data?.msg ||
      error.message ||
      '服务器异常'

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
