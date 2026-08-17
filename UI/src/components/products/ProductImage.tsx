import { ImageIcon } from "lucide-react"
import { cn } from "@/lib/utils"

interface ProductImageProps {
  src: string | null | undefined
  alt: string
  className?: string
  /** Ảnh đầu tiên trong khung nhìn (hero/gallery chính) nên tải ngay, phần còn lại lazy. */
  eager?: boolean
}

/**
 * Khung ảnh vuông cố định tỉ lệ để không gây layout shift lúc ảnh về,
 * và có fallback rõ ràng khi seller chưa upload ảnh nào.
 */
export function ProductImage({ src, alt, className, eager }: ProductImageProps) {
  return (
    <div className={cn("relative aspect-square overflow-hidden bg-muted", className)}>
      {src ? (
        <img
          src={src}
          alt={alt}
          loading={eager ? "eager" : "lazy"}
          decoding="async"
          className="size-full object-cover transition-transform duration-300 group-hover:scale-105"
        />
      ) : (
        <div className="flex size-full items-center justify-center">
          <ImageIcon className="size-8 text-muted-foreground/50" aria-hidden="true" />
          <span className="sr-only">Chưa có ảnh sản phẩm</span>
        </div>
      )}
    </div>
  )
}
