package fiap.grupo11.msagendamento.service;

import fiap.grupo11.msagendamento.config.DemoUser;
import fiap.grupo11.msagendamento.dto.AgendamentoRequest;
import fiap.grupo11.msagendamento.entity.Agendamento;
import fiap.grupo11.msagendamento.entity.OutboxEvent;
import fiap.grupo11.msagendamento.exception.AgendamentoConflictException;
import fiap.grupo11.msagendamento.messaging.AppointmentEventSerializer;
import fiap.grupo11.msagendamento.repository.AgendamentoRepository;
import fiap.grupo11.msagendamento.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository repository;

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private AppointmentEventSerializer eventSerializer;

    @InjectMocks
    private AgendamentoService service;

    @Test
    void nurseCanCreateAnAppointment() {
        AgendamentoRequest request = request(3L, 1L);
        when(repository.existsOverlapping(any(), any(), any())).thenReturn(false);
        when(repository.save(any(Agendamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eventSerializer.serialize(any(), any(), any(), any())).thenReturn("{}");

        var response = service.create(request, authentication(1L, "ROLE_ENFERMEIRO"));

        assertThat(response.patientId()).isEqualTo(3L);
        assertThat(response.professionalId()).isEqualTo(1L);
        verify(repository).save(any(Agendamento.class));
        verify(outboxRepository, times(2)).save(any(OutboxEvent.class));
    }

    @Test
    void patientCannotCreateAnAppointment() {
        assertThatThrownBy(() -> service.create(
                request(3L, 1L), authentication(3L, "ROLE_PACIENTE")))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void doctorCannotCreateAnAppointment() {
        assertThatThrownBy(() -> service.create(
                request(3L, 1L), authentication(2L, "ROLE_MEDICO")))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void nurseCannotEditAnAppointment() {
        assertThatThrownBy(() -> service.update(
                1L, request(3L, 1L), authentication(1L, "ROLE_ENFERMEIRO")))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findById(any());
    }

    @Test
    void conflictingProfessionalScheduleIsRejected() {
        when(repository.existsOverlapping(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.create(
                request(3L, 1L), authentication(2L, "ROLE_ENFERMEIRO")))
                .isInstanceOf(AgendamentoConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void patientListUsesTheAuthenticatedSubjectId() {
        Agendamento ownAppointment = new Agendamento(
                1L, 3L, 1L,
                Instant.now().plusSeconds(3600), Instant.now().plusSeconds(5400));
        when(repository.findByPatientIdOrderByScheduledAtAsc(3L))
                .thenReturn(List.of(ownAppointment));

        var appointments = service.listMine(authentication(3L, "ROLE_PACIENTE"));

        assertThat(appointments).hasSize(1);
        assertThat(appointments.get(0).patientId()).isEqualTo(3L);
        verify(repository).findByPatientIdOrderByScheduledAtAsc(3L);
        verify(repository, never()).findAllByScheduledAtAsc();
    }

    @Test
    void patientCannotListAllAppointments() {
        assertThatThrownBy(() -> service.list(authentication(3L, "ROLE_PACIENTE")))
                .isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findAllByScheduledAtAsc();
    }

    private AgendamentoRequest request(Long patientId, Long professionalId) {
        Instant scheduledAt = Instant.now().plusSeconds(3600);
        return new AgendamentoRequest(patientId, professionalId, scheduledAt, scheduledAt.plusSeconds(1800));
    }

    private Authentication authentication(Long userId, String role) {
        DemoUser user = new DemoUser("user", "password", userId,
                List.of(new SimpleGrantedAuthority(role)));
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}