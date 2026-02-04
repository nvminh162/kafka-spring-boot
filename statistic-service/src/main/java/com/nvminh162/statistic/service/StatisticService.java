package com.nvminh162.statistic.service;

import com.nvminh162.statistic.model.Statistic;
import com.nvminh162.statistic.repository.StatisticRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StatisticService {

    StatisticRepository statisticRepository;

    /*
    Tạo topics retry 2000 x 2 = 4000, 4000 x 2 = 8000, 8000 x 2 = 16000
    */
    @RetryableTopic(attempts = "5", dltTopicSuffix = "dlt", backOff = @BackOff(delay = 2_000, multiplier = 2))
    @KafkaListener(id = "statisticGroup", topics = "statistic")
    public void listen(Statistic statistic) {
        log.info("Received: {}", statistic);
        // statisticRepository.save(statistic);
        // demo nếu listener này ném ra exception cố gắng gửi lại 2 lần, nếu không được
        // sẽ gửi event vào dlt topic
        throw new RuntimeException();
    }

    @KafkaListener(id = "dltGroup", topics = "statistic-dlt")
    public void listenDLT(Statistic statistic) {
        log.info("Received DLT: {}", statistic.getMessage());
        // save to dlt db & gửi lại kafka broker server
    }
}
