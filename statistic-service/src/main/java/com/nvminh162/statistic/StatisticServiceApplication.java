package com.nvminh162.statistic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@SpringBootApplication
public class StatisticServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StatisticServiceApplication.class, args);
	}

	/*
	tham số template là để gửi event lỗi vào topic dlt
	DeadLetterPublishingRecoverer là để gửi event lỗi vào topic dlt
	FixedBackOff là để retry event lỗi 2 lần sau 1 giây (cố gắng phân phối lại event sau 1s, gửi lại 2 lần)
	 */
	// @Bean
	// DefaultErrorHandler defaultErrorHandler(KafkaOperations<String, Object> template) {
	// 	return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), new FixedBackOff(1000L, 2));
	// }

	/*
	Có thể không cần vì kafka có thể tạo topic dlt tự động
	*/
	// @Bean
	// NewTopic dlt() {
	// 	return new NewTopic("statistic.DLT", 1, (short) 1);
	// }
}
