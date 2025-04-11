import request, { getRequestConfig } from '@/utils/request'
import type {
  Resource,
  ResourceDetail,
  ResourceInfo,
  CreateResourceRequest,
  UpdateResourceRequest,
  CursorPageRequest,
  CursorPageResponse,
  ResourceQueryParams,
  ResourceQueryRequest,
  ResourceVO,
  Page,
  ResourcePageResponse
} from '@/types/resource'

// 获取资源列表
export function getResourceList(params: ResourceQueryRequest) {
  return request<CursorPageResponse<ResourceInfo>>({
    url: '/user/wx/resources',
    method: 'get',
    params
  })
}

/**
 * 获取资源列表 (Admin)
 * @param params 查询参数，ResourceQueryRequest
 */
export function getAllResourcesAdmin(params: ResourceQueryRequest) {
  return request<ResourcePageResponse<ResourceVO>>(getRequestConfig({
    url: '/user/wx/resources/',
    method: 'get',
    params
  }))
}

/**
 * 获取资源详情
 */
export function getResourceDetail(id: number) {
  return request<ResourceVO>(getRequestConfig({
    url: `/user/wx/resources/${id}`,
    method: 'get'
  }))
}

/**
 * 创建资源 (元数据)
 */
export function createResourceAdmin(data: CreateResourceRequest) {
  return request<number>(getRequestConfig({
    url: '/user/wx/resources/',
    method: 'post',
    data
  }))
}

/**
 * 更新资源 (元数据)
 */
export function updateResourceAdmin(data: UpdateResourceRequest) {
  return request<boolean>(getRequestConfig({
    url: `/user/wx/resources/${data.id}`,
    method: 'put',
    data
  }))
}

/**
 * 删除资源 (Admin)
 * @param id 资源ID
 */
export function deleteResourceAdmin(id: number) {
  return request<boolean>(getRequestConfig({
    url: `/user/wx/resources/${id}`,
    method: 'delete'
  }))
}

// 获取最热资源
export function getTopResource() {
  return request<ResourceInfo>({
    url: '/user/wx/resources/top',
    method: 'get'
  })
}

// 更新资源浏览量
export function updateResourceViews(id: number) {
  return request<boolean>({
    url: `/user/wx/resources/${id}/view`,
    method: 'put'
  })
}

// 获取用户收藏的资源列表
export function listFavorites(userId: number, params: CursorPageRequest) {
  return request<CursorPageResponse<ResourceInfo>>({
    url: '/user/wx/resources/favorites',
    method: 'get',
    params: { userId, ...params }
  })
}

// 添加收藏
export function addFavorite(userId: number, resourceId: number) {
  return request<boolean>({
    url: `/user/wx/resources/${resourceId}/favorite`,
    method: 'post',
    params: { userId }
  })
}

// 取消收藏
export function removeFavorite(userId: number, resourceId: number) {
  return request<boolean>({
    url: `/user/wx/resources/${resourceId}/favorite`,
    method: 'delete',
    params: { userId }
  })
} 