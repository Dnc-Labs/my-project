import { useParams } from "react-router-dom"

export function ProductDetailPage() {
  const { id } = useParams()
  return (
    <div className="mx-auto max-w-7xl px-4 py-10 md:px-8">
      <h1 className="font-heading text-3xl font-semibold tracking-tight">Sản phẩm #{id}</h1>
      <p className="mt-2 text-sm text-muted-foreground">
        Chi tiết sản phẩm sẽ được dựng ở Phase 3.
      </p>
    </div>
  )
}
