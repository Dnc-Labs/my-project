import { apiClient, unwrap } from "./client"
import type { BaseResponse, UpdateUserRequest, UserResponse } from "@/types/api"

export async function getUser(id: number): Promise<UserResponse> {
  const res = await apiClient.get<BaseResponse<UserResponse>>(`/users/${id}`)
  return unwrap(res)
}

export async function updateUser(id: number, payload: UpdateUserRequest): Promise<UserResponse> {
  const res = await apiClient.put<BaseResponse<UserResponse>>(`/users/${id}`, payload)
  return unwrap(res)
}
