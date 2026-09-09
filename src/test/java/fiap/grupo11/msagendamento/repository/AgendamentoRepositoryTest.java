package fiap.grupo11.msagendamento.repository;

import fiap.grupo11.msagendamento.entity.Agendamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AgendamentoRepositoryTest {

    @Autowired
    private AgendamentoRepository repository;

    @Test
    void findsAppointmentsByPatientInScheduledOrder() {
        Instant first = Instant.parse("2026-04-15T14:00:00Z");
        Instant second = Instant.parse("2026-04-15T15:00:00Z");
        repository.save(new Agendamento(null, 3L, 1L,
                second, second.plusSeconds(1800)));
        repository.save(new Agendamento(null, 3L, 1L,
                first, first.plusSeconds(1800)));
        repository.save(new Agendamento(null, 4L, 1L,
                first, first.plusSeconds(1800)));

        List<Agendamento> appointments = repository.findByPatientIdOrderByScheduledAtAsc(3L);

        assertThat(appointments).hasSize(2);
        assertThat(appointments.get(0).getScheduledAt()).isEqualTo(first);
        assertThat(appointments.get(1).getScheduledAt()).isEqualTo(second);
    }
}