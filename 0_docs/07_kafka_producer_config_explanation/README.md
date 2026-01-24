# Giải thích chi tiết Kafka Producer Configuration

## Tổng quan các cấu hình trong `application.yaml`

```yaml
spring:
  kafka:
    producer:
      acks: -1
      retries: 1
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
        request.timeout.ms: 1  # Demo timeout
```

---

## 1. `request.timeout.ms: 1` (Timeout cho mỗi request)

### Cơ chế hoạt động:

**`request.timeout.ms`** là thời gian chờ tối đa để nhận response từ Kafka broker cho mỗi request.

### Flow khi timeout:

```
T0: Producer gửi message → Kafka Broker
    ↓
T1: Đợi response từ broker...
    ↓
T2: Sau 1ms (request.timeout.ms: 1)
    ↓
    ❌ TIMEOUT! → Không nhận được response
    ↓
    → Kafka coi request này là FAILED
    ↓
    → Trigger retry mechanism (nếu retries > 0)
```

### Ví dụ với `request.timeout.ms: 1`:

```
Timeline:
0ms:   Producer gửi message
1ms:   Timeout! (chưa nhận được response)
       → Kafka: "Request failed - timeout"
       → Trigger retry (nếu retries > 0)
```

**Vấn đề:** 1ms quá ngắn, hầu hết request sẽ timeout vì:
- Network latency: ~1-5ms
- Kafka xử lý: ~1-10ms
- Response về: ~1-5ms
- **Tổng: ~3-20ms > 1ms → TIMEOUT!**

---

## 2. `retries: 1` (Số lần retry)

### Cơ chế hoạt động:

**`retries`** là số lần Producer sẽ tự động gửi lại message khi gặp lỗi.

### Flow retry:

```
Lần 1: Gửi message → FAILED (timeout/error)
    ↓
    → Retry 1 lần (retries: 1)
    ↓
Lần 2: Gửi lại message → SUCCESS hoặc FAILED
    ↓
    Nếu SUCCESS: → Done
    Nếu FAILED: → Throw exception (không retry nữa)
```

### Ví dụ chi tiết:

```
Request 1:
├── T0: Gửi message
├── T1: Timeout (1ms) → FAILED
└── Retry 1:
    ├── T2: Gửi lại message
    ├── T3: Timeout (1ms) → FAILED
    └── ❌ Throw exception (hết retry)
```

### Lưu ý:

- `retries: 1` = retry 1 lần = tổng cộng 2 lần gửi (lần đầu + 1 retry)
- Nếu cả 2 lần đều fail → Throw exception
- Exception sẽ được catch trong `whenComplete()` callback

---

## 3. `acks: -1` (Acknowledgment)

### Cơ chế hoạt động:

**`acks`** xác định Producer cần nhận acknowledgment từ bao nhiêu replicas trước khi coi là thành công.

### Các giá trị:

| Giá trị | Ý nghĩa | Đảm bảo |
|---------|---------|---------|
| `0` | Không đợi ack | ❌ Có thể mất message |
| `1` | Đợi leader ack | ⚠️ Có thể mất nếu leader fail |
| `-1` hoặc `all` | Đợi tất cả ISR (In-Sync Replicas) ack | ✅ Đảm bảo không mất |

### Flow với `acks: -1`:

```
Producer gửi message
    ↓
Kafka Leader nhận message
    ↓
Leader ghi vào log
    ↓
Leader gửi đến Followers (ISR)
    ↓
Follower 1: Copy thành công → ACK
Follower 2: Copy thành công → ACK
    ↓
Leader nhận ACK từ tất cả ISR
    ↓
Leader gửi ACK về Producer
    ↓
Producer nhận ACK → SUCCESS
```

**Vấn đề với `request.timeout.ms: 1`:**
- Quá trình trên mất ~10-50ms
- Nhưng timeout chỉ 1ms → Timeout trước khi nhận ACK!

---

## 4. `enable.idempotence: true` (Chống duplicate)

### Cơ chế hoạt động:

**`enable.idempotence`** đảm bảo message không bị gửi trùng lặp khi retry.

### Vấn đề không có idempotence:

```
Lần 1: Gửi message → Timeout (nhưng thực ra đã ghi vào Kafka)
    ↓
Retry: Gửi lại message → SUCCESS
    ↓
Kết quả: Message bị duplicate (2 bản copy trong Kafka)
```

### Với `enable.idempotence: true`:

```
Lần 1: Gửi message với Producer ID + Sequence Number
    ↓
    → Kafka ghi message với ID này
    ↓
Retry: Gửi lại message với CÙNG Producer ID + Sequence Number
    ↓
    → Kafka kiểm tra: "ID này đã có rồi"
    ↓
    → Bỏ qua, không ghi duplicate
    ↓
Kết quả: Chỉ có 1 bản copy trong Kafka
```

### Producer ID và Sequence Number:

- **Producer ID**: Kafka tự động gán cho mỗi Producer
- **Sequence Number**: Tăng dần cho mỗi message (0, 1, 2, 3...)
- Kafka dùng cặp (Producer ID, Sequence Number) để detect duplicate

---

## 5. `max.in.flight.requests.per.connection: 5`

### Cơ chế hoạt động:

**`max.in.flight.requests`** là số lượng requests có thể gửi song song mà chưa nhận được response.

### Vấn đề khi > 5 (không có idempotence):

```
Request 1: Gửi message A → Đang đợi response
Request 2: Gửi message B → Đang đợi response
Request 3: Gửi message C → Đang đợi response
...
Request 6: Gửi message F → Đang đợi response

Nếu Request 1 timeout → Retry
→ Message A có thể được ghi SAU message B, C, D, E
→ Mất thứ tự (out of order)
```

