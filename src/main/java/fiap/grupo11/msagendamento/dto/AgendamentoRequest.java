package fiap.grupo11.msagendamento.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record AgendamentoRequest(
        @NotNull(message = "patientId is required")
        @Positive(message = "patientId must be positive")
        Long patientId,
        @NotNull(message = "professionalId is required")
        @Positive(message = "professionalId must be positive")
        Long professionalId,
        @NotNull(message = "scheduledAt is required")
        @Future(message = "scheduledAt must be in the future")
        Instant scheduledAt,
        @NotNull(message = "endsAt is required")
        @Future(message = "endsAt must be in the future")
        Instant endsAt
) {
}