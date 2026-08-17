import { Link } from "react-router-dom"
import { ShoppingCartIcon } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { RatingStars } from "@/components/common/RatingStars"
import { ProductImage } from "@/components/products/ProductImage"
import { formatCurrency, formatRating } from "@/lib/format"
import type { ProductResponse } from "@/types/api"
import { cn } from "@/lib/utils"

interface ProductCardProps {
  product: ProductResponse
  onAddToCart?: (product: ProductResponse) => void
  isAdding?: boolean
  className?: string
}

export function ProductCard({ product, onAddToCart, isAdding, className }: ProductCardProps) {
  const outOfStock = product.stock <= 0
  const inactive = product.status === "INACTIVE"
  const unavailable = outOfStock || inactive

  return (
    <article
      className={cn(
        "group relative flex flex-col overflow-hidden rounded-xl border bg-card transition-shadow duration-200 hover:shadow-md",
        className
      )}
    >
      <Link to={`/products/${product.id}`} className="block" tabIndex={-1} aria-hidden="true">
        <ProductImage src={product.primaryImageUrl} alt={product.name} />
      </Link>

      {unavailable ? (
        <Badge variant="secondary" className="absolute top-3 left-3 border bg-background/90">
          {inactive ? "Ngừng bán" : "Hết hàng"}
        </Badge>
      ) : null}

      <div className="flex flex-1 flex-col gap-2 p-4">
        {product.categoryName ? (
          <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">
            {product.categoryName}
          </p>
        ) : null}

        <h3 className="line-clamp-2 text-sm leading-snug font-medium">
          {/* Link phủ toàn card: vùng bấm rộng nhưng vẫn chỉ 1 điểm dừng khi tab. */}
          <Link to={`/products/${product.id}`} className="after:absolute after:inset-0 focus-visible:outline-none">
            {product.name}
          </Link>
        </h3>

        <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
          {product.reviewCount > 0 ? (
            <>
              <RatingStars value={product.averageRating} size="sm" />
              <span className="tabular">{formatRating(product.averageRating)}</span>
              <span>({product.reviewCount})</span>
            </>
          ) : (
            <span>Chưa có đánh giá</span>
          )}
        </div>

        <div className="mt-auto flex items-end justify-between gap-2 pt-2">
          <p className="tabular text-base font-semibold">{formatCurrency(product.price)}</p>
          {onAddToCart ? (
            <Button
              size="icon-sm"
              variant="secondary"
              // z-10 để nút nổi lên trên link phủ card, nếu không cú bấm sẽ rơi vào link.
              className="relative z-10"
              disabled={unavailable || isAdding}
              onClick={() => onAddToCart(product)}
              aria-label={`Thêm ${product.name} vào giỏ hàng`}
            >
              <ShoppingCartIcon aria-hidden="true" />
            </Button>
          ) : null}
        </div>
      </div>
    </article>
  )
}
