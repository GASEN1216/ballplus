import request, { getRequestConfig } from '@/utils/request'
import type {
  Event,
  EventDetail,
  DetailEvent,
  EventInfo,
  CreateEventRequest,
  UpdateEventRequest,
  CursorPageRequest,
  CursorPageResponse,
  EventQueryParams,
  UserIdAndAvatar
} from '@/types/event'

/**
 * 获取活动列表
 */
export const getEventList = () => {
  return request(getRequestConfig({
    url: '/user/wx/admin/getEvent',
    method: 'get'
  }))
}

/**
 * 获取活动详情
 */
export const getEventDetail = (eventId: number) => {
  return request(getRequestConfig({
    url: '/user/wx/getEventDetailById',
    method: 'get',
    params: { eventId }
  }))
}

/**
 * 创建活动
 */
export const createEvent = (data: CreateEventRequest) => {
  return request(getRequestConfig({
    url: '/user/wx/createEvent',
    method: 'post',
    data
  }))
}

/**
 * 更新活动
 */
export const updateEvent = (data: UpdateEventRequest) => {
  return request({
    url: '/admin/event/update',
    method: 'post',
    data
  })
}

/**
 * 删除活动
 */
export const deleteEvent = (id: number) => {
  return request({
    url: '/admin/event/delete',
    method: 'post',
    data: { id }
  })
}

/**
 * 取消活动
 */
export const cancelEvent = (userId: number, eventId: number, cancelReason: string) => {
  return request(getRequestConfig({
    url: '/user/wx/cancelEvent',
    method: 'post',
    params: { userId, eventId, cancelReason }
  }))
}

/**
 * 获取活动参与者列表（通过活动详情接口获取）
 */
export const getEventParticipants = (eventId: number) => {
  return request<DetailEvent>(getRequestConfig({
    url: '/user/wx/getEventDetailById',
    method: 'get',
    params: { eventId }
  })).then((response: any) => {
    return response && response.persons ? response.persons : []
  })
}

/**
 * 获取地图模式活动列表
 */
export const getMapEvents = (params: {
  longitude: number
  latitude: number
  radius: number
}) => {
  return request<EventInfo[]>({
    url: '/user/wx/getMapEvents',
    method: 'get',
    params
  })
}

/**
 * 获取个人活动模板
 */
export const getEventTemplates = () => {
  return request<{
    id: number
    name: string
    description: string
    location: string
    maxParticipants: number
    eventDate: string
    eventTime: string
  }[]>({
    url: '/user/wx/getEventTemplates',
    method: 'get'
  })
}

/**
 * 创建活动模板
 */
export const createEventTemplate = (data: {
  name: string
  description: string
  location: string
  maxParticipants: number
  eventDate: string
  eventTime: string
}) => {
  return request<number>({
    url: '/user/wx/createEventTemplate',
    method: 'post',
    data
  })
}

/**
 * 删除活动模板
 */
export const deleteEventTemplate = (id: number) => {
  return request({
    url: '/user/wx/deleteEventTemplate',
    method: 'post',
    data: { id }
  })
}

/**
 * 获取活动匹配
 */
export const getEventMatches = (id: number) => {
  return request<EventInfo[]>({
    url: '/user/wx/getEventMatches',
    method: 'get',
    params: { id }
  })
}

/**
 * 获取活动通知
 */
export const getEventNotifications = () => {
  return request<{
    id: number
    eventId: number
    type: string
    content: string
    createTime: string
  }[]>({
    url: '/user/wx/getEventNotifications',
    method: 'get'
  })
}

/**
 * 获取用户活动列表（游标分页）
 */
export const getUserEvents = (params: CursorPageRequest) => {
  return request<CursorPageResponse<EventInfo>>({
    url: '/user/wx/getUserEvents',
    method: 'get',
    params
  })
} 