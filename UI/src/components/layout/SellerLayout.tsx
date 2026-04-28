import { NavLink, Outlet } from "react-router-dom"
import { Package, PlusCircle } from "lucide-react"
import { Header } from "./Header"
import { Footer } from "./Footer"
import { cn } from "@/lib/utils"

const sellerNav = [
  { to: "/seller/products", label: "Sản phẩm của tôi", icon: Package, end: true },
  { to: "/seller/products/new", label: "Thêm sản phẩm", icon: PlusCircle, end: false },
]

export function SellerLayout() {
  return (
    <div className="flex min-h-dvh flex-col">
      <Header />
      <div className="mx-auto flex w-full max-w-7xl flex-1 gap-8 px-4 py-8 md:px-8">
        <aside className="hidden w-56 shrink-0 md:block" aria-label="Seller navigation">
          <nav className="sticky top-24 flex flex-col gap-1">
            {sellerNav.map(({ to, label, icon: Icon, end }) => (
              <NavLink
                key={to}
                to={to}
                end={end}
                className={({ isActive }) =>
                  cn(
                    "flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                    isActive
                      ? "bg-secondary text-foreground"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground"
                  )
                }
              >
                <Icon className="h-4 w-4" aria-hidden="true" />
                {label}
              </NavLink>
            ))}
          </nav>
        </aside>
        <main className="flex-1 min-w-0">
          <Outlet />
        </main>
      </div>
      <Footer />
    </div>
  )
}
