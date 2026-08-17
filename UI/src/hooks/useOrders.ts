import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import { checkout, getOrder, listMyOrders, updateOrderStatus } from "@/lib/api/orders"
import { createVnpayPayment } from "@/lib/api/payments"
import { getApiErrorMessage } from "@/lib/errors"
import { CART_KEY } from "@/hooks/useCart"
import type {
  CreateOrderRequest,
  OrderDetailResponse,
  OrderStatus,
  OrderSummaryResponse,
  PageParams,
  PageResponse,
} from "@/types/api"

export function useMyOrders(params: PageParams & { status?: OrderStatus }) {
  return useQuery<PageResponse<OrderSummaryResponse>>({
    queryKey: ["orders", params],
    queryFn: () => listMyOrders(params),
    placeholderData: (previous) => previous,
  })
}

export function useOrder(id: number | undefined) {
  return useQuery<OrderDetailResponse>({
    queryKey: ["order", id],
    queryFn: () => getOrder(id!),
    enabled: Boolean(id),
  })
}

export function useCheckout() {
  const queryClient = useQueryClient()
  return useMutation<OrderDetailResponse, Error, CreateOrderRequest>({
    mutationFn: checkout,
    onSuccess: (order) => {
      // Checkout dọn sạch giỏ ở backend → cache giỏ và danh sách đơn đều đã cũ.
      queryClient.invalidateQueries({ queryKey: CART_KEY })
      queryClient.invalidateQueries({ queryKey: ["orders"] })
      queryClient.setQueryData(["order", order.id], order)
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useUpdateOrderStatus() {
  const queryClient = useQueryClient()
  return useMutation<OrderDetailResponse, Error, { id: number; status: OrderStatus }>({
    mutationFn: ({ id, status }) => updateOrderStatus(id, status),
    onSuccess: (order) => {
      queryClient.setQueryData(["order", order.id], order)
      queryClient.invalidateQueries({ queryKey: ["orders"] })
      // Huỷ đơn hoàn lại tồn kho → số liệu sản phẩm hiển thị cũng đã cũ.
      queryClient.invalidateQueries({ queryKey: ["products"] })
      toast.success("Đã cập nhật trạng thái đơn hàng")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

/** Lấy URL cổng VNPay rồi chuyển hướng cả tab sang trang thanh toán. */
export function useCreatePayment() {
  return useMutation<string, Error, number>({
    mutationFn: async (orderId) => (await createVnpayPayment(orderId)).paymentUrl,
    onSuccess: (paymentUrl) => {
      window.location.href = paymentUrl
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}
