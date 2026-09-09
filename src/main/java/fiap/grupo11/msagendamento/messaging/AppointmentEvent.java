package fiap.grupo11.msagendamento.messaging;

import java.time.Instant;

public record AppointmentEvent(
        Long eventId,
        String eventType,
        Instant occurredAt,
        Long aggregateId,
        Long patientId,
        Long professionalId,
        Instant scheduledAt,
        Instant endsAt
) {
}