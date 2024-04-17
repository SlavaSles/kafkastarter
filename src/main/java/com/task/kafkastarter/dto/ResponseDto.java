package com.task.kafkastarter.dto;

/**
 * Класс, описывающий формат ответа на запрос.
 *
 * @param message сообщение ответа.
 */
public record ResponseDto (
    String message
) {
}
