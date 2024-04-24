package com.task.kafkastarter.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.kafkastarter.dto.RequestDto;
import com.task.kafkastarter.dto.ResponseDto;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaSyncTemplateImpl implements KafkaSyncTemplate {

    private final Map<String, Exchanger<Object>> exchangerMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    private final KafkaProducerService kafkaProducerService;

    @Value("${kafka-sync-starter.timeout:5000}")
    private final Long EXCHANGE_TIMEOUT;

    private final String HEADER_NAME = "exchangerId";

    private final String consumerTopic = "${kafka-sync-starter.consumer.topic:demo-topic}";

    private final String kafkaConsumerGroupId = "${kafka-sync-starter.group-id:consumer-group1}";

    public ResponseDto kafkaExchange(RequestDto requestDto) {
        Exchanger<Object> exchanger = new Exchanger<>();
        String exchangerUuid = UUID.randomUUID().toString();
        exchangerMap.put(exchangerUuid, exchanger);
        Object answer;
        try {
            kafkaProducerService.sendMessage(exchangerUuid, objectMapper.writeValueAsString(requestDto),
                HEADER_NAME);
            answer = exchanger.exchange(null, EXCHANGE_TIMEOUT, TimeUnit.MILLISECONDS);
            exchangerMap.remove(exchangerUuid);
        } catch (InterruptedException | TimeoutException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("RestService successfully convert message to ResponseDto = {}", ((ResponseDto) answer).message());
        return (ResponseDto) answer;
    }

    @KafkaListener(topics = consumerTopic, groupId = kafkaConsumerGroupId)
    private void receiveMessage(ConsumerRecord<String, String> record) {
        String exchangerUuid = getExchangerUuid(record.headers());
        String tempMessage = record.value().replace("\\\"", "\"");
        int size = tempMessage.length();
        String message = tempMessage.substring(1, size - 1);
        log.info("Consumer receive value with id {} and message {} ", exchangerUuid, message);
        if (exchangerUuid != null) {
            exchangeMessage(exchangerUuid, message);
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
        if (exchangerMap.containsKey(exchangerUuid)) {
            log.info("Returning response message = {}", message);
            try {
                RequestDto requestDto = objectMapper.readValue(message, RequestDto.class);
                ResponseDto responseDto = new ResponseDto(requestDto.getMessage());
                Exchanger<Object> exchanger = exchangerMap.get(exchangerUuid);
                exchanger.exchange(responseDto, EXCHANGE_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | TimeoutException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
