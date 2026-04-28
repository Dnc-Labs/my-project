import { apiClient, unwrap } from "./client"
import type { BaseResponse, CategoryResponse } from "@/types/api"

export async function listCategories(): Promise<CategoryResponse[]> {
  const res = await apiClient.get<BaseResponse<CategoryResponse[]>>("/categories")
  return unwrap(res)
}

export async function getCategory(id: number): Promise<CategoryResponse> {
  const res = await apiClient.get<BaseResponse<CategoryResponse>>(`/categories/${id}`)
  return unwrap(res)
}
