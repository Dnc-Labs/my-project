import { AlertTriangleIcon, RefreshCwIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { getApiErrorMessage } from "@/lib/errors"
import { cn } from "@/lib/utils"

interface ErrorStateProps {
  error: unknown
  onRetry?: () => void
  className?: string
}

/** Lỗi tải dữ liệu phải nói được nguyên nhân VÀ cách thoát ra (retry), không chỉ báo đỏ. */
export function ErrorState({ error, onRetry, className }: ErrorStateProps) {
  return (
    <div
      role="alert"
      className={cn(
        "flex flex-col items-center justify-center rounded-xl border border-destructive/30 bg-destructive/5 px-6 py-12 text-center",
        className
      )}
    >
      <AlertTriangleIcon className="size-6 text-destructive" aria-hidden="true" />
      <h3 className="mt-3 text-base font-semibold">Không tải được dữ liệu</h3>
      <p className="mt-1.5 max-w-md text-sm text-muted-foreground">{getApiErrorMessage(error)}</p>
      {onRetry ? (
        <Button variant="outline" size="sm" className="mt-5" onClick={onRetry}>
          <RefreshCwIcon aria-hidden="true" />
          Thử lại
        </Button>
      ) : null}
    </div>
  )
}
