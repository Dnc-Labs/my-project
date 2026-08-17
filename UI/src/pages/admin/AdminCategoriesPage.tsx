import { useState } from "react"
import { ChevronRightIcon, FolderTreeIcon, PencilIcon, PlusIcon, Trash2Icon } from "lucide-react"
import { Badge } from "@/components/ui/badge"
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { Textarea } from "@/components/ui/textarea"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { EmptyState } from "@/components/common/EmptyState"
import { ErrorState } from "@/components/common/ErrorState"
import {
  flattenCategories,
  useCategories,
  useCreateCategory,
  useDeleteCategory,
  useUpdateCategory,
} from "@/hooks/useCategories"
import { slugify } from "@/lib/format"
import type { CategoryResponse } from "@/types/api"

interface FormState {
  name: string
  slug: string
  description: string
  parentId: string
  status: "ACTIVE" | "INACTIVE"
}

const EMPTY_FORM: FormState = {
  name: "",
  slug: "",
  description: "",
  parentId: "none",
  status: "ACTIVE",
}

function CategoryNode({
  category,
  depth,
  onAddChild,
  onEdit,
  onDelete,
}: {
  category: CategoryResponse
  depth: number
  onAddChild: (parent: CategoryResponse) => void
  onEdit: (category: CategoryResponse) => void
  onDelete: (category: CategoryResponse) => void
}) {
  const children = category.children ?? []

  return (
    <li>
      <div
        className="flex flex-wrap items-center gap-2 border-b py-2.5 last:border-b-0"
        style={{ paddingLeft: `${depth * 1.5}rem` }}
      >
        {depth > 0 ? (
          <ChevronRightIcon className="size-3.5 shrink-0 text-muted-foreground" aria-hidden="true" />
        ) : null}
        <span className="font-medium">{category.name}</span>
        <span className="font-mono text-xs text-muted-foreground">/{category.slug}</span>
        {category.status === "INACTIVE" ? <Badge variant="outline">Ẩn</Badge> : null}

        <div className="ml-auto flex gap-1">
          <Button
            variant="ghost"
            size="icon-sm"
            aria-label={`Thêm danh mục con cho ${category.name}`}
            onClick={() => onAddChild(category)}
          >
            <PlusIcon aria-hidden="true" />
          </Button>
          <Button
            variant="ghost"
            size="icon-sm"
            aria-label={`Sửa ${category.name}`}
            onClick={() => onEdit(category)}
          >
            <PencilIcon aria-hidden="true" />
          </Button>
          <Button
            variant="ghost"
            size="icon-sm"
            aria-label={`Xoá ${category.name}`}
            className="text-muted-foreground hover:text-destructive"
            onClick={() => onDelete(category)}
          >
            <Trash2Icon aria-hidden="true" />
          </Button>
        </div>
      </div>

      {children.length > 0 ? (
        <ul>
          {children.map((child) => (
            <CategoryNode
              key={child.id}
              category={child}
              depth={depth + 1}
              onAddChild={onAddChild}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          ))}
        </ul>
      ) : null}
    </li>
  )
}

