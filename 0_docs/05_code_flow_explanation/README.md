# Giải thích luồng Source Code dựa trên Kafka Core Concepts

## Tổng quan kiến trúc

Dự án này là một hệ thống **Event-Driven Architecture** sử dụng Kafka làm message broker, bao gồm 3 microservices:

1. **account-service** (Port 8081)**: Producer - gửi event lên Kafka
2. **notification-service** (Port 8082)**: Consumer - đọc event từ topic "notification"
3. **statistic-service** (Port 8083)**: Consumer - đọc event từ topic "statistic"

---

## Luồng hoạt động chi tiết

### 1. Kafka Broker & Cluster

Theo khái niệm bạn đã note:
> Broker là Một con server mang hình thức của lớp lưu dữ liệu, sự kiện vào file

**Trong code:**
- Tất cả 3 services đều kết nối đến **Kafka Broker** tại `localhost:9092` (trong file `application.yaml`)
- Broker này lưu trữ các **Topics**: `notification` và `statistic`

```yaml
# Tất cả services đều có cấu hình này:
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

---

### 2. Producer (account-service)

Theo khái niệm:
> Producer là ứng dụng gửi event đến topic trong kafka server
> Producer lên kafka phải có thư viện Serializer để convert đối tượng chuỗi byte[] trước khi gửi lên kafka

**Trong code:**

#### File: `AccountController.java`

```java
@RestController
public class AccountController {
    KafkaTemplate<String, Object> kafkaTemplate;  // Công cụ để gửi event
    
    @PostMapping("/new")
    public AccountDTO create(@RequestBody AccountDTO accountDTO) {
        // Tạo 2 event objects
        StatisticDTO statisticDTO = StatisticDTO.builder()...build();
        MessageDTO messageDTO = MessageDTO.builder()...build();
        
        // Gửi event lên 2 topics khác nhau
        kafkaTemplate.send("notification", messageDTO);    // Gửi đến topic "notification"
        kafkaTemplate.send("statistic", statisticDTO);     // Gửi đến topic "statistic"
    }
}
```

**Giải thích:**
- `KafkaTemplate` là công cụ của Spring Kafka để gửi event (Producer)
- Khi client gọi API `POST /accounts/new`, controller tạo 2 event objects và gửi lên 2 topics

#### Serializer Configuration

**File: `account-service/src/main/resources/application.yaml`**

```yaml
spring:
  kafka:
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

**Giải thích:**
- `JsonSerializer` tự động convert `MessageDTO` và `StatisticDTO` (Java objects) thành chuỗi JSON (byte[])
- Trước khi gửi lên Kafka Broker, object được serialize thành byte[]

#### Cấu hình đảm bảo dữ liệu không mất

```yaml
acks: -1  # FOR SURE - event phải ghi thành công vào leader & follower
retries: 1
properties:
  enable.idempotence: true  # Chống duplicate event khi retry
```

**Giải thích theo khái niệm:**
> -1 or all: FOR SURE => event này buộc phải ghi thành công vào leader & follower copy lại thành công thì mới xác minh thành công

---

### 3. Topics

Theo khái niệm:
> Topic giống danh mục bố trí các event trong kafka broker
> (giống table trong database, folder trong hệ thống file)

**Trong code:**
- Topic `"notification"`: Lưu trữ event `MessageDTO` để gửi email
- Topic `"statistic"`: Lưu trữ event `StatisticDTO` để lưu thống kê

**Khi Producer gửi event:**
```java
kafkaTemplate.send("notification", messageDTO);  // Gửi event vào topic "notification"
```

**Giải thích:**
- Event được gửi vào topic mà không có key (key = null)
- Theo khái niệm: "nếu gửi event lên không với key thì event sẽ được ghi vào ngẫu nhiên 1 partition trong topic"

---

### 4. Topic Partitions

Theo khái niệm:
> Trong kafka topics được chia thành nhiều các phần nhỏ gọi là Kafka Topic Partition
> Khi producer gửi event đến topic, event sẽ được ghi vào 1 partition cụ thể

**Trong code:**
- Khi gửi event không có key, Kafka tự động chọn partition ngẫu nhiên
- Mỗi event được gán một **Offset** duy nhất trong partition đó

**Trong hình ảnh Offset Explorer bạn đang xem:**
- Topic `statistic` có Partition 0
- Các event có Offset từ 0 đến 7
- Mỗi event có Timestamp riêng

---

### 5. Consumer (notification-service & statistic-service)

Theo khái niệm:
> Consumer là ứng dụng đọc event từ topic trong kafka server
> Consumer lên kafka phải có thư viện Deserializer để convert chuỗi byte[] về đối tượng ban đầu

#### Consumer 1: notification-service

**File: `MessageServiceImpl.java`**

```java
@Service
public class MessageServiceImpl {
    @KafkaListener(id = "notificationGroup", topics = "notification")
    public void listen(MessageDTO messageDTO) {
        log.info("Received: {}", messageDTO.getTo());
        emailService.sendEmail(messageDTO);  // Xử lý event: gửi email
    }
}
```

**Giải thích:**
- `@KafkaListener`: Annotation đánh dấu method này là Consumer
- `topics = "notification"`: Lắng nghe event từ topic "notification"
- `id = "notificationGroup"`: Consumer Group ID (dùng để quản lý offset)
- Method `listen()` tự động được gọi khi có event mới trong topic

#### Deserializer Configuration

**File: `notification-service/src/main/resources/application.yaml`**

```yaml
spring:
  kafka:
    consumer:
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.value.default.type: com.nvminh162.notification_service.model.MessageDTO
```

