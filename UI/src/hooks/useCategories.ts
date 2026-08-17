import { useMemo } from "react"
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import {
  createCategory,
  deleteCategory,
  listCategories,
  updateCategory,
} from "@/lib/api/categories"
import { getApiErrorMessage } from "@/lib/errors"
import type {
  CategoryResponse,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from "@/types/api"

export const CATEGORIES_KEY = ["categories"] as const

export function useCategories() {
  return useQuery<CategoryResponse[]>({
    queryKey: CATEGORIES_KEY,
    queryFn: listCategories,
    // Danh mục gần như tĩnh — giữ cache lâu để không refetch mỗi lần đổi trang.
    staleTime: 5 * 60_000,
  })
}

export interface FlatCategory extends CategoryResponse {
  depth: number
}

/** Duỗi cây thành danh sách phẳng kèm depth để render select/nav thụt đầu dòng. */
export function flattenCategories(nodes: CategoryResponse[], depth = 0): FlatCategory[] {
  return nodes.flatMap((node) => [
    { ...node, depth },
    ...flattenCategories(node.children ?? [], depth + 1),
  ])
}

export function useFlatCategories() {
  const query = useCategories()
  const flat = useMemo(() => flattenCategories(query.data ?? []), [query.data])
  return { ...query, flat }
}

export function useCreateCategory() {
  const queryClient = useQueryClient()
  return useMutation<CategoryResponse, Error, CreateCategoryRequest>({
    mutationFn: createCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CATEGORIES_KEY })
      toast.success("Đã tạo danh mục")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useUpdateCategory() {
  const queryClient = useQueryClient()
  return useMutation<CategoryResponse, Error, { id: number; payload: UpdateCategoryRequest }>({
    mutationFn: ({ id, payload }) => updateCategory(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CATEGORIES_KEY })
      toast.success("Đã cập nhật danh mục")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useDeleteCategory() {
  const queryClient = useQueryClient()
  return useMutation<void, Error, number>({
    mutationFn: deleteCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: CATEGORIES_KEY })
      toast.success("Đã xoá danh mục")
    },
    // Backend chặn xoá khi còn danh mục con hoặc còn sản phẩm → hiện đúng message đó.
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}
