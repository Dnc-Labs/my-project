import { useMemo, useState } from "react"
import { Link, useParams } from "react-router-dom"
import { ChevronRightIcon, PackageIcon, PencilIcon, ShoppingCartIcon, StoreIcon } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { Skeleton } from "@/components/ui/skeleton"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ErrorState } from "@/components/common/ErrorState"
import { QuantityStepper } from "@/components/common/QuantityStepper"
import { RatingStars } from "@/components/common/RatingStars"
import { ProductGallery } from "@/components/products/ProductGallery"
import { ReviewSection } from "@/components/reviews/ReviewSection"
import { useProduct } from "@/hooks/useProducts"
import { useVariants } from "@/hooks/useVariants"
import { useAddToCartAction } from "@/hooks/useAddToCartAction"
import { useAuthStore } from "@/store/authStore"
import { formatCurrency, formatNumber, formatRating } from "@/lib/format"
import { cn } from "@/lib/utils"

export function ProductDetailPage() {
  const { id } = useParams()
  const productId = Number(id)

  const { data: product, isPending, isError, error, refetch } = useProduct(productId)
  const { data: variants } = useVariants(productId)
  const { addToCart, isPending: isAdding } = useAddToCartAction()
  const user = useAuthStore((s) => s.user)

  const [variantId, setVariantId] = useState<number | null>(null)
  const [quantity, setQuantity] = useState(1)

  const selectedVariant = useMemo(
    () => variants?.find((variant) => variant.id === variantId) ?? null,
    [variants, variantId]
  )

  // Có biến thể thì giá và tồn kho lấy theo biến thể đang chọn, không phải của product cha.
  const price = selectedVariant?.price ?? product?.price ?? 0
  const stock = selectedVariant?.stock ?? product?.stock ?? 0
  const hasVariants = Boolean(variants?.length)
  const mustPickVariant = hasVariants && !selectedVariant
  const outOfStock = stock <= 0
  const inactive = product?.status === "INACTIVE"
  // ADMIN sửa được mọi sản phẩm, SELLER chỉ sản phẩm của mình — khớp @PreAuthorize ở backend.
  const canManage =
    Boolean(product) &&
    (user?.role === "ADMIN" || (user?.role === "SELLER" && user.id === product?.sellerId))

  if (isPending) {
    return (
      <div className="container-page grid gap-10 py-10 lg:grid-cols-2">
        <Skeleton className="aspect-square rounded-xl" />
        <div className="space-y-4">
          <Skeleton className="h-8 w-3/4" />
          <Skeleton className="h-5 w-40" />
          <Skeleton className="h-10 w-48" />
          <Skeleton className="h-24 w-full" />
        </div>
      </div>
    )
  }

  if (isError || !product) {
    return (
      <div className="container-page py-10">
        <ErrorState error={error} onRetry={() => refetch()} />
      </div>
    )
  }

  return (
    <div className="container-page py-8 md:py-10">
      <nav aria-label="Breadcrumb" className="mb-6 flex flex-wrap items-center gap-1 text-sm text-muted-foreground">
        <Link to="/" className="transition-colors hover:text-foreground">
          Trang chủ
        </Link>
        <ChevronRightIcon className="size-3.5" aria-hidden="true" />
        <Link to="/products" className="transition-colors hover:text-foreground">
          Sản phẩm
        </Link>
        {product.categoryName ? (
          <>
            <ChevronRightIcon className="size-3.5" aria-hidden="true" />
            <Link
              to={`/products?categoryId=${product.categoryId}`}
              className="transition-colors hover:text-foreground"
            >
              {product.categoryName}
            </Link>
          </>
        ) : null}
        <ChevronRightIcon className="size-3.5" aria-hidden="true" />
        <span className="truncate text-foreground">{product.name}</span>
      </nav>

      <div className="grid gap-10 lg:grid-cols-2">
        <ProductGallery
          images={product.images}
          primaryImageUrl={product.primaryImageUrl}
          alt={product.name}
          overrideUrl={selectedVariant?.imageUrl}
        />

        <div className="flex flex-col">
          {canManage ? (
            <div className="mb-4 flex flex-wrap items-center gap-2 rounded-lg border border-dashed bg-muted/40 px-3 py-2 text-sm">
              <span className="text-muted-foreground">Bạn quản lý sản phẩm này.</span>
              <Button variant="outline" size="sm" asChild className="ml-auto">
                <Link to={`/seller/products/${product.id}/edit`}>
                  <PencilIcon aria-hidden="true" />
                  Sửa sản phẩm
                </Link>
              </Button>
            </div>
          ) : null}

          <h1 className="font-heading text-2xl font-semibold tracking-tight md:text-3xl">
            {product.name}
          </h1>

          <div className="mt-3 flex flex-wrap items-center gap-3 text-sm">
            {product.reviewCount > 0 ? (
              <span className="flex items-center gap-1.5">
                <RatingStars value={product.averageRating} size="sm" />
                <span className="tabular font-medium">{formatRating(product.averageRating)}</span>
                <span className="text-muted-foreground">
                  ({formatNumber(product.reviewCount)} đánh giá)
                </span>
              </span>
            ) : (
              <span className="text-muted-foreground">Chưa có đánh giá</span>
            )}
            <span className="text-muted-foreground">·</span>
            <span className="text-muted-foreground">SKU: {product.sku}</span>
          </div>

          <p className="tabular mt-5 font-heading text-3xl font-semibold">{formatCurrency(price)}</p>

          <div className="mt-2 flex items-center gap-2 text-sm">
            {inactive ? (
              <Badge variant="secondary">Ngừng bán</Badge>
            ) : outOfStock ? (
              <Badge variant="secondary">Hết hàng</Badge>
            ) : (
              <span className="text-muted-foreground">
                Còn <span className="tabular font-medium text-foreground">{formatNumber(stock)}</span>{" "}
                sản phẩm
              </span>
            )}
          </div>

          {hasVariants ? (
            <fieldset className="mt-6">
              <legend className="mb-2 text-sm font-medium">
                Phân loại <span className="text-destructive">*</span>
              </legend>
              <div className="flex flex-wrap gap-2">
                {variants!.map((variant) => {
                  const disabled = variant.stock <= 0
                  const active = variant.id === variantId
                  return (
                    <button
                      key={variant.id}
                      type="button"
                      disabled={disabled}
                      onClick={() => {
                        setVariantId(variant.id)
                        setQuantity(1)
                      }}
                      aria-pressed={active}
                      className={cn(
                        "min-h-11 rounded-lg border px-3 py-2 text-sm transition-colors focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-none",
                        active
                          ? "border-foreground bg-secondary font-medium"
                          : "border-border hover:border-muted-foreground",
                        disabled && "cursor-not-allowed opacity-50 line-through"
                      )}
                    >
                      {variant.size} / {variant.color}
                      {disabled ? <span className="sr-only"> — hết hàng</span> : null}
                    </button>
                  )
                })}
              </div>
              {mustPickVariant ? (
                <p className="mt-2 text-xs text-muted-foreground">
                  Chọn một phân loại để xem giá và tồn kho chính xác.
                </p>
              ) : null}
            </fieldset>
          ) : null}

          <div className="mt-6 flex flex-wrap items-center gap-3">
            <QuantityStepper
              value={quantity}
              onChange={setQuantity}
              max={Math.max(1, stock)}
              disabled={outOfStock || inactive}
            />
            <Button
              size="lg"
              className="flex-1 sm:flex-none"
              disabled={outOfStock || inactive || mustPickVariant || isAdding}
              onClick={() =>
                addToCart({
                  productId: product.id,
                  variantId: selectedVariant?.id ?? null,
                  quantity,
                })
              }
            >
              <ShoppingCartIcon aria-hidden="true" />
              {isAdding ? "Đang thêm..." : "Thêm vào giỏ hàng"}
            </Button>
          </div>

          <Separator className="my-6" />

          <dl className="grid gap-3 text-sm">
            <div className="flex items-center gap-2">
              <dt className="w-28 shrink-0 text-muted-foreground">Người bán</dt>
              <dd className="flex items-center gap-1.5 font-medium">
                <StoreIcon className="size-4 text-muted-foreground" aria-hidden="true" />
                {product.sellerName ?? `#${product.sellerId}`}
              </dd>
            </div>
            <div className="flex items-center gap-2">
              <dt className="w-28 shrink-0 text-muted-foreground">Danh mục</dt>
              <dd>
                {product.categoryName ? (
                  <Link
                    to={`/products?categoryId=${product.categoryId}`}
                    className="font-medium underline-offset-4 hover:underline"
                  >
                    {product.categoryName}
                  </Link>
                ) : (
                  "—"
                )}
              </dd>
            </div>
            <div className="flex items-center gap-2">
              <dt className="w-28 shrink-0 text-muted-foreground">Tồn kho</dt>
              <dd className="tabular flex items-center gap-1.5">
                <PackageIcon className="size-4 text-muted-foreground" aria-hidden="true" />
                {formatNumber(stock)}
              </dd>
            </div>
          </dl>
        </div>
      </div>

      <Tabs defaultValue="description" className="mt-12">
        <TabsList>
          <TabsTrigger value="description">Mô tả</TabsTrigger>
          <TabsTrigger value="reviews">Đánh giá ({formatNumber(product.reviewCount)})</TabsTrigger>
        </TabsList>

        <TabsContent value="description">
          <div className="max-w-3xl rounded-xl border bg-card p-6 text-sm leading-relaxed whitespace-pre-line">
            {product.description?.trim() ? (
              product.description
            ) : (
              <span className="text-muted-foreground">Người bán chưa thêm mô tả cho sản phẩm này.</span>
            )}
          </div>
        </TabsContent>

        <TabsContent value="reviews">
          <div className="max-w-3xl">
            <ReviewSection product={product} />
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
