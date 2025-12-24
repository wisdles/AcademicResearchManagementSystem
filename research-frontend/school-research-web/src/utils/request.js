import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8080', // 后端地址
  timeout: 60000
})

// 1. 请求拦截器：每次请求自动带上 Token
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  console.log('请求拦截器，Token:', token)
  if (token) {
    // 注意：Bearer 后面有个空格
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 2. 响应拦截器：统一处理错误
request.interceptors.response.use(
  response => {
    const res = response.data
    // 如果后端返回 code 不是 200，视为业务错误
    if (res.code && res.code !== 200) {
      ElMessage.error(res.msg || '系统错误')
      return Promise.reject(new Error(res.msg || 'Error'))
    }
    return res // 返回 data 部分
  },
  error => {
    // 处理 HTTP 状态码错误
    if (error.response) {
      if (error.response.status === 403) {
        ElMessage.error('登录过期或无权访问，请重新登录')
        localStorage.removeItem('token')//本地测试阶段先不删除token
        router.push('/login')
      } else {
        ElMessage.error(error.response.data.msg || '网络错误')
      }
    }
    return Promise.reject(error)
  }
)

export default request