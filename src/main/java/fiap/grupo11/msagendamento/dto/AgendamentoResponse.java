package fiap.grupo11.msagendamento.dto;

import fiap.grupo11.msagendamento.entity.Agendamento;

import java.time.Instant;

public record AgendamentoResponse(
        Long id,
        Long patientId,
        Long professionalId,
        Instant scheduledAt,
        Instant endsAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static AgendamentoResponse from(Agendamento appointment) {
        return new AgendamentoResponse(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getProfessionalId(),
                appointment.getScheduledAt(),
                appointment.getEndsAt(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt());
    }
}