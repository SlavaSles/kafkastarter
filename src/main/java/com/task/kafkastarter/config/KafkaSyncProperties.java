package com.task.kafkastarter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka-sync-starter")
public class KafkaSyncProperties {

    private TopicConfig producer;

    private TopicConfig consumer;

    private String bootstrapServers;

    private String groupId;

    private Long timeout;
}
