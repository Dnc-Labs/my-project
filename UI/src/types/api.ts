export interface BaseResponse<T> {
  success: boolean
  message: string
  data: T
}

export type Role = "CUSTOMER" | "SELLER" | "ADMIN"
export type UserStatus = "ACTIVE" | "INACTIVE" | "BANNED"

export interface UserResponse {
  id: number
  email: string
  fullName: string
  phone: string | null
  address: string | null
  role: Role
  status: UserStatus
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
}

export interface CategoryResponse {
  id: number
  name: string
  slug?: string
  parentId: number | null
  children?: CategoryResponse[]
}

export interface ProductResponse {
  id: number
  name: string
  description: string
  price: number
  stock?: number
  imageUrl?: string
  categoryId: number
  categoryName?: string
  sellerId: number
  sellerName?: string
  createdAt?: string
  updatedAt?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RefreshTokenRequest {
  token: string
}

export interface CreateUserRequest {
  email: string
  password: string
  fullName: string
  phone?: string
  address?: string
}

export interface CreateProductRequest {
  name: string
  description: string
  price: number
  stock?: number
  imageUrl?: string
  categoryId: number
}

export type UpdateProductRequest = Partial<CreateProductRequest>

export interface CreateCategoryRequest {
  name: string
  parentId?: number | null
}

export type UpdateCategoryRequest = Partial<CreateCategoryRequest>

export interface JwtPayload {
  sub: string
  role: Role
  iat: number
  exp: number
}

export type AuthUser = Pick<UserResponse, "email" | "role">
