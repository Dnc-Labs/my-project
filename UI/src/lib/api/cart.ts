import { apiClient, unwrap } from "./client"
import type {
  AddToCartRequest,
  BaseResponse,
  CartResponse,
  UpdateCartItemRequest,
} from "@/types/api"

export async function getMyCart(): Promise<CartResponse> {
  const res = await apiClient.get<BaseResponse<CartResponse>>("/carts")
  return unwrap(res)
}

export async function addToCart(payload: AddToCartRequest): Promise<CartResponse> {
  const res = await apiClient.post<BaseResponse<CartResponse>>("/carts/items", payload)
  return unwrap(res)
}

export async function updateCartItem(
  itemId: number,
  payload: UpdateCartItemRequest
): Promise<CartResponse> {
  const res = await apiClient.put<BaseResponse<CartResponse>>(`/carts/items/${itemId}`, payload)
  return unwrap(res)
}

export async function removeCartItem(itemId: number): Promise<void> {
  await apiClient.delete<BaseResponse<null>>(`/carts/items/${itemId}`)
}

export async function clearCart(): Promise<void> {
  await apiClient.delete<BaseResponse<null>>("/carts")
}
