import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import {
  createVariant,
  deleteVariant,
  listVariants,
  updateVariant,
} from "@/lib/api/variants"
import { getApiErrorMessage } from "@/lib/errors"
import type {
  CreateProductVariantRequest,
  ProductVariantResponse,
  UpdateProductVariantRequest,
} from "@/types/api"

export function useVariants(productId: number | undefined) {
  return useQuery<ProductVariantResponse[]>({
    queryKey: ["variants", productId],
    queryFn: () => listVariants(productId!),
    enabled: Boolean(productId),
  })
}

export function useCreateVariant(productId: number) {
  const queryClient = useQueryClient()
  return useMutation<ProductVariantResponse, Error, CreateProductVariantRequest>({
    mutationFn: (payload) => createVariant(productId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["variants", productId] })
      toast.success("Đã thêm biến thể")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useUpdateVariant(productId: number) {
  const queryClient = useQueryClient()
  return useMutation<
    ProductVariantResponse,
    Error,
    { id: number; payload: UpdateProductVariantRequest }
  >({
    mutationFn: ({ id, payload }) => updateVariant(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["variants", productId] })
      toast.success("Đã cập nhật biến thể")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useDeleteVariant(productId: number) {
  const queryClient = useQueryClient()
  return useMutation<void, Error, number>({
    mutationFn: deleteVariant,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["variants", productId] })
      toast.success("Đã xoá biến thể")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}
