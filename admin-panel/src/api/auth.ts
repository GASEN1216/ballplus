import request, { getRequestConfig } from '@/utils/request'
import type { LoginRequest, LoginResponse } from '@/types/auth'

export const login = (data: LoginRequest) => {
  return request<LoginResponse>({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/login`,
    method: 'post',
    data
  }).then(response => {
    // 确保返回的是正确的数据格式
    if (response && typeof response === 'object') {
      return response
    }
    throw new Error('登录响应格式错误')
  })
} 