// 投诉信息接口
export interface Complaint {
  id: number
  eventId: number
  complainerId: number
  complainedId: number
  content: string
  status: ComplaintStatus
  rejectReason?: string
  createTime: string
}

// 投诉视图对象接口
export interface ComplaintVO {
  id: number;               // 投诉 ID
  eventId: number;          // 活动 ID
  complainerId: number;     // 投诉人 ID
  complainerName: string;   // 投诉人名称
  complainerAvatar: string; // 投诉人头像
  complainedId: number;     // 被投诉人 ID
  complainedName: string;   // 被投诉人名称
  complainedAvatar: string; // 被投诉人头像
  content: string;          // 投诉内容
  status: ComplaintStatus;  // 投诉状态 (使用枚举)
  rejectReason?: string;    // 拒绝原因 (可选)
  createTime: string;       // 创建时间
}

// 用户信息接口
export interface User {
  id: number
  userAccount: string
  avatarUrl: string
}

// 提交投诉请求接口
export interface SubmitComplaintRequest {
  userId: number
  eventId: number
  complainedIds: string
  content: string
}

// 投诉详情接口
export interface ComplaintDetail extends Complaint {
  complainant: {
    id: number
    username: string
    avatarUrl: string
  }
  respondent: {
    id: number
    username: string
    avatarUrl: string
  }
}

// 投诉列表项接口
export interface ComplaintInfo {
  id: number
  content: string
  complainantId: number
  complainantName: string
  complainantAvatar: string
  respondentId: number
  respondentName: string
  respondentAvatar: string
  type: number
  status: number
  result: string
  createTime: string
}

// 处理投诉请求接口
export interface HandleComplaintRequest {
  complaintId: number;      // 后端需要 complaintId
  status: ComplaintStatus;  // 使用枚举
  rejectReason?: string;    // 拒绝原因，status 为 REJECTED 时需要
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

// 投诉查询参数
export interface ComplaintQueryParams {
  keyword?: string
  complainantName?: string
  respondentName?: string
  type?: number
  status?: number
}

// 投诉类型枚举
export enum ComplaintType {
  POST = 1, // 帖子
  COMMENT = 2, // 评论
  SUB_COMMENT = 3, // 子评论
  USER = 4 // 用户
}

// 投诉状态枚举
export enum ComplaintStatus {
  PENDING = 0, // 待处理
  APPROVED = 1, // 已通过
  REJECTED = 2 // 已拒绝
} 