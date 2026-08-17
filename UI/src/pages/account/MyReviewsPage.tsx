import { useState } from "react"
import { Link } from "react-router-dom"
import { MessageSquareIcon, PencilIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { EmptyState } from "@/components/common/EmptyState"
import { ErrorState } from "@/components/common/ErrorState"
import { Pagination } from "@/components/common/Pagination"
import { RatingStars } from "@/components/common/RatingStars"
import { ReviewForm } from "@/components/reviews/ReviewForm"
import { useMyReviews, useUpdateReview } from "@/hooks/useReviews"
import { formatDateTime, formatNumber } from "@/lib/format"
import type { ReviewResponse } from "@/types/api"

const PAGE_SIZE = 10

export function MyReviewsPage() {
  const [page, setPage] = useState(0)
  const [editing, setEditing] = useState<ReviewResponse | null>(null)

  const { data, isPending, isError, error, refetch } = useMyReviews({
    page,
    size: PAGE_SIZE,
    sort: "createdAt,desc",
  })
  // productId truyền vào để hook invalidate luôn cache của sản phẩm liên quan.
  const updateReview = useUpdateReview(editing?.productId)

  return (
    <div className="container-page max-w-3xl py-8 md:py-10">
      <header className="mb-6">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Đánh giá của tôi</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          {data ? `${formatNumber(data.page.totalElements)} đánh giá đã gửi` : "Đang tải..."}
        </p>
      </header>

      {isPending ? (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 3 }, (_, i) => (
            <Skeleton key={i} className="h-28 w-full rounded-xl" />
          ))}
        </div>
      ) : isError ? (
        <ErrorState error={error} onRetry={() => refetch()} />
      ) : data && data.content.length > 0 ? (
        <>
          <ul className="flex flex-col gap-3">
            {data.content.map((review) => (
              <li key={review.id}>
                {editing?.id === review.id ? (
                  <ReviewForm
                    defaultRating={review.rating}
                    defaultComment={review.comment ?? ""}
                    submitLabel="Lưu thay đổi"
                    isPending={updateReview.isPending}
                    onCancel={() => setEditing(null)}
                    onSubmit={(payload) =>
                      updateReview.mutate(
                        { reviewId: review.id, payload },
                        { onSuccess: () => setEditing(null) }
                      )
                    }
                  />
                ) : (
                  <Card className="py-4">
                    <CardContent className="px-4">
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <Link
                          to={`/products/${review.productId}`}
                          className="text-sm font-medium underline-offset-4 hover:underline"
                        >
                          Sản phẩm #{review.productId}
                        </Link>
                        <Button variant="ghost" size="sm" onClick={() => setEditing(review)}>
                          <PencilIcon aria-hidden="true" />
                          Sửa
                        </Button>
                      </div>

                      <div className="mt-2 flex items-center gap-2">
                        <RatingStars value={review.rating} size="sm" />
                        <span className="text-xs text-muted-foreground">
                          {formatDateTime(review.createdAt)}
                        </span>
                      </div>

                      {review.comment ? (
                        <p className="mt-2 text-sm whitespace-pre-line">{review.comment}</p>
                      ) : (
                        <p className="mt-2 text-sm text-muted-foreground italic">
                          Không có nhận xét.
                        </p>
                      )}
                    </CardContent>
                  </Card>
                )}
              </li>
            ))}
          </ul>
          <Pagination meta={data.page} onPageChange={setPage} className="mt-8" />
        </>
      ) : (
        <EmptyState
          icon={MessageSquareIcon}
          title="Bạn chưa đánh giá sản phẩm nào"
          description="Sau khi đơn hàng được giao thành công, bạn có thể đánh giá sản phẩm đã mua."
          action={
            <Button asChild>
              <Link to="/orders">Xem đơn hàng</Link>
            </Button>
          }
        />
      )}
    </div>
  )
}