export function AdminCategoriesPage() {
  const { data: categories, isPending, isError, error, refetch } = useCategories()
  const createCategory = useCreateCategory()
  const updateCategory = useUpdateCategory()
  const deleteCategory = useDeleteCategory()

  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<CategoryResponse | null>(null)
  const [form, setForm] = useState<FormState>(EMPTY_FORM)
  const [formError, setFormError] = useState<string | null>(null)
  const [pendingDelete, setPendingDelete] = useState<CategoryResponse | null>(null)

  const flat = flattenCategories(categories ?? [])

  const openCreate = (parent?: CategoryResponse) => {
    setEditing(null)
    setForm({ ...EMPTY_FORM, parentId: parent ? String(parent.id) : "none" })
    setFormError(null)
    setOpen(true)
  }

  const openEdit = (category: CategoryResponse) => {
    setEditing(category)
    setForm({
      name: category.name,
      slug: category.slug,
      description: category.description ?? "",
      // Backend không trả parentId trong response, nên khi sửa mặc định giữ nguyên
      // vị trí hiện tại; chỉ đổi cha khi người dùng chủ động chọn lại.
      parentId: "keep",
      status: category.status,
    })
    setFormError(null)
    setOpen(true)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!form.name.trim() || !form.slug.trim()) {
      setFormError("Tên và slug đều bắt buộc.")
      return
    }
    setFormError(null)

    const parentId =
      form.parentId === "none" ? null : form.parentId === "keep" ? undefined : Number(form.parentId)

    if (editing) {
      updateCategory.mutate(
        {
          id: editing.id,
          payload: {
            name: form.name.trim(),
            slug: form.slug.trim(),
            description: form.description.trim() || undefined,
            status: form.status,
            ...(parentId === undefined ? {} : { parentId }),
          },
        },
        { onSuccess: () => setOpen(false) }
      )
    } else {
      createCategory.mutate(
        {
          name: form.name.trim(),
          slug: form.slug.trim(),
          description: form.description.trim() || undefined,
          parentId: parentId ?? null,
        },
        { onSuccess: () => setOpen(false) }
      )
    }
  }

  const isSaving = createCategory.isPending || updateCategory.isPending

  return (
    <div className="flex flex-col gap-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-heading text-2xl font-semibold tracking-tight">Danh mục</h1>
          <p className="mt-1.5 text-sm text-muted-foreground">
            Cấu trúc cây dùng chung cho toàn sàn — chỉ quản trị viên chỉnh sửa được.
          </p>
        </div>
        <Button onClick={() => openCreate()}>
          <PlusIcon aria-hidden="true" />
          Thêm danh mục
        </Button>
      </header>

      {isPending ? (
        <div className="flex flex-col gap-2">
          {Array.from({ length: 6 }, (_, i) => (
            <Skeleton key={i} className="h-11 w-full" />
          ))}
        </div>
      ) : isError ? (
        <ErrorState error={error} onRetry={() => refetch()} />
      ) : categories && categories.length > 0 ? (
        <div className="rounded-xl border px-4">
          <ul>
            {categories.map((category) => (
              <CategoryNode
                key={category.id}
                category={category}
                depth={0}
                onAddChild={openCreate}
                onEdit={openEdit}
                onDelete={setPendingDelete}
              />
            ))}
          </ul>
        </div>
      ) : (
        <EmptyState
          icon={FolderTreeIcon}
          title="Chưa có danh mục nào"
          description="Tạo danh mục gốc trước, sau đó thêm danh mục con bên trong."
          action={<Button onClick={() => openCreate()}>Tạo danh mục gốc</Button>}
        />
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editing ? "Sửa danh mục" : "Thêm danh mục"}</DialogTitle>
            <DialogDescription>
              Slug được dùng trong URL nên phải là duy nhất trên toàn sàn.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
            <div>
              <Label htmlFor="category-name" className="mb-1.5">
                Tên <span className="text-destructive">*</span>
              </Label>
              <Input
                id="category-name"
                value={form.name}
                onChange={(e) => {
                  const name = e.target.value
                  // Tạo mới thì slug bám theo tên; khi sửa giữ nguyên slug cũ để không phá URL đang chạy.
                  setForm((prev) => ({
                    ...prev,
                    name,
                    slug: editing ? prev.slug : slugify(name),
                  }))
                }}
                placeholder="Thời trang nam"
              />
            </div>

            <div>
              <Label htmlFor="category-slug" className="mb-1.5">
                Slug <span className="text-destructive">*</span>
              </Label>
              <Input
                id="category-slug"
                value={form.slug}
                onChange={(e) => setForm({ ...form, slug: e.target.value })}
                placeholder="thoi-trang-nam"
              />
            </div>

            <div>
              <Label htmlFor="category-parent" className="mb-1.5">
                Danh mục cha
              </Label>
              <Select
                value={form.parentId}
                onValueChange={(value) => setForm({ ...form, parentId: value })}
              >
                <SelectTrigger id="category-parent" className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {editing ? <SelectItem value="keep">Giữ nguyên vị trí hiện tại</SelectItem> : null}
                  <SelectItem value="none">Không có (danh mục gốc)</SelectItem>
                  {flat
                    .filter((category) => category.id !== editing?.id)
                    .map((category) => (
                      <SelectItem key={category.id} value={String(category.id)}>
                        {"— ".repeat(category.depth)}
                        {category.name}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>

            {editing ? (
              <div>
                <Label htmlFor="category-status" className="mb-1.5">
                  Trạng thái
                </Label>
                <Select
                  value={form.status}
                  onValueChange={(value: "ACTIVE" | "INACTIVE") =>
                    setForm({ ...form, status: value })
                  }
                >
                  <SelectTrigger id="category-status" className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ACTIVE">Hiển thị</SelectItem>
                    <SelectItem value="INACTIVE">Ẩn</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            ) : null}

            <div>
              <Label htmlFor="category-description" className="mb-1.5">
                Mô tả
              </Label>
              <Textarea
                id="category-description"
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                className="min-h-20"
              />
            </div>

            {formError ? (
              <p role="alert" className="text-sm text-destructive">
                {formError}
              </p>
            ) : null}

            <div className="flex justify-end gap-2">
              <Button type="button" variant="ghost" onClick={() => setOpen(false)}>
                Huỷ
              </Button>
              <Button type="submit" disabled={isSaving}>
                {isSaving ? "Đang lưu..." : editing ? "Lưu thay đổi" : "Tạo danh mục"}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      <AlertDialog
        open={Boolean(pendingDelete)}
        onOpenChange={(isOpen) => !isOpen && setPendingDelete(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Xoá danh mục &quot;{pendingDelete?.name}&quot;?</AlertDialogTitle>
            <AlertDialogDescription>
              Backend sẽ từ chối nếu danh mục còn danh mục con hoặc còn sản phẩm bên trong.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Huỷ</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                if (pendingDelete) deleteCategory.mutate(pendingDelete.id)
                setPendingDelete(null)
              }}
            >
              Xoá danh mục
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
