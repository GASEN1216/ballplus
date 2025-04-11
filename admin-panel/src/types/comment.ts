import type { SubCommentDetail } from './subComment'; // 导入子评论详情类型
// import type { UserInfo } from './user' // 移除或注释掉此行

/**
 * 评论状态枚举
 */
export enum CommentStatus {
  NORMAL = 0, // 正常
  DELETED = 1 // 已删除
}

/**
 * 评论基本信息
 */
export interface Comment {
  id: number
  userId: number
  postId: number
  content: string
  status: CommentStatus
  createTime: string
  updateTime: string
}

/**
 * 评论详情 (适配后端 CommentDetail VO)
 */
export interface CommentDetail {
  commentId: number;        // 评论 ID
  postId: number | null;    // 允许 postId 为 null
  appId: number;            // 发布用户 ID
  appName: string;          // 发布用户名
  avatar: string;           // 用户头像 URL
  grade: number | null;     // 允许 grade 为 null
  content: string;          // 评论内容
  likes: number;            // 点赞数
  createTime: string;       // 创建时间
  subComments: SubCommentDetail[] | null; // 允许 subComments 为 null
  // 移除 userId, status, updateTime, isLiked, subCommentCount 等字段
}

/**
 * 评论信息（用于列表展示，适配后端 /admin/comment/list 返回结构）
 */
export interface CommentInfo {
  commentId: number;      // 评论 ID
  postId: number;         // 所属帖子 ID
  postTitle: string | null; // 帖子标题 (可能为 null)
  appId: number;          // 发布用户 ID
  appName: string;        // 发布用户名
  avatar: string;         // 用户头像 URL
  grade: number;          // 用户等级
  content: string;        // 评论内容
  likes: number;          // 点赞数
  comments: number;       // 回复数
  createTime: string;     // 创建时间
}

/**
 * 添加评论请求
 */
export interface AddCommentRequest {
  userId: number
  postId: number
  content: string
}

/**
 * 更新评论请求
 */
export interface UpdateCommentRequest {
  commentId: number
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
 * 评论查询参数
 */
export interface CommentQueryParams {
  postId?: number
  userId?: number
  status?: CommentStatus
} 