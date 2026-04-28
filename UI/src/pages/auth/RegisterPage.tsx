import { Link, useNavigate } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Loader2 } from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { useRegister } from "@/hooks/useAuth"
import { getApiErrorMessage } from "@/lib/errors"

const schema = z.object({
  fullName: z
    .string()
    .min(1, "Vui lòng nhập họ tên")
    .max(100, "Họ tên không được vượt quá 100 ký tự"),
  email: z.string().min(1, "Vui lòng nhập email").email("Email không hợp lệ"),
  password: z
    .string()
    .min(6, "Mật khẩu tối thiểu 6 ký tự")
    .max(72, "Mật khẩu không được vượt quá 72 ký tự"),
  phone: z.string().max(20, "Số điện thoại không hợp lệ").optional(),
  address: z.string().max(255, "Địa chỉ không được vượt quá 255 ký tự").optional(),
})

type FormValues = z.infer<typeof schema>

export function RegisterPage() {
  const navigate = useNavigate()
  const register = useRegister()

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { fullName: "", email: "", password: "", phone: "", address: "" },
  })

  const onSubmit = (values: FormValues) => {
    const payload = {
      fullName: values.fullName,
      email: values.email,
      password: values.password,
      ...(values.phone ? { phone: values.phone } : {}),
      ...(values.address ? { address: values.address } : {}),
    }
    register.mutate(payload, {
      onSuccess: () => {
        toast.success("Đăng ký thành công! Vui lòng đăng nhập để tiếp tục.")
        navigate("/login", { replace: true })
      },
      onError: (err) => {
        toast.error(getApiErrorMessage(err, "Đăng ký thất bại"))
      },
    })
  }

  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle className="text-2xl">Tạo tài khoản</CardTitle>
        <CardDescription>Bắt đầu mua sắm hoặc bán hàng trong vài phút.</CardDescription>
      </CardHeader>
      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4" noValidate>
            <FormField
              control={form.control}
              name="fullName"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Họ và tên</FormLabel>
                  <FormControl>
                    <Input autoComplete="name" placeholder="Nguyễn Văn A" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Email</FormLabel>
                  <FormControl>
                    <Input
                      type="email"
                      autoComplete="email"
                      placeholder="ban@example.com"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="password"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Mật khẩu</FormLabel>
                  <FormControl>
                    <Input type="password" autoComplete="new-password" {...field} />
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
                  <FormLabel>
                    Số điện thoại{" "}
                    <span className="font-normal text-muted-foreground">(không bắt buộc)</span>
                  </FormLabel>
                  <FormControl>
                    <Input type="tel" autoComplete="tel" placeholder="0901234567" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="address"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>
                    Địa chỉ{" "}
                    <span className="font-normal text-muted-foreground">(không bắt buộc)</span>
                  </FormLabel>
                  <FormControl>
                    <Input autoComplete="street-address" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <Button type="submit" className="w-full" disabled={register.isPending}>
              {register.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" aria-hidden="true" />}
              Tạo tài khoản
            </Button>
          </form>
        </Form>

        <p className="mt-6 text-center text-sm text-muted-foreground">
          Đã có tài khoản?{" "}
          <Link to="/login" className="font-medium text-foreground underline-offset-4 hover:underline">
            Đăng nhập
          </Link>
        </p>
      </CardContent>
    </Card>
  )
}
