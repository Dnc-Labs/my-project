import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { RatingInput } from "@/components/common/RatingStars"

interface ReviewFormProps {
  defaultRating?: number
  defaultComment?: string
  submitLabel?: string
  isPending?: boolean
  onSubmit: (payload: { rating: number; comment?: string }) => void
  onCancel?: () => void
}

const MAX_COMMENT = 2000

export function ReviewForm({
  defaultRating = 5,
  defaultComment = "",
  submitLabel = "Gửi đánh giá",
  isPending,
  onSubmit,
  onCancel,
}: ReviewFormProps) {
  const [rating, setRating] = useState(defaultRating)
  const [comment, setComment] = useState(defaultComment)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (rating < 1 || rating > 5) {
      setError("Vui lòng chọn từ 1 đến 5 sao.")
      return
    }
    setError(null)
    onSubmit({ rating, comment: comment.trim() || undefined })
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4 rounded-xl border bg-card p-5">
      <div>
        <Label className="mb-2 block">
          Chấm điểm <span className="text-destructive">*</span>
        </Label>
        <RatingInput value={rating} onChange={setRating} disabled={isPending} />
        {error ? (
          <p role="alert" className="mt-1.5 text-sm text-destructive">
            {error}
          </p>
        ) : null}
      </div>

      <div>
        <Label htmlFor="review-comment" className="mb-2 block">
          Nhận xét
        </Label>
        <Textarea
          id="review-comment"
          value={comment}
          maxLength={MAX_COMMENT}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Sản phẩm dùng có tốt không? Đóng gói, giao hàng thế nào?"
          className="min-h-28"
          disabled={isPending}
        />
        <p className="mt-1.5 text-xs text-muted-foreground">
          Không bắt buộc — tối đa {MAX_COMMENT} ký tự ({comment.length}/{MAX_COMMENT}).
        </p>
      </div>

      <div className="flex gap-2">
        <Button type="submit" disabled={isPending}>
          {isPending ? "Đang gửi..." : submitLabel}
        </Button>
        {onCancel ? (
          <Button type="button" variant="ghost" onClick={onCancel} disabled={isPending}>
            Huỷ
          </Button>
        ) : null}
      </div>
    </form>
  )
}
