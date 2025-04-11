import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 5000
})

/**
 * 获取请求配置，自动添加token
 * @param config 原始请求配置
 * @returns 添加token后的请求配置
 */
export const getRequestConfig = (config: any) => {
  const token = localStorage.getItem('token')
  if (token) {
    if (!config.params) {
      config.params = {}
    }
    config.params.token = token
  }
  return config
}

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = localStorage.getItem('token')
    
    // 将token添加到请求参数中
    if (token && config.params) {
      config.params.token = token
    } else if (token) {
      config.params = { token }
    }
    
    // 如果不是登录请求，检查用户是否已登录
    if (!config.url?.includes('/user/login') && !userStore.userInfo) {
      router.push('/login')
      return Promise.reject(new Error('未登录'))
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 如果响应不是成功状态
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      // 如果是未登录或token过期
      if (res.code === 401) {
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service 