package com.task.kafkastarter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.kafkastarter.service.KafkaProducerService;
import com.task.kafkastarter.service.KafkaSyncTemplate;
import com.task.kafkastarter.service.impl.KafkaProducerServiceImpl;
import com.task.kafkastarter.service.impl.KafkaSyncTemplateImpl;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.support.serializer.JsonSerializer;

import static org.springframework.kafka.support.serializer.JsonDeserializer.TYPE_MAPPINGS;

@Configuration
@EnableConfigurationProperties(KafkaSyncProperties.class)
public class KafkaSyncConfig {

    @Bean
    @ConditionalOnProperty(prefix = "kafka-sync-starter", name = "create-topic", havingValue = "true")
    public KafkaAdmin.NewTopics topics(KafkaSyncProperties kafkaSyncProperties) {
        return new KafkaAdmin.NewTopics(
            TopicBuilder.name(
                kafkaSyncProperties.getProducer().getTopic())
                .partitions(kafkaSyncProperties.getProducer().getPartitions())
                .replicas(kafkaSyncProperties.getProducer().getReplicas())
                .build(),
            TopicBuilder.name(
                kafkaSyncProperties.getConsumer().getTopic())
                .partitions(kafkaSyncProperties.getConsumer().getPartitions())
                .replicas(kafkaSyncProperties.getConsumer().getReplicas())
                .build()
        );
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtils.enhancedObjectMapper();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(
        KafkaProperties kafkaProperties, ObjectMapper objectMapper, KafkaSyncProperties kafkaSyncProperties) {
        var props = kafkaProperties.buildProducerProperties(null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaSyncProperties.getBootstrapServers());

        var kafkaProducerFactory = new DefaultKafkaProducerFactory<String, Object>(props);
        kafkaProducerFactory.setValueSerializer(new JsonSerializer<>(objectMapper));
        return kafkaProducerFactory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(
        KafkaProperties kafkaProperties, KafkaSyncProperties kafkaSyncProperties) {
        var props = kafkaProperties.buildConsumerProperties(null);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaSyncProperties.getBootstrapServers());
        props.put(TYPE_MAPPINGS, "java.lang.String");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 3);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 3_000);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> concurrentKafkaListenerContainerFactory(
        ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory
            = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.getContainerProperties().setIdleBetweenPolls(1_000);
        factory.getContainerProperties().setPollTimeout(1_000);
        return factory;
    }

    @Bean
    public KafkaProducerService kafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate,
                                                    KafkaSyncProperties kafkaSyncProperties) {
        return new KafkaProducerServiceImpl(kafkaSyncProperties.getProducer().getTopic(), kafkaTemplate);
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
