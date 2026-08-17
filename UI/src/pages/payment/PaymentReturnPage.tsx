import { useEffect } from "react"
import { Link, useSearchParams } from "react-router-dom"
import { useQuery } from "@tanstack/react-query"
import { useQueryClient } from "@tanstack/react-query"
import { CheckCircle2Icon, InfoIcon, Loader2Icon, XCircleIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { ErrorState } from "@/components/common/ErrorState"
import { readVnpayReturn } from "@/lib/api/payments"
import { formatCurrency } from "@/lib/format"

/** vnp_OrderInfo backend đặt là "Thanh toan don hang {orderId}" → lấy lại id để link về đơn. */
function parseOrderId(orderInfo: string | null): number | null {
  if (!orderInfo) return null
  const match = orderInfo.match(/(\d+)\s*$/)
  return match ? Number(match[1]) : null
}

export function PaymentReturnPage() {
  const [searchParams] = useSearchParams()
  const queryClient = useQueryClient()

  const query = searchParams.toString()
  const orderId = parseOrderId(searchParams.get("vnp_OrderInfo"))
  // vnp_Amount là số tiền nhân 100 theo quy ước VNPay.
  const amountRaw = searchParams.get("vnp_Amount")
  const amount = amountRaw ? Number(amountRaw) / 100 : null
  const responseCode = searchParams.get("vnp_ResponseCode")

  const result = useQuery({
    queryKey: ["vnpay-return", query],
    queryFn: () => readVnpayReturn(`?${query}`),
    enabled: query.length > 0,
    retry: false,
  })

  const success = responseCode === "00" && result.data?.toLowerCase().includes("thanh cong")

  // IPN (server-to-server) mới là nguồn cập nhật trạng thái đơn; khi user về tới đây
  // IPN thường đã chạy xong nên chỉ cần bỏ cache để lần xem đơn kế tiếp là số liệu mới.
  useEffect(() => {
    if (!result.isSuccess) return
    queryClient.invalidateQueries({ queryKey: ["orders"] })
    if (orderId) queryClient.invalidateQueries({ queryKey: ["order", orderId] })
  }, [result.isSuccess, orderId, queryClient])

  return (
    <div className="container-page flex justify-center py-14">
      <Card className="w-full max-w-lg">
        <CardContent className="flex flex-col items-center gap-4 text-center">
          {result.isPending ? (
            <>
              <Loader2Icon className="size-10 animate-spin text-muted-foreground" aria-hidden="true" />
              <h1 className="font-heading text-xl font-semibold">Đang xác nhận giao dịch...</h1>
              <p className="text-sm text-muted-foreground">
                Vui lòng không đóng trang trong lúc kiểm tra chữ ký giao dịch.
              </p>
            </>
          ) : result.isError ? (
            <ErrorState error={result.error} className="w-full border-0 bg-transparent p-0" />
          ) : (
            <>
              <span
                className={`flex size-14 items-center justify-center rounded-full ${
                  success ? "bg-success/15" : "bg-destructive/10"
                }`}
              >
                {success ? (
                  <CheckCircle2Icon className="size-8 text-success-foreground" aria-hidden="true" />
                ) : (
                  <XCircleIcon className="size-8 text-destructive" aria-hidden="true" />
                )}
              </span>

              <h1 className="font-heading text-xl font-semibold">
                {success ? "Thanh toán thành công" : "Thanh toán không thành công"}
              </h1>

              <dl className="w-full space-y-2 rounded-lg bg-muted/50 p-4 text-sm">
                {amount !== null ? (
                  <div className="flex justify-between">
                    <dt className="text-muted-foreground">Số tiền</dt>
                    <dd className="tabular font-medium">{formatCurrency(amount)}</dd>
                  </div>
                ) : null}
                {orderId ? (
                  <div className="flex justify-between">
                    <dt className="text-muted-foreground">Đơn hàng</dt>
                    <dd className="tabular font-medium">#{orderId}</dd>
                  </div>
                ) : null}
                {searchParams.get("vnp_TransactionNo") ? (
                  <div className="flex justify-between">
                    <dt className="text-muted-foreground">Mã giao dịch VNPay</dt>
                    <dd className="tabular font-medium">{searchParams.get("vnp_TransactionNo")}</dd>
                  </div>
                ) : null}
              </dl>

              <p className="flex items-start gap-2 text-left text-xs text-muted-foreground">
                <InfoIcon className="mt-0.5 size-3.5 shrink-0" aria-hidden="true" />
                Trạng thái hiển thị ở đây chỉ để tham khảo. Trạng thái chính thức của đơn được cập
                nhật qua IPN từ VNPay, hãy mở lại chi tiết đơn để xem kết quả cuối cùng.
              </p>

              <div className="mt-2 flex w-full flex-col gap-2 sm:flex-row sm:justify-center">
                {orderId ? (
                  <Button asChild>
                    <Link to={`/orders/${orderId}`}>Xem đơn hàng</Link>
                  </Button>
                ) : null}
                <Button variant="outline" asChild>
                  <Link to="/orders">Danh sách đơn hàng</Link>
                </Button>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
