package fiap.grupo11.msagendamento.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fiap.grupo11.msagendamento.entity.Agendamento;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentEventSerializerTest {

    private final AppointmentEventSerializer serializer = new AppointmentEventSerializer(
            new ObjectMapper().registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));

    @Test
    void serializesTheDocumentedAppointmentEventEnvelope() {
        Long appointmentId = 10L;
        Long eventId = 20L;
        Instant scheduledAt = Instant.parse("2030-04-15T14:00:00Z");
        Agendamento appointment = new Agendamento(
                appointmentId, 3L, 1L, scheduledAt, scheduledAt.plusSeconds(1800));

        String payload = serializer.serialize(eventId, "APPOINTMENT_CREATED", Instant.parse("2030-04-01T12:00:00Z"), appointment);

        assertThat(payload).contains("\"eventId\":" + eventId);
        assertThat(payload).contains("\"eventType\":\"APPOINTMENT_CREATED\"");
        assertThat(payload).contains("\"aggregateId\":" + appointmentId);
        assertThat(payload).contains("\"scheduledAt\":\"2030-04-15T14:00:00Z\"");
    }
}