import { useEffect } from "react"
import { Link, useNavigate } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Loader2Icon, ShieldCheckIcon, ShoppingCartIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { Separator } from "@/components/ui/separator"
import { Textarea } from "@/components/ui/textarea"
import { EmptyState } from "@/components/common/EmptyState"
import { useCart } from "@/hooks/useCart"
import { useCheckout } from "@/hooks/useOrders"
import { useMe } from "@/hooks/useAuth"
import { formatCurrency, formatNumber } from "@/lib/format"

const schema = z.object({
  shippingAddress: z.string().trim().min(10, "Địa chỉ cần ít nhất 10 ký tự để giao hàng chính xác"),
  note: z.string().trim().max(500, "Ghi chú tối đa 500 ký tự").optional(),
})

type FormValues = z.infer<typeof schema>

export function CheckoutPage() {
  const navigate = useNavigate()
  const { data: cart, isPending } = useCart()
  const { data: me } = useMe()
  const checkout = useCheckout()

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { shippingAddress: "", note: "" },
  })

  // Điền sẵn địa chỉ trong hồ sơ nhưng vẫn cho sửa — địa chỉ giao có thể khác địa chỉ mặc định.
  useEffect(() => {
    if (me?.address && !form.getValues("shippingAddress")) {
      form.setValue("shippingAddress", me.address)
    }
  }, [me?.address, form])

  const items = cart?.items ?? []

  const onSubmit = (values: FormValues) => {
    checkout.mutate(
      { shippingAddress: values.shippingAddress, note: values.note || undefined },
      {
        onSuccess: (order) => navigate(`/orders/${order.id}?justCreated=1`, { replace: true }),
      }
    )
  }

  if (!isPending && items.length === 0) {
    return (
      <div className="container-page py-10">
        <EmptyState
          icon={ShoppingCartIcon}
          title="Không có gì để đặt hàng"
          description="Giỏ hàng trống nên chưa thể tạo đơn. Thêm sản phẩm rồi quay lại nhé."
          action={
            <Button asChild>
              <Link to="/products">Xem sản phẩm</Link>
            </Button>
          }
        />
      </div>
    )
  }

  return (
    <div className="container-page py-8 md:py-10">
      <h1 className="font-heading text-3xl font-semibold tracking-tight">Đặt hàng</h1>
      <p className="mt-2 text-sm text-muted-foreground">
        Kiểm tra lại sản phẩm và nhập địa chỉ nhận hàng.
      </p>

      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_340px]">
        <Card>
          <CardContent>
            <h2 className="font-heading text-lg font-semibold">Thông tin giao hàng</h2>
            <Form {...form}>
              <form onSubmit={form.handleSubmit(onSubmit)} className="mt-5 space-y-5" noValidate>
                <FormField
                  control={form.control}
                  name="shippingAddress"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>
                        Địa chỉ nhận hàng <span className="text-destructive">*</span>
                      </FormLabel>
                      <FormControl>
                        <Input
                          autoComplete="street-address"
                          placeholder="Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành"
                          {...field}
                        />
                      </FormControl>
                      <FormDescription>
                        Ghi đầy đủ để người bán giao đúng địa chỉ.
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="note"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Ghi chú cho người bán</FormLabel>
                      <FormControl>
                        <Textarea
                          placeholder="Ví dụ: giao giờ hành chính, gọi trước khi giao..."
                          className="min-h-24"
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <Button type="submit" size="lg" className="w-full" disabled={checkout.isPending}>
                  {checkout.isPending ? (
                    <Loader2Icon className="animate-spin" aria-hidden="true" />
                  ) : null}
                  {checkout.isPending ? "Đang tạo đơn..." : "Xác nhận đặt hàng"}
                </Button>

                <p className="flex items-start gap-2 text-xs text-muted-foreground">
                  <ShieldCheckIcon className="mt-0.5 size-3.5 shrink-0" aria-hidden="true" />
                  Đơn được tạo trước, sau đó bạn chọn thanh toán VNPay hoặc thanh toán khi nhận hàng.
                </p>
              </form>
            </Form>
          </CardContent>
        </Card>

        <aside className="lg:sticky lg:top-24 lg:self-start">
          <Card>
            <CardContent className="flex flex-col gap-4">
              <h2 className="font-heading text-lg font-semibold">Đơn của bạn</h2>
              <ul className="flex flex-col gap-3">
                {items.map((item) => (
                  <li key={item.id} className="flex gap-3 text-sm">
                    <span className="tabular flex size-6 shrink-0 items-center justify-center rounded bg-muted text-xs font-medium">
                      {item.quantity}
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="line-clamp-2">{item.productName}</p>
                      {item.variantName ? (
                        <p className="text-xs text-muted-foreground">{item.variantName}</p>
                      ) : null}
                    </div>
                    <span className="tabular shrink-0">{formatCurrency(item.subTotal)}</span>
                  </li>
                ))}
              </ul>

              <Separator />

              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">Số lượng</span>
                <span className="tabular">{formatNumber(cart?.totalItems ?? 0)}</span>
              </div>
              <div className="flex items-baseline justify-between">
                <span className="font-medium">Tổng cộng</span>
                <span className="tabular font-heading text-xl font-semibold">
                  {formatCurrency(cart?.totalAmount ?? 0)}
                </span>
              </div>

              <Button variant="ghost" size="sm" asChild className="w-full">
                <Link to="/cart">Quay lại giỏ hàng</Link>
              </Button>
            </CardContent>
          </Card>
        </aside>
      </div>
    </div>
  )
}
