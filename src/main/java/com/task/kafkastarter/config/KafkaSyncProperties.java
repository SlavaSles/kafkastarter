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

    public TopicConfig getConsumer() {
        if (consumer != null) {
            if (consumer.getPartitions() == null) {
                consumer.setPartitions(1);
            }
            if (consumer.getReplicas() == null) {
                consumer.setReplicas(1);
            }
        }
        return consumer;
    }

    public TopicConfig getProducer() {
        if (producer != null) {
            if (producer.getPartitions() == null) {
                producer.setPartitions(1);
            }
            if (producer.getReplicas() == null) {
                producer.setReplicas(1);
            }
        }
        return producer;
    }

    public Long getTimeout() {
        if (timeout == null) {
            return 5_000L;
        }
        return timeout;
    }
}
