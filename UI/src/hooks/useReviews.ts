import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import {
  canReview,
  createReview,
  deleteReview,
  listMyReviews,
  listProductReviews,
  updateReview,
} from "@/lib/api/reviews"
import { getApiErrorMessage } from "@/lib/errors"
import { useAuthStore } from "@/store/authStore"
import type { PageParams, PageResponse, ReviewRequest, ReviewResponse } from "@/types/api"

export function useProductReviews(productId: number | undefined, params: PageParams) {
  return useQuery<PageResponse<ReviewResponse>>({
    queryKey: ["reviews", productId, params],
    queryFn: () => listProductReviews(productId!, params),
    enabled: Boolean(productId),
    placeholderData: (previous) => previous,
  })
}

/** Endpoint can-review yêu cầu đăng nhập → chưa login thì bỏ qua, UI hiện CTA đăng nhập. */
export function useCanReview(productId: number | undefined) {
  const isAuthenticated = useAuthStore((s) => Boolean(s.accessToken && s.user))
  return useQuery<boolean>({
    queryKey: ["can-review", productId],
    queryFn: () => canReview(productId!),
    enabled: Boolean(productId) && isAuthenticated,
    // Đây là câu hỏi có/không, không phải dữ liệu hiển thị: lỗi thì hỏi lại cũng ra lỗi,
    // nên retry chỉ nhân đôi request. UI tự xử lý trường hợp không biết được quyền.
    retry: false,
  })
}

export function useMyReviews(params: PageParams) {
  return useQuery<PageResponse<ReviewResponse>>({
    queryKey: ["my-reviews", params],
    queryFn: () => listMyReviews(params),
    placeholderData: (previous) => previous,
  })
}

/** Rating trung bình được denormalize trên product → phải invalidate cả product sau khi ghi review. */
function useReviewInvalidation(productId: number | undefined) {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: ["reviews", productId] })
    queryClient.invalidateQueries({ queryKey: ["can-review", productId] })
    queryClient.invalidateQueries({ queryKey: ["my-reviews"] })
    queryClient.invalidateQueries({ queryKey: ["product", productId] })
    queryClient.invalidateQueries({ queryKey: ["products"] })
  }
}

export function useCreateReview(productId: number | undefined) {
  const invalidate = useReviewInvalidation(productId)
  return useMutation<ReviewResponse, Error, ReviewRequest>({
    mutationFn: (payload) => createReview(productId!, payload),
    onSuccess: () => {
      invalidate()
      toast.success("Cảm ơn bạn đã đánh giá!")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useUpdateReview(productId?: number) {
  const invalidate = useReviewInvalidation(productId)
  return useMutation<ReviewResponse, Error, { reviewId: number; payload: ReviewRequest }>({
    mutationFn: ({ reviewId, payload }) => updateReview(reviewId, payload),
    onSuccess: () => {
      invalidate()
      toast.success("Đã cập nhật đánh giá")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useDeleteReview(productId?: number) {
  const invalidate = useReviewInvalidation(productId)
  return useMutation<void, Error, number>({
    mutationFn: deleteReview,
    onSuccess: () => {
      invalidate()
      toast.success("Đã gỡ đánh giá")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}
