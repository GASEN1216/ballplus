import request from '@/utils/request'
import type {
  SubComment,
  SubCommentDetail,
  SubCommentInfo,
  UpdateSubCommentRequest,
  CursorPageRequest,
  CursorPageResponse,
  SubCommentQueryParams,
  AddSubCommentRequest
} from '@/types/subComment'

/**
 * 添加子评论
 */
export const addSubComment = (data: AddSubCommentRequest) => {
  return request({
    url: '/user/wx/addSubComment',
    method: 'post',
    data
  })
}

/**
 * 删除子评论
 */
export const deleteSubComment = (commentId: number, subCommentId: number, userId: number) => {
  return request({
    url: '/user/wx/deleteSubComment',
    method: 'post',
    params: { commentId, subCommentId, userId }
  })
}

/**
 * 更新子评论
 */
export const updateSubComment = (data: UpdateSubCommentRequest) => {
  return request({
    url: '/user/wx/updateSubComment',
    method: 'post',
    data
  })
}

/**
 * 获取子评论列表
 */
export const getSubCommentList = (commentId: number) => {
  return request<SubCommentInfo[]>({
    url: '/user/wx/getSubCommentList',
    method: 'post',
    params: { commentId }
  })
}

/**
 * 获取子评论详情
 */
export const getSubCommentDetail = (id: number) => {
  return request<SubCommentDetail>({
    url: '/user/wx/getSubCommentDetail',
    method: 'get',
    params: { id }
  })
}

/**
 * 点赞子评论
 */
export const likeSubComment = (subCommentId: number) => {
  return request({
    url: '/user/wx/likeSubComment',
    method: 'post',
    params: { subCommentId }
  })
} 