import { useMemo, useState } from "react"
import { Link } from "react-router-dom"
import { ImageIcon, PackagePlusIcon, PencilIcon, SearchIcon, Trash2Icon } from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { EmptyState } from "@/components/common/EmptyState"
import { ErrorState } from "@/components/common/ErrorState"
import { RatingStars } from "@/components/common/RatingStars"
import { useDeleteProduct, useProducts } from "@/hooks/useProducts"
import { useAuthStore } from "@/store/authStore"
import { formatCurrency, formatNumber } from "@/lib/format"
import type { ProductResponse } from "@/types/api"

/**
 * Backend chưa có filter theo sellerId, nên trang này kéo về tối đa 100 sản phẩm
 * (maxPageSize của Pageable) rồi lọc theo chủ sở hữu ở client. ADMIN thấy toàn bộ.
 */
const FETCH_SIZE = 100

export function SellerProductListPage() {
  const user = useAuthStore((s) => s.user)
  const [keyword, setKeyword] = useState("")
  const [pendingDelete, setPendingDelete] = useState<ProductResponse | null>(null)

  const { data, isPending, isError, error, refetch } = useProducts({
    size: FETCH_SIZE,
    sort: "createdAt,desc",
  })
  const deleteProduct = useDeleteProduct()

  const products = useMemo(() => {
    const all = data?.content ?? []
    const owned = user?.role === "ADMIN" ? all : all.filter((p) => p.sellerId === user?.id)
    const q = keyword.trim().toLowerCase()
    return q
      ? owned.filter(
          (p) => p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q)
        )
      : owned
  }, [data, user, keyword])

  return (
    <div className="flex flex-col gap-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-heading text-2xl font-semibold tracking-tight">
            {user?.role === "ADMIN" ? "Tất cả sản phẩm" : "Sản phẩm của tôi"}
          </h1>
          <p className="mt-1.5 text-sm text-muted-foreground">
            {isPending ? "Đang tải..." : `${formatNumber(products.length)} sản phẩm`}
          </p>
        </div>
        <Button asChild>
          <Link to="/seller/products/new">
            <PackagePlusIcon aria-hidden="true" />
            Thêm sản phẩm
          </Link>
        </Button>
      </header>

      <div className="relative max-w-sm">
        <SearchIcon
          className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden="true"
        />
        <Input
          type="search"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="Lọc theo tên hoặc SKU..."
          className="pl-9"
          aria-label="Lọc sản phẩm"
        />
      </div>

      {isPending ? (
        <div className="flex flex-col gap-2">
          {Array.from({ length: 6 }, (_, i) => (
            <Skeleton key={i} className="h-14 w-full" />
          ))}
        </div>
      ) : isError ? (
        <ErrorState error={error} onRetry={() => refetch()} />
      ) : products.length === 0 ? (
        <EmptyState
          icon={PackagePlusIcon}
          title={keyword ? "Không khớp sản phẩm nào" : "Bạn chưa đăng sản phẩm nào"}
          description={
            keyword
              ? "Thử từ khoá khác hoặc xoá bộ lọc."
              : "Tạo sản phẩm đầu tiên để bắt đầu bán hàng trên sàn."
          }
          action={
            <Button asChild>
              <Link to="/seller/products/new">Thêm sản phẩm</Link>
            </Button>
          }
        />
      ) : (
        <div className="rounded-xl border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Sản phẩm</TableHead>
                <TableHead>SKU</TableHead>
                <TableHead className="text-right">Giá</TableHead>
                <TableHead className="text-right">Tồn kho</TableHead>
                <TableHead>Đánh giá</TableHead>
                <TableHead>Trạng thái</TableHead>
                <TableHead className="text-right">Thao tác</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {products.map((product) => (
                <TableRow key={product.id}>
                  <TableCell className="max-w-xs">
                    <Link
                      to={`/products/${product.id}`}
                      className="block truncate font-medium underline-offset-4 hover:underline"
                    >
                      {product.name}
                    </Link>
                    <span className="text-xs text-muted-foreground">
                      {product.categoryName ?? "Chưa phân loại"}
                    </span>
                  </TableCell>
                  <TableCell className="font-mono text-xs">{product.sku}</TableCell>
                  <TableCell className="tabular text-right">
                    {formatCurrency(product.price)}
                  </TableCell>
                  <TableCell className="tabular text-right">
                    <span className={product.stock <= 0 ? "text-destructive" : undefined}>
                      {formatNumber(product.stock)}
                    </span>
                  </TableCell>
                  <TableCell>
                    {product.reviewCount > 0 ? (
                      <span className="flex items-center gap-1.5">
                        <RatingStars value={product.averageRating} size="sm" />
                        <span className="tabular text-xs text-muted-foreground">
                          ({product.reviewCount})
                        </span>
                      </span>
                    ) : (
                      <span className="text-xs text-muted-foreground">—</span>
                    )}
                  </TableCell>
                  <TableCell>
                    <Badge variant={product.status === "ACTIVE" ? "secondary" : "outline"}>
                      {product.status === "ACTIVE" ? "Đang bán" : "Ngừng bán"}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex justify-end gap-1">
                      <Button variant="ghost" size="icon-sm" asChild aria-label="Ảnh và biến thể">
                        <Link to={`/seller/products/${product.id}/edit?tab=media`}>
                          <ImageIcon aria-hidden="true" />
                        </Link>
                      </Button>
                      <Button variant="ghost" size="icon-sm" asChild aria-label="Sửa sản phẩm">
                        <Link to={`/seller/products/${product.id}/edit`}>
                          <PencilIcon aria-hidden="true" />
                        </Link>
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        aria-label="Xoá sản phẩm"
                        className="text-muted-foreground hover:text-destructive"
                        onClick={() => setPendingDelete(product)}
                      >
                        <Trash2Icon aria-hidden="true" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {data && data.page.totalElements > FETCH_SIZE ? (
        <p className="text-xs text-muted-foreground">
          Đang hiển thị {FETCH_SIZE} sản phẩm mới nhất của sàn. Backend chưa hỗ trợ lọc theo người
          bán nên danh sách có thể chưa đủ khi số sản phẩm vượt {FETCH_SIZE}.
        </p>
      ) : null}

      <AlertDialog
        open={Boolean(pendingDelete)}
        onOpenChange={(open) => !open && setPendingDelete(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xoá sản phẩm này?</AlertDialogTitle>
            <AlertDialogDescription>
              &quot;{pendingDelete?.name}&quot; sẽ bị xoá khỏi sàn. Nếu sản phẩm đã nằm trong đơn
              hàng, backend sẽ từ chối thao tác này.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Huỷ</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (pendingDelete) deleteProduct.mutate(pendingDelete.id)
                setPendingDelete(null)
              }}
            >
              Xoá sản phẩm
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
