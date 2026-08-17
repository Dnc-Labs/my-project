import { useEffect } from "react"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Loader2Icon } from "lucide-react"
import { toast } from "sonner"
import { Badge } from "@/components/ui/badge"
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
import { Skeleton } from "@/components/ui/skeleton"
import { ErrorState } from "@/components/common/ErrorState"
import { useMe, useUpdateProfile } from "@/hooks/useAuth"
import { getApiErrorMessage } from "@/lib/errors"

const schema = z.object({
  fullName: z.string().trim().min(1, "Vui lòng nhập họ tên"),
  phone: z
    .string()
    .trim()
    .regex(/^(0\d{9,10})?$/, "Số điện thoại gồm 10-11 chữ số, bắt đầu bằng 0")
    .optional(),
  address: z.string().trim().optional(),
})

type FormValues = z.infer<typeof schema>

export function ProfilePage() {
  const { data: me, isPending, isError, error, refetch } = useMe()
  const updateProfile = useUpdateProfile()

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { fullName: "", phone: "", address: "" },
  })

  // Form chỉ nhận giá trị server một lần khi data về; reset theo `me` để tránh
  // ghi đè những gì người dùng đang gõ mỗi lần component re-render.
  useEffect(() => {
    if (!me) return
    form.reset({
      fullName: me.fullName ?? "",
      phone: me.phone ?? "",
      address: me.address ?? "",
    })
  }, [me, form])

  if (isPending) {
    return (
      <div className="container-page max-w-2xl py-10">
        <Skeleton className="mb-6 h-9 w-40" />
        <Skeleton className="h-96 w-full rounded-xl" />
      </div>
    )
  }

  if (isError || !me) {
    return (
      <div className="container-page max-w-2xl py-10">
        <ErrorState error={error} onRetry={() => refetch()} />
      </div>
    )
  }

  const onSubmit = (values: FormValues) => {
    updateProfile.mutate(
      {
        fullName: values.fullName,
        phone: values.phone || undefined,
        address: values.address || undefined,
      },
      {
        onSuccess: () => toast.success("Đã cập nhật hồ sơ"),
        onError: (err) => toast.error(getApiErrorMessage(err)),
      }
    )
  }

  return (
    <div className="container-page max-w-2xl py-8 md:py-10">
      <header className="mb-6">
        <h1 className="font-heading text-3xl font-semibold tracking-tight">Hồ sơ của tôi</h1>
        <p className="mt-2 text-sm text-muted-foreground">
          Thông tin này được dùng để điền sẵn khi bạn đặt hàng.
        </p>
      </header>

      <Card>
        <CardContent className="flex flex-col gap-6">
          <div className="flex flex-wrap items-center gap-3">
            <div className="min-w-0 flex-1">
              <p className="truncate font-medium">{me.email}</p>
              <p className="text-xs text-muted-foreground">Email không thể thay đổi tại đây.</p>
            </div>
            <Badge variant="secondary">{me.role}</Badge>
            <Badge variant="outline">{me.status}</Badge>
          </div>

          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5" noValidate>
              <FormField
                control={form.control}
                name="fullName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      Họ và tên <span className="text-destructive">*</span>
                    </FormLabel>
                    <FormControl>
                      <Input autoComplete="name" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="phone"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Số điện thoại</FormLabel>
                    <FormControl>
                      <Input type="tel" inputMode="tel" autoComplete="tel" {...field} />
                    </FormControl>
                    <FormDescription>Người bán dùng số này để liên hệ khi giao hàng.</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Địa chỉ mặc định</FormLabel>
                    <FormControl>
                      <Input autoComplete="street-address" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <Button type="submit" disabled={updateProfile.isPending || !form.formState.isDirty}>
                {updateProfile.isPending ? (
                  <Loader2Icon className="animate-spin" aria-hidden="true" />
                ) : null}
                Lưu thay đổi
              </Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  )
}
