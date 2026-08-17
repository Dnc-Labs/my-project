import { Link, isRouteErrorResponse, useRouteError } from "react-router-dom"
import { AlertTriangleIcon, RotateCcwIcon } from "lucide-react"
import { Button } from "@/components/ui/button"

/**
 * Lưới an toàn cuối cùng cho lỗi render/lazy-load ở cấp route.
 * Không có nó, một exception trong page sẽ làm trắng cả trang mà người dùng
 * không biết chuyện gì xảy ra hay phải làm gì tiếp.
 */
export function RouteError() {
  const error = useRouteError()

  const message = isRouteErrorResponse(error)
    ? `${error.status} — ${error.statusText}`
    : error instanceof Error
      ? error.message
      : "Lỗi không xác định"

  return (
    <div className="container-page flex min-h-[60vh] flex-col items-center justify-center text-center">
      <span className="flex size-12 items-center justify-center rounded-full bg-destructive/10">
        <AlertTriangleIcon className="size-6 text-destructive" aria-hidden="true" />
      </span>
      <h1 className="mt-5 font-heading text-2xl font-semibold tracking-tight">
        Trang gặp sự cố
      </h1>
      <p className="mt-2 max-w-md text-sm text-muted-foreground">
        Có lỗi xảy ra khi hiển thị nội dung. Bạn có thể tải lại trang hoặc quay về trang chủ.
      </p>
      <p className="mt-3 max-w-md rounded-md bg-muted px-3 py-2 font-mono text-xs break-words text-muted-foreground">
        {message}
      </p>
      <div className="mt-6 flex flex-wrap justify-center gap-2">
        <Button onClick={() => window.location.reload()}>
          <RotateCcwIcon aria-hidden="true" />
          Tải lại trang
        </Button>
        <Button variant="outline" asChild>
          <Link to="/">Về trang chủ</Link>
        </Button>
      </div>
    </div>
  )
}
