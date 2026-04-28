import { Link, NavLink, useNavigate } from "react-router-dom"
import { LogOut, ShoppingBag, Store, User } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { useAuthStore } from "@/store/authStore"

export function Header() {
  const navigate = useNavigate()
  const { user, isAuthenticated, clear } = useAuthStore()

  const handleLogout = () => {
    clear()
    navigate("/")
  }

  return (
    <header className="sticky top-0 z-40 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 md:px-8">
        <Link to="/" className="flex items-center gap-2 font-heading text-xl font-semibold tracking-tight">
          <ShoppingBag className="h-5 w-5" aria-hidden="true" />
          <span>Marketplace</span>
        </Link>

        <nav className="hidden gap-6 text-sm font-medium md:flex" aria-label="Primary navigation">
          <NavLink
            to="/products"
            className={({ isActive }) =>
              isActive ? "text-foreground" : "text-muted-foreground transition-colors hover:text-foreground"
            }
          >
            Sản phẩm
          </NavLink>
          {user?.role === "SELLER" || user?.role === "ADMIN" ? (
            <NavLink
              to="/seller/products"
              className={({ isActive }) =>
                isActive ? "text-foreground" : "text-muted-foreground transition-colors hover:text-foreground"
              }
            >
              Quản lý bán hàng
            </NavLink>
          ) : null}
        </nav>

        <div className="flex items-center gap-2">
          {isAuthenticated() && user ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="gap-2">
                  <User className="h-4 w-4" aria-hidden="true" />
                  <span className="hidden max-w-[160px] truncate sm:inline">{user.email}</span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel className="text-xs font-normal text-muted-foreground">
                  {user.role}
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                {(user.role === "SELLER" || user.role === "ADMIN") && (
                  <DropdownMenuItem onSelect={() => navigate("/seller/products")}>
                    <Store className="mr-2 h-4 w-4" aria-hidden="true" />
                    Quản lý bán hàng
                  </DropdownMenuItem>
                )}
                <DropdownMenuItem onSelect={handleLogout} className="text-destructive focus:text-destructive">
                  <LogOut className="mr-2 h-4 w-4" aria-hidden="true" />
                  Đăng xuất
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <>
              <Button variant="ghost" size="sm" asChild>
                <Link to="/login">Đăng nhập</Link>
              </Button>
              <Button size="sm" asChild>
                <Link to="/register">Đăng ký</Link>
              </Button>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
