package com.task.kafkastarter.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Сервис, считывающий сообщения из топика Кафки.
 */
public interface KafkaConsumerService {

    /**
     * Метод, считывающий сообщение из Кафки.
     *
     * @param record запись из Кафки, содержащая header и тело запроса.
     */
    void listen(ConsumerRecord<String, String> record);
}
