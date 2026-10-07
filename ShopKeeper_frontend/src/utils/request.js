import axios from 'axios'
import { message } from 'antd'

// 后端基于 session/cookie，必须 withCredentials
const request = axios.create({
  timeout: 10000,
  withCredentials: true
})

// 响应拦截：统一处理后端 Result 结构
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    message.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      // 未登录/会话过期：跳转登录页（登录页自身不重复跳）
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    } else {
      message.error(error.response?.data?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
