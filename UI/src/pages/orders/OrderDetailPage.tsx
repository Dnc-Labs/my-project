import { useState } from "react"
import { Link, useParams, useSearchParams } from "react-router-dom"
import {
  ArrowLeftIcon,
  CheckIcon,
  CreditCardIcon,
  Loader2Icon,
  MapPinIcon,
  PartyPopperIcon,
  StickyNoteIcon,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { Skeleton } from "@/components/ui/skeleton"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { ErrorState } from "@/components/common/ErrorState"
import { OrderStatusBadge } from "@/components/common/OrderStatusBadge"
import { useCreatePayment, useOrder, useUpdateOrderStatus } from "@/hooks/useOrders"
import { useAuthStore } from "@/store/authStore"
import { formatCurrency, formatDateTime, formatNumber } from "@/lib/format"
import { ORDER_FLOW, ORDER_STATUS_META, canCustomerCancel, canPay, nextStatuses } from "@/lib/orderStatus"
import { cn } from "@/lib/utils"

export function OrderDetailPage() {
  const { id } = useParams()
  const orderId = Number(id)
  const [searchParams] = useSearchParams()
  const justCreated = searchParams.get("justCreated") === "1"

  const { data: order, isPending, isError, error, refetch } = useOrder(orderId)
  const updateStatus = useUpdateOrderStatus()
  const createPayment = useCreatePayment()
  const isStaff = useAuthStore((s) => s.isStaff())
  const [confirmCancel, setConfirmCancel] = useState(false)

  if (isPending) {
    return (
      <div className="container-page py-10">
        <Skeleton className="mb-6 h-9 w-56" />
        <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
          <Skeleton className="h-72 w-full rounded-xl" />
          <Skeleton className="h-56 w-full rounded-xl" />
        </div>
      </div>
    )
  }

  if (isError || !order) {
    return (
      <div className="container-page py-10">
        <ErrorState error={error} onRetry={() => refetch()} />
      </div>
    )
  }

  const currentStep = ORDER_FLOW.indexOf(order.status)
  const cancelled = order.status === "CANCELLED"
  // Staff được đẩy trạng thái tiến; khách chỉ được huỷ khi đơn chưa rời kho.
  const staffActions = isStaff ? nextStatuses(order.status).filter((s) => s !== "CANCELLED") : []

  return (
    <div className="container-page py-8 md:py-10">
      <Button variant="ghost" size="sm" asChild className="mb-4 -ml-2">
        <Link to="/orders">
          <ArrowLeftIcon aria-hidden="true" />
          Tất cả đơn hàng
        </Link>
      </Button>

      {justCreated ? (
        <div
          role="status"
          className="mb-6 flex items-start gap-3 rounded-xl border border-success/30 bg-success/10 p-4"
        >
          <PartyPopperIcon className="mt-0.5 size-5 shrink-0 text-success-foreground" aria-hidden="true" />
          <div>
            <p className="font-medium">Đặt hàng thành công!</p>
            <p className="mt-0.5 text-sm text-muted-foreground">
              Đơn đang chờ người bán xác nhận. Bạn có thể thanh toán trước qua VNPay hoặc trả khi
              nhận hàng.
            </p>
          </div>
        </div>
      ) : null}

      <header className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="tabular font-heading text-3xl font-semibold tracking-tight">
            Đơn #{order.id}
          </h1>
          <p className="mt-2 text-sm text-muted-foreground">
            Đặt lúc {formatDateTime(order.createdAt)}
          </p>
        </div>
        <OrderStatusBadge status={order.status} className="text-sm" />
      </header>

      <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
        <div className="flex flex-col gap-6">
          <Card>
            <CardContent>
              <h2 className="mb-5 font-heading text-lg font-semibold">Tiến trình đơn hàng</h2>

              {cancelled ? (
                <p className="rounded-lg bg-destructive/10 p-4 text-sm text-destructive">
                  {ORDER_STATUS_META.CANCELLED.description}
                </p>
              ) : (
                <ol className="flex flex-col gap-0 sm:flex-row">
                  {ORDER_FLOW.map((status, index) => {
                    const done = index <= currentStep
                    const active = index === currentStep
                    return (
                      <li key={status} className="flex flex-1 gap-3 sm:flex-col sm:gap-2">
                        <div className="flex flex-col items-center sm:w-full sm:flex-row">
                          <span
                            className={cn(
                              "flex size-8 shrink-0 items-center justify-center rounded-full border text-xs font-medium",
                              done
                                ? "border-success bg-success/15 text-success-foreground"
                                : "border-border bg-background text-muted-foreground"
                            )}
                            aria-hidden="true"
                          >
                            {done ? <CheckIcon className="size-4" /> : index + 1}
                          </span>
                          {index < ORDER_FLOW.length - 1 ? (
                            <span
                              className={cn(
                                "w-px flex-1 sm:h-px sm:w-full",
                                index < currentStep ? "bg-success" : "bg-border"
                              )}
                              aria-hidden="true"
                            />
                          ) : null}
                        </div>
                        <div className="pb-6 sm:pb-0">
                          <p className={cn("text-sm", active ? "font-semibold" : "font-medium")}>
                            {ORDER_STATUS_META[status].label}
                          </p>
                          {active ? (
                            <p className="mt-0.5 text-xs text-muted-foreground">
                              {ORDER_STATUS_META[status].description}
                            </p>
                          ) : null}
                        </div>
                      </li>
                    )
                  })}
                </ol>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <h2 className="mb-4 font-heading text-lg font-semibold">
                Sản phẩm ({formatNumber(order.items.length)})
              </h2>
              <ul className="flex flex-col divide-y">
                {order.items.map((item, index) => (
                  <li key={`${item.productId}-${item.variantId ?? "base"}-${index}`} className="flex gap-4 py-4 first:pt-0 last:pb-0">
                    <span className="tabular flex size-8 shrink-0 items-center justify-center rounded bg-muted text-sm font-medium">
                      {item.quantity}
                    </span>
                    <div className="min-w-0 flex-1">
                      <Link
                        to={`/products/${item.productId}`}
                        className="text-sm font-medium underline-offset-4 hover:underline"
                      >
                        {item.productName}
                      </Link>
                      {item.variantName ? (
                        <p className="mt-0.5 text-xs text-muted-foreground">{item.variantName}</p>
                      ) : null}
                      {/* Giá đã snapshot lúc đặt — sau này người bán đổi giá cũng không ảnh hưởng đơn cũ. */}
                      <p className="tabular mt-1 text-sm text-muted-foreground">
                        {formatCurrency(item.price)} × {item.quantity}
                      </p>
                    </div>
                    <p className="tabular text-sm font-semibold">{formatCurrency(item.subTotal)}</p>
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="flex flex-col gap-4">
              <h2 className="font-heading text-lg font-semibold">Thông tin giao hàng</h2>
              <div className="flex gap-3 text-sm">
                <MapPinIcon className="mt-0.5 size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
                <p>{order.shippingAddress}</p>
              </div>
              {order.note ? (
                <div className="flex gap-3 text-sm">
                  <StickyNoteIcon className="mt-0.5 size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
                  <p className="whitespace-pre-line text-muted-foreground">{order.note}</p>
                </div>
              ) : null}
            </CardContent>
          </Card>
        </div>

        <aside className="lg:sticky lg:top-24 lg:self-start">
          <Card>
            <CardContent className="flex flex-col gap-4">
              <h2 className="font-heading text-lg font-semibold">Thanh toán</h2>
              <div className="flex items-baseline justify-between">
                <span className="text-sm text-muted-foreground">Tổng tiền</span>
                <span className="tabular font-heading text-xl font-semibold">
                  {formatCurrency(order.totalAmount)}
                </span>
              </div>

              <Separator />

              {canPay(order.status) ? (
                <Button
                  size="lg"
                  className="w-full"
                  disabled={createPayment.isPending}
                  onClick={() => createPayment.mutate(order.id)}
                >
                  {createPayment.isPending ? (
                    <Loader2Icon className="animate-spin" aria-hidden="true" />
                  ) : (
                    <CreditCardIcon aria-hidden="true" />
                  )}
                  Thanh toán qua VNPay
                </Button>
              ) : null}

              {staffActions.map((status) => (
                <Button
                  key={status}
                  variant="outline"
                  className="w-full"
                  disabled={updateStatus.isPending}
                  onClick={() => updateStatus.mutate({ id: order.id, status })}
                >
                  Chuyển sang: {ORDER_STATUS_META[status].label}
                </Button>
              ))}

              {canCustomerCancel(order.status) ? (
                <Button
                  variant="ghost"
                  className="w-full text-destructive hover:text-destructive"
                  disabled={updateStatus.isPending}
                  onClick={() => setConfirmCancel(true)}
                >
                  Huỷ đơn hàng
                </Button>
              ) : null}

              {order.status === "DELIVERED" ? (
                <p className="text-xs text-muted-foreground">
                  Đơn đã giao — bạn có thể vào trang sản phẩm để đánh giá.
                </p>
              ) : null}
            </CardContent>
          </Card>
        </aside>
      </div>

      <AlertDialog open={confirmCancel} onOpenChange={setConfirmCancel}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Huỷ đơn #{order.id}?</AlertDialogTitle>
            <AlertDialogDescription>
              Đơn sẽ chuyển sang trạng thái đã huỷ và toàn bộ tồn kho được hoàn lại. Bạn không thể
              khôi phục đơn sau khi huỷ.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Giữ đơn</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => updateStatus.mutate({ id: order.id, status: "CANCELLED" })}
            >
              Huỷ đơn hàng
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
