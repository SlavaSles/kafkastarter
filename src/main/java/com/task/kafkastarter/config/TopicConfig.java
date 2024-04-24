package com.task.kafkastarter.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicConfig {

    private String topic;

    private Integer partitions;

    private Integer replicas;
}
