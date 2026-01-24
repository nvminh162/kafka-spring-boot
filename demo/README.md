# Kafka Demo - Hiểu về Topic, Partition, Offset

Demo ứng dụng đơn giản để hiểu rõ các khái niệm cơ bản của Kafka:
- **Topic**: Thư mục chứa messages
- **Partition**: Chia topic thành nhiều phần
- **Offset**: ID của message trong partition

---

## 🚀 Cách chạy

### 1. Đảm bảo Kafka đang chạy

Kafka của bạn đang chạy tại `localhost:9092` (hoặc `localhost:9093` nếu dùng KRaft mode).

### 2. Chạy ứng dụng

```bash
cd demo
./mvnw spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

### 3. Test các API

---

## 📚 Các khái niệm Kafka

### 1. **Topic** (Chủ đề)

**Topic là gì?**
- Giống như một **thư mục** hoặc **bảng trong database**
- Chứa tất cả các messages/events cùng loại
- Ví dụ: `demo-topic`, `notification`, `statistic`

**Trong code:**
```java
// Tạo topic "demo-topic" với 3 partitions
@Bean
public NewTopic demoTopic() {
    return TopicBuilder.name("demo-topic")
            .partitions(3)  // Chia thành 3 phần
            .replicas(1)
            .build();
}
```

**Ví dụ thực tế:**
- Topic `orders`: Chứa tất cả events về đơn hàng
- Topic `notifications`: Chứa tất cả thông báo
- Topic `user-events`: Chứa tất cả hành động của user

---

### 2. **Partition** (Phân vùng)

**Partition là gì?**
- Topic được **chia thành nhiều partitions** (phân vùng)
- Mỗi partition là một "ngăn kéo" riêng biệt
- Partitions được phân phối trên nhiều brokers khác nhau

**Tại sao cần partitions?**
1. **Scalability**: Phân tán dữ liệu trên nhiều brokers
2. **Parallelism**: Nhiều consumers có thể đọc song song
3. **Throughput**: Xử lý nhiều messages cùng lúc

**Ví dụ:**
```
Topic "demo-topic" có 3 partitions:

┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Partition 0 │  │ Partition 1 │  │ Partition 2 │
│             │  │             │  │             │
│ Offset: 0   │  │ Offset: 0   │  │ Offset: 0   │
│ Offset: 1   │  │ Offset: 1   │  │ Offset: 1   │
│ Offset: 2   │  │ Offset: 2   │  │ Offset: 2   │
│ ...         │  │ ...         │  │ ...         │
└─────────────┘  └─────────────┘  └─────────────┘
```

**Message được gửi vào partition nào?**

#### A. **Không có Key** → Phân phối ngẫu nhiên (Round-robin)
```java
// Gửi message không có key
kafkaTemplate.send("demo-topic", message);
// → Kafka tự chọn partition 0, 1, hoặc 2 (ngẫu nhiên)
```

#### B. **Có Key** → Cùng key → Cùng partition
```java
// Gửi message có key
kafkaTemplate.send("demo-topic", "user123", message);
// → Kafka hash key "user123" → chọn partition cố định
// → Tất cả messages với key "user123" → cùng partition
```

**Tại sao cùng key → cùng partition?**
- Đảm bảo **thứ tự** (ordering) của messages
- Messages với cùng key được xử lý theo thứ tự

---

### 3. **Offset** (Vị trí)

**Offset là gì?**
- **ID duy nhất** của message trong partition
- Là **số thứ tự** của message trong partition (0, 1, 2, 3, ...)
- Offset **không bao giờ thay đổi** sau khi được gán

**Ví dụ:**
```
Partition 0:
┌─────────┬─────────┬─────────┬─────────┐
│ Offset 0│ Offset 1│ Offset 2│ Offset 3│
│ Message │ Message │ Message │ Message │
│   A     │   B     │   C     │   D     │
└─────────┴─────────┴─────────┴─────────┘
```

**Consumer sử dụng Offset để:**
- Track đã đọc đến đâu
- Có thể đọc lại từ offset cũ
- Commit offset sau khi xử lý xong

**Ví dụ trong code:**
```java
@KafkaListener(topics = "demo-topic")
public void consumeMessage(ConsumerRecord<String, Message> record) {
    log.info("Partition: {}", record.partition());  // Partition nào
    log.info("Offset: {}", record.offset());        // Offset là bao nhiêu
    // ...
}
```

---

## 🧪 Test các API

### 1. Gửi message KHÔNG có key

```bash
curl -X POST http://localhost:8080/api/messages/no-key \
  -H "Content-Type: application/json" \
  -d '{"content": "Hello Kafka!"}'
