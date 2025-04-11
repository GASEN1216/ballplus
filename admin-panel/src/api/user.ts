import request, { getRequestConfig } from '@/utils/request'
import type { User, UserQueryParams, UserListResponse, BanUserParams } from '@/types/user'

/**
 * 用户登录
 * @param userAccount 用户账号
 * @param password 密码
 */
export const login = (userAccount: string, password: string) => {
  return request({
    url: '/user/login',
    method: 'post',
    data: { userAccount, password }
  })
}

/**
 * 用户登出
 */
export const logout = () => {
  return request(getRequestConfig({
    url: '/user/logout',
    method: 'post'
  }))
}

/**
 * 获取当前登录用户信息
 */
export const getCurrentUser = () => {
  return request(getRequestConfig({
    url: '/user/current',
    method: 'get'
  }))
}

/**
 * 获取所有用户列表
 */
export const getAllUsers = () => {
  return request(getRequestConfig({
    url: '/user/all',
    method: 'get'
  }))
}

/**
 * 更新用户信息
 * @param user 用户信息
 */
export const updateUser = (user: User) => {
  return request(getRequestConfig({
    url: '/user/update',
    method: 'post',
    data: user
  }))
}

/**
 * 删除用户
 * @param id 用户ID
 */
export const deleteUser = (id: number) => {
  return request(getRequestConfig({
    url: '/user/delete',
    method: 'post',
    params: { id }
  }))
}

/**
 * 获取用户列表
 */
export function getUserList(params: UserQueryParams) {
  return request<UserListResponse>(getRequestConfig({
    url: '/admin/user/list',
    method: 'get',
    params
  }))
}

/**
 * 设置管理员
 */
export function setUserAdmin(userId: number) {
  return request<boolean>(getRequestConfig({
    url: '/admin/user/setAdmin',
    method: 'post',
    data: { userId }
  }))
}

/**
 * 取消管理员
 */
export function unsetUserAdmin(userId: number) {
  return request<boolean>(getRequestConfig({
    url: '/admin/user/unsetAdmin',
    method: 'post',
    data: { userId }
  }))
}

/**
 * 封禁用户
 */
export function banUser(params: BanUserParams) {
  return request<boolean>(getRequestConfig({
    url: '/user/banned',
    method: 'post',
    data: params
  }))
}

/**
 * 解封用户
 */
export function unbanUser(userId: number) {
  return request<boolean>(getRequestConfig({
    url: '/user/unbanned',
    method: 'post',
    data: { userId }
  }))
} 