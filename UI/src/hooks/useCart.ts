import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import {
  addToCart,
  clearCart,
  getMyCart,
  removeCartItem,
  updateCartItem,
} from "@/lib/api/cart"
import { getApiErrorMessage } from "@/lib/errors"
import { useAuthStore } from "@/store/authStore"
import type { AddToCartRequest, CartResponse } from "@/types/api"

export const CART_KEY = ["cart"] as const

/** Giỏ hàng chỉ tồn tại khi đã đăng nhập — chưa login thì không gọi API (tránh 401 vô nghĩa). */
export function useCart() {
  const isAuthenticated = useAuthStore((s) => Boolean(s.accessToken && s.user))
  return useQuery<CartResponse>({
    queryKey: CART_KEY,
    queryFn: getMyCart,
    enabled: isAuthenticated,
  })
}

/** Số lượng item hiển thị trên badge header — đọc từ cache nên không phát sinh request thừa. */
export function useCartCount(): number {
  const { data } = useCart()
  return data?.totalItems ?? 0
}

export function useAddToCart() {
  const queryClient = useQueryClient()
  return useMutation<CartResponse, Error, AddToCartRequest>({
    mutationFn: addToCart,
    onSuccess: () => {
      // KHÔNG set thẳng response vào cache: item vừa thêm được backend map sang DTO
      // trước khi flush nên `id` còn null, mà UI cần id để sửa/xoá dòng đó.
      queryClient.invalidateQueries({ queryKey: CART_KEY })
      toast.success("Đã thêm vào giỏ hàng")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useUpdateCartItem() {
  const queryClient = useQueryClient()
  return useMutation<CartResponse, Error, { itemId: number; quantity: number }>({
    mutationFn: ({ itemId, quantity }) => updateCartItem(itemId, { quantity }),
    onSuccess: (cart) => {
      queryClient.setQueryData(CART_KEY, cart)
      // Item đang sửa đã tồn tại trong DB nên id hợp lệ; vẫn đồng bộ lại nền
      // phòng khi tồn kho/giá đổi giữa chừng.
      queryClient.invalidateQueries({ queryKey: CART_KEY })
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useRemoveCartItem() {
  const queryClient = useQueryClient()
  return useMutation<void, Error, number>({
    mutationFn: removeCartItem,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CART_KEY })
      toast.success("Đã xoá sản phẩm khỏi giỏ")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useClearCart() {
  const queryClient = useQueryClient()
  return useMutation<void, Error, void>({
    mutationFn: clearCart,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CART_KEY })
      toast.success("Đã xoá toàn bộ giỏ hàng")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}
