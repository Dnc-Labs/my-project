import { Link, NavLink, useNavigate } from "react-router-dom"
import {
  FolderTreeIcon,
  LogOutIcon,
  MenuIcon,
  PackageIcon,
  ShoppingBagIcon,
  ShoppingCartIcon,
  StarIcon,
  StoreIcon,
  UserIcon,
} from "lucide-react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from "@/components/ui/sheet"
import { CategoryMenu } from "@/components/layout/CategoryMenu"
import { ThemeToggle } from "@/components/common/ThemeToggle"
import { useCartCount } from "@/hooks/useCart"
import { useCategories } from "@/hooks/useCategories"
import { useAuthStore } from "@/store/authStore"
import { cn } from "@/lib/utils"

const NAV_LINKS = [
  { to: "/products", label: "Sản phẩm" },
  { to: "/orders", label: "Đơn hàng", authOnly: true },
]

export function Header() {
  const navigate = useNavigate()
  const { user, isAuthenticated, clear } = useAuthStore()
  const cartCount = useCartCount()
  // Danh mục dùng chung cache với CategoryMenu (staleTime 5 phút) nên không phát sinh request thêm.
  const { data: categories } = useCategories()
  const isStaff = user?.role === "SELLER" || user?.role === "ADMIN"
  const loggedIn = isAuthenticated()

  const handleLogout = () => {
    clear()
    navigate("/")
  }

  const navClass = ({ isActive }: { isActive: boolean }) =>
    isActive
      ? "text-foreground"
      : "text-muted-foreground transition-colors hover:text-foreground"

  return (
    <header className="sticky top-0 z-40 w-full border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80">
      <div className="container-page flex h-16 items-center justify-between gap-4">
        <div className="flex items-center gap-6">
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className="md:hidden" aria-label="Mở menu">
                <MenuIcon aria-hidden="true" />
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-72">
              <SheetHeader>
                <SheetTitle>Menu</SheetTitle>
              </SheetHeader>
              <nav className="flex flex-col gap-1 p-4">
                {NAV_LINKS.filter((link) => !link.authOnly || loggedIn).map((link) => (
                  <NavLink
                    key={link.to}
                    to={link.to}
                    className={({ isActive }) =>
                      cn(
                        "rounded-md px-3 py-2.5 text-sm font-medium transition-colors",
                        isActive ? "bg-secondary" : "hover:bg-muted"
                      )
                    }
                  >
                    {link.label}
                  </NavLink>
                ))}
                {isStaff ? (
                  <NavLink
                    to="/seller/products"
                    className={({ isActive }) =>
                      cn(
                        "rounded-md px-3 py-2.5 text-sm font-medium transition-colors",
                        isActive ? "bg-secondary" : "hover:bg-muted"
                      )
                    }
                  >
                    Quản lý bán hàng
                  </NavLink>
                ) : null}

                {categories?.length ? (
                  <>
                    <p className="mt-4 px-3 text-xs font-semibold tracking-wide text-muted-foreground uppercase">
                      Danh mục
                    </p>
                    {categories.map((category) => (
                      <Link
                        key={category.id}
                        to={`/products?categoryId=${category.id}`}
                        className="rounded-md px-3 py-2.5 text-sm transition-colors hover:bg-muted"
                      >
                        {category.name}
                      </Link>
                    ))}
                  </>
                ) : null}
              </nav>
            </SheetContent>
          </Sheet>

          <Link
            to="/"
            className="flex items-center gap-2 font-heading text-xl font-semibold tracking-tight"
          >
            <ShoppingBagIcon className="size-5" aria-hidden="true" />
            <span>Marketplace</span>
          </Link>

          <nav
            className="hidden items-center gap-6 text-sm font-medium md:flex"
            aria-label="Điều hướng chính"
          >
            <CategoryMenu />
            {NAV_LINKS.filter((link) => !link.authOnly || loggedIn).map((link) => (
              <NavLink key={link.to} to={link.to} className={navClass}>
                {link.label}
              </NavLink>
            ))}
            {isStaff ? (
              <NavLink to="/seller/products" className={navClass}>
                Quản lý bán hàng
              </NavLink>
            ) : null}
          </nav>
        </div>

        <div className="flex items-center gap-1">
          <ThemeToggle />

          <Button variant="ghost" size="icon" asChild className="relative" aria-label="Giỏ hàng">
            <Link to="/cart">
              <ShoppingCartIcon aria-hidden="true" />
              {cartCount > 0 ? (
                <Badge className="tabular absolute -top-0.5 -right-0.5 h-5 min-w-5 justify-center px-1 text-[10px]">
                  {cartCount > 99 ? "99+" : cartCount}
                  <span className="sr-only">sản phẩm trong giỏ</span>
                </Badge>
              ) : null}
            </Link>
          </Button>

          {loggedIn && user ? (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="gap-2">
                  <UserIcon aria-hidden="true" />
                  <span className="hidden max-w-[160px] truncate sm:inline">{user.email}</span>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel className="text-xs font-normal text-muted-foreground">
                  {user.role}
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onSelect={() => navigate("/account/profile")}>
                  <UserIcon aria-hidden="true" />
                  Hồ sơ của tôi
                </DropdownMenuItem>
                <DropdownMenuItem onSelect={() => navigate("/orders")}>
                  <PackageIcon aria-hidden="true" />
                  Đơn hàng của tôi
                </DropdownMenuItem>
                <DropdownMenuItem onSelect={() => navigate("/account/reviews")}>
                  <StarIcon aria-hidden="true" />
                  Đánh giá của tôi
                </DropdownMenuItem>
                {isStaff ? (
                  <>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem onSelect={() => navigate("/seller/products")}>
                      <StoreIcon aria-hidden="true" />
                      Quản lý bán hàng
                    </DropdownMenuItem>
                    {user.role === "ADMIN" ? (
                      <DropdownMenuItem onSelect={() => navigate("/admin/categories")}>
                        <FolderTreeIcon aria-hidden="true" />
                        Quản lý danh mục
                      </DropdownMenuItem>
                    ) : null}
                  </>
                ) : null}
                {/* Đăng xuất tách khỏi nhóm điều hướng để không bấm nhầm. */}
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  onSelect={handleLogout}
                  className="text-destructive focus:text-destructive"
                >
                  <LogOutIcon aria-hidden="true" />
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
