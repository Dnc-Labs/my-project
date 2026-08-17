import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { toast } from "sonner"
import { deleteImage, listImages, setPrimaryImage, uploadImage } from "@/lib/api/images"
import { getApiErrorMessage } from "@/lib/errors"
import type { ProductImageResponse } from "@/types/api"

export function useProductImages(productId: number | undefined) {
  return useQuery<ProductImageResponse[]>({
    queryKey: ["images", productId],
    queryFn: () => listImages(productId!),
    enabled: Boolean(productId),
  })
}

/** Mọi thao tác ảnh đều đổi `primaryImageUrl` của product → invalidate luôn cache product. */
function useImageInvalidation(productId: number) {
  const queryClient = useQueryClient()
  return () => {
    queryClient.invalidateQueries({ queryKey: ["images", productId] })
    queryClient.invalidateQueries({ queryKey: ["product", productId] })
    queryClient.invalidateQueries({ queryKey: ["products"] })
  }
}

export function useUploadImage(productId: number) {
  const invalidate = useImageInvalidation(productId)
  return useMutation<ProductImageResponse, Error, File>({
    mutationFn: (file) => uploadImage(productId, file),
    onSuccess: () => {
      invalidate()
      toast.success("Đã tải ảnh lên")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useSetPrimaryImage(productId: number) {
  const invalidate = useImageInvalidation(productId)
  return useMutation<ProductImageResponse, Error, number>({
    mutationFn: setPrimaryImage,
    onSuccess: () => {
      invalidate()
      toast.success("Đã đặt làm ảnh chính")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}

export function useDeleteImage(productId: number) {
  const invalidate = useImageInvalidation(productId)
  return useMutation<void, Error, number>({
    mutationFn: deleteImage,
    onSuccess: () => {
      invalidate()
      toast.success("Đã xoá ảnh")
    },
    onError: (err) => toast.error(getApiErrorMessage(err)),
  })
}
