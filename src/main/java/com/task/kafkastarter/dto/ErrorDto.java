package com.task.kafkastarter.dto;

/**
 * Класс, описывающий формат сообщений об ошибках.
 *
 * @param errorCode код ошибки HTTP.
 * @param errorMessage поясняющее сообщение об ошибке.
 */
public record ErrorDto (

    int errorCode,

    String errorMessage
) {
}
