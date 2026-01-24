![diagram](./img-01.png)
```
Kafka Cluster: tập hợp các broker kafka chạy độc lập với nhau để cung cấp phân tán 
=> đảm bảo các con server không bị down cùng 1 lúc

Trong môi trường production tối thiếu thường phải từ 3 con broker trở lên để đảm bảo tính sẵn sàng cao (high availability)
```

```
Zookeeper: là một phần mềm quản lý: kafka cluster, topics, offsets, events, ...
(Zookeeper sẽ được thay thế bởi KRaft mode trong các phiên bản kafka mới hơn)
```

```
Event: là một đơn vị dữ liệu được gửi từ producer đến kafka cluster
```

```
Producer: là ứng dụng hoặc dịch vụ chịu trách nhiệm gửi event lên broker (thường trong topics)
```

```
Consumer: là ứng dụng hoặc dịch vụ chịu trách nhiệm nhận event từ broker
```
```
Producer và Consumer: là ứng dụng chạy độc lập với kafka cluster
Kafka ở giữa đóng vai trò nhận event từ producer & consumer sẽ đọc event từ kafka về để xử lý
```
```
Kafka highlights:
+ Core capabilities (Khả năng cốt lõi)
    - High Throughput (Băng thông cao)
    - Scalability (Khả năng mở rộng)
    - Permanent storage (Lưu trữ vĩnh viễn)
    - High availability (Tính sẵn sàng cao)
+ Ecosystem (Hệ sinh thái)
    - Connect to almost anything (Kết nối với hầu hết mọi thứ)
    - Client libraries (Thư viện khách hàng)
    - Large ecosystem (Hệ sinh thái lớn)
+ Trust & ease of use (Độ tin cậy & dễ sử dụng)
```