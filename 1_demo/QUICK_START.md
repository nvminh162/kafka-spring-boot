# 🚀 Quick Start Guide

## Bước 1: Kiểm tra Kafka đang chạy

Kafka của bạn đang chạy tại:
- `localhost:9092` (Zookeeper mode) hoặc
- `localhost:9093` (KRaft mode - như trong hình của bạn)

Nếu Kafka chạy ở port khác, sửa file `application.yaml`:
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9093  # Thay đổi nếu cần
```

## Bước 2: Chạy ứng dụng

```bash
cd demo
./mvnw spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

## Bước 3: Test ngay!

### Test 1: Gửi message không có key
```bash
curl -X POST http://localhost:8080/api/messages/no-key \
  -H "Content-Type: application/json" \
  -d "{\"content\": \"Hello Kafka!\"}"
```

**Xem logs** để thấy:
- Partition nào nhận message
- Offset của message

### Test 2: Gửi message có key
```bash
curl -X POST "http://localhost:8080/api/messages/with-key?key=user123" \
  -H "Content-Type: application/json" \
  -d "{\"content\": \"Message with key\"}"
```

**Xem logs** để thấy:
- Cùng key → cùng partition

### Test 3: Gửi nhiều messages
```bash
curl -X POST http://localhost:8080/api/messages/batch
```

**Xem logs** để thấy:
- Messages được phân phối vào các partitions khác nhau

### Test 4: Gửi messages cùng key
```bash
curl -X POST "http://localhost:8080/api/messages/same-key?key=user123"
```

**Xem logs** để thấy:
- Tất cả messages cùng key → cùng partition

## 📊 Xem kết quả trong logs

Bạn sẽ thấy output như này:

```
📨 RECEIVED MESSAGE
   Topic:     demo-topic
   Partition: 1  ← Message này nằm ở partition nào
   Offset:    3  ← Vị trí của message trong partition
   Key:       user123

💡 GIẢI THÍCH:
   📁 Topic: 'demo-topic'
      → Giống như một thư mục chứa tất cả messages
   
   📦 Partition: 1
      → Topic được chia thành nhiều partitions
      → Message này nằm ở partition 1
   
   🔢 Offset: 3
      → ID duy nhất của message trong partition này
      → Offset 3 là message thứ 4 trong partition 1
```

## 🎯 Những gì bạn sẽ học được

1. **Topic**: Thư mục chứa messages
2. **Partition**: Topic được chia thành 3 partitions (0, 1, 2)
3. **Offset**: ID của message trong partition (0, 1, 2, 3...)
4. **Key**: Cùng key → cùng partition
5. **Không có Key**: Phân phối ngẫu nhiên

## ❓ Troubleshooting

### Lỗi: Connection refused
- Kiểm tra Kafka đang chạy: `netstat -an | findstr 9092` (Windows)
- Sửa port trong `application.yaml` nếu cần

### Không thấy messages
- Kiểm tra logs của ứng dụng
- Đảm bảo Consumer đang chạy (check logs)

### Topic chưa được tạo
- Topic sẽ tự động được tạo khi app start
- Hoặc tạo thủ công bằng Kafka CLI
