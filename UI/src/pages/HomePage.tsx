import { Link } from "react-router-dom"
import { ArrowRight, Search } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"

export function HomePage() {
  return (
    <div className="mx-auto max-w-7xl px-4 md:px-8">
      <section className="flex min-h-[60vh] flex-col items-center justify-center py-16 text-center">
        <h1 className="max-w-3xl font-heading text-4xl font-semibold tracking-tight md:text-6xl">
          Mua bán mọi thứ, đơn giản và minh bạch.
        </h1>
        <p className="mt-6 max-w-xl text-base text-muted-foreground md:text-lg">
          Khám phá hàng nghìn sản phẩm từ những người bán uy tín. Hoặc trở thành seller chỉ với vài bước.
        </p>

        <form className="mt-10 flex w-full max-w-xl items-center gap-2" role="search">
          <div className="relative flex-1">
            <Search
              className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
              aria-hidden="true"
            />
            <Input
              type="search"
              placeholder="Tìm sản phẩm..."
              className="pl-9 h-12"
              aria-label="Tìm sản phẩm"
            />
          </div>
          <Button asChild size="lg" className="h-12">
            <Link to="/products">
              Tìm kiếm
              <ArrowRight className="ml-1 h-4 w-4" aria-hidden="true" />
            </Link>
          </Button>
        </form>

        <div className="mt-8 flex flex-wrap items-center justify-center gap-2 text-sm text-muted-foreground">
          <span>Phổ biến:</span>
          {["Điện tử", "Thời trang", "Sách", "Đồ gia dụng"].map((tag) => (
            <Link
              key={tag}
              to="/products"
              className="rounded-full border border-border px-3 py-1 transition-colors hover:border-foreground hover:text-foreground"
            >
              {tag}
            </Link>
          ))}
        </div>
      </section>
    </div>
  )
}
