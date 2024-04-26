package com.task.kafkastarter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.kafkastarter.service.KafkaProducerService;
import com.task.kafkastarter.service.KafkaSyncTemplate;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
public class KafkaSyncTemplateImpl<T, S> implements KafkaSyncTemplate<T, S> {

    private final Map<String, Exchanger<S>> exchangerMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    private final KafkaProducerService kafkaProducerService;

    private final Long EXCHANGE_TIMEOUT;

    private final String HEADER_NAME = "exchangerId";

    private final String consumerTopic = "${kafka-sync-starter.consumer.topic}";

    private final String kafkaConsumerGroupId = "${kafka-sync-starter.group-id}";

    private Class<?> clazz;

    public S kafkaExchange(T t, S s) {
        clazz = s.getClass();
        Exchanger<S> exchanger = new Exchanger<>();
        String exchangerUuid = UUID.randomUUID().toString();
        exchangerMap.put(exchangerUuid, exchanger);
        S answer;
        try {
            kafkaProducerService.sendMessage(exchangerUuid, t, HEADER_NAME);
            answer = exchanger.exchange(s, EXCHANGE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        } finally {
            exchangerMap.remove(exchangerUuid);
        }
        log.info("RestService successfully convert message to ResponseDto = {}", answer.toString());
        return answer;
    }

    @KafkaListener(topics = consumerTopic, groupId = kafkaConsumerGroupId)
    private void receiveMessage(ConsumerRecord<String, String> record) {
        String exchangerUuid = getExchangerUuid(record.headers());
        log.info("Consumer receive value with id {} and message {} ", exchangerUuid, record.value());
        if (exchangerUuid != null) {
            exchangeMessage(exchangerUuid, record.value());
        }
    }

    private String getExchangerUuid(Headers headers) {
        Header id = headers.headers(HEADER_NAME).iterator().next();
        if (id == null) {
            return null;
        }
        return new String(id.value());
    }

    private void exchangeMessage(String exchangerUuid, String message) {
        if (!exchangerMap.containsKey(exchangerUuid)) {
            return;
        }
        log.info("Returning response message = {}", message);
        try {
            S response = (S) objectMapper.readValue(message, clazz);
            Exchanger<S> exchanger = exchangerMap.get(exchangerUuid);
            if (exchanger != null) {
                exchanger.exchange(response, EXCHANGE_TIMEOUT, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException | TimeoutException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
