import type { UserInfo } from './user'

/**
 * 子评论状态枚举
 */
export enum SubCommentStatus {
  NORMAL = 0, // 正常
  DELETED = 1 // 已删除
}

/**
 * 子评论基本信息
 */
export interface SubComment {
  id: number
  userId: number
  commentId: number
  content: string
  status: SubCommentStatus
  createTime: string
  updateTime: string
}

/**
 * 子评论详情
 */
export interface SubCommentDetail extends SubComment {
  user: UserInfo
  likeCount: number
  isLiked: boolean
}

/**
 * 子评论信息（用于列表展示）
 */
export interface SubCommentInfo {
  id: number
  userId: number
  commentId: number
  content: string
  status: SubCommentStatus
  createTime: string
  updateTime: string
  user: UserInfo
  likeCount: number
  isLiked: boolean
}

/**
 * 添加子评论请求
 */
export interface AddSubCommentRequest {
  userId: number
  commentId: number
  content: string
}

/**
 * 更新子评论请求
 */
export interface UpdateSubCommentRequest {
  subCommentId: number
  userId: number
  content: string
}

/**
 * 游标分页请求参数
 */
export interface CursorPageRequest {
  cursor?: number
  pageSize?: number
}

/**
 * 游标分页响应
 */
export interface CursorPageResponse<T> {
  records: T[]
  nextCursor: number
  hasNext: boolean
}

/**
 * 子评论查询参数
 */
export interface SubCommentQueryParams {
  commentId?: number
  userId?: number
  status?: SubCommentStatus
} 