package com.example.account.controller;

import com.example.account.model.AccountDTO;
import com.example.account.model.MessageDTO;
import com.example.account.model.StatisticDTO;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
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

        for (int i = 0; i < 100; i++) {
            kafkaTemplate.send("notification", messageDTO).whenComplete((result, ex) -> {
                if (ex == null) {
                    // handle success
                    System.out.println(result.getRecordMetadata().partition());
                } else {
                    // handle fail, save db event failed
                    ex.printStackTrace();
                }
            });
        }

        // key ngẫu nhiên
        // kafkaTemplate.send("notification", messageDTO);
        kafkaTemplate.send("statistic", statisticDTO);

        return accountDTO;
    }
}