```

**Kết quả:**
- Message được gửi vào partition ngẫu nhiên (0, 1, hoặc 2)
- Check logs để xem partition và offset

### 2. Gửi message CÓ key

```bash
curl -X POST "http://localhost:8080/api/messages/with-key?key=user123" \
  -H "Content-Type: application/json" \
  -d '{"content": "Message with key"}'
```

**Kết quả:**
- Message được gửi vào partition được hash từ key
- Cùng key → cùng partition

### 3. Gửi nhiều messages (batch)

```bash
curl -X POST http://localhost:8080/api/messages/batch
```

**Kết quả:**
- Gửi 10 messages không có key
- Xem chúng được phân phối vào các partitions như thế nào

### 4. Gửi messages với cùng key

```bash
curl -X POST "http://localhost:8080/api/messages/same-key?key=user123"
```

**Kết quả:**
- Gửi 5 messages với cùng key "user123"
- Tất cả sẽ vào cùng 1 partition
- Check logs để xác nhận

---

## 📊 Ví dụ thực tế

### Scenario: E-commerce Order Processing

```
Topic: "orders"
Partitions: 3

Khi user "user123" đặt hàng:
1. Order created → key="user123" → Partition 1, Offset 0
2. Payment processed → key="user123" → Partition 1, Offset 1
3. Order shipped → key="user123" → Partition 1, Offset 2

→ Tất cả events của user123 đều ở Partition 1
→ Đảm bảo thứ tự: created → paid → shipped
```

### Scenario: Notification Service

```
Topic: "notifications"
Partitions: 5

Gửi notifications không có key:
- Notification 1 → Partition 2, Offset 0
- Notification 2 → Partition 0, Offset 0
- Notification 3 → Partition 3, Offset 0
- Notification 4 → Partition 1, Offset 0

→ Phân phối đều trên các partitions
→ Nhiều consumers có thể xử lý song song
```

---

## 🔍 Xem logs để hiểu rõ hơn

Khi chạy ứng dụng, bạn sẽ thấy logs như sau:

```
📨 RECEIVED MESSAGE
   Topic:     demo-topic
   Partition: 1  ← Message này nằm ở partition nào
   Offset:    3  ← Vị trí của message trong partition
   Key:       user123  ← Key của message

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

---

## 📝 Tóm tắt

| Khái niệm | Giải thích | Ví dụ |
|-----------|------------|-------|
| **Topic** | Thư mục chứa messages | `demo-topic`, `orders`, `notifications` |
| **Partition** | Chia topic thành nhiều phần | Topic có 3 partitions: 0, 1, 2 |
| **Offset** | ID của message trong partition | Partition 0: offset 0, 1, 2, 3... |
| **Key** | Dùng để xác định partition | Cùng key → cùng partition |

---

## 🎯 Key Takeaways

1. **Topic** = Thư mục chứa messages
2. **Partition** = Chia topic để scale và parallel processing
3. **Offset** = ID duy nhất của message trong partition
4. **Key** = Đảm bảo cùng key → cùng partition → đảm bảo thứ tự
5. **Không có Key** = Phân phối ngẫu nhiên trên các partitions

---

## 🔗 Tài liệu tham khảo

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Documentation](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
