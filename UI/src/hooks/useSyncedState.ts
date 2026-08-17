import { useState } from "react"

/**
 * State cục bộ nhưng tự nhận giá trị mới khi nguồn bên ngoài đổi (ví dụ query param
 * thay đổi vì người dùng bấm link khác hoặc nút back).
 *
 * Dùng cách "điều chỉnh state khi render" thay vì useEffect + setState: React xử lý
 * ngay trong lượt render đó nên không tốn thêm một lượt render thừa, và không có
 * khoảnh khắc UI hiển thị giá trị cũ.
 */
export function useSyncedState<T>(external: T): [T, (value: T) => void] {
  const [value, setValue] = useState(external)
  const [previous, setPrevious] = useState(external)

  if (!Object.is(previous, external)) {
    setPrevious(external)
    setValue(external)
  }

  return [value, setValue]
}
