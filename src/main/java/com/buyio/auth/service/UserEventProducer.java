package com.buyio.auth.service;

import com.buyio.auth.dto.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventProducer.class);
    private static final String TOPIC = "UserEvents";

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserEvent(String eventType, Long userId, String username, String email) {
        UserEvent event = UserEvent.builder()
                .eventType(eventType)
                .userId(userId)
                .username(username)
                .email(email)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            kafkaTemplate.send(TOPIC, username, event);
            logger.info("Evento Kafka publicado exitosamente en {}: {}", TOPIC, event);
        } catch (Exception e) {
            logger.error("Error al publicar evento en Kafka: {}", e.getMessage(), e);
        }
    }
}