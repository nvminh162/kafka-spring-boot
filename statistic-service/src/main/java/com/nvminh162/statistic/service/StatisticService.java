package com.nvminh162.statistic.service;

import com.nvminh162.statistic.model.Statistic;
import com.nvminh162.statistic.repository.StatisticRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StatisticService {

    StatisticRepository statisticRepository;

    @KafkaListener(id = "statisticGroup", topics = "statistic")
    public void listen(Statistic statistic) {
        log.info("Received: {}", statistic);
        statisticRepository.save(statistic);
        log.error("LOGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");

        // demo nếu listener này ném ra exception cố gắng gửi lại 2 lần, nếu không được sẽ gửi event vào dlt topic
        // throw new RuntimeException("Test error");
    }

    // @KafkaListener(id = "dltGroup", topics = "statistic.DLT")
    // public void listenDLT(Statistic statistic) {
    //     log.info("Received DLT: {}", statistic);
    //     // save to dlt db & gửi lại kafka broker server
    // }
}
