package fiap.grupo11.msagendamento.service;

import fiap.grupo11.msagendamento.dto.AgendamentoRequest;
import fiap.grupo11.msagendamento.dto.AgendamentoResponse;
import fiap.grupo11.msagendamento.entity.Agendamento;
import fiap.grupo11.msagendamento.exception.AgendamentoConflictException;
import fiap.grupo11.msagendamento.exception.AgendamentoNotFoundException;
import fiap.grupo11.msagendamento.entity.OutboxEvent;
import fiap.grupo11.msagendamento.messaging.AppointmentEventSerializer;
import fiap.grupo11.msagendamento.repository.AgendamentoRepository;
import fiap.grupo11.msagendamento.repository.OutboxRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AgendamentoService {

    private final AgendamentoRepository repository;
    private final OutboxRepository outboxRepository;
    private final AppointmentEventSerializer eventSerializer;

    public AgendamentoService(AgendamentoRepository repository, OutboxRepository outboxRepository,
                              AppointmentEventSerializer eventSerializer) {
        this.repository = repository;
        this.outboxRepository = outboxRepository;
        this.eventSerializer = eventSerializer;
    }

    @Transactional
    public AgendamentoResponse create(AgendamentoRequest request, Authentication authentication) {
        requireRole(authentication, "ROLE_ENFERMEIRO",
                "Only nurses can register appointments");
        validatePeriod(request.scheduledAt(), request.endsAt());
        if (repository.existsOverlapping(request.professionalId(), request.scheduledAt(), request.endsAt())) {
            throw new AgendamentoConflictException();
        }
        Agendamento appointment = new Agendamento(
                null, request.patientId(), request.professionalId(), request.scheduledAt(), request.endsAt());
        Agendamento saved = repository.save(appointment);
        saveEvent(saved, "APPOINTMENT_CREATED");
        return AgendamentoResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> list(Authentication authentication) {
        requireProfessional(authentication);
        return repository.findAllByScheduledAtAsc().stream()
                .map(AgendamentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponse> listMine(Authentication authentication) {
        requireRole(authentication, "ROLE_PACIENTE",
                "Only patients can list their own appointments");
        List<Agendamento> appointments = repository.findByPatientIdOrderByScheduledAtAsc(
                currentUserId(authentication));
        return appointments.stream().map(AgendamentoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AgendamentoResponse get(Long id, Authentication authentication) {
        Agendamento appointment = find(id);
        if (!isProfessional(authentication) && !appointment.getPatientId().equals(currentUserId(authentication))) {
            throw new AccessDeniedException("Appointment belongs to another patient");
        }
        return AgendamentoResponse.from(appointment);
    }

    @Transactional
    public AgendamentoResponse update(Long id, AgendamentoRequest request, Authentication authentication) {
        requireRole(authentication, "ROLE_MEDICO",
                "Only doctors can edit appointments");
        Agendamento appointment = find(id);
        validatePeriod(request.scheduledAt(), request.endsAt());
        if (repository.existsOverlappingForAnotherAppointment(
                id, request.professionalId(), request.scheduledAt(), request.endsAt())) {
            throw new AgendamentoConflictException();
        }
        appointment.update(request.patientId(), request.professionalId(), request.scheduledAt(), request.endsAt());
        Agendamento saved = repository.save(appointment);
        saveEvent(saved, "APPOINTMENT_UPDATED");
        return AgendamentoResponse.from(saved);
    }

    private void saveEvent(Agendamento appointment, String eventType) {
        OutboxEvent event = outboxRepository.save(new OutboxEvent(null, appointment.getId(), eventType, ""));
        event.setPayload(eventSerializer.serialize(event.getId(), eventType, Instant.now(), appointment));
        outboxRepository.save(event);
    }

    private Agendamento find(Long id) {
        return repository.findById(id).orElseThrow(() -> new AgendamentoNotFoundException(id));
    }

    private void validatePeriod(Instant scheduledAt, Instant endsAt) {
        if (!endsAt.isAfter(scheduledAt)) {
            throw new IllegalArgumentException("endsAt must be after scheduledAt");
        }
    }

    private void requireRole(Authentication authentication, String requiredRole, String message) {
        currentUserId(authentication);
        boolean hasRequiredRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredRole::equals);
        if (!hasRequiredRole) {
            throw new AccessDeniedException(message);
        }
    }

    private void requireProfessional(Authentication authentication) {
        currentUserId(authentication);
        boolean hasProfessionalRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_MEDICO") || authority.equals("ROLE_ENFERMEIRO"));
        if (!hasProfessionalRole) {
            throw new AccessDeniedException("Only medical professionals can list all appointments");
        }
    }

    private boolean isProfessional(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_MEDICO") || authority.equals("ROLE_ENFERMEIRO"));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("Authenticated application user is required");
        }
        if (authentication.getPrincipal() instanceof fiap.grupo11.msagendamento.config.DemoUser user) {
            return user.getUserId();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return parseUserId(jwt.getSubject());
        }
        return parseUserId(authentication.getName());
    }

    private Long parseUserId(String userId) {
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException exception) {
            throw new AccessDeniedException("Authenticated application user has an invalid id");
        }
    }
}