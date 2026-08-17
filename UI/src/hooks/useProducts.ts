import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import {
  createProduct,
  deleteProduct,
  getProduct,
  listProducts,
  updateProduct,
} from "@/lib/api/products"
import { getApiErrorMessage } from "@/lib/errors"
import type {
  CreateProductRequest,
  PageResponse,
  ProductFilterParams,
  ProductResponse,
  UpdateProductRequest,
} from "@/types/api"

export function useProducts(params: ProductFilterParams) {
  return useQuery<PageResponse<ProductResponse>>({
    queryKey: ["products", params],
    queryFn: () => listProducts(params),
    // Giữ trang cũ hiển thị trong lúc tải trang mới → không nháy layout khi đổi filter.
    placeholderData: (previous) => previous,
  })
}

export function useProduct(id: number | undefined) {
  return useQuery<ProductResponse>({
    queryKey: ["product", id],
    queryFn: () => getProduct(id!),
    enabled: Boolean(id),
  })
}

export function useCreateProduct() {
  const queryClient = useQueryClient()
  return useMutation<ProductResponse, Error, CreateProductRequest>({
    mutationFn: createProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] })
      toast.success("Đã tạo sản phẩm")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useUpdateProduct() {
  const queryClient = useQueryClient()
  return useMutation<ProductResponse, Error, { id: number; payload: UpdateProductRequest }>({
    mutationFn: ({ id, payload }) => updateProduct(id, payload),
    onSuccess: (product) => {
      queryClient.invalidateQueries({ queryKey: ["products"] })
      queryClient.setQueryData(["product", product.id], product)
      toast.success("Đã cập nhật sản phẩm")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useDeleteProduct() {
  const queryClient = useQueryClient()
  return useMutation<void, Error, number>({
    mutationFn: deleteProduct,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] })
      toast.success("Đã xoá sản phẩm")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}
