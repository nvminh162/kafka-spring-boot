package com.nvminh162.notification_service.service.impl;

import com.nvminh162.notification_service.model.MessageDTO;
import com.nvminh162.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MessageServiceImpl {
    EmailService emailService;

    @KafkaListener(id = "notificationGroup", topics = "notification")
    public void listen(MessageDTO messageDTO) {
        log.info("Received: {}", messageDTO.getTo());
        emailService.sendEmail(messageDTO);
    }
}
