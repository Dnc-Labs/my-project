import { useRef, useState } from "react"
import { CopyIcon, ImagePlusIcon, Loader2Icon, StarIcon, Trash2Icon } from "lucide-react"
import { toast } from "sonner"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Skeleton } from "@/components/ui/skeleton"
import { EmptyState } from "@/components/common/EmptyState"
import { ProductImage } from "@/components/products/ProductImage"
import { useDeleteImage, useProductImages, useSetPrimaryImage, useUploadImage } from "@/hooks/useImages"

// Khớp giới hạn multipart ở application.yaml (max-file-size: 5MB).
const MAX_SIZE_MB = 5
const ACCEPTED = ["image/jpeg", "image/png", "image/webp", "image/gif"]

export function ProductImageManager({ productId }: { productId: number }) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [dragging, setDragging] = useState(false)

  const { data: images, isPending } = useProductImages(productId)
  const upload = useUploadImage(productId)
  const setPrimary = useSetPrimaryImage(productId)
  const removeImage = useDeleteImage(productId)

  const copyUrl = async (url: string) => {
    try {
      await navigator.clipboard.writeText(url)
      toast.success("Đã sao chép URL ảnh")
    } catch {
      // clipboard API bị chặn (không phải HTTPS/localhost, hoặc user từ chối quyền)
      toast.error("Trình duyệt không cho sao chép — hãy copy thủ công từ tab Network")
    }
  }

  // Chặn sớm ở client cho hai lỗi phổ biến nhất, đỡ tốn một vòng request chỉ để nhận 400.
  const handleFiles = (files: FileList | null) => {
    if (!files?.length) return
    Array.from(files).forEach((file) => {
      if (!ACCEPTED.includes(file.type)) {
        toast.error(`${file.name}: chỉ nhận ảnh JPG, PNG, WEBP hoặc GIF`)
        return
      }
      if (file.size > MAX_SIZE_MB * 1024 * 1024) {
        toast.error(`${file.name}: vượt quá ${MAX_SIZE_MB}MB`)
        return
      }
      upload.mutate(file)
    })
  }

  return (
    <div className="flex flex-col gap-5">
      <div
        onDragOver={(e) => {
          e.preventDefault()
          setDragging(true)
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={(e) => {
          e.preventDefault()
          setDragging(false)
          handleFiles(e.dataTransfer.files)
        }}
        className={`flex flex-col items-center justify-center rounded-xl border-2 border-dashed p-8 text-center transition-colors ${
          dragging ? "border-foreground bg-muted/50" : "border-border"
        }`}
      >
        <ImagePlusIcon className="size-8 text-muted-foreground" aria-hidden="true" />
        <p className="mt-3 text-sm font-medium">Kéo thả ảnh vào đây</p>
        <p className="mt-1 text-xs text-muted-foreground">
          JPG, PNG, WEBP hoặc GIF · tối đa {MAX_SIZE_MB}MB mỗi ảnh
        </p>
        <Button
          type="button"
          variant="outline"
          size="sm"
          className="mt-4"
          disabled={upload.isPending}
          onClick={() => inputRef.current?.click()}
        >
          {upload.isPending ? <Loader2Icon className="animate-spin" aria-hidden="true" /> : null}
          {upload.isPending ? "Đang tải lên..." : "Chọn ảnh từ máy"}
        </Button>
        <input
          ref={inputRef}
          type="file"
          accept={ACCEPTED.join(",")}
          multiple
          className="sr-only"
          onChange={(e) => {
            handleFiles(e.target.files)
            e.target.value = ""
          }}
        />
      </div>

      {isPending ? (
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {Array.from({ length: 4 }, (_, i) => (
            <Skeleton key={i} className="aspect-square rounded-xl" />
          ))}
        </div>
      ) : images && images.length > 0 ? (
        <ul className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
          {images.map((image) => (
            <li key={image.id} className="overflow-hidden rounded-xl border">
              <div className="relative">
                <ProductImage src={image.url} alt={image.originalName} />
                {image.isPrimary ? (
                  <Badge className="absolute top-2 left-2 gap-1">
                    <StarIcon className="fill-current" aria-hidden="true" />
                    Ảnh chính
                  </Badge>
                ) : null}
              </div>
              <div className="flex items-center justify-between gap-1 p-2">
                <Button
                  variant="ghost"
                  size="xs"
                  disabled={image.isPrimary || setPrimary.isPending}
                  onClick={() => setPrimary.mutate(image.id)}
                >
                  <StarIcon aria-hidden="true" />
                  Đặt chính
                </Button>
                <div className="flex gap-0.5">
                  {/* Copy URL để dán sang ô "ảnh riêng của biến thể" ở tab Biến thể. */}
                  <Button
                    variant="ghost"
                    size="icon-xs"
                    aria-label={`Sao chép URL ảnh ${image.originalName}`}
                    onClick={() => copyUrl(image.url)}
                  >
                    <CopyIcon aria-hidden="true" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon-xs"
                    aria-label={`Xoá ảnh ${image.originalName}`}
                    className="text-muted-foreground hover:text-destructive"
                    disabled={removeImage.isPending}
                    onClick={() => removeImage.mutate(image.id)}
                  >
                    <Trash2Icon aria-hidden="true" />
                  </Button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <EmptyState
          icon={ImagePlusIcon}
          title="Chưa có ảnh nào"
          description="Ảnh đầu tiên bạn tải lên sẽ tự động trở thành ảnh chính hiển thị ở danh sách sản phẩm."
        />
      )}
    </div>
  )
}
