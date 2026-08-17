import { useState } from "react"
import { Link, useNavigate } from "react-router-dom"
import {
  ArrowRightIcon,
  BadgeCheckIcon,
  LayoutGridIcon,
  PackageIcon,
  SearchIcon,
  ShieldCheckIcon,
  TruckIcon,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { ProductCard } from "@/components/products/ProductCard"
import { ProductGridSkeleton } from "@/components/products/ProductCardSkeleton"
import { EmptyState } from "@/components/common/EmptyState"
import { useProducts } from "@/hooks/useProducts"
import { useCategories } from "@/hooks/useCategories"
import { useAddToCartAction } from "@/hooks/useAddToCartAction"

const TRUST_ITEMS = [
  {
    icon: ShieldCheckIcon,
    title: "Thanh toán an toàn",
    description: "Cổng VNPay xác thực chữ ký HMAC cho từng giao dịch.",
  },
  {
    icon: TruckIcon,
    title: "Theo dõi đơn hàng",
    description: "Trạng thái cập nhật theo từng bước: xác nhận, giao, hoàn tất.",
  },
  {
    icon: BadgeCheckIcon,
    title: "Đánh giá thật",
    description: "Chỉ người đã nhận hàng mới được đánh giá sản phẩm.",
  },
]

export function HomePage() {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState("")
  const { addToCart, isPending, variables } = useAddToCartAction()

  const newest = useProducts({ size: 8, sort: "createdAt,desc", status: "ACTIVE" })
  const topRated = useProducts({ size: 4, sort: "averageRating,desc", minRating: 4, status: "ACTIVE" })
  const { data: categories } = useCategories()

  // Chip "Phổ biến" ưu tiên danh mục lá (cụ thể hơn, ra kết quả sát ý người tìm);
  // nếu cây chỉ có 1 cấp thì dùng luôn danh mục gốc.
  const popularChildren = (categories ?? [])
    .flatMap((root) => (root.children?.length ? root.children : [root]))
    .slice(0, 5)

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    const trimmed = keyword.trim()
    navigate(trimmed ? `/products?keyword=${encodeURIComponent(trimmed)}` : "/products")
  }

  return (
    <div className="flex flex-col">
      <section className="border-b bg-secondary/40">
        <div className="container-page flex flex-col items-center py-16 text-center md:py-24">
          <h1 className="max-w-3xl font-heading text-4xl font-semibold tracking-tight md:text-6xl">
            Mua bán mọi thứ, đơn giản và minh bạch.
          </h1>
          <p className="mt-6 max-w-xl text-base text-muted-foreground md:text-lg">
            Khám phá hàng nghìn sản phẩm từ những người bán uy tín. Hoặc trở thành seller chỉ với
            vài bước.
          </p>

          <form className="mt-10 flex w-full max-w-xl items-center gap-2" role="search" onSubmit={handleSearch}>
            <div className="relative flex-1">
              <SearchIcon
                className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground"
                aria-hidden="true"
              />
              <Input
                type="search"
                placeholder="Tìm sản phẩm..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                className="h-12 bg-background pl-9"
                aria-label="Tìm sản phẩm"
              />
            </div>
            <Button type="submit" size="lg" className="h-12">
              Tìm kiếm
              <ArrowRightIcon aria-hidden="true" />
            </Button>
          </form>

          {popularChildren.length > 0 ? (
            <div className="mt-8 flex flex-wrap items-center justify-center gap-2 text-sm text-muted-foreground">
              <span>Phổ biến:</span>
              {popularChildren.map((category) => (
                <Link
                  key={category.id}
                  to={`/products?categoryId=${category.id}`}
                  className="rounded-full border border-border bg-background px-3 py-1 transition-colors hover:border-foreground hover:text-foreground"
                >
                  {category.name}
                </Link>
              ))}
            </div>
          ) : null}
        </div>
      </section>

      {categories?.length ? (
        <section className="container-page py-14">
          <div className="mb-6">
            <h2 className="font-heading text-2xl font-semibold tracking-tight">Danh mục</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Chọn nhóm hàng bạn muốn xem trước.
            </p>
          </div>
          <ul className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
            {categories.map((category) => {
              const childCount = category.children?.length ?? 0
              return (
                <li key={category.id}>
                  <Link
                    to={`/products?categoryId=${category.id}`}
                    className="group flex h-full flex-col justify-between gap-6 rounded-xl border bg-card p-5 transition-shadow hover:shadow-md"
                  >
                    <span className="flex size-10 items-center justify-center rounded-lg bg-secondary">
                      <LayoutGridIcon className="size-5" aria-hidden="true" />
                    </span>
                    <span>
                      <span className="block font-medium">{category.name}</span>
                      <span className="mt-0.5 block text-xs text-muted-foreground">
                        {childCount > 0 ? `${childCount} danh mục con` : "Xem sản phẩm"}
                      </span>
                    </span>
                  </Link>
                </li>
              )
            })}
          </ul>
        </section>
      ) : null}

      <section className="container-page py-14">
        <div className="mb-6 flex items-end justify-between gap-4">
          <div>
            <h2 className="font-heading text-2xl font-semibold tracking-tight">Sản phẩm mới nhất</h2>
            <p className="mt-1 text-sm text-muted-foreground">Vừa được người bán đăng lên sàn.</p>
          </div>
          <Button variant="ghost" size="sm" asChild>
            <Link to="/products">
              Xem tất cả
              <ArrowRightIcon aria-hidden="true" />
            </Link>
          </Button>
        </div>

        {newest.isPending ? (
          <ProductGridSkeleton count={8} />
        ) : newest.data?.content.length ? (
          <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
            {newest.data.content.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAddToCart={(p) => addToCart({ productId: p.id, quantity: 1 })}
                isAdding={isPending && variables?.productId === product.id}
              />
            ))}
          </div>
        ) : (
          <EmptyState
            icon={PackageIcon}
            title="Chưa có sản phẩm nào"
            description="Khi người bán đăng sản phẩm đầu tiên, nó sẽ xuất hiện ở đây."
          />
        )}
      </section>

      {topRated.data?.content.length ? (
        <section className="container-page pb-14">
          <div className="mb-6">
            <h2 className="font-heading text-2xl font-semibold tracking-tight">Được đánh giá cao</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Sản phẩm đạt từ 4 sao trở lên từ những người đã mua.
            </p>
          </div>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4">
            {topRated.data.content.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onAddToCart={(p) => addToCart({ productId: p.id, quantity: 1 })}
                isAdding={isPending && variables?.productId === product.id}
              />
            ))}
          </div>
        </section>
      ) : null}

      <section className="border-t bg-secondary/30">
        <div className="container-page grid gap-8 py-14 md:grid-cols-3">
          {TRUST_ITEMS.map(({ icon: Icon, title, description }) => (
            <div key={title} className="flex gap-4">
              <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-background">
                <Icon className="size-5" aria-hidden="true" />
              </span>
              <div>
                <h3 className="text-sm font-semibold">{title}</h3>
                <p className="mt-1 text-sm text-muted-foreground">{description}</p>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}
