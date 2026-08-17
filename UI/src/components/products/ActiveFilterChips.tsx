import { XIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useFlatCategories } from "@/hooks/useCategories"
import { formatCurrency } from "@/lib/format"
import type { ProductFilterValue } from "@/components/products/ProductFilters"

interface ActiveFilterChipsProps {
  keyword: string
  value: ProductFilterValue
  onRemove: (patch: Partial<ProductFilterValue> & { keyword?: undefined }) => void
  onClearKeyword: () => void
  onReset: () => void
}

/**
 * Hiển thị điều kiện đang lọc ngay trên lưới kết quả. Không có nó, bộ lọc trong Sheet
 * (mobile) trở thành trạng thái ẩn: người dùng thấy ít kết quả mà không biết vì sao.
 */
export function ActiveFilterChips({
  keyword,
  value,
  onRemove,
  onClearKeyword,
  onReset,
}: ActiveFilterChipsProps) {
  const { flat } = useFlatCategories()
  const categoryName = flat.find((category) => category.id === value.categoryId)?.name

  const chips: { key: string; label: string; clear: () => void }[] = []

  if (keyword) {
    chips.push({ key: "keyword", label: `Từ khoá: "${keyword}"`, clear: onClearKeyword })
  }
  if (value.categoryId !== undefined) {
    chips.push({
      key: "category",
      label: `Danh mục: ${categoryName ?? `#${value.categoryId}`}`,
      clear: () => onRemove({ categoryId: undefined }),
    })
  }
  if (value.minPrice !== undefined || value.maxPrice !== undefined) {
    const from = value.minPrice !== undefined ? formatCurrency(value.minPrice) : "0₫"
    const to = value.maxPrice !== undefined ? formatCurrency(value.maxPrice) : "không giới hạn"
    chips.push({
      key: "price",
      label: `Giá: ${from} – ${to}`,
      clear: () => onRemove({ minPrice: undefined, maxPrice: undefined }),
    })
  }
  if (value.minRating !== undefined) {
    chips.push({
      key: "rating",
      label: `Từ ${value.minRating} sao trở lên`,
      clear: () => onRemove({ minRating: undefined }),
    })
  }

  if (chips.length === 0) return null

  return (
    <div className="mb-5 flex flex-wrap items-center gap-2">
      {chips.map((chip) => (
        <span
          key={chip.key}
          className="inline-flex items-center gap-1 rounded-full border bg-secondary py-1 pr-1 pl-3 text-xs font-medium"
        >
          {chip.label}
          <button
            type="button"
            onClick={chip.clear}
            aria-label={`Bỏ lọc ${chip.label}`}
            className="flex size-5 items-center justify-center rounded-full transition-colors hover:bg-background focus-visible:ring-[3px] focus-visible:ring-ring/50 focus-visible:outline-none"
          >
            <XIcon className="size-3" aria-hidden="true" />
          </button>
        </span>
      ))}
      {chips.length > 1 ? (
        <Button variant="ghost" size="xs" onClick={onReset}>
          Xoá tất cả
        </Button>
      ) : null}
    </div>
  )
}
