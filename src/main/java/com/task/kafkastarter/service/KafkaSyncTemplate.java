package com.task.kafkastarter.service;

/**
 * Сервис, публикующий сообщение в Кафку и возвращающий его обратно.
 */
public interface KafkaSyncTemplate<T, S> {

    /**
     * Метод, вызывающий продьюсера Кафки {@link KafkaProducerService} для публикации сообщения и возвращающий ответ
     * клиенту.
     *
     * @param t тело запроса типа T.
     * @param s объект для получения ответа из Кафки.
     * @return возвращает прочитанное из Кафки сообщение.
     */
    S kafkaExchange(T t, S s);
}
