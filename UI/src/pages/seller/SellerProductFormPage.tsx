import { useEffect } from "react"
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { ArrowLeftIcon, Loader2Icon, WandSparklesIcon } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Textarea } from "@/components/ui/textarea"
import { ErrorState } from "@/components/common/ErrorState"
import { ProductImageManager } from "@/components/seller/ProductImageManager"
import { ProductVariantManager } from "@/components/seller/ProductVariantManager"
import { useCreateProduct, useProduct, useUpdateProduct } from "@/hooks/useProducts"
import { useFlatCategories } from "@/hooks/useCategories"
import { slugify } from "@/lib/format"

/**
 * Mọi field giữ kiểu string đúng như giá trị thật của <input>/<Select>;
 * việc đổi sang number dồn vào lúc submit. Cách này tránh kiểu `unknown`
 * mà z.coerce sinh ra ở phía input của form.
 */
const schema = z.object({
  name: z.string().trim().min(1, "Vui lòng nhập tên sản phẩm"),
  slug: z
    .string()
    .trim()
    .min(1, "Slug bắt buộc")
    .regex(/^[a-z0-9-]+$/, "Slug chỉ gồm chữ thường, số và dấu gạch ngang"),
  sku: z.string().trim().min(1, "SKU bắt buộc"),
  price: z
    .string()
    .trim()
    .min(1, "Vui lòng nhập giá")
    .refine((value) => Number(value) > 0, "Giá phải lớn hơn 0"),
  stock: z
    .string()
    .trim()
    .min(1, "Vui lòng nhập tồn kho")
    .refine(
      (value) => Number.isInteger(Number(value)) && Number(value) >= 0,
      "Tồn kho phải là số nguyên không âm"
    ),
  description: z.string().trim().optional(),
  categoryId: z.string().min(1, "Vui lòng chọn danh mục"),
  status: z.enum(["ACTIVE", "INACTIVE"]),
})

type FormValues = z.infer<typeof schema>

