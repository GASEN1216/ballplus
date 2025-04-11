import request, { getRequestConfig } from '@/utils/request'
import type {
  Complaint,
  ComplaintDetail,
  ComplaintInfo,
  HandleComplaintRequest,
  CursorPageRequest,
  CursorPageResponse,
  ComplaintQueryParams,
  ComplaintVO,
  User,
  ComplaintStatus
} from '@/types/complaint'
import type { Page } from './comment' // 复用 Page 定义或创建通用定义

/**
 * 获取所有投诉列表 (Admin)
 * @param params 分页及过滤参数 { pageNum, pageSize, status? }
 */
export const getAllComplaintsAdmin = (params: {
  pageNum: number;
  pageSize: number;
  status?: ComplaintStatus | null; // 可选的状态过滤
}) => {
  return request<Page<ComplaintVO>>(getRequestConfig({
    // 假设的新后端接口路径
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/complaint/admin/complaint/list`,
    method: 'get',
    params
  }))
}

/**
 * 获取投诉列表
 */
export const getComplaintList = (eventId: number) => {
  return request<ComplaintVO[]>({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/complaint/list`,
    method: 'get',
    params: { eventId }
  })
}

/**
 * 检查是否可以投诉
 */
export const checkCanComplaint = (userId: number, eventId: number) => {
  return request<boolean>({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/complaint/check`,
    method: 'get',
    params: { userId, eventId }
  })
}

/**
 * 获取活动参与者
 */
export const getEventParticipants = (userId: number, eventId: number) => {
  return request<Array<{
    userId: number
    userName: string
    avatar: string
  }>>({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/complaint/participants`,
    method: 'get',
    params: { userId, eventId }
  })
}

/**
 * 提交投诉
 */
export const submitComplaint = (data: {
  userId: number
  eventId: number
  complainedIds: string
  content: string
}) => {
  return request({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/complaint/submit`,
    method: 'post',
    data
  })
}

/**
 * 获取投诉列表（游标分页）
 */
export const getComplaintListWithCursor = (params: CursorPageRequest & ComplaintQueryParams) => {
  return request<CursorPageResponse<ComplaintInfo>>({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/complaint/admin/complaint/list`,
    method: 'get',
    params
  })
}

/**
 * 获取投诉详情
 */
export const getComplaintDetail = (id: number) => {
  return request<ComplaintDetail>({
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/complaint/admin/complaint/detail`,
    method: 'get',
    params: { id }
  })
}

/**
 * 处理投诉 (Admin)
 */
export const handleComplaintAdmin = (data: HandleComplaintRequest) => {
  return request(getRequestConfig({
    // 假设的新后端接口路径
    url: `${import.meta.env.VITE_API_BASE_URL}/user/wx/complaint/admin/complaint/handle`,
    method: 'post',
    // 后端使用 @RequestParam，所以用 params
    params: {
      complaintId: data.complaintId,
      status: data.status,
      rejectReason: data.rejectReason // 如果 undefined, params 中不会包含此项
    }
  }))
}

/**
 * 获取用户投诉列表（游标分页）
 */
export const getUserComplaintsWithCursor = (userId: number, params: CursorPageRequest) => {
  return request<CursorPageResponse<ComplaintInfo>>({
    url: `${import.meta.env.VITE_API_BASE_URL}/admin/complaint/user`,
    method: 'get',
    params: { userId, ...params }
  })
} 