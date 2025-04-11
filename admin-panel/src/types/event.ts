// 活动信息接口（匹配后端实际返回数据）
export interface Event {
  id: number
  appId: number
  avatar: string
  name: string
  eventDate: string
  eventTime: string
  eventTimee: string
  location: string
  locationDetail: string
  latitude: number
  longitude: number
  totalParticipants: number
  participants: number
  phoneNumber: string
  type: number
  remarks: string
  labels: string
  limits: number
  visibility: boolean
  level: number
  feeMode: number
  fee: number
  penalty: boolean
  isTemplate: boolean
  state: number // 0: 正常, 1: 已满员, 2: 已取消, 3: 已结束
  isDelete: number
  createTime: string
  updateTime: string
}

// 从后端返回的详细活动信息
export interface DetailEvent {
  id: number
  appId: number
  title: string
  content: string
  eventDate: string
  eventTime: string
  location: string
  latitude: number
  longitude: number
  maxParticipants: number
  currentParticipants: number
  status: number
  createTime: string
  updateTime: string
  persons: UserIdAndAvatar[]
}

// 用户ID和头像
export interface UserIdAndAvatar {
  userId: number
  avatarUrl: string
}

// 首页显示的活动信息
export interface IndexEvent {
  id: number
  appId: number
  title: string
  content: string
  eventDate: string
  eventTime: string
  location: string
  latitude: number
  longitude: number
  maxParticipants: number
  currentParticipants: number
  status: number
  createTime: string
}

// 带状态的活动信息（用于个人活动列表）
export interface IndexEventWithState extends IndexEvent {
  state: number
}

// 活动模板
export interface EventTemplate {
  id: number
  appId: number
  title: string
  content: string
  eventDate: string
  eventTime: string
  location: string
  latitude: number
  longitude: number
  maxParticipants: number
  isTemplate: boolean
}

// 活动详情接口（区别于Event基本信息）
export interface EventDetail {
  id: number
  appId: number
  avatar: string
  name: string
  eventDate: string
  eventTime: string
  eventTimee: string
  location: string
  locationDetail: string
  latitude: number
  longitude: number
  totalParticipants: number
  participantsCount: number // 改为participantsCount以区别于下面的participants数组
  phoneNumber: string
  type: number
  remarks: string
  labels: string
  limits: number
  visibility: boolean
  level: number
  feeMode: number
  fee: number
  penalty: boolean
  isTemplate: boolean
  state: number
  isDelete: number
  createTime: string
  updateTime: string
  // 额外添加的详情字段
  creator: {
    id: number
    username: string
    avatarUrl: string
  }
  participants: {
    id: number
    username: string
    avatarUrl: string
  }[]
}

// 活动列表项接口
export interface EventInfo {
  id: number
  title: string
  content: string
  eventDate: string
  eventTime: string
  location: string
  maxParticipants: number
  currentParticipants: number
  status: number
  createTime: string
  creatorId: number
  creatorName: string
  creatorAvatar: string
}

// 创建活动请求接口
export interface CreateEventRequest {
  appId: number
  name: string
  remarks?: string
  eventDate: string
  eventTime: string
  eventTimee: string
  location: string
  locationDetail?: string
  latitude: number
  longitude: number
  phoneNumber?: string
  totalParticipants: number
  type?: number
  limits?: number
  visibility?: boolean
  level?: number
  feeMode?: number
  fee?: number
  penalty?: boolean
}

// 更新活动请求接口
export interface UpdateEventRequest {
  id: number
  title?: string
  content?: string
  eventDate?: string
  eventTime?: string
  location?: string
  latitude?: number
  longitude?: number
  maxParticipants?: number
  status?: number
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

// 活动查询参数
export interface EventQueryParams {
  keyword?: string
  creatorName?: string
  status?: number
  startDate?: string
  endDate?: string
}

// 活动状态枚举
export enum EventStatus {
  NORMAL = 0, // 正常
  FULL = 1,   // 已满员
  CANCELLED = 2, // 已取消
  ENDED = 3   // 已结束
} 