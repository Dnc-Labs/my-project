import { useState } from "react"
import { ProductImage } from "@/components/products/ProductImage"
import type { ProductImageResponse } from "@/types/api"
import { cn } from "@/lib/utils"

interface ProductGalleryProps {
  images: ProductImageResponse[]
  primaryImageUrl: string | null
  alt: string
  /** Ảnh riêng của biến thể đang chọn — ưu tiên hiển thị để khách thấy đúng màu/mẫu đã chọn. */
  overrideUrl?: string | null
}

export function ProductGallery({ images, primaryImageUrl, alt, overrideUrl }: ProductGalleryProps) {
  // Ảnh primary đứng đầu; nếu product chưa có bản ghi image nào thì vẫn hiển thị khung rỗng.
  const ordered = [...images].sort(
    (a, b) => Number(b.isPrimary) - Number(a.isPrimary)
  )
  const [activeId, setActiveId] = useState<number | null>(ordered[0]?.id ?? null)
  const active = ordered.find((image) => image.id === activeId) ?? ordered[0]

  return (
    <div className="flex flex-col gap-3">
      <ProductImage
        src={overrideUrl ?? active?.url ?? primaryImageUrl}
        alt={alt}
        eager
        className="rounded-xl border"
      />

      {ordered.length > 1 ? (
        <ul className="grid grid-cols-5 gap-2" aria-label="Ảnh sản phẩm">
          {ordered.map((image) => (
            <li key={image.id}>
              <button
                type="button"
                onClick={() => setActiveId(image.id)}
                aria-label={`Xem ảnh ${image.originalName}`}
                aria-pressed={image.id === active?.id}
                className={cn(
                  "block w-full overflow-hidden rounded-lg border transition-colors focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-none",
                  image.id === active?.id
                    ? "border-foreground"
                    : "border-border hover:border-muted-foreground"
                )}
              >
                <ProductImage src={image.url} alt={image.originalName} />
              </button>
            </li>
          ))}
        </ul>
      ) : null}
    </div>
  )
}
