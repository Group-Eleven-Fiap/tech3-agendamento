package fiap.grupo11.msagendamento.messaging;

import fiap.grupo11.msagendamento.entity.OutboxEvent;
import fiap.grupo11.msagendamento.entity.OutboxStatus;
import fiap.grupo11.msagendamento.repository.OutboxRepository;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.outbox.publisher-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private final OutboxRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;
    private final long retryDelaySeconds;

    public OutboxPublisher(
            OutboxRepository repository,
            RabbitTemplate rabbitTemplate,
            @Value("${app.messaging.exchange:agendamento.events}") String exchange,
            @Value("${app.messaging.routing-key:agendamento.appointment.changed}") String routingKey,
            @Value("${app.outbox.retry-delay-seconds:30}") long retryDelaySeconds) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.retryDelaySeconds = retryDelaySeconds;
    }

    @Scheduled(fixedDelayString = "${app.outbox.publisher-delay-ms:5000}")
    @Transactional
    public void publishPending() {
        Instant now = Instant.now();
        List<OutboxEvent> events = repository.findReadyToPublish(OutboxStatus.PENDING, now);
        events.forEach(event -> publish(event, now));
    }

    private void publish(OutboxEvent event, Instant now) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event.getPayload());
            event.markDelivered(now);
        } catch (AmqpException exception) {
            event.scheduleRetry(now.plusSeconds(retryDelaySeconds));
        }
        repository.save(event);
    }
}