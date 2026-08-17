import { PencilIcon, Trash2Icon } from "lucide-react"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { RatingStars } from "@/components/common/RatingStars"
import { formatDateTime } from "@/lib/format"
import type { ReviewResponse } from "@/types/api"

interface ReviewItemProps {
  review: ReviewResponse
  canEdit?: boolean
  canDelete?: boolean
  onEdit?: () => void
  onDelete?: () => void
}

function initials(name: string): string {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(-2)
    .map((part) => part[0]?.toUpperCase() ?? "")
    .join("")
}

export function ReviewItem({ review, canEdit, canDelete, onEdit, onDelete }: ReviewItemProps) {
  const edited = review.updatedAt && review.updatedAt !== review.createdAt

  return (
    <article className="flex gap-3 border-b py-5 last:border-b-0">
      <Avatar>
        <AvatarFallback>{initials(review.userName) || "?"}</AvatarFallback>
      </Avatar>

      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
          <span className="text-sm font-medium">{review.userName}</span>
          <RatingStars value={review.rating} size="sm" />
          <span className="text-xs text-muted-foreground">{formatDateTime(review.createdAt)}</span>
          {edited ? (
            <Badge variant="secondary" className="text-[10px]">
              đã chỉnh sửa
            </Badge>
          ) : null}
        </div>

        {review.comment ? (
          <p className="mt-2 text-sm whitespace-pre-line text-foreground/90">{review.comment}</p>
        ) : (
          <p className="mt-2 text-sm text-muted-foreground italic">Không có nhận xét.</p>
        )}

        {canEdit || canDelete ? (
          <div className="mt-3 flex gap-1">
            {canEdit ? (
              <Button variant="ghost" size="xs" onClick={onEdit}>
                <PencilIcon aria-hidden="true" />
                Sửa
              </Button>
            ) : null}
            {canDelete ? (
              <Button
                variant="ghost"
                size="xs"
                onClick={onDelete}
                className="text-destructive hover:text-destructive"
              >
                <Trash2Icon aria-hidden="true" />
                Gỡ
              </Button>
            ) : null}
          </div>
        ) : null}
      </div>
    </article>
  )
}
