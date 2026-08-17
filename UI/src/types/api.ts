/**
 * Type mirror của DTO backend (Spring Boot).
 * Quy ước: field nào backend khai `BigDecimal` → number ở đây (Jackson serialize ra JSON number).
 * LocalDateTime → string ISO ("2026-08-12T10:15:30").
 */

export interface BaseResponse<T> {
  success: boolean
  message: string
  data: T
}

/** Wrapper phân trang tự định nghĩa ở backend (PageResponse<T>) — KHÔNG phải Page của Spring Data. */
export interface PageResponse<T> {
  content: T[]
  page: {
    number: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
  }
}

/** Tham số phân trang/sắp xếp mà Spring Pageable hiểu (sort dạng "field,direction"). */
export interface PageParams {
  page?: number
  size?: number
  sort?: string
}

// ---------------------------------------------------------------- enums

export type Role = "CUSTOMER" | "SELLER" | "ADMIN"
export type UserStatus = "ACTIVE" | "INACTIVE" | "BANNED"
export type ProductStatus = "ACTIVE" | "INACTIVE"
export type CategoryStatus = "ACTIVE" | "INACTIVE"
export type OrderStatus = "PENDING" | "CONFIRMED" | "SHIPPING" | "DELIVERED" | "CANCELLED"

// ---------------------------------------------------------------- user & auth

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

export interface UpdateUserRequest {
  fullName: string
  phone?: string
  address?: string
}

/** Claim của access token — backend đặt userId + role, subject là email. */
export interface JwtPayload {
  sub: string
  userId: number
  role: Role
  iat: number
  exp: number
}

export interface AuthUser {
  id: number
  email: string
  role: Role
}

// ---------------------------------------------------------------- category

export interface CategoryResponse {
  id: number
  name: string
  slug: string
  description: string | null
  status: CategoryStatus
  /** Backend trả dạng cây; node lá có children = [] */
  children: CategoryResponse[]
}

export interface CreateCategoryRequest {
  name: string
  slug: string
  description?: string
  parentId?: number | null
}

export interface UpdateCategoryRequest {
  name?: string
  slug?: string
  description?: string
  parentId?: number | null
  status?: CategoryStatus
}

// ---------------------------------------------------------------- product

export interface ProductImageResponse {
  id: number
  url: string
  originalName: string
  isPrimary: boolean
}

export interface ProductVariantResponse {
  id: number
  size: string
  color: string
  sku: string
  price: number
  stock: number
  imageUrl: string | null
  productId: number
}

export interface ProductResponse {
  id: number
  name: string
  slug: string
  sku: string
  price: number
  description: string | null
  stock: number
  status: ProductStatus
  categoryId: number
  categoryName: string | null
  sellerId: number
  sellerName: string | null
  createdAt: string
  updatedAt: string
  /** 0 nghĩa là chưa có review — dựa vào reviewCount để hiện "chưa có đánh giá". */
  averageRating: number
  reviewCount: number
  primaryImageUrl: string | null
  images: ProductImageResponse[]
}

export interface ProductFilterParams extends PageParams {
  keyword?: string
  categoryId?: number
  minPrice?: number
  maxPrice?: number
  status?: ProductStatus
  minRating?: number
}

export interface CreateProductRequest {
  name: string
  slug: string
  sku: string
  price: number
  description?: string
  stock: number
  categoryId: number
}

/**
 * Update là PUT toàn phần: backend khai `stock = 0` làm default nên field nào không gửi
 * sẽ bị ghi đè bằng default → form luôn gửi đủ field.
 */
export interface UpdateProductRequest {
  name?: string
  slug?: string
  sku?: string
  price?: number
  description?: string
  stock: number
  status?: ProductStatus
  categoryId?: number
}

export interface CreateProductVariantRequest {
  size: string
  color: string
  sku: string
  price: number
  stock: number
  imageUrl?: string
}

export type UpdateProductVariantRequest = Partial<CreateProductVariantRequest>

// ---------------------------------------------------------------- cart

export interface CartItemResponse {
  id: number
  productId: number
  productName: string
  variantId: number | null
  variantName: string | null
  quantity: number
  unitPrice: number
  subTotal: number
}

export interface CartResponse {
  cartId: number
  items: CartItemResponse[]
  totalAmount: number
  totalItems: number
}

export interface AddToCartRequest {
  productId: number
  variantId?: number | null
  quantity: number
}

export interface UpdateCartItemRequest {
  quantity: number
}

// ---------------------------------------------------------------- order

export interface OrderItemResponse {
  quantity: number
  price: number
  productId: number
  productName: string
  variantId: number | null
  variantName: string | null
  subTotal: number
}

export interface OrderSummaryResponse {
  id: number
  status: OrderStatus
  totalAmount: number
  createdAt: string
}

export interface OrderDetailResponse {
  id: number
  totalAmount: number
  status: OrderStatus
  shippingAddress: string
  note: string | null
  items: OrderItemResponse[]
  createdAt: string
}

export interface CreateOrderRequest {
  shippingAddress: string
  note?: string
}

export interface UpdateOrderStatusRequest {
  status: OrderStatus
}

// ---------------------------------------------------------------- payment

export interface CreatePaymentRequest {
  orderId: number
}

export interface PaymentUrlResponse {
  paymentUrl: string
}

// ---------------------------------------------------------------- review

export interface ReviewResponse {
  id: number
  rating: number
  comment: string | null
  userId: number
  userName: string
  productId: number
  createdAt: string
  updatedAt: string
}

export interface ReviewRequest {
  rating: number
  comment?: string
}
