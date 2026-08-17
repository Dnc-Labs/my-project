import { apiClient, unwrap } from "./client"
import type {
  BaseResponse,
  CreateOrderRequest,
  OrderDetailResponse,
  OrderStatus,
  OrderSummaryResponse,
  PageParams,
  PageResponse,
} from "@/types/api"

export async function checkout(payload: CreateOrderRequest): Promise<OrderDetailResponse> {
  const res = await apiClient.post<BaseResponse<OrderDetailResponse>>("/orders", payload)
  return unwrap(res)
}

export async function listMyOrders(
  params: PageParams & { status?: OrderStatus } = {}
): Promise<PageResponse<OrderSummaryResponse>> {
  const res = await apiClient.get<BaseResponse<PageResponse<OrderSummaryResponse>>>("/orders", {
    params,
  })
  return unwrap(res)
}

export async function getOrder(id: number): Promise<OrderDetailResponse> {
  const res = await apiClient.get<BaseResponse<OrderDetailResponse>>(`/orders/${id}`)
  return unwrap(res)
}

/** State machine + phân quyền do backend giữ; FE chỉ hiển thị action hợp lệ. */
export async function updateOrderStatus(
  id: number,
  status: OrderStatus
): Promise<OrderDetailResponse> {
  const res = await apiClient.put<BaseResponse<OrderDetailResponse>>(`/orders/${id}/status`, {
    status,
  })
  return unwrap(res)
}
