import { useState } from "react"
import { LayersIcon, PencilIcon, PlusIcon, Trash2Icon } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Skeleton } from "@/components/ui/skeleton"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { EmptyState } from "@/components/common/EmptyState"
import { useCreateVariant, useDeleteVariant, useUpdateVariant, useVariants } from "@/hooks/useVariants"
import { formatCurrency, formatNumber } from "@/lib/format"
import type { ProductVariantResponse } from "@/types/api"

interface VariantFormState {
  size: string
  color: string
  sku: string
  price: string
  stock: string
  imageUrl: string
}

const EMPTY_FORM: VariantFormState = {
  size: "",
  color: "",
  sku: "",
  price: "",
  stock: "0",
  imageUrl: "",
}

export function ProductVariantManager({ productId }: { productId: number }) {
  const { data: variants, isPending } = useVariants(productId)
  const createVariant = useCreateVariant(productId)
  const updateVariant = useUpdateVariant(productId)
  const deleteVariant = useDeleteVariant(productId)

  const [editing, setEditing] = useState<ProductVariantResponse | null>(null)
  const [open, setOpen] = useState(false)
  const [form, setForm] = useState<VariantFormState>(EMPTY_FORM)
  const [error, setError] = useState<string | null>(null)

  const openCreate = () => {
    setEditing(null)
    setForm(EMPTY_FORM)
    setError(null)
    setOpen(true)
  }

  const openEdit = (variant: ProductVariantResponse) => {
    setEditing(variant)
    setForm({
      size: variant.size,
      color: variant.color,
      sku: variant.sku,
      price: String(variant.price),
      stock: String(variant.stock),
      imageUrl: variant.imageUrl ?? "",
    })
    setError(null)
    setOpen(true)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    const price = Number(form.price)
    const stock = Number(form.stock)

    if (!form.size.trim() || !form.color.trim() || !form.sku.trim()) {
      setError("Size, màu và SKU đều bắt buộc.")
      return
    }
    if (!Number.isFinite(price) || price <= 0) {
      setError("Giá phải lớn hơn 0.")
      return
    }
    if (!Number.isInteger(stock) || stock < 0) {
      setError("Tồn kho phải là số nguyên không âm.")
      return
    }
    setError(null)

    const payload = {
      size: form.size.trim(),
      color: form.color.trim(),
      sku: form.sku.trim(),
      price,
      stock,
      imageUrl: form.imageUrl.trim() || undefined,
    }

    if (editing) {
      updateVariant.mutate({ id: editing.id, payload }, { onSuccess: () => setOpen(false) })
    } else {
      createVariant.mutate(payload, { onSuccess: () => setOpen(false) })
    }
  }

  const isSaving = createVariant.isPending || updateVariant.isPending

  return (
    <div className="flex flex-col gap-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold">Biến thể sản phẩm</h3>
          <p className="mt-1 text-sm text-muted-foreground">
            Khi có biến thể, khách phải chọn một loại và giá/tồn kho lấy theo biến thể đó.
          </p>
        </div>
        <Button size="sm" onClick={openCreate}>
          <PlusIcon aria-hidden="true" />
          Thêm biến thể
        </Button>
      </div>

      {isPending ? (
        <div className="flex flex-col gap-2">
          {Array.from({ length: 3 }, (_, i) => (
            <Skeleton key={i} className="h-12 w-full" />
          ))}
        </div>
      ) : variants && variants.length > 0 ? (
        <div className="rounded-xl border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Size</TableHead>
                <TableHead>Màu</TableHead>
                <TableHead>SKU</TableHead>
                <TableHead className="text-right">Giá</TableHead>
                <TableHead className="text-right">Tồn kho</TableHead>
                <TableHead className="text-right">Thao tác</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {variants.map((variant) => (
                <TableRow key={variant.id}>
                  <TableCell className="font-medium">{variant.size}</TableCell>
                  <TableCell>{variant.color}</TableCell>
                  <TableCell className="font-mono text-xs">{variant.sku}</TableCell>
                  <TableCell className="tabular text-right">
                    {formatCurrency(variant.price)}
                  </TableCell>
                  <TableCell className="tabular text-right">
                    <span className={variant.stock <= 0 ? "text-destructive" : undefined}>
                      {formatNumber(variant.stock)}
                    </span>
                  </TableCell>
                  <TableCell>
                    <div className="flex justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        aria-label={`Sửa biến thể ${variant.size} ${variant.color}`}
                        onClick={() => openEdit(variant)}
                      >
                        <PencilIcon aria-hidden="true" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon-sm"
                        aria-label={`Xoá biến thể ${variant.size} ${variant.color}`}
                        className="text-muted-foreground hover:text-destructive"
                        disabled={deleteVariant.isPending}
                        onClick={() => deleteVariant.mutate(variant.id)}
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
      ) : (
        <EmptyState
          icon={LayersIcon}
          title="Sản phẩm chưa có biến thể"
          description="Không bắt buộc — nếu bỏ trống, khách sẽ mua theo giá và tồn kho của sản phẩm gốc."
          action={
            <Button variant="outline" onClick={openCreate}>
              Thêm biến thể đầu tiên
            </Button>
          }
        />
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editing ? "Sửa biến thể" : "Thêm biến thể"}</DialogTitle>
            <DialogDescription>
              Mỗi biến thể cần SKU riêng để phân biệt khi xuất kho.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="variant-size" className="mb-1.5">
                  Size <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="variant-size"
                  value={form.size}
                  onChange={(e) => setForm({ ...form, size: e.target.value })}
                  placeholder="M, L, 42..."
                />
              </div>
              <div>
                <Label htmlFor="variant-color" className="mb-1.5">
                  Màu <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="variant-color"
                  value={form.color}
                  onChange={(e) => setForm({ ...form, color: e.target.value })}
                  placeholder="Đen, Trắng..."
                />
              </div>
            </div>

            <div>
              <Label htmlFor="variant-sku" className="mb-1.5">
                SKU <span className="text-destructive">*</span>
              </Label>
              <Input
                id="variant-sku"
                value={form.sku}
                onChange={(e) => setForm({ ...form, sku: e.target.value })}
                placeholder="AO-THUN-M-DEN"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <Label htmlFor="variant-price" className="mb-1.5">
                  Giá (VND) <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="variant-price"
                  type="number"
                  inputMode="numeric"
                  min={1}
                  value={form.price}
                  onChange={(e) => setForm({ ...form, price: e.target.value })}
                />
              </div>
              <div>
                <Label htmlFor="variant-stock" className="mb-1.5">
                  Tồn kho <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="variant-stock"
                  type="number"
                  inputMode="numeric"
                  min={0}
                  value={form.stock}
                  onChange={(e) => setForm({ ...form, stock: e.target.value })}
                />
              </div>
            </div>

            <div>
              <Label htmlFor="variant-image" className="mb-1.5">
                Ảnh riêng của biến thể
              </Label>
              <Input
                id="variant-image"
                value={form.imageUrl}
                onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
                placeholder="/uploads/ao-thun-den.jpg"
              />
              <p className="mt-1.5 text-xs text-muted-foreground">
                Không bắt buộc. Dán URL ảnh (có thể lấy từ tab Ảnh) để khách chọn biến thể này thấy
                đúng ảnh.
              </p>
            </div>

            {error ? (
              <p role="alert" className="text-sm text-destructive">
                {error}
              </p>
            ) : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="ghost" onClick={() => setOpen(false)}>
                Huỷ
              </Button>
              <Button type="submit" disabled={isSaving}>
                {isSaving ? "Đang lưu..." : editing ? "Lưu thay đổi" : "Thêm biến thể"}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
