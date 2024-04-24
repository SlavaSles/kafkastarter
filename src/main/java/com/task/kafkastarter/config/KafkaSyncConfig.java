package com.task.kafkastarter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.kafkastarter.service.KafkaProducerService;
import com.task.kafkastarter.service.KafkaSyncTemplate;
import com.task.kafkastarter.service.impl.KafkaProducerServiceImpl;
import com.task.kafkastarter.service.impl.KafkaSyncTemplateImpl;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableConfigurationProperties(KafkaSyncProperties.class)
public class KafkaSyncConfig {

    @Bean
    @Qualifier("producer")
    public NewTopic producerTopic(KafkaSyncProperties kafkaSyncProperties) {
        return TopicBuilder.name(
            kafkaSyncProperties.getProducer().getTopic())
            .partitions(kafkaSyncProperties.getProducer().getPartitions())
            .replicas(kafkaSyncProperties.getProducer().getReplicas())
            .build();
    }

    @Bean
    @Qualifier("consumer")
    public NewTopic consumerTopic(KafkaSyncProperties kafkaSyncProperties) {
        return TopicBuilder.name(
            kafkaSyncProperties.getConsumer().getTopic())
            .partitions(kafkaSyncProperties.getConsumer().getPartitions())
            .replicas(kafkaSyncProperties.getConsumer().getReplicas())
            .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtils.enhancedObjectMapper();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(
        KafkaProperties kafkaProperties, ObjectMapper objectMapper, KafkaSyncProperties kafkaSyncProperties) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaSyncProperties.getBootstrapServers());

        var kafkaProducerFactory = new DefaultKafkaProducerFactory<String, String>(props);
        kafkaProducerFactory.setValueSerializer(new JsonSerializer<>(objectMapper));
        return kafkaProducerFactory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaProducerService kafkaProducerService(KafkaTemplate<String, String> kafkaTemplate,
                                                     @Qualifier("producer") NewTopic producerTopic) {
        return new KafkaProducerServiceImpl(producerTopic, kafkaTemplate);
    }

    @Bean
    public KafkaSyncTemplate kafkaSyncTemplate(KafkaProducerService kafkaProducerService,
                                               ObjectMapper objectMapper,
                                               KafkaSyncProperties kafkaSyncProperties) {
        return new KafkaSyncTemplateImpl(objectMapper,
            kafkaProducerService,
            kafkaSyncProperties.getTimeout());
    }
}
