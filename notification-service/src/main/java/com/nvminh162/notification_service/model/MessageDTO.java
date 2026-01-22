package com.nvminh162.notification_service.model;

import lombok.Data;

@Data
public class MessageDTO {
    private String to;
    private String toName;
    private String subject;
    private String content;
}