```
+ Event Driven Architecture (EDA) là một design pattern phần mềm, cho giải pháp kiến trúc hệ thống nào đó
```
```
+ Là ứng dụng khác nhau có thể bất đồng bộ giao tiếp với nhau
    Tạo ra (produce) các event/message
    Gửi lên platform trung gian (event/message broker)
    Sau đó nhiều ứng dụng khác tiêu thụ (consume) các event/message
    => Giúp hệ thống tương tác với nhau gửi/nhận các event/message một cách bất đồng bộ
```
![diagram](./img-01.png)
