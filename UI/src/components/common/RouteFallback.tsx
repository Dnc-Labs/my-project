import { Skeleton } from "@/components/ui/skeleton"

/**
 * Fallback trong lúc chunk của route đang tải. Dựng sẵn khối có kích thước gần giống
 * nội dung thật để tránh nhấp nháy và giật layout khi chunk về.
 */
export function RouteFallback() {
  return (
    <div className="container-page py-10" role="status" aria-live="polite">
      <span className="sr-only">Đang tải nội dung trang</span>
      <Skeleton className="h-9 w-56" />
      <Skeleton className="mt-3 h-4 w-72" />
      <div className="mt-8 grid gap-4 md:grid-cols-3">
        {Array.from({ length: 3 }, (_, i) => (
          <Skeleton key={i} className="h-40 w-full rounded-xl" />
        ))}
      </div>
    </div>
  )
}
