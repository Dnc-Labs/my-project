const currencyFormatter = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
})

const numberFormatter = new Intl.NumberFormat("vi-VN")

const dateTimeFormatter = new Intl.DateTimeFormat("vi-VN", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
})

const dateFormatter = new Intl.DateTimeFormat("vi-VN", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
})

export function formatCurrency(value: number | null | undefined): string {
  if (value === null || value === undefined) return "—"
  return currencyFormatter.format(value)
}

export function formatNumber(value: number | null | undefined): string {
  if (value === null || value === undefined) return "—"
  return numberFormatter.format(value)
}

/**
 * Backend trả LocalDateTime không kèm timezone ("2026-08-12T10:15:30").
 * Trình duyệt hiểu chuỗi đó là giờ local — đúng ý ở đây vì server cũng chạy giờ local.
 */
export function formatDateTime(value: string | null | undefined): string {
  if (!value) return "—"
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? "—" : dateTimeFormatter.format(date)
}

export function formatDate(value: string | null | undefined): string {
  if (!value) return "—"
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? "—" : dateFormatter.format(date)
}

export function formatRating(value: number | null | undefined): string {
  if (!value) return "0.0"
  return value.toFixed(1)
}

/** Slug hoá tiếng Việt để gợi ý sẵn field slug/sku khi seller nhập tên sản phẩm. */
export function slugify(input: string): string {
  return input
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9\s-]/g, "")
    .replace(/\s+/g, "-")
    .replace(/-+/g, "-")
}
