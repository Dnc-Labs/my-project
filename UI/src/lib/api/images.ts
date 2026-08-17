import { apiClient, unwrap } from "./client"
import type { BaseResponse, ProductImageResponse } from "@/types/api"

export async function listImages(productId: number): Promise<ProductImageResponse[]> {
  const res = await apiClient.get<BaseResponse<ProductImageResponse[]>>(
    `/products/${productId}/images`
  )
  return unwrap(res)
}

/**
 * Upload multipart. KHÔNG set Content-Type thủ công — để axios tự sinh boundary,
 * nếu ghi đè "multipart/form-data" trần thì Spring không parse được part.
 */
export async function uploadImage(productId: number, file: File): Promise<ProductImageResponse> {
  const form = new FormData()
  form.append("file", file)
  const res = await apiClient.post<BaseResponse<ProductImageResponse>>(
    `/products/${productId}/images`,
    form,
    { headers: { "Content-Type": undefined } }
  )
  return unwrap(res)
}

export async function setPrimaryImage(imageId: number): Promise<ProductImageResponse> {
  const res = await apiClient.put<BaseResponse<ProductImageResponse>>(
    `/images/${imageId}/primary`
  )
  return unwrap(res)
}

export async function deleteImage(imageId: number): Promise<void> {
  await apiClient.delete<BaseResponse<null>>(`/images/${imageId}`)
}
