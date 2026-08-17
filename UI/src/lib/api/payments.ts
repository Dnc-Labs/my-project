import { apiClient, unwrap } from "./client"
import type { BaseResponse, PaymentUrlResponse } from "@/types/api"

/** Trả URL cổng VNPay — client tự redirect trình duyệt sang đó. */
export async function createVnpayPayment(orderId: number): Promise<PaymentUrlResponse> {
  const res = await apiClient.post<BaseResponse<PaymentUrlResponse>>("/payments/vnpay/create", {
    orderId,
  })
  return unwrap(res)
}

/**
 * Return URL chỉ để HIỂN THỊ kết quả cho user; trạng thái thật do IPN
 * (server-to-server) quyết định, nên sau khi đọc xong vẫn phải refetch order.
 */
export async function readVnpayReturn(query: string): Promise<string> {
  const res = await apiClient.get<BaseResponse<string>>(`/payments/vnpay/return${query}`)
  return unwrap(res)
}
