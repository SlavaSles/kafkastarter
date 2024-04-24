package com.task.kafkastarter.service;

import com.task.kafkastarter.dto.RequestDto;
import com.task.kafkastarter.dto.ResponseDto;

/**
 * Сервис, публикующий сообщение в Кафку и возвращающий его обратно.
 */
public interface KafkaSyncTemplate {

    /**
     * Метод, вызывающий продьюсера Кафки {@link KafkaProducerService} для публикации сообщения и возвращающий ответ
     * клиенту.
     *
     * @param requestDto тело запрос с полем message в {@link RequestDto}.
     * @return возвращает прочитанное из Кафки сообщение {@link ResponseDto}
     */
    ResponseDto kafkaExchange(RequestDto requestDto);
}
