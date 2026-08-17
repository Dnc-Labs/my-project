import { StarIcon, XIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Separator } from "@/components/ui/separator"
import { Skeleton } from "@/components/ui/skeleton"
import { useFlatCategories } from "@/hooks/useCategories"
import { useSyncedState } from "@/hooks/useSyncedState"
import { cn } from "@/lib/utils"

export interface ProductFilterValue {
  categoryId?: number
  minPrice?: number
  maxPrice?: number
  minRating?: number
}

interface ProductFiltersProps {
  value: ProductFilterValue
  onChange: (patch: Partial<ProductFilterValue>) => void
  onReset: () => void
}

const RATING_OPTIONS = [4, 3, 2] as const

export function ProductFilters({ value, onChange, onReset }: ProductFiltersProps) {
  const { flat, isPending } = useFlatCategories()

  // Giá nhập rời khỏi URL cho tới khi bấm "Áp dụng": gõ từng chữ số mà refetch ngay
  // sẽ bắn một loạt request rác cho các khoảng giá dở dang (5 → 50 → 500...).
  // Vẫn đồng bộ lại khi URL đổi từ bên ngoài (nút back, xoá bộ lọc).
  const [minPrice, setMinPrice] = useSyncedState(value.minPrice?.toString() ?? "")
  const [maxPrice, setMaxPrice] = useSyncedState(value.maxPrice?.toString() ?? "")

  const applyPrice = () => {
    onChange({
      minPrice: minPrice ? Number(minPrice) : undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
    })
  }

  const hasFilter =
    value.categoryId !== undefined ||
    value.minPrice !== undefined ||
    value.maxPrice !== undefined ||
    value.minRating !== undefined

  return (
    <div className="flex flex-col gap-6">
      <section aria-labelledby="filter-category">
        <h3 id="filter-category" className="mb-3 text-sm font-semibold">
          Danh mục
        </h3>
        {isPending ? (
          <div className="flex flex-col gap-2">
            {Array.from({ length: 5 }, (_, i) => (
              <Skeleton key={i} className="h-7 w-full" />
            ))}
          </div>
        ) : (
          <ul className="flex max-h-72 flex-col gap-0.5 overflow-y-auto pr-1">
            <li>
              <button
                type="button"
                onClick={() => onChange({ categoryId: undefined })}
                className={cn(
                  "w-full rounded-md px-2 py-1.5 text-left text-sm transition-colors hover:bg-muted",
                  value.categoryId === undefined && "bg-secondary font-medium"
                )}
              >
                Tất cả danh mục
              </button>
            </li>
            {flat.map((category) => (
              <li key={category.id}>
                <button
                  type="button"
                  onClick={() => onChange({ categoryId: category.id })}
                  style={{ paddingLeft: `${0.5 + category.depth * 0.75}rem` }}
                  className={cn(
                    "w-full rounded-md py-1.5 pr-2 text-left text-sm transition-colors hover:bg-muted",
                    value.categoryId === category.id && "bg-secondary font-medium"
                  )}
                >
                  {category.name}
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      <Separator />

      <section aria-labelledby="filter-price">
        <h3 id="filter-price" className="mb-3 text-sm font-semibold">
          Khoảng giá
        </h3>
        <div className="flex items-end gap-2">
          <div className="flex-1">
            <Label htmlFor="min-price" className="mb-1.5 text-xs text-muted-foreground">
              Từ
            </Label>
            <Input
              id="min-price"
              type="number"
              inputMode="numeric"
              min={0}
              placeholder="0"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && applyPrice()}
            />
          </div>
          <div className="flex-1">
            <Label htmlFor="max-price" className="mb-1.5 text-xs text-muted-foreground">
              Đến
            </Label>
            <Input
              id="max-price"
              type="number"
              inputMode="numeric"
              min={0}
              placeholder="Không giới hạn"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && applyPrice()}
            />
          </div>
        </div>
        <Button variant="outline" size="sm" className="mt-3 w-full" onClick={applyPrice}>
          Áp dụng khoảng giá
        </Button>
      </section>

      <Separator />

      <section aria-labelledby="filter-rating">
        <h3 id="filter-rating" className="mb-3 text-sm font-semibold">
          Đánh giá
        </h3>
        <ul className="flex flex-col gap-0.5">
          <li>
            <button
              type="button"
              onClick={() => onChange({ minRating: undefined })}
              className={cn(
                "w-full rounded-md px-2 py-1.5 text-left text-sm transition-colors hover:bg-muted",
                value.minRating === undefined && "bg-secondary font-medium"
              )}
            >
              Tất cả
            </button>
          </li>
          {RATING_OPTIONS.map((rating) => (
            <li key={rating}>
              <button
                type="button"
                onClick={() => onChange({ minRating: rating })}
                className={cn(
                  "flex w-full items-center gap-1.5 rounded-md px-2 py-1.5 text-left text-sm transition-colors hover:bg-muted",
                  value.minRating === rating && "bg-secondary font-medium"
                )}
              >
                <StarIcon className="size-3.5 fill-warning text-warning" aria-hidden="true" />
                <span>từ {rating} sao trở lên</span>
              </button>
            </li>
          ))}
        </ul>
      </section>

      {hasFilter ? (
        <Button variant="ghost" size="sm" onClick={onReset} className="self-start">
          <XIcon aria-hidden="true" />
          Xoá bộ lọc
        </Button>
      ) : null}
    </div>
  )
}