export function SellerProductFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const isEdit = Boolean(id)
  const productId = Number(id)

  const product = useProduct(isEdit ? productId : undefined)
  const { flat: categories, isPending: loadingCategories } = useFlatCategories()
  const createProduct = useCreateProduct()
  const updateProduct = useUpdateProduct()

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: "",
      slug: "",
      sku: "",
      price: "",
      stock: "0",
      description: "",
      categoryId: "",
      status: "ACTIVE",
    },
  })

  useEffect(() => {
    if (!product.data) return
    form.reset({
      name: product.data.name,
      slug: product.data.slug,
      sku: product.data.sku,
      price: String(product.data.price),
      stock: String(product.data.stock),
      description: product.data.description ?? "",
      categoryId: String(product.data.categoryId),
      status: product.data.status,
    })
  }, [product.data, form])

  const fillSlugFromName = () => {
    const name = form.getValues("name")
    if (!name) return
    form.setValue("slug", slugify(name), { shouldDirty: true, shouldValidate: true })
  }

  const onSubmit = (values: FormValues) => {
    const common = {
      name: values.name.trim(),
      slug: values.slug.trim(),
      sku: values.sku.trim(),
      price: Number(values.price),
      description: values.description?.trim() || undefined,
      stock: Number(values.stock),
      categoryId: Number(values.categoryId),
    }

    if (isEdit) {
      // PUT ghi đè toàn phần: backend đặt default stock = 0 cho field vắng mặt,
      // nên phải gửi đủ mọi field kể cả những field không đổi.
      updateProduct.mutate(
        { id: productId, payload: { ...common, status: values.status } },
        { onSuccess: () => navigate("/seller/products") }
      )
    } else {
      createProduct.mutate(
        common,
        // Sau khi tạo, đi thẳng sang trang sửa để seller upload ảnh và thêm biến thể.
        { onSuccess: (created) => navigate(`/seller/products/${created.id}/edit?tab=media`) }
      )
    }
  }

  const isSaving = createProduct.isPending || updateProduct.isPending

  if (isEdit && product.isPending) {
    return (
      <div className="flex flex-col gap-4">
        <Skeleton className="h-8 w-56" />
        <Skeleton className="h-96 w-full rounded-xl" />
      </div>
    )
  }

  if (isEdit && product.isError) {
    return <ErrorState error={product.error} onRetry={() => product.refetch()} />
  }

  const formSection = (
    <Card>
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5" noValidate>
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>
                    Tên sản phẩm <span className="text-destructive">*</span>
                  </FormLabel>
                  <FormControl>
                    <Input placeholder="Áo thun nam cổ tròn" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid gap-5 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="slug"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Slug <span className="text-destructive">*</span>
                    </FormLabel>
                    <div className="flex gap-2">
                      <FormControl>
                        <Input placeholder="ao-thun-nam-co-tron" {...field} />
                      </FormControl>
                      <Button
                        type="button"
                        variant="outline"
                        size="icon"
                        onClick={fillSlugFromName}
                        aria-label="Tạo slug từ tên sản phẩm"
                      >
                        <WandSparklesIcon aria-hidden="true" />
                      </Button>
                    </div>
                    <FormDescription>Dùng cho URL thân thiện, phải là duy nhất.</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="sku"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      SKU <span className="text-destructive">*</span>
                    </FormLabel>
                    <FormControl>
                      <Input placeholder="AO-THUN-001" {...field} />
                    </FormControl>
                    <FormDescription>Mã kho nội bộ, không trùng với sản phẩm khác.</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="grid gap-5 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="price"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Giá (VND) <span className="text-destructive">*</span>
                    </FormLabel>
                    <FormControl>
                      <Input type="number" inputMode="numeric" min={1} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="stock"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Tồn kho <span className="text-destructive">*</span>
                    </FormLabel>
                    <FormControl>
                      <Input type="number" inputMode="numeric" min={0} {...field} />
                    </FormControl>
                    <FormDescription>Bỏ qua nếu sản phẩm bán theo biến thể.</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="grid gap-5 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="categoryId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Danh mục <span className="text-destructive">*</span>
                    </FormLabel>
                    <Select
                      value={field.value || undefined}
                      onValueChange={field.onChange}
                      disabled={loadingCategories}
                    >
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue placeholder="Chọn danh mục" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {categories.map((category) => (
                          <SelectItem key={category.id} value={String(category.id)}>
                            {"— ".repeat(category.depth)}
                            {category.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {isEdit ? (
                <FormField
                  control={form.control}
                  name="status"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Trạng thái</FormLabel>
                      <Select value={field.value} onValueChange={field.onChange}>
                        <FormControl>
                          <SelectTrigger className="w-full">
                            <SelectValue />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="ACTIVE">Đang bán</SelectItem>
                          <SelectItem value="INACTIVE">Ngừng bán</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormDescription>Sản phẩm ngừng bán sẽ ẩn khỏi trang mua sắm.</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              ) : null}
            </div>

            <FormField
              control={form.control}
              name="description"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Mô tả</FormLabel>
                  <FormControl>
                    <Textarea
                      className="min-h-32"
                      placeholder="Chất liệu, kích thước, hướng dẫn bảo quản..."
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex gap-2">
              <Button type="submit" disabled={isSaving}>
                {isSaving ? <Loader2Icon className="animate-spin" aria-hidden="true" /> : null}
                {isEdit ? "Lưu thay đổi" : "Tạo sản phẩm"}
              </Button>
              <Button type="button" variant="ghost" asChild>
                <Link to="/seller/products">Huỷ</Link>
              </Button>
            </div>
          </form>
        </Form>
      </CardContent>
    </Card>
  )

  return (
    <div className="flex flex-col gap-6">
      <div>
        <Button variant="ghost" size="sm" asChild className="mb-3 -ml-2">
          <Link to="/seller/products">
            <ArrowLeftIcon aria-hidden="true" />
            Danh sách sản phẩm
          </Link>
        </Button>
        <h1 className="font-heading text-2xl font-semibold tracking-tight">
          {isEdit ? product.data?.name ?? "Cập nhật sản phẩm" : "Thêm sản phẩm mới"}
        </h1>
        <p className="mt-1.5 text-sm text-muted-foreground">
          {isEdit
            ? "Chỉnh sửa thông tin, quản lý ảnh và biến thể của sản phẩm."
            : "Điền thông tin cơ bản; ảnh và biến thể thêm được ngay sau khi tạo."}
        </p>
      </div>

      {isEdit ? (
        <Tabs
          value={searchParams.get("tab") ?? "info"}
          onValueChange={(tab) =>
            setSearchParams(tab === "info" ? {} : { tab }, { replace: true })
          }
        >
          <TabsList>
            <TabsTrigger value="info">Thông tin</TabsTrigger>
            <TabsTrigger value="media">Ảnh</TabsTrigger>
            <TabsTrigger value="variants">Biến thể</TabsTrigger>
          </TabsList>
          <TabsContent value="info">{formSection}</TabsContent>
          <TabsContent value="media">
            <ProductImageManager productId={productId} />
          </TabsContent>
          <TabsContent value="variants">
            <ProductVariantManager productId={productId} />
          </TabsContent>
        </Tabs>
      ) : (
        formSection
      )}
    </div>
  )
}
