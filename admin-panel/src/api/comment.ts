import request, { getRequestConfig } from '@/utils/request'
import type {
  Comment,
  CommentDetail,
  CommentInfo,
  UpdateCommentRequest,
  CursorPageRequest,
  CursorPageResponse,
  CommentQueryParams,
  AddCommentRequest
} from '@/types/comment'

// 定义通用的分页接口 (如果项目中没有统一的定义)
export interface Page<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
}

/**
 * 添加评论
 */
export const addComment = (data: AddCommentRequest) => {
  return request({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/addComment`,
    method: 'post',
    data
  })
}

/**
 * 获取所有评论列表 (Admin)
 * @param params 分页参数 { pageNum, pageSize }
 */
export const getAllCommentsAdmin = (params: { pageNum: number, pageSize: number }) => {
  return request<Page<CommentInfo>>(getRequestConfig({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/admin/comment/list`,
    method: 'get',
    params // 直接传递分页参数
  }))
}

/**
 * 删除评论 (Admin)
 */
export const deleteCommentAdmin = (commentId: number) => {
  return request(getRequestConfig({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/admin/comment/delete`,
    method: 'post',
    params: { commentId } // 使用 params 传递
  }))
}

/**
 * 更新评论
 */
export const updateComment = (data: UpdateCommentRequest) => {
  return request({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/updateComment`,
    method: 'post',
    data
  })
}

/**
 * 获取帖子评论列表 (原接口)
 */
export const getCommentList = (postId: number) => {
  return request<CommentInfo[]>(getRequestConfig({
    url: '/user/wx/getCommentList',
    method: 'get',
    params: { postId }
  }))
}

/**
 * 获取评论详情
 */
export const getCommentDetail = (commentId: number) => {
  return request<CommentDetail>(getRequestConfig({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/admin/getCommentDetail`, // 使用现有接口
    method: 'post', // 注意：后端是 PostMapping，但这里似乎应该是GET？根据后端调整，暂时维持Post
    params: { commentId } // 或者根据后端@RequestParam调整为params
  }))
}

/**
 * 点赞评论
 */
export const likeComment = (commentId: number) => {
  return request({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/likeComment`,
    method: 'post',
    params: { commentId }
  })
}

/**
 * 获取我的帖子评论（游标分页）
 */
export const getMyPostCommentsWithCursor = (params: CursorPageRequest) => {
  return request<CursorPageResponse<CommentInfo>>({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/getMyPostCommentsWithCursor`,
    method: 'get',
    params
  })
}

/**
 * 获取我的评论回复（游标分页）
 */
export const getMyCommentRepliesWithCursor = (params: CursorPageRequest) => {
  return request<CursorPageResponse<CommentInfo>>({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/getMyCommentRepliesWithCursor`,
    method: 'get',
    params
  })
}

/**
 * 删除评论 (原接口 - 用户自删逻辑)
 */
export const deleteComment = (postId: number, commentId: number, userId: number) => {
  return request(getRequestConfig({
    url: '/user/wx/deleteComment',
    method: 'post',
    params: { postId, commentId, userId }
  }))
} 