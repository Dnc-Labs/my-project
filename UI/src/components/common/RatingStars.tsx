import { StarIcon } from "lucide-react"
import { cn } from "@/lib/utils"

interface RatingStarsProps {
  value: number
  size?: "sm" | "md" | "lg"
  className?: string
}

const SIZE_CLASS = {
  sm: "size-3.5",
  md: "size-4",
  lg: "size-5",
} as const

/**
 * Hiển thị điểm dạng sao (chỉ đọc). Nửa sao được cắt bằng overlay width %
 * thay vì icon "half-star" riêng để mọi mức lẻ (4.3) đều thể hiện đúng.
 */
export function RatingStars({ value, size = "md", className }: RatingStarsProps) {
  const clamped = Math.max(0, Math.min(5, value))
  const iconClass = SIZE_CLASS[size]

  return (
    <span
      className={cn("relative inline-flex shrink-0", className)}
      role="img"
      aria-label={`${clamped.toFixed(1)} trên 5 sao`}
    >
      <span className="flex gap-0.5 text-muted-foreground/40">
        {Array.from({ length: 5 }, (_, i) => (
          <StarIcon key={i} className={iconClass} aria-hidden="true" />
        ))}
      </span>
      <span
        className="pointer-events-none absolute inset-0 overflow-hidden"
        style={{ width: `${(clamped / 5) * 100}%` }}
        aria-hidden="true"
      >
        <span className="flex gap-0.5 text-warning">
          {Array.from({ length: 5 }, (_, i) => (
            <StarIcon key={i} className={cn(iconClass, "fill-current")} />
          ))}
        </span>
      </span>
    </span>
  )
}

interface RatingInputProps {
  value: number
  onChange: (value: number) => void
  disabled?: boolean
  className?: string
}

/** Bản nhập liệu: dùng radio thật để hỗ trợ bàn phím + screen reader, chỉ ẩn phần hiển thị mặc định. */
export function RatingInput({ value, onChange, disabled, className }: RatingInputProps) {
  return (
    <div className={cn("flex items-center gap-1", className)} role="radiogroup" aria-label="Chọn số sao">
      {[1, 2, 3, 4, 5].map((star) => (
        <label
          key={star}
          className={cn(
            "cursor-pointer rounded-md p-1 transition-transform focus-within:ring-[3px] focus-within:ring-ring/50",
            !disabled && "hover:scale-110",
            disabled && "cursor-not-allowed opacity-50"
          )}
        >
          <input
            type="radio"
            name="rating"
            value={star}
            checked={value === star}
            disabled={disabled}
            onChange={() => onChange(star)}
            className="sr-only"
          />
          <StarIcon
            className={cn(
              "size-6 transition-colors",
              star <= value ? "fill-warning text-warning" : "text-muted-foreground/40"
            )}
            aria-hidden="true"
          />
          <span className="sr-only">{star} sao</span>
        </label>
      ))}
    </div>
  )
}
