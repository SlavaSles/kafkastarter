package com.task.kafkastarter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.kafkastarter.dto.RequestDto;
import com.task.kafkastarter.dto.ResponseDto;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class RestServiceImpl {

    private final Map<String, Exchanger<Object>> exchangerMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    private final KafkaProducerServiceImpl kafkaProducerService;

    public ResponseDto sendMessage(RequestDto requestDto) {
        Exchanger<Object> exchanger = new Exchanger<>();
        String exchangerUuid = UUID.randomUUID().toString();
        exchangerMap.put(exchangerUuid, exchanger);
        Object answer;
        try {
            kafkaProducerService.send(exchangerUuid, objectMapper.writeValueAsString(requestDto));
            answer = exchanger.exchange(null, 5_000, TimeUnit.MILLISECONDS);
            exchangerMap.remove(exchangerUuid);
        } catch (InterruptedException | TimeoutException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("RestService successfully convert message to ResponseDto = {}", ((ResponseDto) answer).message());
        return (ResponseDto) answer;
    }

    public void receiveMessage(String exchangerUuid, String message) {
        if (exchangerMap.containsKey(exchangerUuid)) {
            log.info("RestService receive message = {}", message);
            try {
                RequestDto requestDto = objectMapper. readValue(message, RequestDto.class);
                log.info("Message convert to RequestDto = {}", requestDto);
                ResponseDto responseDto = new ResponseDto(requestDto.getMessage());
                log.info("Message convert to ResponseDto = {}", responseDto);
                Exchanger<Object> exchanger = exchangerMap.get(exchangerUuid);
                exchanger.exchange(responseDto, 5_000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | TimeoutException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
