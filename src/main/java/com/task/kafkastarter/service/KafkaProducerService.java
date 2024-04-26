package com.task.kafkastarter.service;

/**
 * Сервис, публикующий сообщение в Кафку.
 */
public interface KafkaProducerService {

    /**
     * Метод, публикущий сообщение в Кафку.
     *
     * @param exchangerUuid идентификатор {@link java.util.concurrent.Exchanger} для конкретного запроса.
     * @param message объект для отправки сообщения в Кафку.
     */
    void sendMessage(String exchangerUuid, Object message, String headerName);
}
