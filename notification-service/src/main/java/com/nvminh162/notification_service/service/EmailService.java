package com.nvminh162.notification_service.service;

import com.nvminh162.notification_service.model.MessageDTO;

public interface EmailService {
    void sendEmail(MessageDTO messageDTO);
}
