package com.task.kafkastarter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl{

    private final RestServiceImpl restService;

    @KafkaListener(topics = "demo-topic", groupId = "consumer-group1")
    public void listen(ConsumerRecord<String, String> record) {
        String exchangerUuid = getExchangerUuid(record.headers());
        log.info("Consumer recieve value with id {} and message {} ", exchangerUuid, record.value());
        if (exchangerUuid != null) {
            restService.receiveMessage(exchangerUuid, record.value());
        }
    }

    private String getExchangerUuid(Headers headers) {
        Header id = headers.headers("exchangerId").iterator().next();
        if (id == null) {
            return null;
        }
        return new String(id.value());
    }
}
