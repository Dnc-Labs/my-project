import { useEffect, useMemo } from "react"
import { useSearchParams } from "react-router-dom"
import { PackageSearchIcon, SearchIcon, SlidersHorizontalIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import { ProductCard } from "@/components/products/ProductCard"
import { ProductGridSkeleton } from "@/components/products/ProductCardSkeleton"
import { ProductFilters, type ProductFilterValue } from "@/components/products/ProductFilters"
import { ActiveFilterChips } from "@/components/products/ActiveFilterChips"
import { EmptyState } from "@/components/common/EmptyState"
import { ErrorState } from "@/components/common/ErrorState"
import { Pagination } from "@/components/common/Pagination"
import { useProducts } from "@/hooks/useProducts"
import { useAddToCartAction } from "@/hooks/useAddToCartAction"
import { useSyncedState } from "@/hooks/useSyncedState"
import { formatNumber } from "@/lib/format"
import type { ProductFilterParams } from "@/types/api"

const SORT_OPTIONS = [
  { value: "createdAt,desc", label: "Mới nhất" },
  { value: "price,asc", label: "Giá thấp đến cao" },
  { value: "price,desc", label: "Giá cao đến thấp" },
  { value: "averageRating,desc", label: "Đánh giá cao nhất" },
  { value: "name,asc", label: "Tên A → Z" },
] as const

const DEFAULT_SORT = SORT_OPTIONS[0].value
const PAGE_SIZE = 12

function numberParam(params: URLSearchParams, key: string): number | undefined {
  const raw = params.get(key)
  if (raw === null || raw === "") return undefined
  const value = Number(raw)
  return Number.isFinite(value) ? value : undefined
}

export function ProductListPage() {
  // Toàn bộ trạng thái lọc nằm trên URL: chia sẻ link giữ nguyên kết quả và nút back hoạt động đúng.
  const [searchParams, setSearchParams] = useSearchParams()

  const keywordParam = searchParams.get("keyword") ?? ""
  // Ô tìm kiếm giữ state riêng để gõ mượt, nhưng vẫn bám theo URL khi người dùng
  // bấm back hoặc mở link có sẵn keyword.
  const [keywordInput, setKeywordInput] = useSyncedState(keywordParam)

  // Gõ tới đâu gọi API tới đó sẽ spam backend → chờ người dùng ngừng gõ 400ms mới đẩy lên URL.
  useEffect(() => {
    if (keywordInput === keywordParam) return
    const timer = setTimeout(() => {
      updateParams({ keyword: keywordInput || undefined, page: undefined })
    }, 400)
    return () => clearTimeout(timer)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [keywordInput])

  function updateParams(patch: Record<string, string | number | undefined>) {
    setSearchParams(
      (prev) => {
        const next = new URLSearchParams(prev)
        Object.entries(patch).forEach(([key, value]) => {
          if (value === undefined || value === "") next.delete(key)
          else next.set(key, String(value))
        })
        return next
      },
      { replace: true }
    )
  }

  const filterValue: ProductFilterValue = {
    categoryId: numberParam(searchParams, "categoryId"),
    minPrice: numberParam(searchParams, "minPrice"),
    maxPrice: numberParam(searchParams, "maxPrice"),
    minRating: numberParam(searchParams, "minRating"),
  }

  const sort = searchParams.get("sort") ?? DEFAULT_SORT
  const page = numberParam(searchParams, "page") ?? 0

  const queryParams: ProductFilterParams = useMemo(
    () => ({
      keyword: keywordParam || undefined,
      ...filterValue,
      // Storefront chỉ bán hàng đang mở bán; hàng INACTIVE chỉ thấy trong khu quản trị.
      status: "ACTIVE",
      page,
      size: PAGE_SIZE,
      sort,
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [keywordParam, searchParams.toString()]
  )

  const { data, isPending, isError, error, refetch, isFetching } = useProducts(queryParams)
  const { addToCart, isPending: isAdding, variables } = useAddToCartAction()

  const handleFilterChange = (patch: Partial<ProductFilterValue>) => {
    // Đổi bộ lọc thì phải về trang 1, nếu không sẽ rơi vào trang trống của tập kết quả mới.
    updateParams({ ...patch, page: undefined })
  }

  const handleReset = () => {
    updateParams({
      categoryId: undefined,
      minPrice: undefined,
      maxPrice: undefined,
      minRating: undefined,
      page: undefined,
    })
  }

  const filters = (
    <ProductFilters value={filterValue} onChange={handleFilterChange} onReset={handleReset} />
  )

  return (
    <div className="container-page py-8 md:py-10">
      <header className="mb-6">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Sản phẩm</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          {data
            ? `${formatNumber(data.page.totalElements)} sản phẩm${keywordParam ? ` cho "${keywordParam}"` : ""}`
            : "Đang tải danh sách sản phẩm..."}
        </p>
      </header>

      <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center">
        <div className="relative flex-1">
          <SearchIcon
            className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden="true"
          />
          <Input
            type="search"
            value={keywordInput}
            onChange={(e) => setKeywordInput(e.target.value)}
            placeholder="Tìm theo tên sản phẩm..."
            className="h-10 pl-9"
            aria-label="Tìm sản phẩm"
          />
        </div>

        <div className="flex items-center gap-2">
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="outline" className="lg:hidden">
                <SlidersHorizontalIcon aria-hidden="true" />
                Bộ lọc
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-[85vw] sm:max-w-sm">
              <SheetHeader>
                <SheetTitle>Bộ lọc</SheetTitle>
              </SheetHeader>
              <div className="overflow-y-auto p-4">{filters}</div>
            </SheetContent>
          </Sheet>

          <Select value={sort} onValueChange={(value) => updateParams({ sort: value, page: undefined })}>
            <SelectTrigger className="h-10 w-full sm:w-52" aria-label="Sắp xếp">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {SORT_OPTIONS.map((option) => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="flex gap-8">
        <aside className="hidden w-60 shrink-0 lg:block" aria-label="Bộ lọc sản phẩm">
          <div className="sticky top-24">{filters}</div>
        </aside>

        <div className="min-w-0 flex-1">
          <ActiveFilterChips
            keyword={keywordParam}
            value={filterValue}
            onRemove={handleFilterChange}
            onClearKeyword={() => {
              setKeywordInput("")
              updateParams({ keyword: undefined, page: undefined })
            }}
            onReset={() => {
              setKeywordInput("")
              updateParams({
                keyword: undefined,
                categoryId: undefined,
                minPrice: undefined,
                maxPrice: undefined,
                minRating: undefined,
                page: undefined,
              })
            }}
          />

          {isPending ? (
            <ProductGridSkeleton count={PAGE_SIZE} />
          ) : isError ? (
            <ErrorState error={error} onRetry={() => refetch()} />
          ) : data && data.content.length > 0 ? (
            <>
              <div
                className="grid grid-cols-2 gap-4 md:grid-cols-3 xl:grid-cols-4"
                aria-busy={isFetching}
              >
                {data.content.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    onAddToCart={(p) => addToCart({ productId: p.id, quantity: 1 })}
                    isAdding={isAdding && variables?.productId === product.id}
                  />
                ))}
              </div>
              <Pagination
                meta={data.page}
                onPageChange={(next) => {
                  updateParams({ page: next === 0 ? undefined : next })
                  window.scrollTo({ top: 0, behavior: "smooth" })
                }}
                className="mt-10"
              />
            </>
          ) : (
            <EmptyState
              icon={PackageSearchIcon}
              title="Không tìm thấy sản phẩm phù hợp"
              description="Thử bỏ bớt bộ lọc hoặc tìm với từ khoá khác."
              action={
                <Button variant="outline" onClick={handleReset}>
                  Xoá bộ lọc
                </Button>
              }
            />
          )}
        </div>
      </div>
    </div>
  )
}
