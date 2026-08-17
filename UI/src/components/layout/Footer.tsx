import { Link } from "react-router-dom"
import { ShoppingBagIcon } from "lucide-react"

const LINKS = [
  { to: "/products", label: "Tất cả sản phẩm" },
  { to: "/orders", label: "Đơn hàng của tôi" },
  { to: "/account/reviews", label: "Đánh giá của tôi" },
  { to: "/account/profile", label: "Hồ sơ" },
]

export function Footer() {
  return (
    <footer className="border-t border-border bg-background">
      <div className="container-page flex flex-col gap-6 py-10 md:flex-row md:items-start md:justify-between">
        <div className="max-w-xs">
          <Link to="/" className="flex items-center gap-2 font-heading font-semibold tracking-tight">
            <ShoppingBagIcon className="size-4" aria-hidden="true" />
            Marketplace
          </Link>
          <p className="mt-2 text-sm text-muted-foreground">
            Sàn thương mại điện tử dựng để học Spring Boot: giỏ hàng, đơn hàng, thanh toán VNPay và
            đánh giá đã xác thực mua hàng.
          </p>
        </div>

        <nav aria-label="Liên kết nhanh">
          <h2 className="text-sm font-semibold">Liên kết</h2>
          <ul className="mt-3 flex flex-col gap-2 text-sm text-muted-foreground">
            {LINKS.map((link) => (
              <li key={link.to}>
                <Link to={link.to} className="transition-colors hover:text-foreground">
                  {link.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
      </div>

      <div className="container-page flex flex-col items-center justify-between gap-2 border-t py-5 text-sm text-muted-foreground md:flex-row">
        <p>© {new Date().getFullYear()} Marketplace. Built with Spring Boot + React.</p>
        <p className="font-mono text-xs">v0.1.0</p>
      </div>
    </footer>
  )
}
