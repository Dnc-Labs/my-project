import { Suspense } from "react"
import { NavLink, Outlet } from "react-router-dom"
import { ClipboardListIcon, FolderTreeIcon, PackageIcon, PlusCircleIcon } from "lucide-react"
import { Header } from "./Header"
import { Footer } from "./Footer"
import { Skeleton } from "@/components/ui/skeleton"
import { useAuthStore } from "@/store/authStore"
import { cn } from "@/lib/utils"

const SELLER_NAV = [
  { to: "/seller/products", label: "Sản phẩm", icon: PackageIcon, end: true },
  { to: "/seller/products/new", label: "Thêm sản phẩm", icon: PlusCircleIcon, end: false },
  { to: "/seller/orders", label: "Xử lý đơn hàng", icon: ClipboardListIcon, end: false },
]

const ADMIN_NAV = [
  { to: "/admin/categories", label: "Danh mục", icon: FolderTreeIcon, end: false },
]

export function SellerLayout() {
  const role = useAuthStore((s) => s.user?.role)
  const items = role === "ADMIN" ? [...SELLER_NAV, ...ADMIN_NAV] : SELLER_NAV

  return (
    <div className="flex min-h-dvh flex-col">
      <Header />
      <div className="container-page flex flex-1 gap-8 py-8">
        <aside className="hidden w-56 shrink-0 md:block" aria-label="Điều hướng khu quản lý">
          <nav className="sticky top-24 flex flex-col gap-1">
            {items.map(({ to, label, icon: Icon, end }) => (
              <NavLink
                key={to}
                to={to}
                end={end}
                className={({ isActive }) =>
                  cn(
                    "flex min-h-10 items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                    isActive
                      ? "bg-secondary text-foreground"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground"
                  )
                }
              >
                <Icon className="size-4" aria-hidden="true" />
                {label}
              </NavLink>
            ))}
          </nav>
        </aside>

        {/* Mobile: sidebar đổi thành thanh cuộn ngang để không chiếm chiều cao màn hình. */}
        <div className="min-w-0 flex-1">
          <nav
            className="mb-6 -mx-4 flex gap-2 overflow-x-auto px-4 pb-2 md:hidden"
            aria-label="Điều hướng khu quản lý"
          >
            {items.map(({ to, label, icon: Icon, end }) => (
              <NavLink
                key={to}
                to={to}
                end={end}
                className={({ isActive }) =>
                  cn(
                    "flex min-h-10 shrink-0 items-center gap-1.5 rounded-full border px-3.5 text-sm font-medium transition-colors",
                    isActive
                      ? "border-foreground bg-secondary"
                      : "border-border text-muted-foreground"
                  )
                }
              >
                <Icon className="size-4" aria-hidden="true" />
                {label}
              </NavLink>
            ))}
          </nav>

          <main>
            <Suspense
              fallback={
                <div className="flex flex-col gap-4">
                  <Skeleton className="h-8 w-56" />
                  <Skeleton className="h-72 w-full rounded-xl" />
                </div>
              }
            >
              <Outlet />
            </Suspense>
          </main>
        </div>
      </div>
      <Footer />
    </div>
  )
}
