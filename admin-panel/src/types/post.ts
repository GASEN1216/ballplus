// 帖子状态枚举
export enum PostStatus {
  NORMAL = 0,
  DELETED = 1
}

// 后端返回的帖子信息接口
export interface PostInfo {
  postId: number
  appId: number
  appName: string
  avatar: string
  grade: number
  title: string
  content: string
  picture: string
  likes: number
  comments: number
  createTime: string
  updateTime: string
  updateContentTime: string
}

// 后端帖子详情接口
export interface PostDetail {
  postId: number
  appId: number
  appName: string
  avatar: string
  grade: number
  title: string
  content: string
  picture: string
  likes: number
  comments: number
  createTime: string
  updateTime: string
  updateContentTime: string
  // 其他详情字段可能包括评论等
}

// 添加帖子请求接口
export interface AddPostRequest {
  addId: number // 发帖人ID
  title: string
  content: string
  picture?: string
}

// 更新帖子请求接口
export interface UpdatePostRequest {
  postId: number
  userId: number
  content: string
  picture?: string
}

// 帖子查询参数
export interface PostQueryParams {
  keyword?: string
}

// 游标分页请求
export interface CursorPageRequest {
  cursor?: string
  pageSize: number
  asc?: boolean
}

// 游标分页响应
export interface CursorPageResponse<T> {
  records: T[]
  nextCursor?: string
  hasMore: boolean
} 