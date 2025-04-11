// 资源信息接口
export interface Resource {
  id: number
  appId: number
  title: string
  content: string
  type: number
  categoryId: number
  views: number
  favorites: number
  createTime: string
  updateTime: string
  isDeleted: boolean
}

// 资源详情接口
export interface ResourceDetail extends Resource {
  category: {
    id: number
    name: string
  }
  creator: {
    id: number
    username: string
    avatarUrl: string
  }
}

// 资源列表项接口
export interface ResourceInfo {
  id: number
  title: string
  content: string
  type: number
  categoryId: number
  categoryName: string
  views: number
  favorites: number
  createTime: string
  creatorId: number
  creatorName: string
  creatorAvatar: string
}

// 创建资源请求接口
export interface CreateResourceRequest {
  appId: number
  title: string
  content: string
  type: number
  categoryId: number
}

// 更新资源请求接口
export interface UpdateResourceRequest {
  id: number
  title?: string
  content?: string
  type?: number
  categoryId?: number
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

// 资源查询参数
export interface ResourceQueryParams {
  keyword?: string
  type?: number
  categoryId?: number
}

// 资源类型枚举
export enum ResourceType {
  ARTICLE = 1,
  VIDEO = 2,
  AUDIO = 3,
  DOCUMENT = 4
}

// 资源类型
export type ResourceType = 'ARTICLE' | 'VIDEO' | 'AUDIO' | 'DOCUMENT'

// 资源基本信息
export interface ResourceInfo {
  id: number
  title: string
  content: string
  type: ResourceType
  views: number
  favorites: number
  createTime: string
  creator: {
    id: number
    nickname: string
  }
}

// 资源详情
export interface ResourceDetail extends ResourceInfo {
  updateTime: string
  creator: {
    id: number
    nickname: string
    avatar: string
  }
}

// 创建资源请求
export interface CreateResourceRequest {
  title: string
  content: string
  type: ResourceType
}

// 更新资源请求
export interface UpdateResourceRequest extends CreateResourceRequest {
  id: number
}

// 资源查询请求
export interface ResourceQueryRequest extends CursorPageRequest {
  keyword?: string
  creatorId?: number
  type?: ResourceType
}

// 游标分页请求
export interface CursorPageRequest {
  cursor?: string
  pageSize: number
  sortOrder: 'ASC' | 'DESC'
}

// 游标分页响应
export interface CursorPageResponse<T> {
  records: T[]
  nextCursor: string | null
  hasMore: boolean
}

// 资源查询请求
export interface ResourceQueryRequest {
  page: number;             // 使用 page
  size: number;             // 使用 size
  keyword?: string;
  type?: ResourceType | null;
  category?: string | null;
}

// 创建资源请求
export interface CreateResourceRequest {
  title: string;
  content: string;
  type: ResourceType;
  categoryId?: number;
  url?: string;
  appId?: number; // 可能需要创建者ID
  // ... 其他 Resource 实体字段 ...
}

// 更新资源请求
export interface UpdateResourceRequest {
  id: number;
  title?: string;
  content?: string;
  type?: ResourceType;
  categoryId?: number;
  url?: string;
  // ... 其他可更新字段 ...
}

// 移除旧 Page<T> (如果与后端返回不符)
// export interface Page<T> { ... }

// 添加匹配后端返回 Map 结构的接口
export interface ResourcePageResponse<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages?: number;
}

// 更新 ResourceVO 接口
export interface ResourceVO {
  id: number;
  title: string;
  description?: string;
  coverImage?: string;
  type: string; // 改为 string
  content?: string;
  views: number;
  likes: number; // 使用 likes
  category?: string; // category 改为 string
  createTime: string;
  isFavorite?: boolean;
  // 移除 favorites, categoryId, categoryName, creatorId, creatorName, creatorAvatar
}

// 更新资源查询请求接口
export interface ResourceQueryRequest {
  page: number;
  size: number;
  keyword?: string;
  type?: string | null; // type 改为 string
  category?: string | null; // 添加 category 过滤
  // 移除 categoryId
} 