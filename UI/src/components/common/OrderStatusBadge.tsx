import {
  CheckCircle2Icon,
  ClockIcon,
  PackageCheckIcon,
  TruckIcon,
  XCircleIcon,
} from "lucide-react"
import type { LucideIcon } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { ORDER_STATUS_META } from "@/lib/orderStatus"
import type { OrderStatus } from "@/types/api"
import { cn } from "@/lib/utils"

const STATUS_ICON: Record<OrderStatus, LucideIcon> = {
  PENDING: ClockIcon,
  CONFIRMED: PackageCheckIcon,
  SHIPPING: TruckIcon,
  DELIVERED: CheckCircle2Icon,
  CANCELLED: XCircleIcon,
}

/** Badge luôn có icon + chữ: trạng thái không được phân biệt bằng riêng màu sắc. */
export function OrderStatusBadge({
  status,
  className,
}: {
  status: OrderStatus
  className?: string
}) {
  const meta = ORDER_STATUS_META[status]
  const Icon = STATUS_ICON[status]

  return (
    <Badge variant="outline" className={cn("gap-1 border", meta.className, className)}>
      <Icon aria-hidden="true" />
      {meta.label}
    </Badge>
  )
}
