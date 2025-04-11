import type { Role } from './role'

// 用户信息接口
export interface User {
  id: number
  openId?: string | null
  userName: string
  userAccount: string
  password?: string | null
  avatarUrl: string | null
  gender: number // 0: 未知, 1: 男, 2: 女
  email: string | null
  phone: string | null
  grade: number
  exp: number
  signIn: string | null
  state: number // 0: 普通用户, 1: 管理员, -1: 被封禁
  unblockingTime: string | null
  isDelete: number | null
  createTime: string | null
  updateTime: string | null
  birthday: string | null
  credit: number | null
  score: number | null
  description: string | null
  label: string | null
  onlyBallNumber: string
}

// 前端展示兼容
export interface User {
  username?: string
  avatar?: string
  userStatus?: number
  userRole?: number
  tags?: string
  profile?: string
}

// 用户登录请求接口
export interface UserLoginRequest {
  code: string
}

// 用户封禁请求接口
export interface BanUserRequest {
  id: number
  days: number
}

// 用户封禁参数接口（与BanUserRequest相同）
export interface BanUserParams {
  id: number
  days: number
}

// 用户查询参数
export interface UserQueryParams {
  username?: string
  userStatus?: number
  userRole?: number
}

// 用户列表响应
export interface UserListResponse {
  list: User[]
  total: number
}

export interface CreditInfo {
  userId: number
  credit: number
  level: number
  description: string
}

export interface CreditHistory {
  id: number
  userId: number
  change: number
  reason: string
  createTime: string
}

export interface ScoreHistory {
  id: number
  userId: number
  change: number
  reason: string
  createTime: string
} 