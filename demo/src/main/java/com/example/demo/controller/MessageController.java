package com.example.demo.controller;

import com.example.demo.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Producer Controller - Gửi messages lên Kafka
 * 
 * Producer là gì?
 * - Ứng dụng gửi event/message đến topic trong Kafka
 * - Sử dụng KafkaTemplate để gửi message
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    /**
     * Gửi message KHÔNG có key
     * 
     * Khi không có key:
     * - Kafka sẽ phân phối message ngẫu nhiên vào các partitions
     * - Message có thể vào partition 0, 1, hoặc 2 (tùy thuộc vào load balancing)
     * 
     * Ví dụ:
     * POST /api/messages/no-key
     * Body: {"content": "Hello Kafka"}
     */
    @PostMapping("/no-key")
    public String sendMessageWithoutKey(@RequestBody MessageRequest request) {
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .content(request.getContent())
                .sender("Producer")
                .timestamp(LocalDateTime.now())
                .build();

        // Gửi message KHÔNG có key → Kafka tự chọn partition
        CompletableFuture<SendResult<String, Message>> future = 
            kafkaTemplate.send("demo-topic", message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Message sent successfully!");
                log.info("   Topic: {}", result.getRecordMetadata().topic());
                log.info("   Partition: {}", result.getRecordMetadata().partition());
                log.info("   Offset: {}", result.getRecordMetadata().offset());
            } else {
                log.error("❌ Failed to send message: {}", ex.getMessage());
            }
        });

        return "Message sent! Check logs for partition and offset info.";
    }

    /**
     * Gửi message CÓ key
     * 
     * Khi có key:
     * - Kafka sử dụng hash của key để xác định partition
     * - Cùng key → cùng partition (đảm bảo thứ tự message)
     * 
     * Ví dụ:
     * POST /api/messages/with-key?key=user123
     * Body: {"content": "Hello Kafka"}
     * 
     * Tất cả message với key="user123" sẽ vào cùng 1 partition
     */
    @PostMapping("/with-key")
    public String sendMessageWithKey(
            @RequestParam String key,
            @RequestBody MessageRequest request) {
        
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .content(request.getContent())
                .sender("Producer")
                .timestamp(LocalDateTime.now())
                .build();

        // Gửi message CÓ key → Kafka hash key để chọn partition
        CompletableFuture<SendResult<String, Message>> future = 
            kafkaTemplate.send("demo-topic", key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("✅ Message sent with key '{}'!", key);
                log.info("   Topic: {}", result.getRecordMetadata().topic());
                log.info("   Partition: {}", result.getRecordMetadata().partition());
                log.info("   Offset: {}", result.getRecordMetadata().offset());
                log.info("   → Messages với cùng key '{}' sẽ vào partition {}", 
                    key, result.getRecordMetadata().partition());
            } else {
                log.error("❌ Failed to send message: {}", ex.getMessage());
            }
        });

        return String.format("Message sent with key '%s'! Check logs for partition and offset.", key);
    }

    /**
     * Gửi nhiều messages để demo partitions
     * 
     * Gửi 10 messages để xem chúng được phân phối vào các partitions như thế nào
     */
    @PostMapping("/batch")
    public String sendBatchMessages() {
        for (int i = 1; i <= 10; i++) {
            Message message = Message.builder()
                    .id(UUID.randomUUID().toString())
                    .content("Message #" + i)
                    .sender("Producer")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Gửi không có key → phân phối ngẫu nhiên
            kafkaTemplate.send("demo-topic", message);
        }

        log.info("📦 Sent 10 messages. Check consumer logs to see partition distribution!");
        return "Sent 10 messages! Check consumer logs to see which partition each message went to.";
    }

    /**
     * Gửi messages với cùng key để demo partition consistency
     */
    @PostMapping("/same-key")
    public String sendMessagesWithSameKey(@RequestParam(defaultValue = "user123") String key) {
        for (int i = 1; i <= 5; i++) {
            Message message = Message.builder()
                    .id(UUID.randomUUID().toString())
                    .content("Message #" + i + " with key: " + key)
                    .sender("Producer")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Tất cả messages có cùng key → cùng partition
            kafkaTemplate.send("demo-topic", key, message);
        }

        log.info("📦 Sent 5 messages with same key '{}'. They will all go to the same partition!", key);
        return String.format("Sent 5 messages with key '%s'. They will all go to the same partition!", key);
    }

    // Inner class for request body
    @lombok.Data
    static class MessageRequest {
        private String content;
    }
}
