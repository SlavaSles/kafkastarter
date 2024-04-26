package com.task.kafkastarter.service.impl;

import com.task.kafkastarter.exception.KafkaStarterException;
import com.task.kafkastarter.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {
    
    private final String topicName;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendMessage(String exchangerUuid, Object message, String HEADER_NAME) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topicName, message);
        record.headers().add(new RecordHeader(HEADER_NAME, exchangerUuid.getBytes()));
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
            throw new KafkaStarterException(ex);
        }
    }
}
