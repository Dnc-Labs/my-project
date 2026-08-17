import { useNavigate } from "react-router-dom"
import { ChevronDownIcon, LayoutGridIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { useCategories } from "@/hooks/useCategories"

/**
 * Điều hướng theo danh mục ở thanh header. Cây danh mục chỉ sâu 2 cấp trong thực tế
 * nên hiển thị dạng "nhóm + mục con" thay vì submenu lồng nhau — ít cú bấm hơn và
 * dùng được bằng bàn phím không cần điều hướng ngang.
 */
export function CategoryMenu() {
  const navigate = useNavigate()
  const { data: categories, isPending } = useCategories()

  if (isPending || !categories?.length) return null

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="sm" className="gap-1.5 px-2 text-muted-foreground hover:text-foreground">
          <LayoutGridIcon aria-hidden="true" />
          Danh mục
          <ChevronDownIcon className="size-3.5" aria-hidden="true" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-64">
        <DropdownMenuItem onSelect={() => navigate("/products")}>
          Tất cả sản phẩm
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        {categories.map((root) => (
          <DropdownMenuGroup key={root.id}>
            <DropdownMenuLabel
              className="cursor-pointer text-xs text-muted-foreground hover:text-foreground"
              onClick={() => navigate(`/products?categoryId=${root.id}`)}
            >
              {root.name}
            </DropdownMenuLabel>
            {(root.children ?? []).map((child) => (
              <DropdownMenuItem
                key={child.id}
                onSelect={() => navigate(`/products?categoryId=${child.id}`)}
              >
                {child.name}
              </DropdownMenuItem>
            ))}
          </DropdownMenuGroup>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
