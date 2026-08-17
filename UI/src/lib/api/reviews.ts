import { apiClient, unwrap } from "./client"
import type {
  BaseResponse,
  PageParams,
  PageResponse,
  ReviewRequest,
  ReviewResponse,
} from "@/types/api"

export async function listProductReviews(
  productId: number,
  params: PageParams = {}
): Promise<PageResponse<ReviewResponse>> {
  const res = await apiClient.get<BaseResponse<PageResponse<ReviewResponse>>>(
    `/products/${productId}/reviews`,
    { params }
  )
  return unwrap(res)
}

export async function createReview(
  productId: number,
  payload: ReviewRequest
): Promise<ReviewResponse> {
  const res = await apiClient.post<BaseResponse<ReviewResponse>>(
    `/products/${productId}/reviews`,
    payload
  )
  return unwrap(res)
}

/** Chỉ true khi user đã có đơn DELIVERED chứa sản phẩm và chưa từng review. */
export async function canReview(productId: number): Promise<boolean> {
  const res = await apiClient.get<BaseResponse<boolean>>(
    `/products/${productId}/reviews/can-review`
  )
  return unwrap(res)
}

export async function listMyReviews(params: PageParams = {}): Promise<PageResponse<ReviewResponse>> {
  const res = await apiClient.get<BaseResponse<PageResponse<ReviewResponse>>>("/reviews/me", {
    params,
  })
  return unwrap(res)
}

export async function updateReview(
  reviewId: number,
  payload: ReviewRequest
): Promise<ReviewResponse> {
  const res = await apiClient.put<BaseResponse<ReviewResponse>>(`/reviews/${reviewId}`, payload)
  return unwrap(res)
}

/** Soft delete — chỉ ADMIN. */
export async function deleteReview(reviewId: number): Promise<void> {
  await apiClient.delete<BaseResponse<null>>(`/reviews/${reviewId}`)
}
