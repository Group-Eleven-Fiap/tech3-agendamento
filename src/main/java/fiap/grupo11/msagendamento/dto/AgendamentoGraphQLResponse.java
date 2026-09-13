package fiap.grupo11.msagendamento.dto;

import fiap.grupo11.msagendamento.entity.Agendamento;

import java.time.Instant;

public record AgendamentoGraphQLResponse(
        Long id,
        Long patientId,
        Long professionalId,
        Instant scheduledAt,
        Instant endsAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static AgendamentoGraphQLResponse from(Agendamento appointment) {
        return new AgendamentoGraphQLResponse(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getProfessionalId(),
                appointment.getScheduledAt(),
                appointment.getEndsAt(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt());
    }
}
