import { apiClient, unwrap } from "./client"
import type {
  BaseResponse,
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from "@/types/api"

/** Backend trả cây danh mục (root kèm children lồng nhau), không phải danh sách phẳng. */
export async function listCategories(): Promise<CategoryResponse[]> {
  const res = await apiClient.get<BaseResponse<CategoryResponse[]>>("/categories")
  return unwrap(res)
}

export async function getCategory(id: number): Promise<CategoryResponse> {
  const res = await apiClient.get<BaseResponse<CategoryResponse>>(`/categories/${id}`)
  return unwrap(res)
}

export async function createCategory(payload: CreateCategoryRequest): Promise<CategoryResponse> {
  const res = await apiClient.post<BaseResponse<CategoryResponse>>("/categories", payload)
  return unwrap(res)
}

export async function updateCategory(
  id: number,
  payload: UpdateCategoryRequest
): Promise<CategoryResponse> {
  const res = await apiClient.put<BaseResponse<CategoryResponse>>(`/categories/${id}`, payload)
  return unwrap(res)
}

export async function deleteCategory(id: number): Promise<void> {
  await apiClient.delete<BaseResponse<null>>(`/categories/${id}`)
}
