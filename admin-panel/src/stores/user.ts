import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi } from '@/api/auth'
import type { LoginRequest, LoginResponse } from '@/types/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 定义存储的key
const USER_INFO_KEY = 'user_info'
const TOKEN_KEY = 'token'
const EXPIRES_KEY = 'expires'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<LoginResponse | null>(null)
  const error = ref<string | null>(null)

  // 从localStorage加载用户信息
  const loadUserInfo = () => {
    const storedUserInfo = localStorage.getItem(USER_INFO_KEY)
    const storedToken = localStorage.getItem(TOKEN_KEY)
    const storedExpires = localStorage.getItem(EXPIRES_KEY)
    
    if (storedUserInfo && storedToken && storedExpires) {
      const expires = new Date(storedExpires)
      if (expires > new Date()) {
        userInfo.value = JSON.parse(storedUserInfo)
        return true
      } else {
        // token已过期，清除存储
        clearStorage()
      }
    }
    return false
  }

  // 清除存储
  const clearStorage = () => {
    localStorage.removeItem(USER_INFO_KEY)
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(EXPIRES_KEY)
    userInfo.value = null
  }

  const login = async (data: LoginRequest) => {
    try {
      const response = await loginApi(data)
      if (!response || typeof response !== 'object') {
        error.value = '登录失败'
        return false
      }
      
      // 检查用户是否是管理员
      if (response.state !== 1) {
        error.value = '非管理员账号，无法登录'
        userInfo.value = null
        return false
      }
      
      userInfo.value = response
      
      // 设置过期时间为1天后
      const expires = new Date()
      expires.setDate(expires.getDate() + 1)
      
      // 存储用户信息和token
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(response))
      localStorage.setItem(TOKEN_KEY, response.token || '')
      localStorage.setItem(EXPIRES_KEY, expires.toISOString())
      
      ElMessage.success('登录成功')
      return true
    } catch (err: any) {
      error.value = err.message || '登录失败'
      userInfo.value = null
      return false
    }
  }

  const logout = () => {
    clearStorage()
    router.push('/login')
  }

  const isAdmin = () => {
    return userInfo.value?.state === 1
  }

  // 初始化时加载用户信息
  loadUserInfo()

  return {
    userInfo,
    error,
    login,
    logout,
    isAdmin
  }
}) 