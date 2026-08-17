import { apiClient, unwrap } from "./client"
import type {
  BaseResponse,
  CreateProductVariantRequest,
  ProductVariantResponse,
  UpdateProductVariantRequest,
} from "@/types/api"

/** URL hybrid: tạo/list nằm dưới /products/{productId}/variants, sửa/xoá dùng /variants/{id}. */
export async function listVariants(productId: number): Promise<ProductVariantResponse[]> {
  const res = await apiClient.get<BaseResponse<ProductVariantResponse[]>>(
    `/products/${productId}/variants`
  )
  return unwrap(res)
}

export async function createVariant(
  productId: number,
  payload: CreateProductVariantRequest
): Promise<ProductVariantResponse> {
  const res = await apiClient.post<BaseResponse<ProductVariantResponse>>(
    `/products/${productId}/variants`,
    payload
  )
  return unwrap(res)
}

export async function updateVariant(
  id: number,
  payload: UpdateProductVariantRequest
): Promise<ProductVariantResponse> {
  const res = await apiClient.put<BaseResponse<ProductVariantResponse>>(`/variants/${id}`, payload)
  return unwrap(res)
}

export async function deleteVariant(id: number): Promise<void> {
  await apiClient.delete<BaseResponse<null>>(`/variants/${id}`)
}
