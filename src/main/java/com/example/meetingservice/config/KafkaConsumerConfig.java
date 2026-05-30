package com.example.meetingservice.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import tools.jackson.core.JacksonException;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public MeetingDltDestinationResolver meetingDltDestinationResolver(
            @Value("${spring.kafka.consumer.group-id}") String consumerGroupId
    ) {
        return new MeetingDltDestinationResolver(consumerGroupId);
    }

    @Bean
    public ProducerFactory<Object, Object> meetingDltProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties();
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<Object, Object> meetingDltKafkaTemplate(
            ProducerFactory<Object, Object> meetingDltProducerFactory
    ) {
        return new KafkaTemplate<>(meetingDltProducerFactory);
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<Object, Object> meetingDltKafkaTemplate,
            MeetingDltDestinationResolver meetingDltDestinationResolver
    ) {
        return new DeadLetterPublishingRecoverer(meetingDltKafkaTemplate, meetingDltDestinationResolver);
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer,
            KafkaRetryProperties retryProperties
    ) {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(retryProperties.getMaxRetries());
        backOff.setInitialInterval(retryProperties.getInitialInterval().toMillis());
        backOff.setMultiplier(retryProperties.getMultiplier());
        backOff.setMaxInterval(retryProperties.getMaxInterval().toMillis());

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backOff);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, JacksonException.class);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }
}
