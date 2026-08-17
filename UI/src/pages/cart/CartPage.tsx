import { useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import { ShoppingCartIcon, Trash2Icon } from "lucide-react"
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
import { EmptyState } from "@/components/common/EmptyState"
import { ErrorState } from "@/components/common/ErrorState"
import { QuantityStepper } from "@/components/common/QuantityStepper"
import { useCart, useClearCart, useRemoveCartItem, useUpdateCartItem } from "@/hooks/useCart"
import { formatCurrency, formatNumber } from "@/lib/format"

export function CartPage() {
  const navigate = useNavigate()
  const { data: cart, isPending, isError, error, refetch } = useCart()
  const updateItem = useUpdateCartItem()
  const removeItem = useRemoveCartItem()
  const clearCart = useClearCart()
  const [confirmClear, setConfirmClear] = useState(false)

  if (isPending) {
    return (
      <div className="container-page py-10">
        <Skeleton className="mb-6 h-9 w-48" />
        <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
          <div className="space-y-3">
            {Array.from({ length: 3 }, (_, i) => (
              <Skeleton key={i} className="h-28 w-full rounded-xl" />
            ))}
          </div>
          <Skeleton className="h-56 w-full rounded-xl" />
        </div>
      </div>
    )
  }

  if (isError) {
    return (
      <div className="container-page py-10">
        <ErrorState error={error} onRetry={() => refetch()} />
      </div>
    )
  }

  const items = cart?.items ?? []

  return (
    <div className="container-page py-8 md:py-10">
      <header className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-heading text-3xl font-semibold tracking-tight">Giỏ hàng</h1>
          <p className="mt-2 text-sm text-muted-foreground">
            {items.length > 0
              ? `${formatNumber(cart?.totalItems ?? 0)} sản phẩm trong giỏ`
              : "Giỏ hàng của bạn đang trống"}
          </p>
        </div>
        {items.length > 0 ? (
          <Button
            variant="ghost"
            size="sm"
            className="text-destructive hover:text-destructive"
            onClick={() => setConfirmClear(true)}
          >
            <Trash2Icon aria-hidden="true" />
            Xoá tất cả
          </Button>
        ) : null}
      </header>

      {items.length === 0 ? (
        <EmptyState
          icon={ShoppingCartIcon}
          title="Chưa có sản phẩm nào trong giỏ"
          description="Thêm sản phẩm bạn thích vào giỏ để tiến hành đặt hàng."
          action={
            <Button asChild>
              <Link to="/products">Khám phá sản phẩm</Link>
            </Button>
          }
        />
      ) : (
        <div className="grid gap-8 lg:grid-cols-[1fr_320px]">
          <ul className="flex flex-col gap-3">
            {items.map((item) => (
              <li key={item.id}>
                <Card className="py-4">
                  <CardContent className="flex flex-wrap items-center gap-4 px-4">
                    <div className="min-w-0 flex-1">
                      <Link
                        to={`/products/${item.productId}`}
                        className="line-clamp-2 text-sm font-medium underline-offset-4 hover:underline"
                      >
                        {item.productName}
                      </Link>
                      {item.variantName ? (
                        <p className="mt-1 text-xs text-muted-foreground">
                          Phân loại: {item.variantName}
                        </p>
                      ) : null}
                      <p className="tabular mt-1 text-sm text-muted-foreground">
                        {formatCurrency(item.unitPrice)} / sản phẩm
                      </p>
                    </div>

                    <QuantityStepper
                      value={item.quantity}
                      disabled={updateItem.isPending}
                      onChange={(quantity) => updateItem.mutate({ itemId: item.id, quantity })}
                    />

                    <p className="tabular w-28 text-right text-sm font-semibold">
                      {formatCurrency(item.subTotal)}
                    </p>

                    <Button
                      variant="ghost"
                      size="icon-sm"
                      aria-label={`Xoá ${item.productName} khỏi giỏ`}
                      disabled={removeItem.isPending}
                      onClick={() => removeItem.mutate(item.id)}
                      className="text-muted-foreground hover:text-destructive"
                    >
                      <Trash2Icon aria-hidden="true" />
                    </Button>
                  </CardContent>
                </Card>
              </li>
            ))}
          </ul>

          <aside className="lg:sticky lg:top-24 lg:self-start">
            <Card>
              <CardContent className="flex flex-col gap-4">
                <h2 className="font-heading text-lg font-semibold">Tóm tắt đơn hàng</h2>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Số lượng</span>
                  <span className="tabular">{formatNumber(cart?.totalItems ?? 0)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Tạm tính</span>
                  <span className="tabular">{formatCurrency(cart?.totalAmount ?? 0)}</span>
                </div>
                <Separator />
                <div className="flex items-baseline justify-between">
                  <span className="font-medium">Tổng cộng</span>
                  <span className="tabular font-heading text-xl font-semibold">
                    {formatCurrency(cart?.totalAmount ?? 0)}
                  </span>
                </div>
                {/* Giá và tồn kho được backend tính lại lúc checkout, nên đây chỉ là ước tính. */}
                <p className="text-xs text-muted-foreground">
                  Giá và tồn kho được xác nhận lại khi đặt hàng.
                </p>
                <Button size="lg" className="w-full" onClick={() => navigate("/checkout")}>
                  Tiến hành đặt hàng
                </Button>
                <Button variant="ghost" size="sm" asChild className="w-full">
                  <Link to="/products">Tiếp tục mua sắm</Link>
                </Button>
              </CardContent>
            </Card>
          </aside>
        </div>
      )}

      <AlertDialog open={confirmClear} onOpenChange={setConfirmClear}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xoá toàn bộ giỏ hàng?</AlertDialogTitle>
            <AlertDialogDescription>
              Tất cả sản phẩm trong giỏ sẽ bị gỡ. Bạn sẽ phải thêm lại từ đầu.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Giữ lại</AlertDialogCancel>
            <AlertDialogAction onClick={() => clearCart.mutate()}>Xoá tất cả</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
