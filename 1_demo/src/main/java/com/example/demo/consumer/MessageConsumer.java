package com.example.demo.consumer;

import com.example.demo.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer - Đọc messages từ Kafka topic
 * 
 * Consumer là gì?
 * - Ứng dụng đọc event/message từ topic trong Kafka
 * - Sử dụng @KafkaListener để lắng nghe messages
 * 
 * Offset là gì?
 * - Mỗi message trong partition có một offset duy nhất (ID)
 * - Offset là số thứ tự của message trong partition (0, 1, 2, 3, ...)
 * - Consumer sử dụng offset để track đã đọc đến đâu
 * - Offset không bao giờ thay đổi sau khi được gán
 */
@Slf4j
@Component
public class MessageConsumer {

    /**
     * Consumer lắng nghe topic "demo-topic"
     * 
     * @KafkaListener:
     * - topics = "demo-topic": Lắng nghe topic này
     * - groupId = "demo-consumer-group": Consumer group ID
     * 
     * ConsumerRecord chứa:
     * - topic: Tên topic
     * - partition: Partition nào chứa message này
     * - offset: Vị trí của message trong partition
     * - key: Key của message (nếu có)
     * - value: Nội dung message
     * - timestamp: Thời gian message được gửi
     */
    @KafkaListener(
        topics = "demo-topic",
        groupId = "demo-consumer-group"
    )
    public void consumeMessage(ConsumerRecord<String, Message> record) {
        Message message = record.value();
        
        log.info("═══════════════════════════════════════════════════════");
        log.info("📨 RECEIVED MESSAGE");
        log.info("   Topic:     {}", record.topic());
        log.info("   Partition: {}  ← Message này nằm ở partition nào", record.partition());
        log.info("   Offset:    {}  ← Vị trí của message trong partition", record.offset());
        log.info("   Key:       {}  ← Key của message (null nếu không có)", record.key());
        log.info("   ───────────────────────────────────────────────────");
        log.info("   Message ID:    {}", message.getId());
        log.info("   Content:       {}", message.getContent());
        log.info("   Sender:        {}", message.getSender());
        log.info("   Timestamp:     {}", message.getTimestamp());
        log.info("═══════════════════════════════════════════════════════");
        
        // Giải thích:
        explainConcepts(record);
    }

    /**
     * Giải thích các khái niệm dựa trên message nhận được
     */
    private void explainConcepts(ConsumerRecord<String, Message> record) {
        log.info("");
        log.info("💡 GIẢI THÍCH:");
        log.info("   📁 Topic: '{}'", record.topic());
        log.info("      → Giống như một thư mục chứa tất cả messages");
        log.info("");
        log.info("   📦 Partition: {}", record.partition());
        log.info("      → Topic được chia thành nhiều partitions");
        log.info("      → Message này nằm ở partition {}", record.partition());
        log.info("      → Mỗi partition có offset riêng (bắt đầu từ 0)");
        log.info("");
        log.info("   🔢 Offset: {}", record.offset());
        log.info("      → ID duy nhất của message trong partition này");
        log.info("      → Offset {} là message thứ {} trong partition {}", 
            record.offset(), record.offset() + 1, record.partition());
        log.info("      → Offset không bao giờ thay đổi");
        log.info("");
        
        if (record.key() != null) {
            log.info("   🔑 Key: '{}'", record.key());
            log.info("      → Message có key → Kafka hash key để chọn partition");
            log.info("      → Cùng key → cùng partition (đảm bảo thứ tự)");
        } else {
            log.info("   🔑 Key: null");
            log.info("      → Message không có key → Kafka chọn partition ngẫu nhiên");
        }
        log.info("");
    }
}
