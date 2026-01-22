```
Kafka is an event streaming platform.

Event là bất cứ dữ liệu nào được ghi lại theo thời gian thực (như bản ghi trong CSDL)

Event stream là luồng dữ liệu theo trình tự thời gian từ nhiều nguồn dữ liệu khác nhau: dbs, IoT, devices, apps, ...

Kafka lưu dữ liệu trong file đọc lại truy vấn lại

Kafka can be:
+ publish (write) & subscribe (read) luồng sự kiện bao gồm liên tục ghi và đọc từ các hệ thống khác nhau
+ Lưu luồng sự kiện lâu dài & cấu hình thời gian để lưu lại
+ Xử lý luồng sự kiện theo thời gian thực
```

```
Kafka cluster là tập hợp nhiều broker server (production >= 3)

Kafka cluster đảm bảo MỞ RỘNG CAO & KHẢ NĂNG CHỊU LỖI
```

![image 1](./img1.png)
```
Broker là tên mang tính chất ý nghĩa

Một con server mang hình thức của lớp lưu dữ liệu, sự kiện vào file
```





![image 2](./img2.png)
