package com.task.kafkastarter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Класс, описывающий формат сообщений в запросах.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDto {

    /**
     * Сообщение запроса.
     */
    private String message;
}
