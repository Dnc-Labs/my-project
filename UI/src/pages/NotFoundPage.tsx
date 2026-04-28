import { Link } from "react-router-dom"
import { Button } from "@/components/ui/button"

export function NotFoundPage() {
  return (
    <div className="mx-auto flex min-h-[60vh] max-w-7xl flex-col items-center justify-center px-4 text-center">
      <p className="font-mono text-sm text-muted-foreground">404</p>
      <h1 className="mt-4 font-heading text-4xl font-semibold tracking-tight">Không tìm thấy trang</h1>
      <p className="mt-2 text-muted-foreground">Trang bạn yêu cầu không tồn tại hoặc đã được di chuyển.</p>
      <Button asChild className="mt-6">
        <Link to="/">Về trang chủ</Link>
      </Button>
    </div>
  )
}
