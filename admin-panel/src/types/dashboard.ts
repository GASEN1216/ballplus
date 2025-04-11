export interface DashboardStats {
  userCount: number
  userTrend: number
  postCount: number
  postTrend: number
  commentCount: number
  commentTrend: number
  complaintCount: number
  complaintTrend: number
}

export interface TrendData {
  date: string
  count: number
}

export interface UserTrendResponse {
  data: TrendData[]
}

export interface ContentTrendResponse {
  postData: TrendData[]
  commentData: TrendData[]
} 