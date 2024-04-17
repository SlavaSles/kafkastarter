package com.task.kafkastarter.service;

import com.task.kafkastarter.config.KafkaSyncProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl {

    private final KafkaSyncProperties kafkaSyncProperties;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String exchangerUuid, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(kafkaSyncProperties.getTopic(), message);
        record.headers().add(new RecordHeader("exchangerId", exchangerUuid.getBytes()));
        try {
            kafkaTemplate.send(record).whenComplete(
                (result, ex) -> {
                    if (ex == null) {
                        log.info("Message {} with offset = {} and id = {} was sent", message,
                            result.getRecordMetadata().offset(), exchangerUuid);
                    } else {
                        log.error("Message {} with id = {} was not sent", message, exchangerUuid);
                    }
                });
        } catch (Exception ex) {
            log.error("Sending error for message {} with id = {}. Error: {}", message, exchangerUuid, ex.getMessage());
        }
    }
}