**Giải thích:**
- `JsonDeserializer` tự động convert byte[] (JSON) từ Kafka về object `MessageDTO`
- Sau khi đọc từ Kafka, byte[] được deserialize thành Java object

#### Consumer 2: statistic-service

**File: `StatisticService.java`**

```java
@Service
public class StatisticService {
    @KafkaListener(id = "statisticGroup", topics = "statistic")
    public void listen(Statistic statistic) {
        log.info("Received: {}", statistic);
        statisticRepository.save(statistic);  // Xử lý event: lưu vào database
    }
}
```

**Giải thích tương tự:**
- Consumer này lắng nghe topic "statistic"
- Khi nhận event, lưu vào database H2

---

### 6. Event Offset

Theo khái niệm:
> Kafka Event's Offset: ID được gắn cho 1 event khi gửi đến trong partition của topic
> Event Offset này được gắn rồi sẽ không thay đổi

**Trong code:**
- Kafka tự động gán Offset cho mỗi event
- Consumer sử dụng Offset để track vị trí đã đọc đến đâu
- `auto-offset-reset: earliest` trong config nghĩa là: nếu chưa có offset, đọc từ đầu

**Trong hình ảnh Offset Explorer:**
- Bạn thấy các event có Offset: 0, 1, 2, 3, 4, 5, 6, 7
- Mỗi Offset là duy nhất và không thay đổi

---

## Luồng hoạt động tổng thể

```
┌─────────────────┐
│  Client/User    │
│  POST /accounts │
│  /new           │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│  account-service (Producer)      │
│  AccountController.create()      │
│  - Tạo MessageDTO               │
│  - Tạo StatisticDTO              │
└────────┬─────────────────────────┘
         │
         │ Serialize (JsonSerializer)
         │ Object → byte[] (JSON)
         │
         ▼
┌─────────────────────────────────┐
│  Kafka Broker (localhost:9092)  │
│  ┌───────────────────────────┐  │
│  │ Topic: "notification"      │  │
│  │ Partition 0              │  │
│  │ - Offset 0: MessageDTO    │  │
│  │ - Offset 1: MessageDTO    │  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ Topic: "statistic"         │  │
│  │ Partition 0                │  │
│  │ - Offset 0: StatisticDTO   │  │
│  │ - Offset 1: StatisticDTO   │  │
│  └───────────────────────────┘  │
└────────┬─────────────────────────┘
         │
         │ Deserialize (JsonDeserializer)
         │ byte[] (JSON) → Object
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────┐ ┌──────────────┐
│notification│ │statistic-  │
│-service   │ │service      │
│(Consumer) │ │(Consumer)   │
│           │ │             │
│@Kafka     │ │@Kafka       │
│Listener  │ │Listener      │
│           │ │             │
│Gửi Email │ │Lưu Database │
└───────────┘ └──────────────┘
```

---

## Điểm quan trọng

### 1. Producer & Consumer độc lập
> Producer & Consumer là ứng dụng chạy độc lập không liên quan với nhau

**Trong code:**
- `account-service` (Java) không cần biết `notification-service` (Java) đang chạy hay không
- Chúng chỉ giao tiếp thông qua Kafka Broker
- Có thể tắt một Consumer, Producer vẫn gửi event bình thường

### 2. Bất đồng bộ (Asynchronous)
> Producer (tạo) & Consumer (đọc) sử dụng kafka broker để trao đổi dữ liệu với nhau bất đồng bộ

**Trong code:**
- Khi `AccountController.create()` gọi `kafkaTemplate.send()`, nó không đợi Consumer xử lý
- Method trả về ngay lập tức
- Consumer xử lý event sau đó (có thể vài giây sau)

### 3. Nhiều Consumer có thể đọc cùng 1 Topic
> Nhiều consumer có thể đọc dữ liệu từ cùng 1 topic

**Ví dụ:**
- Có thể có nhiều `notification-service` instances cùng đọc topic "notification"
- Kafka tự động phân phối event cho các Consumer trong cùng Consumer Group

---

## Tóm tắt mapping Code → Khái niệm

| Khái niệm Kafka | Trong Source Code |
|----------------|-------------------|
| **Broker** | `localhost:9092` trong `application.yaml` |
| **Producer** | `AccountController` với `KafkaTemplate` |
| **Consumer** | `MessageServiceImpl` và `StatisticService` với `@KafkaListener` |
| **Topic** | `"notification"` và `"statistic"` |
| **Event** | `MessageDTO` và `StatisticDTO` objects |
| **Serializer** | `JsonSerializer` trong `account-service` config |
| **Deserializer** | `JsonDeserializer` trong consumer services config |
| **Offset** | Tự động quản lý bởi Kafka, hiển thị trong Offset Explorer |
| **Partition** | Tự động tạo bởi Kafka (có thể thấy trong Offset Explorer) |

---

## Cách test luồng

1. **Khởi động Kafka Broker** (localhost:9092)
2. **Khởi động 3 services:**
   - account-service (port 8081)
   - notification-service (port 8082)
   - statistic-service (port 8083)
3. **Gửi request:**
   ```bash
   POST http://localhost:8081/accounts/new
   Body: {
     "name": "Nguyen Van Minh",
     "email": "nvminh@example.com"
   }
   ```
4. **Kết quả:**
   - Event được gửi lên topic "notification" → notification-service nhận và gửi email
   - Event được gửi lên topic "statistic" → statistic-service nhận và lưu database
   - Xem event trong Offset Explorer (như hình bạn đang xem)
