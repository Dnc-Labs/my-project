import { useLocation, useNavigate } from "react-router-dom"
import { toast } from "sonner"
import { useAddToCart } from "@/hooks/useCart"
import { useAuthStore } from "@/store/authStore"
import type { AddToCartRequest } from "@/types/api"

/**
 * Bọc thêm bước kiểm tra đăng nhập trước khi gọi API giỏ hàng:
 * chưa đăng nhập thì đưa sang /login kèm `from` để quay lại đúng chỗ vừa bấm,
 * thay vì để request rơi xuống backend rồi nhận 401 khó hiểu.
 */
export function useAddToCartAction() {
  const navigate = useNavigate()
  const location = useLocation()
  const isAuthenticated = useAuthStore((s) => Boolean(s.accessToken && s.user))
  const mutation = useAddToCart()

  const addToCart = (payload: AddToCartRequest) => {
    if (!isAuthenticated) {
      toast.info("Vui lòng đăng nhập để thêm sản phẩm vào giỏ")
      navigate("/login", { state: { from: location } })
      return
    }
    mutation.mutate(payload)
  }

  return { addToCart, isPending: mutation.isPending, variables: mutation.variables }
}
