package com.task.kafkastarter.service;

/**
 * Сервис, публикующий сообщение в Кафку.
 */
public interface KafkaProducerService {

    /**
     * Метод, публикущий сообщение в Кафку.
     *
     * @param exchangerUuid идентификатор {@link java.util.concurrent.Exchanger} для конкретного запроса.
     * @param message сериализованное сообщение для Кафки.
     */
    void send(String exchangerUuid, String message);
}
