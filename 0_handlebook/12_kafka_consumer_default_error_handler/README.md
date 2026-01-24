```
Default Error Handler
Xử lý lỗi phía consumer

Trong thực tế khi đọc event về từ broker server
=> consumer xử lý dữ liệu có thể dữ liệu sai
(bắn ra exception => Exception nghiệm trọng có thể mất dữ liệu)

=> Phía consumer có thể xử lý bằng cách cố gắng phân phối lại event cho consumer xử lý
sao 1 số lần cố gắng nhất định mà vẫn xảy ra lỗi cũ

=> Gủi event đó vào topic khác DLT (Dead Letter Topic)
```
![image](./image.png)

```
Dead Letter Topic sẽ sao lưu y hết topic chính thêm hậu tố dlt

Lưu event bị lỗi vào topic này và consumer vẫn tiếp tục xử lý event của Offset sau nó
(Tạo consumer đọc event lỗi dlt => gửi lại lên broker server <phân tích tại sao lại xảy ra lỗi này> => tránh mất dữ liệu)

Default Error Handler tuần tự xử lý event được đảm bảo
(Ví dụ: đang xử lý event Offset 2 thì 3 4 5 phải đợi => khi nào offset 2 xử lý thành công or đưa vào dlt thì mới tới lượt 3 4 5)

```

