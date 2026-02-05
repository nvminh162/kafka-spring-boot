# Hướng dẫn lấy IP Address của máy tính

File này hướng dẫn cách lấy IP Address của máy tính để cấu hình cho Kafka và các services.

## 📋 Mục đích

IP Address được sử dụng trong file `local.env` để cấu hình `HOST_IP_ADDRESS`, giúp:
- Kafka brokers có thể được kết nối từ bên ngoài Docker network
- Các services có thể kết nối với Kafka từ host machine
- Client từ máy khác có thể kết nối đến Kafka cluster

## 🔧 Cách 1: Sử dụng PowerShell Script (Khuyến nghị)

### Bước 1: Chạy script tự động

Mở PowerShell tại thư mục project và chạy:

```powershell
.\get-host-ip.ps1
```

Script sẽ tự động:
- Tìm IP Address của máy tính (loại trừ loopback và Docker network)
- Hiển thị IP Address tìm được
- Đưa ra hướng dẫn cập nhật file `local.env`

### Bước 2: Cập nhật file local.env

Sau khi chạy script, copy IP Address được hiển thị và cập nhật vào file `local.env`:

```env
HOST_IP_ADDRESS=192.168.1.7
```

**Lưu ý:** Thay `192.168.1.7` bằng IP Address thực tế của máy bạn.

## 🔧 Cách 2: Sử dụng Command Prompt (CMD)

### Bước 1: Mở Command Prompt

Nhấn `Win + R`, gõ `cmd` và nhấn Enter.

### Bước 2: Chạy lệnh ipconfig

```cmd
ipconfig
```

### Bước 3: Tìm IPv4 Address

Tìm dòng **IPv4 Address** trong kết quả. Thường sẽ có nhiều adapter, chọn IP của adapter chính (không phải Docker, không phải loopback):

```
Ethernet adapter Ethernet:
   IPv4 Address. . . . . . . . . . . : 192.168.1.7
```

Hoặc nếu dùng WiFi:

```
Wireless LAN adapter Wi-Fi:
   IPv4 Address. . . . . . . . . . . : 192.168.1.7
```

### Bước 4: Cập nhật file local.env

Copy IP Address và cập nhật vào file `local.env`:

```env
HOST_IP_ADDRESS=192.168.1.7
```

## 🔧 Cách 3: Sử dụng PowerShell trực tiếp

### Bước 1: Mở PowerShell

Nhấn `Win + X` và chọn "Windows PowerShell" hoặc "Terminal".

### Bước 2: Chạy lệnh

```powershell
Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -notlike '127.*' -and 
    $_.IPAddress -notlike '169.254.*' -and
    $_.IPAddress -notlike '172.*' -and
    $_.InterfaceAlias -notlike '*Loopback*' -and
    $_.InterfaceAlias -notlike '*Docker*'
} | Select-Object IPAddress, InterfaceAlias | Format-Table
```

### Bước 3: Chọn IP Address phù hợp

Kết quả sẽ hiển thị danh sách IP Address. Chọn IP của adapter chính (thường là Ethernet hoặc Wi-Fi, không phải Docker).

### Bước 4: Cập nhật file local.env

Copy IP Address và cập nhật vào file `local.env`.

## 🔧 Cách 4: Sử dụng Settings (Windows 10/11)

### Bước 1: Mở Settings

Nhấn `Win + I` để mở Settings.

### Bước 2: Vào Network & Internet

1. Chọn **Network & Internet**
2. Chọn **Ethernet** hoặc **Wi-Fi** (tùy theo kết nối của bạn)

### Bước 3: Xem IP Address

Scroll xuống phần **Properties**, tìm **IPv4 address**.

### Bước 4: Cập nhật file local.env

Copy IP Address và cập nhật vào file `local.env`.

## 📝 Cách cập nhật file local.env

### Phương pháp 1: Sử dụng Editor

1. Mở file `local.env` bằng text editor (Notepad, VS Code, etc.)
2. Cập nhật dòng:
   ```env
   HOST_IP_ADDRESS=YOUR_IP_ADDRESS
   ```
3. Lưu file

### Phương pháp 2: Sử dụng PowerShell

```powershell
# Lấy IP Address
$ip = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object {
    $_.IPAddress -notlike '127.*' -and 
    $_.IPAddress -notlike '169.254.*' -and
    $_.IPAddress -notlike '172.*' -and
    $_.InterfaceAlias -notlike '*Loopback*' -and
    $_.InterfaceAlias -notlike '*Docker*'
} | Select-Object -First 1).IPAddress

# Cập nhật file local.env
Set-Content -Path "local.env" -Value "HOST_IP_ADDRESS=$ip"
```

## ⚠️ Lưu ý quan trọng

1. **Loại trừ các IP không phù hợp:**
   - `127.0.0.1` hoặc `127.*` - Loopback address
   - `169.254.*` - APIPA (Automatic Private IP Addressing)
   - `172.*` - Docker network (thường là `172.17.0.1`, `172.28.0.1`, etc.)
   - `192.168.*` - Private network (có thể dùng nếu là IP chính)

2. **Chọn IP phù hợp:**
   - Nếu máy trong mạng LAN: Chọn IP private (ví dụ: `192.168.1.7`)
   - Nếu máy có IP public: Có thể dùng IP public
   - Thường chọn IP của adapter Ethernet hoặc Wi-Fi chính

3. **Kiểm tra IP sau khi thay đổi:**
   - Nếu thay đổi network (chuyển từ Ethernet sang Wi-Fi hoặc ngược lại)
   - Nếu IP được cấp động (DHCP) và có thể thay đổi
   - Cần cập nhật lại `local.env` và restart Docker containers

## 🔄 Sau khi cập nhật IP

Sau khi cập nhật `HOST_IP_ADDRESS` trong file `local.env`, cần restart Docker containers:

```bash
docker-compose down
docker-compose up -d
```

## 🧪 Kiểm tra cấu hình

Sau khi restart, kiểm tra xem Kafka có chạy đúng không:

```bash
docker ps
```

Kiểm tra logs của Kafka:

```bash
docker logs kafka-spring-boot-kafka-1-1
```

## 📚 Tài liệu tham khảo

- [Docker Networking](https://docs.docker.com/network/)
- [Kafka Configuration](https://kafka.apache.org/documentation/#configuration)
- [Windows Network Commands](https://docs.microsoft.com/en-us/windows-server/administration/windows-commands/ipconfig)

## ❓ Troubleshooting

### Vấn đề: Không tìm thấy IP Address

**Giải pháp:**
- Kiểm tra kết nối mạng
- Đảm bảo adapter network đang hoạt động
- Thử các cách khác trong hướng dẫn

### Vấn đề: IP Address thay đổi thường xuyên

**Giải pháp:**
- Cấu hình static IP trong Windows
- Hoặc tạo script tự động cập nhật IP mỗi khi khởi động

### Vấn đề: Không kết nối được Kafka từ máy khác

**Giải pháp:**
- Kiểm tra firewall Windows
- Đảm bảo ports 9092, 9093, 9094 đã được mở
- Kiểm tra IP Address trong `local.env` có đúng không

---

**Tác giả:** Generated for Kafka Spring Boot Project  
**Ngày cập nhật:** 2026-02-05
