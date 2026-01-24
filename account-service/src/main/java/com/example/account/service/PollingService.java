package com.example.account.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.account.model.Message;
import com.example.account.model.Statistic;
import com.example.account.repository.MessageRepository;
import com.example.account.repository.StatisticRepository;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PollingService {

    KafkaTemplate<String, Object> kafkaTemplate;
    MessageRepository messageRepository;
    StatisticRepository statisticRepository;

    @Scheduled(fixedDelay = 1000)
    public void producer() {
        List<Message> messages = messageRepository.findByStatus(false);
        for (Message message : messages) {
            kafkaTemplate.send("notification", message).whenComplete((result, ex) -> {
                if (ex == null) {
                    // handle success
                    // System.out.println(result.getRecordMetadata().partition());
                    message.setStatus(true);
                    messageRepository.save(message);
                    log.error(">>>>>>>>>>>>>> SUCCESS TO SEND MESSAGE TO KAFKA {}",
                            result.getRecordMetadata().partition());
                } else {
                    // handle fail, save db event failed
                    log.error(">>>>>>>>>>>>>> FAIL TO SEND MESSAGE TO KAFKA {}", ex);
                }
            });
        }

        List<Statistic> statistics = statisticRepository.findByStatus(false);
        for (Statistic statistic : statistics) {
            kafkaTemplate.send("statistic", statistic).whenComplete((result, ex) -> {
                if (ex == null) {
                    // handle success
                    // System.out.println(result.getRecordMetadata().partition());
                    statistic.setStatus(true);
                    statisticRepository.save(statistic);
                    log.error(">>>>>>>>>>>>>> SUCCESS TO SEND statistic TO KAFKA {}",
                            result.getRecordMetadata().partition());
                } else {
                    // handle fail, save db event failed
                    log.error(">>>>>>>>>>>>>> FAIL TO SEND statistic TO KAFKA {}", ex);
                }
            });
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void delete() {
        List<Message> messages = messageRepository.findByStatus(true);
        List<Integer> messageIds = messages.stream()
                .map(Message::getId)
                .collect(Collectors.toList());
        messageRepository.deleteAllByIdInBatch(messageIds);

        List<Statistic> statistics = statisticRepository.findByStatus(true);
        List<Integer> statisticIds = statistics.stream()
                .map(Statistic::getId)
                .collect(Collectors.toList());
        statisticRepository.deleteAllByIdInBatch(statisticIds);
    }
}
