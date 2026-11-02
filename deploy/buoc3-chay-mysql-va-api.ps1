# =============================================================================
# BƯỚC 3 — Dựng MySQL + chạy API trong cùng một mạng Docker
#
# ⚠️  BẠN TỰ CHẠY SCRIPT NÀY. Mentor không được phép tự ghi vào database
#     (tạo schema + Flyway migration đều là thao tác GHI).
#
# Script này làm gì:
#   1. Tạo một Docker network riêng tên "ecom-net"
#   2. Chạy MySQL 8.0 trong network đó, dữ liệu lưu vào volume "ecom-mysql-data"
#      -> MySQL tự tạo schema rỗng tên "ecommerce" (biến MYSQL_DATABASE)
#   3. Chờ MySQL sẵn sàng
#   4. Chạy API trong cùng network, trỏ vào mysql:3306 bằng biến môi trường
#      -> Flyway của Spring Boot sẽ TỰ CHẠY MIGRATION, tạo bảng trong schema đó
#
# Ảnh hưởng tới dữ liệu:
#   - Tạo MỚI schema "ecommerce" trong một MySQL container MỚI (volume mới).
#   - KHÔNG đụng tới MySQL nào đang chạy sẵn trên máy bạn
#     (container này không publish cổng ra host, nên không đụng cổng 3306/3309).
#   - Flyway tạo bảng theo các file migration trong api/src/main/resources/db/migration
#
# Cách ROLLBACK (xoá sạch, về trạng thái ban đầu):
#   docker rm -f ecom-api ecom-mysql
#   docker volume rm ecom-mysql-data      # <- XOÁ VĨNH VIỄN dữ liệu DB
#   docker network rm ecom-net
# =============================================================================

$ErrorActionPreference = "Stop"

Write-Host "=== Dọn dẹp lần chạy trước (nếu có) ===" -ForegroundColor Cyan
docker rm -f ecom-api ecom-mysql 2>$null | Out-Null
docker network rm ecom-net 2>$null | Out-Null

# -----------------------------------------------------------------------------
# ① MẠNG RIÊNG
# Bài 1.5: bridge mặc định KHÔNG có DNS nội bộ -> container không gọi nhau bằng tên.
# Mạng tự tạo thì có, nhờ DNS server của Docker ở 127.0.0.11.
# -----------------------------------------------------------------------------
Write-Host "`n=== ① Tạo mạng ecom-net ===" -ForegroundColor Cyan
docker network create ecom-net

# -----------------------------------------------------------------------------
# ② MYSQL
#   --network ecom-net        -> vào cùng mạng với API
#   -v ecom-mysql-data:...    -> VOLUME, dữ liệu sống qua docker rm (bài 1.4)
#   KHÔNG có -p               -> cố tình không mở cổng ra host.
#                                API gọi qua mạng nội bộ, host không cần thấy.
#                                Bớt một cửa vào = bớt một rủi ro.
# -----------------------------------------------------------------------------
Write-Host "`n=== ② Chạy MySQL 8.0 ===" -ForegroundColor Cyan
docker run -d --name ecom-mysql `
  --network ecom-net `
  -e MYSQL_ROOT_PASSWORD=admin `
  -e MYSQL_DATABASE=ecommerce `
  -v ecom-mysql-data:/var/lib/mysql `
  mysql:8.0

# -----------------------------------------------------------------------------
# ③ CHỜ MYSQL SẴN SÀNG
# MySQL lần đầu phải khởi tạo data directory -> mất ~20-40 giây.
# Chạy API quá sớm thì Flyway sẽ báo "Connection refused" y như bước 2.
# -----------------------------------------------------------------------------
Write-Host "`n=== ③ Chờ MySQL sẵn sàng (lần đầu ~30s) ===" -ForegroundColor Cyan
$ready = $false
foreach ($i in 1..40) {
  docker exec ecom-mysql mysqladmin ping -uroot -padmin 2>$null | Out-Null
  if ($?) { Write-Host "  MySQL SẴN SÀNG sau ~$($i*3)s" -ForegroundColor Green; $ready = $true; break }
  Start-Sleep 3
}
if (-not $ready) { Write-Host "  MySQL chưa sẵn sàng. Xem: docker logs ecom-mysql" -ForegroundColor Red; exit 1 }

# -----------------------------------------------------------------------------
# ④ API
# KHÔNG sửa application.yaml. Ghi đè bằng biến môi trường (relaxed binding):
#   spring.datasource.url  ->  SPRING_DATASOURCE_URL
#
#   ecom-mysql = TÊN CONTAINER, phải khớp CHÍNH XÁC với --name ở bước ②
#                DNS của Docker không suy đoán rút gọn: "mysql" sẽ KHÔNG phân giải được
#   3306       = cổng THẬT bên trong container MySQL (không phải 3309)
#
# MAIL_USERNAME / MAIL_PASSWORD: application.yaml yêu cầu 2 biến này.
# Đặt giá trị giả để app khởi động được; chức năng gửi mail sẽ không dùng tới.
# -----------------------------------------------------------------------------
Write-Host "`n=== ④ Chạy API ===" -ForegroundColor Cyan
docker run -d --name ecom-api `
  --network ecom-net `
  -p 8080:8080 `
  -e SPRING_DATASOURCE_URL="jdbc:mysql://ecom-mysql:3306/ecommerce" `
  -e SPRING_DATASOURCE_USERNAME=root `
  -e SPRING_DATASOURCE_PASSWORD=admin `
  -e MAIL_USERNAME=noreply@localhost `
  -e MAIL_PASSWORD=khong-dung `
  ecom-api:buoc1

Write-Host "`n=== ⑤ Theo dõi API khởi động (Ctrl+C để thoát) ===" -ForegroundColor Cyan
Write-Host "  Chờ dòng: 'Started ApiApplication in ... seconds'`n" -ForegroundColor Yellow
docker logs -f ecom-api
