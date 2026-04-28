import { AxiosError } from "axios"
import type { BaseResponse } from "@/types/api"

export function getApiErrorMessage(err: unknown, fallback = "Đã có lỗi xảy ra, vui lòng thử lại."): string {
  if (err instanceof AxiosError) {
    const data = err.response?.data as Partial<BaseResponse<unknown>> | undefined
    if (data?.message) return data.message
    if (err.message) return err.message
  }
  if (err instanceof Error) return err.message
  return fallback
}
