import { Link, useSearchParams } from "react-router-dom"
import { ChevronRightIcon, PackageIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { EmptyState } from "@/components/common/EmptyState"
import { ErrorState } from "@/components/common/ErrorState"
import { OrderStatusBadge } from "@/components/common/OrderStatusBadge"
import { Pagination } from "@/components/common/Pagination"
import { useMyOrders } from "@/hooks/useOrders"
import { formatCurrency, formatDateTime, formatNumber } from "@/lib/format"
import { ORDER_STATUS_META } from "@/lib/orderStatus"
import type { OrderStatus } from "@/types/api"
import { cn } from "@/lib/utils"

const PAGE_SIZE = 10
const STATUS_TABS: (OrderStatus | "ALL")[] = [
  "ALL",
  "PENDING",
  "CONFIRMED",
  "SHIPPING",
  "DELIVERED",
  "CANCELLED",
]

export function OrdersPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const statusParam = searchParams.get("status") as OrderStatus | null
  const page = Number(searchParams.get("page") ?? 0)

  const { data, isPending, isError, error, refetch } = useMyOrders({
    status: statusParam ?? undefined,
    page,
    size: PAGE_SIZE,
    sort: "createdAt,desc",
  })

  const updateParams = (patch: Record<string, string | number | undefined>) => {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev)
      Object.entries(patch).forEach(([key, value]) => {
        if (value === undefined || value === "") next.delete(key)
        else next.set(key, String(value))
      })
      return next
    })
  }

  return (
    <div className="container-page py-8 md:py-10">
      <header className="mb-6">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Đơn hàng của tôi</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          {data ? `${formatNumber(data.page.totalElements)} đơn hàng` : "Đang tải đơn hàng..."}
        </p>
      </header>

      {/* Lọc trạng thái nằm trên URL để chia sẻ/quay lại giữ nguyên tab đang xem. */}
      <div className="mb-6 flex flex-wrap gap-2" role="tablist" aria-label="Lọc theo trạng thái">
        {STATUS_TABS.map((status) => {
          const active = status === "ALL" ? !statusParam : statusParam === status
          return (
            <button
              key={status}
              type="button"
              role="tab"
              aria-selected={active}
              onClick={() =>
                updateParams({ status: status === "ALL" ? undefined : status, page: undefined })
              }
              className={cn(
                "min-h-9 rounded-full border px-3.5 text-sm transition-colors",
                active
                  ? "border-foreground bg-secondary font-medium"
                  : "border-border text-muted-foreground hover:border-muted-foreground hover:text-foreground"
              )}
            >
              {status === "ALL" ? "Tất cả" : ORDER_STATUS_META[status].label}
            </button>
          )
        })}
      </div>

      {isPending ? (
        <div className="flex flex-col gap-3">
          {Array.from({ length: 4 }, (_, i) => (
            <Skeleton key={i} className="h-24 w-full rounded-xl" />
          ))}
        </div>
      ) : isError ? (
        <ErrorState error={error} onRetry={() => refetch()} />
      ) : data && data.content.length > 0 ? (
        <>
          <ul className="flex flex-col gap-3">
            {data.content.map((order) => (
              <li key={order.id}>
                <Card className="py-4 transition-shadow hover:shadow-md">
                  <CardContent className="flex flex-wrap items-center gap-4 px-4">
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="tabular font-medium">Đơn #{order.id}</span>
                        <OrderStatusBadge status={order.status} />
                      </div>
                      <p className="mt-1 text-sm text-muted-foreground">
                        Đặt lúc {formatDateTime(order.createdAt)}
                      </p>
                    </div>

                    <p className="tabular font-heading text-lg font-semibold">
                      {formatCurrency(order.totalAmount)}
                    </p>

                    <Button variant="outline" size="sm" asChild>
                      <Link to={`/orders/${order.id}`}>
                        Chi tiết
                        <ChevronRightIcon aria-hidden="true" />
                      </Link>
                    </Button>
                  </CardContent>
                </Card>
              </li>
            ))}
          </ul>
          <Pagination
            meta={data.page}
            onPageChange={(next) => updateParams({ page: next === 0 ? undefined : next })}
            className="mt-8"
          />
        </>
      ) : (
        <EmptyState
          icon={PackageIcon}
          title={statusParam ? "Không có đơn nào ở trạng thái này" : "Bạn chưa có đơn hàng nào"}
          description={
            statusParam
              ? "Thử chọn trạng thái khác để xem các đơn còn lại."
              : "Khi bạn đặt hàng, đơn sẽ xuất hiện ở đây để theo dõi trạng thái."
          }
          action={
            <Button asChild>
              <Link to="/products">Mua sắm ngay</Link>
            </Button>
          }
        />
      )}
    </div>
  )
}
