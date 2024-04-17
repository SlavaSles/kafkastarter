package com.task.kafkastarter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.kafkastarter.service.KafkaProducerServiceImpl;
import com.task.kafkastarter.service.RestServiceImpl;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(KafkaSyncProperties.class)
public class KafkaSyncConfig {

//    public static String TOPIC;

//    public KafkaSyncConfig(@Value("${kafka.sync.starter.topic}") String topicName) {
//        if (topicName.isBlank()) {
//            topicName = "demo-topic";
//        }
//        KafkaSyncConfig.TOPIC = topicName;
//    }

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
        KafkaProperties kafkaProperties, ObjectMapper mapper, KafkaSyncProperties kafkaSyncProperties) {
        var props = kafkaProperties.buildProducerProperties((SslBundles) null);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());

        var kafkaProducerFactory = new DefaultKafkaProducerFactory<String, String>(props);
        kafkaProducerFactory.setValueSerializer(new JsonSerializer<>(mapper));
        return kafkaProducerFactory;
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic topic(KafkaSyncProperties kafkaSyncProperties) {
        return TopicBuilder.name(kafkaSyncProperties.getTopic()).partitions(1).replicas(1).build();
    }

    @Bean
    public KafkaProducerServiceImpl kafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, KafkaSyncProperties kafkaSyncProperties) {
        return new KafkaProducerServiceImpl(kafkaSyncProperties, kafkaTemplate);
    }

    @Bean
    public RestServiceImpl restService(KafkaProducerServiceImpl kafkaProducerService, ObjectMapper objectMapper) {
        return new RestServiceImpl(new ObjectMapper(), kafkaProducerService);
    }

//    @Bean
//    public KafkaConsumerService kafkaConsumerService(RestService restService) {
//        KafkaConsumerServiceImpl kafkaConsumerService = new KafkaConsumerServiceImpl(restService);
//        return (KafkaConsumerService) kafkaConsumerService;
//    }
}
