import { useState } from "react"
import { Link } from "react-router-dom"
import { MessageSquareIcon } from "lucide-react"
import { Skeleton } from "@/components/ui/skeleton"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { EmptyState } from "@/components/common/EmptyState"
import { ErrorState } from "@/components/common/ErrorState"
import { Pagination } from "@/components/common/Pagination"
import { RatingStars } from "@/components/common/RatingStars"
import { ReviewForm } from "@/components/reviews/ReviewForm"
import { ReviewItem } from "@/components/reviews/ReviewItem"
import {
  useCanReview,
  useCreateReview,
  useDeleteReview,
  useProductReviews,
  useUpdateReview,
} from "@/hooks/useReviews"
import { useAuthStore } from "@/store/authStore"
import { formatNumber, formatRating } from "@/lib/format"
import type { ProductResponse, ReviewResponse } from "@/types/api"

const PAGE_SIZE = 5

export function ReviewSection({ product }: { product: ProductResponse }) {
  const [page, setPage] = useState(0)
  const [editing, setEditing] = useState<ReviewResponse | null>(null)
  const [pendingDelete, setPendingDelete] = useState<ReviewResponse | null>(null)

  const user = useAuthStore((s) => s.user)
  const reviews = useProductReviews(product.id, { page, size: PAGE_SIZE, sort: "createdAt,desc" })
  const { data: canReview, isError: canReviewFailed } = useCanReview(product.id)

  const createReview = useCreateReview(product.id)
  const updateReview = useUpdateReview(product.id)
  const deleteReview = useDeleteReview(product.id)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center gap-4 rounded-xl border bg-card p-5">
        <div className="text-center">
          <p className="tabular font-heading text-4xl font-semibold">
            {formatRating(product.averageRating)}
          </p>
          <RatingStars value={product.averageRating} className="mt-1" />
          <p className="mt-1 text-xs text-muted-foreground">
            {formatNumber(product.reviewCount)} đánh giá
          </p>
        </div>
        <div className="min-w-0 flex-1 text-sm text-muted-foreground">
          {product.reviewCount > 0
            ? "Điểm trung bình được tính từ đánh giá của những người đã nhận hàng."
            : "Sản phẩm chưa có đánh giá nào. Hãy là người đầu tiên sau khi nhận hàng."}
        </div>
      </div>

      {/* can-review = false khi chưa mua, đơn chưa DELIVERED, hoặc đã đánh giá rồi. */}
      {!user ? (
        <div className="rounded-xl border border-dashed p-5 text-sm text-muted-foreground">
          <Link to="/login" className="font-medium text-foreground underline underline-offset-4">
            Đăng nhập
          </Link>{" "}
          để đánh giá sản phẩm bạn đã mua.
        </div>
      ) : canReview ? (
        <ReviewForm
          isPending={createReview.isPending}
          onSubmit={(payload) => createReview.mutate(payload)}
        />
      ) : canReviewFailed ? (
        // Không xác định được quyền (endpoint can-review lỗi): vẫn mở form thay vì chặn oan.
        // Nếu thực sự chưa đủ điều kiện, backend từ chối lúc gửi và toast hiện đúng lý do.
        <div className="flex flex-col gap-3">
          <p className="rounded-lg border border-warning/30 bg-warning/10 px-4 py-2.5 text-sm text-warning-foreground">
            Chưa kiểm tra được điều kiện đánh giá. Bạn vẫn có thể gửi — nếu đơn chưa giao hoặc bạn
            đã đánh giá rồi, hệ thống sẽ thông báo.
          </p>
          <ReviewForm
            isPending={createReview.isPending}
            onSubmit={(payload) => createReview.mutate(payload)}
          />
        </div>
      ) : null}

      {editing ? (
        <ReviewForm
          defaultRating={editing.rating}
          defaultComment={editing.comment ?? ""}
          submitLabel="Lưu thay đổi"
          isPending={updateReview.isPending}
          onCancel={() => setEditing(null)}
          onSubmit={(payload) =>
            updateReview.mutate(
              { reviewId: editing.id, payload },
              { onSuccess: () => setEditing(null) }
            )
          }
        />
      ) : null}

      {reviews.isPending ? (
        <div className="flex flex-col gap-4">
          {Array.from({ length: 3 }, (_, i) => (
            <div key={i} className="flex gap-3">
              <Skeleton className="size-9 rounded-full" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-4 w-40" />
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-2/3" />
              </div>
            </div>
          ))}
        </div>
      ) : reviews.isError ? (
        <ErrorState error={reviews.error} onRetry={() => reviews.refetch()} />
      ) : reviews.data && reviews.data.content.length > 0 ? (
        <>
          <div className="rounded-xl border bg-card px-5">
            {reviews.data.content.map((review) => (
              <ReviewItem
                key={review.id}
                review={review}
                canEdit={user?.id === review.userId}
                canDelete={user?.role === "ADMIN"}
                onEdit={() => setEditing(review)}
                onDelete={() => setPendingDelete(review)}
              />
            ))}
          </div>
          <Pagination meta={reviews.data.page} onPageChange={setPage} />
        </>
      ) : (
        <EmptyState
          icon={MessageSquareIcon}
          title="Chưa có đánh giá"
          description="Đánh giá chỉ mở cho người đã nhận hàng, nên mọi nhận xét ở đây đều từ đơn thật."
        />
      )}

      <AlertDialog open={Boolean(pendingDelete)} onOpenChange={(open) => !open && setPendingDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Gỡ đánh giá này?</AlertDialogTitle>
            <AlertDialogDescription>
              Đánh giá của {pendingDelete?.userName} sẽ bị ẩn khỏi sản phẩm và điểm trung bình được
              tính lại. Thao tác này dành cho quản trị viên.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Huỷ</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (pendingDelete) deleteReview.mutate(pendingDelete.id)
                setPendingDelete(null)
              }}
            >
              Gỡ đánh giá
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
