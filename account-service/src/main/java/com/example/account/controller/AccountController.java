package com.example.account.controller;

import com.example.account.model.AccountDTO;
import com.example.account.model.MessageDTO;
import com.example.account.model.StatisticDTO;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {

    KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/new")
    public AccountDTO create(@RequestBody AccountDTO accountDTO) {

        StatisticDTO statisticDTO = StatisticDTO.builder()
                .message("Account " + accountDTO.getEmail() + " is created")
                .createdDate(new Date())
                .build();

        MessageDTO messageDTO = MessageDTO.builder()
                .to(accountDTO.getEmail())
                .toName(accountDTO.getName())
                .subject("Welcome to Nguyen Van Minh - 22003405")
                .content("Nguyen Van Minh 22003405 is practice `software design architecture`")
                .build();

        // key ngẫu nhiên
        kafkaTemplate.send("notification", messageDTO);
        kafkaTemplate.send("statistic", statisticDTO);

        return accountDTO;
    }
}
