package com.example.account.controller;

import com.example.account.model.Account;
import com.example.account.model.Message;
import com.example.account.model.Statistic;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.MessageRepository;
import com.example.account.repository.StatisticRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {

    AccountRepository accountRepository;
    MessageRepository messageRepository;
    StatisticRepository statisticRepository;

    @PostMapping("/new")
    public Account create(@RequestBody Account accountDTO) {

        Statistic statisticDTO = Statistic.builder()
                .message("Account " + accountDTO.getEmail() + " is created")
                .createdDate(new Date())
                .status(false)
                .build();

        Message messageDTO = Message.builder()
                .to(accountDTO.getEmail())
                .toName(accountDTO.getName())
                .subject("Welcome to Nguyen Van Minh - 22003405")
                .content("Nguyen Van Minh 22003405 is practice `software design architecture`")
                .status(false)
                .build();

        accountRepository.save(accountDTO);
        messageRepository.save(messageDTO);
        statisticRepository.save(statisticDTO);

        // key ngẫu nhiên
        // kafkaTemplate.send("notification", messageDTO);
        // kafkaTemplate.send("statistic", statisticDTO);

        return accountDTO;
    }
}
