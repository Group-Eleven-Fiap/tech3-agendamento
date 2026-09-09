package fiap.grupo11.msagendamento.repository;

import fiap.grupo11.msagendamento.entity.OutboxEvent;
import fiap.grupo11.msagendamento.entity.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OutboxRepositoryTest {

    @Autowired
    private OutboxRepository repository;

    @Test
    void findsPendingEventsThatAreReadyForPublication() {
        OutboxEvent event = repository.save(new OutboxEvent(
                null, 10L, "APPOINTMENT_CREATED", "{}"));

        List<OutboxEvent> ready = repository.findReadyToPublish(
                OutboxStatus.PENDING, Instant.now().plusSeconds(1));

        assertThat(ready).extracting(OutboxEvent::getId).containsExactly(event.getId());
    }
}