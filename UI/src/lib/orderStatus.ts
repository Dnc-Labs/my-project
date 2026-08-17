import type { OrderStatus } from "@/types/api"

interface OrderStatusMeta {
  label: string
  /** class nền/chữ cho badge — không dùng màu đơn thuần để phân biệt, luôn kèm nhãn chữ */
  className: string
  description: string
}

export const ORDER_STATUS_META: Record<OrderStatus, OrderStatusMeta> = {
  PENDING: {
    label: "Chờ xác nhận",
    className: "bg-warning/15 text-warning-foreground border-warning/30",
    description: "Đơn vừa được tạo, chờ người bán xác nhận.",
  },
  CONFIRMED: {
    label: "Đã xác nhận",
    className: "bg-info/15 text-info-foreground border-info/30",
    description: "Người bán đã xác nhận và đang chuẩn bị hàng.",
  },
  SHIPPING: {
    label: "Đang giao",
    className: "bg-info/15 text-info-foreground border-info/30",
    description: "Đơn đang trên đường giao đến bạn.",
  },
  DELIVERED: {
    label: "Đã giao",
    className: "bg-success/15 text-success-foreground border-success/30",
    description: "Đơn đã giao thành công. Bạn có thể đánh giá sản phẩm.",
  },
  CANCELLED: {
    label: "Đã huỷ",
    className: "bg-destructive/10 text-destructive border-destructive/30",
    description: "Đơn đã bị huỷ, tồn kho đã được hoàn lại.",
  },
}

/** Các bước hiển thị trên timeline (CANCELLED nằm ngoài luồng nên không có ở đây). */
export const ORDER_FLOW: OrderStatus[] = ["PENDING", "CONFIRMED", "SHIPPING", "DELIVERED"]

/** Mirror state machine ở OrderService.validateTransition — FE chỉ hiện nút hợp lệ. */
export function nextStatuses(current: OrderStatus): OrderStatus[] {
  switch (current) {
    case "PENDING":
      return ["CONFIRMED", "CANCELLED"]
    case "CONFIRMED":
      return ["SHIPPING", "CANCELLED"]
    case "SHIPPING":
      return ["DELIVERED"]
    default:
      return []
  }
}

export function canCustomerCancel(status: OrderStatus): boolean {
  return status === "PENDING" || status === "CONFIRMED"
}

/** Chỉ đơn chưa thanh toán và chưa kết thúc mới còn ý nghĩa để mở cổng VNPay. */
export function canPay(status: OrderStatus): boolean {
  return status === "PENDING"
}
