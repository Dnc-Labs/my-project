import { ChevronLeftIcon, ChevronRightIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import type { PageResponse } from "@/types/api"
import { cn } from "@/lib/utils"

interface PaginationProps {
  meta: PageResponse<unknown>["page"]
  onPageChange: (page: number) => void
  className?: string
}

/**
 * Sinh dãy số trang có rút gọn: luôn giữ trang đầu/cuối + 1 trang kề trang hiện tại,
 * phần bị cắt thay bằng "…" để thanh phân trang không giãn vô hạn khi có nhiều trang.
 */
function buildPages(current: number, total: number): (number | "ellipsis")[] {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i)

  const pages = new Set<number>([0, total - 1, current])
  if (current > 0) pages.add(current - 1)
  if (current < total - 1) pages.add(current + 1)

  const sorted = [...pages].sort((a, b) => a - b)
  const result: (number | "ellipsis")[] = []
  sorted.forEach((page, index) => {
    if (index > 0 && page - sorted[index - 1] > 1) result.push("ellipsis")
    result.push(page)
  })
  return result
}

export function Pagination({ meta, onPageChange, className }: PaginationProps) {
  if (meta.totalPages <= 1) return null

  const pages = buildPages(meta.number, meta.totalPages)

  return (
    <nav
      className={cn("flex flex-wrap items-center justify-center gap-1.5", className)}
      aria-label="Phân trang"
    >
      <Button
        variant="outline"
        size="sm"
        disabled={meta.first}
        onClick={() => onPageChange(meta.number - 1)}
        aria-label="Trang trước"
      >
        <ChevronLeftIcon aria-hidden="true" />
        <span className="hidden sm:inline">Trước</span>
      </Button>

      {pages.map((page, index) =>
        page === "ellipsis" ? (
          <span key={`gap-${index}`} className="px-1.5 text-sm text-muted-foreground" aria-hidden="true">
            …
          </span>
        ) : (
          <Button
            key={page}
            variant={page === meta.number ? "default" : "ghost"}
            size="icon-sm"
            onClick={() => onPageChange(page)}
            aria-label={`Trang ${page + 1}`}
            aria-current={page === meta.number ? "page" : undefined}
          >
            {page + 1}
          </Button>
        )
      )}

      <Button
        variant="outline"
        size="sm"
        disabled={meta.last}
        onClick={() => onPageChange(meta.number + 1)}
        aria-label="Trang sau"
      >
        <span className="hidden sm:inline">Sau</span>
        <ChevronRightIcon aria-hidden="true" />
      </Button>
    </nav>
  )
}
