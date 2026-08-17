import { useState } from "react"
import { InfoIcon, Loader2Icon, SearchIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"
import { OrderStatusBadge } from "@/components/common/OrderStatusBadge"
import { useUpdateOrderStatus } from "@/hooks/useOrders"
import { formatCurrency, formatDateTime } from "@/lib/format"
import { ORDER_STATUS_META, nextStatuses } from "@/lib/orderStatus"
import type { OrderDetailResponse, OrderStatus } from "@/types/api"

/**
 * Backend hiện chỉ có GET /api/orders cho ĐƠN CỦA CHÍNH MÌNH và GET /api/orders/{id}
 * kiểm tra ownership, nên seller/admin không liệt kê hay xem trước được đơn của khách.
 * Endpoint duy nhất mở cho staff là PUT /api/orders/{id}/status — nó trả về order detail,
 * nên trang này thao tác theo mã đơn rồi hiển thị kết quả nhận được.
 */
export function SellerOrdersPage() {
  const [orderId, setOrderId] = useState("")
  const [order, setOrder] = useState<OrderDetailResponse | null>(null)
  const updateStatus = useUpdateOrderStatus()

  const applyStatus = (status: OrderStatus) => {
    const id = Number(orderId)
    if (!Number.isInteger(id) || id <= 0) return
    updateStatus.mutate({ id, status }, { onSuccess: (data) => setOrder(data) })
  }

  // Chưa tải được đơn thì cho phép mọi bước đầu luồng; sau khi có đơn thì bám đúng state machine.
  const actions: OrderStatus[] = order
    ? nextStatuses(order.status).filter((status) => status !== "CANCELLED")
    : ["CONFIRMED", "SHIPPING", "DELIVERED"]

  return (
    <div className="flex flex-col gap-6">
      <header>
        <h1 className="font-heading text-2xl font-semibold tracking-tight">Xử lý đơn hàng</h1>
        <p className="mt-1.5 text-sm text-muted-foreground">
          Xác nhận, giao và hoàn tất đơn của khách theo mã đơn.
        </p>
      </header>

      <div className="flex items-start gap-3 rounded-xl border border-info/30 bg-info/10 p-4 text-sm">
        <InfoIcon className="mt-0.5 size-4 shrink-0 text-info-foreground" aria-hidden="true" />
        <div>
          <p className="font-medium">Giới hạn của API hiện tại</p>
          <p className="mt-1 text-muted-foreground">
            Backend chưa có endpoint liệt kê đơn cho người bán và{" "}
            <code className="font-mono text-xs">GET /api/orders/{"{id}"}</code> chỉ trả về đơn của
            chính chủ đơn. Vì vậy trang này thao tác theo mã đơn: chọn hành động, backend kiểm tra
            quyền và luồng trạng thái rồi trả về đơn sau khi cập nhật.
          </p>
        </div>
      </div>

      <Card>
        <CardContent className="flex flex-col gap-4">
          <div className="flex flex-wrap items-end gap-3">
            <div className="min-w-48 flex-1">
              <Label htmlFor="order-id" className="mb-1.5">
                Mã đơn hàng
              </Label>
              <div className="relative">
                <SearchIcon
                  className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground"
                  aria-hidden="true"
                />
                <Input
                  id="order-id"
                  type="number"
                  inputMode="numeric"
                  min={1}
                  value={orderId}
                  onChange={(e) => {
                    setOrderId(e.target.value)
                    setOrder(null)
                  }}
                  placeholder="Ví dụ: 12"
                  className="pl-9"
                />
              </div>
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            {actions.map((status) => (
              <Button
                key={status}
                variant="outline"
                disabled={!orderId || updateStatus.isPending}
                onClick={() => applyStatus(status)}
              >
                {updateStatus.isPending ? (
                  <Loader2Icon className="animate-spin" aria-hidden="true" />
                ) : null}
                Chuyển sang: {ORDER_STATUS_META[status].label}
              </Button>
            ))}
          </div>

          <p className="text-xs text-muted-foreground">
            Luồng hợp lệ: Chờ xác nhận → Đã xác nhận → Đang giao → Đã giao. Backend từ chối các bước
            nhảy cóc hoặc thao tác sai quyền.
          </p>
        </CardContent>
      </Card>

      {order ? (
        <Card>
          <CardContent className="flex flex-col gap-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="tabular font-heading text-lg font-semibold">Đơn #{order.id}</h2>
                <p className="mt-0.5 text-sm text-muted-foreground">
                  Đặt lúc {formatDateTime(order.createdAt)}
                </p>
              </div>
              <OrderStatusBadge status={order.status} />
            </div>

            <Separator />

            <ul className="flex flex-col divide-y">
              {order.items.map((item, index) => (
                <li
                  key={`${item.productId}-${item.variantId ?? "base"}-${index}`}
                  className="flex gap-3 py-3 first:pt-0 last:pb-0 text-sm"
                >
                  <span className="tabular flex size-7 shrink-0 items-center justify-center rounded bg-muted text-xs font-medium">
                    {item.quantity}
                  </span>
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-medium">{item.productName}</p>
                    {item.variantName ? (
                      <p className="text-xs text-muted-foreground">{item.variantName}</p>
                    ) : null}
                  </div>
                  <span className="tabular">{formatCurrency(item.subTotal)}</span>
                </li>
              ))}
            </ul>

            <Separator />

            <div className="flex items-baseline justify-between">
              <span className="font-medium">Tổng tiền</span>
              <span className="tabular font-heading text-xl font-semibold">
                {formatCurrency(order.totalAmount)}
              </span>
            </div>

            <div className="text-sm">
              <p className="text-muted-foreground">Địa chỉ giao</p>
              <p className="mt-0.5">{order.shippingAddress}</p>
            </div>
            {order.note ? (
              <div className="text-sm">
                <p className="text-muted-foreground">Ghi chú của khách</p>
                <p className="mt-0.5 whitespace-pre-line">{order.note}</p>
              </div>
            ) : null}
          </CardContent>
        </Card>
      ) : null}
    </div>
  )
}
