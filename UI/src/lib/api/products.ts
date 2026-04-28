import { apiClient, unwrap } from "./client"
import type {
  BaseResponse,
  CreateProductRequest,
  ProductResponse,
  UpdateProductRequest,
} from "@/types/api"

export async function listProducts(): Promise<ProductResponse[]> {
  const res = await apiClient.get<BaseResponse<ProductResponse[]>>("/products")
  return unwrap(res)
}

export async function getProduct(id: number): Promise<ProductResponse> {
  const res = await apiClient.get<BaseResponse<ProductResponse>>(`/products/${id}`)
  return unwrap(res)
}

export async function createProduct(payload: CreateProductRequest): Promise<ProductResponse> {
  const res = await apiClient.post<BaseResponse<ProductResponse>>("/products", payload)
  return unwrap(res)
}

export async function updateProduct(
  id: number,
  payload: UpdateProductRequest
): Promise<ProductResponse> {
  const res = await apiClient.put<BaseResponse<ProductResponse>>(`/products/${id}`, payload)
  return unwrap(res)
}

export async function deleteProduct(id: number): Promise<void> {
  await apiClient.delete<BaseResponse<null>>(`/products/${id}`)
}
