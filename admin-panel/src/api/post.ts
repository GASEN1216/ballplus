import request, { getRequestConfig } from '@/utils/request'
import type {
  PostDetail,
  PostInfo,
  AddPostRequest,
  UpdatePostRequest,
  PostQueryParams,
  CursorPageRequest,
  CursorPageResponse,
  PostListResponse
} from '@/types/post'

/**
 * 获取帖子列表（游标分页）
 */
export const getPostListWithCursor = (params: CursorPageRequest & { keyword?: string }) => {
  return request<CursorPageResponse<PostInfo>>(getRequestConfig({
    url: '/user/wx/getPostListWithCursor',
    method: 'get',
    params
  }))
}

/**
 * 获取帖子详情
 */
export const getPostDetail = (postId: number) => {
  return request<PostDetail>(getRequestConfig({
    url: '/user/wx/getPostDetail',
    method: 'post',
    params: { postId }
  }))
}

/**
 * 添加帖子
 */
export const addPost = (data: AddPostRequest) => {
  return request(getRequestConfig({
    url: '/user/wx/addPost',
    method: 'post',
    data
  }))
}

/**
 * 更新帖子
 */
export const updatePost = (data: UpdatePostRequest) => {
  return request(getRequestConfig({
    url: '/user/wx/updatePost',
    method: 'post',
    data
  }))
}

/**
 * 删除帖子
 */
export const deletePost = (postId: number, userId: number) => {
  return request(getRequestConfig({
    url: '/user/wx/deletePost',
    method: 'post',
    params: { postId, userId }
  }))
}

/**
 * 获取点赞数最高的帖子
 */
export const getTopPost = () => {
  return request(getRequestConfig({
    url: '/user/wx/posts/top',
    method: 'get'
  }))
}

/**
 * 获取用户发布的帖子列表（游标分页）
 */
export const getMyPostsWithCursor = (userId: number, params: CursorPageRequest) => {
  return request<CursorPageResponse<PostInfo>>(getRequestConfig({
    url: '/user/wx/getMyPostsWithCursor',
    method: 'post',
    params: { userId, ...params }
  }))
}

/**
 * 点赞帖子
 */
export const likePost = (postId: number) => {
  return request(getRequestConfig({
    url: '/user/wx/likePost',
    method: 'post',
    params: { postId }
  }))
} 