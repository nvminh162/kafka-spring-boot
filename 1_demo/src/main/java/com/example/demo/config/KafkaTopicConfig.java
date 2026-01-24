package com.example.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Cấu hình Kafka Topics
 * 
 * Topic: Giống như một thư mục hoặc bảng trong database
 * - Tên topic: "demo-topic"
 * - Partitions: 3 (chia topic thành 3 phần)
 * - Replication: 1 (chỉ có 1 bản copy, dùng cho development)
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * Tạo topic "demo-topic" với 3 partitions
     * 
     * Partition là gì?
     * - Topic được chia thành nhiều partitions để:
     *   1. Phân tán dữ liệu trên nhiều broker (scalability)
     *   2. Cho phép nhiều consumer đọc song song
     *   3. Tăng throughput (xử lý nhiều message cùng lúc)
     * 
     * Ví dụ: Topic có 3 partitions = 3 "ngăn kéo" riêng biệt
     * - Partition 0: chứa message với offset 0, 1, 2, ...
     * - Partition 1: chứa message với offset 0, 1, 2, ...
     * - Partition 2: chứa message với offset 0, 1, 2, ...
     */
    @Bean
    public NewTopic demoTopic() {
        return TopicBuilder.name("demo-topic")
                .partitions(3)  // Chia thành 3 partitions
                .replicas(1)    // 1 bản copy (development only)
                .build();
    }
}
