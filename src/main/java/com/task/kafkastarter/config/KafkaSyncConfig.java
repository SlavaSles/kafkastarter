package com.task.kafkastarter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.kafkastarter.service.KafkaConsumerService;
import com.task.kafkastarter.service.KafkaProducerService;
import com.task.kafkastarter.service.RestService;
import com.task.kafkastarter.service.impl.KafkaConsumerServiceImpl;
import com.task.kafkastarter.service.impl.KafkaProducerServiceImpl;
import com.task.kafkastarter.service.impl.RestServiceImpl;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaSyncConfig {

    public static String TOPIC;

    public KafkaSyncConfig(@Value("${kafka.sync.starter.topic}") String topicName) {
        if (topicName.isBlank()) {
            topicName = "demo-topic";
        }
        KafkaSyncConfig.TOPIC = topicName;
    }

//    @Bean("PropertyTopic")
//    @ConditionalOnProperty(prefix = "kafka.sync.starter", name = "topic")
//    String createKafkaTopic() {
//        String topic = "${kafka.sync.starter.topic}";
//        return topic;
//    }

//    @Bean
//    KafkaTopic createTopic() {
//        String topic = "${kafka.sync.starter.topic}";
//        if (topic == null) {
//            topic = "demo-topic";
//        }
//        KafkaTopic kafkaTopic = new KafkaTopic();
//        kafkaTopic.setTopic(topic);
//        return kafkaTopic;
//    }

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtils.enhancedObjectMapper();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(
        KafkaProperties kafkaProperties, ObjectMapper mapper) {
        var props = kafkaProperties.buildProducerProperties((SslBundles) null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        var kafkaProducerFactory = new DefaultKafkaProducerFactory<String, String>(props);
        kafkaProducerFactory.setValueSerializer(new JsonSerializer<>(mapper));
        return kafkaProducerFactory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(TOPIC).partitions(1).replicas(1).build();
    }

    @Bean
    public KafkaProducerService kafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        KafkaProducerServiceImpl kafkaProducerService = new KafkaProducerServiceImpl(kafkaTemplate);
        return (KafkaProducerService) kafkaProducerService;
    }

    @Bean
    public RestService restService(KafkaProducerService kafkaProducerService, ObjectMapper objectMapper) {
        RestServiceImpl restService = new RestServiceImpl(new ObjectMapper(), kafkaProducerService);
        return (RestService) restService;
    }

//    @Bean
//    public KafkaConsumerService kafkaConsumerService(RestService restService) {
//        KafkaConsumerServiceImpl kafkaConsumerService = new KafkaConsumerServiceImpl(restService);
//        return (KafkaConsumerService) kafkaConsumerService;
//    }
}
