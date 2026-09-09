package fiap.grupo11.msagendamento.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.grupo11.msagendamento.entity.Agendamento;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AppointmentEventSerializer {

    private final ObjectMapper objectMapper;

    public AppointmentEventSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(Long eventId, String eventType, Instant occurredAt, Agendamento appointment) {
        AppointmentEvent event = new AppointmentEvent(
                eventId,
                eventType,
                occurredAt,
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getProfessionalId(),
                appointment.getScheduledAt(),
                appointment.getEndsAt());
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize appointment event", exception);
        }
    }
}