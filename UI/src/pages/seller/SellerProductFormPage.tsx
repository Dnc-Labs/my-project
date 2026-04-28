import { useParams } from "react-router-dom"

export function SellerProductFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  return (
    <div>
      <h1 className="font-heading text-2xl font-semibold tracking-tight">
        {isEdit ? `Cập nhật sản phẩm #${id}` : "Thêm sản phẩm mới"}
      </h1>
      <p className="mt-2 text-sm text-muted-foreground">
        Form sẽ được dựng ở Phase 4.
      </p>
    </div>
  )
}