### Với `max.in.flight.requests.per.connection: 5`:

- Giới hạn tối đa 5 requests chưa nhận response
- Kết hợp với `enable.idempotence: true` → Đảm bảo thứ tự
- Nếu > 5 → Producer sẽ đợi response trước khi gửi tiếp

---

## 6. Flow hoạt động tổng thể

### Scenario: Gửi message với config hiện tại

```
Config:
- request.timeout.ms: 1
- retries: 1
- acks: -1
- enable.idempotence: true
- max.in.flight.requests.per.connection: 5
```

### Flow chi tiết:

```
Bước 1: Producer gửi message
    ↓
Bước 2: Đợi response từ Kafka
    ├── Network latency: ~2ms
    ├── Kafka xử lý: ~5ms
    └── Response về: ~2ms
    Tổng: ~9ms
    ↓
Bước 3: Sau 1ms (request.timeout.ms)
    ❌ TIMEOUT! (chưa nhận được response)
    ↓
Bước 4: Retry mechanism trigger
    ├── Kiểm tra: retries = 1 → Còn 1 lần retry
    └── Gửi lại message (với cùng Producer ID + Sequence Number)
    ↓
Bước 5: Đợi response lần 2
    ├── Network latency: ~2ms
    ├── Kafka xử lý: ~5ms
    └── Response về: ~2ms
    Tổng: ~9ms
    ↓
Bước 6: Sau 1ms (request.timeout.ms)
    ❌ TIMEOUT lại!
    ↓
Bước 7: Hết retry
    ❌ Throw exception
    ↓
Bước 8: Exception được catch trong whenComplete()
    ex.printStackTrace() → In ra console
```

### Kết quả:

- **Hầu hết messages sẽ FAIL** vì timeout quá ngắn
- Exception sẽ được in ra console trong callback
- Message có thể đã được ghi vào Kafka (nhưng Producer không biết vì timeout)

---

## 7. Vấn đề với config hiện tại

### Vấn đề 1: Timeout quá ngắn

```
request.timeout.ms: 1
→ Hầu hết request sẽ timeout
→ Retry cũng sẽ timeout
→ Messages fail liên tục
```

### Vấn đề 2: Message có thể đã được ghi

```
Scenario:
1. Producer gửi message
2. Kafka nhận và ghi vào log (mất 5ms)
3. Sau 1ms → Producer timeout
4. Producer retry
5. Kafka: "Message này đã có rồi" (idempotence)
6. Kafka trả về success
7. Nhưng Producer timeout trước khi nhận response
8. → Producer nghĩ là fail, nhưng message đã được ghi
```

### Vấn đề 3: Exception trong callback

```
whenComplete((result, ex) -> {
    if (ex == null) {
        // Không bao giờ vào đây với timeout 1ms
    } else {
        // Hầu hết sẽ vào đây
        ex.printStackTrace(); // In exception ra console
    }
});
```

---

## 8. Cách test timeout

### Test 1: Xem exception

Với `request.timeout.ms: 1`, bạn sẽ thấy exception trong console:

```
org.apache.kafka.common.errors.TimeoutException: ...
```

### Test 2: Kiểm tra message có được ghi không

Mặc dù exception, message có thể đã được ghi vào Kafka (do idempotence).

Kiểm tra trong Offset Explorer:
- Xem topic "notification" có messages không
- Có thể có messages mặc dù Producer báo fail

### Test 3: So sánh với timeout lớn hơn

Thử đổi `request.timeout.ms: 30000`:
- Messages sẽ thành công
- Không có exception
- Callback vào `if (ex == null)`

---

## 9. Tóm tắt các config

| Config | Giá trị | Ý nghĩa | Vấn đề với 1ms |
|--------|---------|---------|----------------|
| `request.timeout.ms` | 1 | Chờ tối đa 1ms cho response | ❌ Quá ngắn, hầu hết timeout |
| `retries` | 1 | Retry 1 lần khi fail | ⚠️ Retry cũng sẽ timeout |
| `acks` | -1 | Đợi tất cả ISR ack | ⚠️ Mất ~10-50ms, timeout trước |
| `enable.idempotence` | true | Chống duplicate | ✅ Hoạt động tốt |
| `max.in.flight.requests` | 5 | Tối đa 5 requests song song | ✅ Hoạt động tốt |

---

## 10. Flow diagram

```
Producer.send()
    ↓
[request.timeout.ms: 1] ← Bắt đầu đếm
    ↓
Gửi request đến Kafka
    ↓
    ├─→ Kafka xử lý (mất ~5-10ms)
    │   ├─→ Ghi vào leader
    │   ├─→ Copy đến followers
    │   └─→ Đợi ACK từ ISR
    │
    └─→ Sau 1ms: TIMEOUT! ❌
        ↓
        [retries: 1] → Còn 1 lần retry
        ↓
        Gửi lại request
        ↓
        [request.timeout.ms: 1] ← Bắt đầu đếm lại
        ↓
        ├─→ Kafka xử lý
        │   ├─→ Kiểm tra idempotence
        │   └─→ "Message đã có" → Trả về success
        │
        └─→ Sau 1ms: TIMEOUT lại! ❌
            ↓
            Hết retry → Throw exception
            ↓
            whenComplete() → ex != null
            ↓
            ex.printStackTrace()
```

---

## 11. Kết luận

Với `request.timeout.ms: 1`:
- ✅ **Demo tốt** để thấy exception
- ❌ **Không phù hợp production** (quá ngắn)
- ⚠️ **Message có thể đã được ghi** mặc dù Producer báo fail
- ⚠️ **Retry cũng sẽ timeout** → Hầu hết messages fail

**Để test exception:** Config này phù hợp
**Để production:** Nên đổi thành `request.timeout.ms: 30000`
