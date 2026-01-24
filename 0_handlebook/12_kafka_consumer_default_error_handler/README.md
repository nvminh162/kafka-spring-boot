```
Xử lý lỗi phía consumer

Trong thực tế khi đọc event về từ broker server
=> consumer xử lý dữ liệu có thể dữ liệu sai
(bắn ra exception => Exception nghiệm trọng có thể mất dữ liệu)

=> Phía consumer có thể xử lý bằng cách cố gắng phân phối lại event cho consumer xử lý
sao 1 số lần cố gắng nhất định mà vẫn xảy ra lỗi cũ

=> Gủi event đó vào topic khác DLT (Dead Letter Topic)
```
![image](./image.png)


