package com.eventDrivenKafka.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name("order-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderEventsDltTopic() {
        return TopicBuilder.name("order-events.DLT")
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000")
                .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name("payment-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentEventsDltTopic() {
        return TopicBuilder.name("payment-events.DLT")
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000")
                .build();
    }

    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name("inventory-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryEventsDltTopic() {
        return TopicBuilder.name("inventory-events.DLT")
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000")
                .build();
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> cf,
            KafkaTemplate<String, String> kafkaTemplate) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(cf);
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) -> new TopicPartition(r.topic() + ".DLT", r.partition()));
        var handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));
        factory.setCommonErrorHandler(handler);
        return factory;
    }
}

