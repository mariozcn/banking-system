package com.banking.banking_monolith.outbox;

import com.banking.banking_monolith.event.TransferCompletedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxEventRepository outboxEventRepository,
                                KafkaTemplate<String, Object> kafkaTemplate,
                                ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findTop50ByProcessedAtIsNullOrderByCreatedAtAsc();
        if (events.isEmpty()) return;

        log.info("Publishing {} outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                TransferCompletedEvent payload = objectMapper.readValue(
                        event.getPayload(), TransferCompletedEvent.class);
                kafkaTemplate.send("transaction-events", payload);
                event.setProcessedAt(Instant.now());
                outboxEventRepository.save(event);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize outbox event {}", event.getId(), e);
            }
        }
    }
}