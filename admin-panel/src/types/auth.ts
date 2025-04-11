export interface LoginRequest {
  userAccount: string
  password: string
}

export interface LoginResponse {
  id: number
  userAccount: string
  userName: string
  avatarUrl: string
  gender: number
  grade: number
  exp: number
  state: number
  email: string
  phone: string
  signIn: string
  unblockingTime: string | null
  token?: string
} 