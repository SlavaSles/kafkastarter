package com.task.kafkastarter.service;

import com.task.kafkastarter.dto.RequestDto;
import com.task.kafkastarter.dto.ResponseDto;

/**
 * Сервис, публикующий сообщение в Кафку и возвращающий его обратно.
 */
public interface RestService {

    /**
     * Метод, вызывающий продьюсера Кафки {@link KafkaProducerService}, для публикации сообщения.
     *
     * @param requestDto тело запрос с полем message в {@link RequestDto}.
     * @return возвращает прочитанное из Кафки сообщение {@link ResponseDto}
     */
    ResponseDto sendMessage(RequestDto requestDto);

    /**
     * Метод, получающий ответ от listner'а {@link KafkaConsumerService}, для передачи его обратно пользователю.
     *
     * @param exchangerUuid идентификатор {@link java.util.concurrent.Exchanger} для конкретного запроса.
     * @param message сериализованное сообщение из Кафки.
     */
    void receiveMessage(String exchangerUuid, String message);
}
