import request, { getRequestConfig } from '@/utils/request'
import type { DashboardStats, UserTrendResponse, ContentTrendResponse } from '@/types/dashboard'

export const getDashboardStats = () => {
  return request<DashboardStats>(getRequestConfig({
    url: '/dashboard/stats',
    method: 'get'
  }))
}

export const getUserTrend = () => {
  return request<UserTrendResponse>(getRequestConfig({
    url: '/dashboard/user-trend',
    method: 'get'
  }))
}

export const getContentTrend = () => {
  return request<ContentTrendResponse>(getRequestConfig({
    url: '/dashboard/content-trend',
    method: 'get'
  }))
} 