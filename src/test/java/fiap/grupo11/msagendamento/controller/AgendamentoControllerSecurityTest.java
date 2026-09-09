package fiap.grupo11.msagendamento.controller;

import fiap.grupo11.msagendamento.entity.Agendamento;
import fiap.grupo11.msagendamento.repository.AgendamentoRepository;
import fiap.grupo11.msagendamento.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository repository;

    @Autowired
    private OutboxRepository outboxRepository;

    @BeforeEach
    void clearAppointments() {
        repository.deleteAll();
        outboxRepository.deleteAll();
    }

    @Test
    void anonymousCallerReceivesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientOnlySeesAppointmentsForTheirSubject() throws Exception {
        Instant scheduledAt = Instant.now().plusSeconds(3600);
        repository.save(new Agendamento(null, 3L, 1L,
                scheduledAt, scheduledAt.plusSeconds(1800)));
        repository.save(new Agendamento(null, 4L, 1L,
                scheduledAt.plusSeconds(3600), scheduledAt.plusSeconds(5400)));

        mockMvc.perform(get("/api/v1/agendamentos/me")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("paciente", "paciente123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].patientId").value(3));
    }

    @Test
    void patientCannotUseProfessionalAppointmentList() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("paciente", "paciente123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void professionalCannotUsePatientAppointmentList() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos/me")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("medico", "medico123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void patientCannotCreateAnAppointment() throws Exception {
        String body = validBody(3L, 1L);

        mockMvc.perform(post("/api/v1/agendamentos")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("paciente", "paciente123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void nurseCanCreateAnAppointment() throws Exception {
        mockMvc.perform(post("/api/v1/agendamentos")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("enfermeiro", "enfermeiro123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(3L, 1L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(3))
                .andExpect(jsonPath("$.professionalId").value(1));
    }

    @Test
    void doctorCannotCreateAnAppointment() throws Exception {
        mockMvc.perform(post("/api/v1/agendamentos")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("medico", "medico123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(3L, 2L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void nurseCannotEditAnAppointment() throws Exception {
        Instant scheduledAt = Instant.now().plusSeconds(3600);
        Agendamento appointment = repository.save(new Agendamento(
                null, 3L, 1L, scheduledAt, scheduledAt.plusSeconds(1800)));

        mockMvc.perform(put("/api/v1/agendamentos/{id}", appointment.getId())
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("enfermeiro", "enfermeiro123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(3L, 1L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void malformedRequestReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/agendamentos")
                        .with(SecurityMockMvcRequestPostProcessors.httpBasic("medico", "medico123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.patientId").exists())
                .andExpect(jsonPath("$.fieldErrors.scheduledAt").exists());
    }

    private String validBody(Long patientId, Long professionalId) {
        Instant scheduledAt = Instant.now().plusSeconds(3600);
        return "{\"patientId\":" + patientId + ","
                + "\"professionalId\":" + professionalId + ","
                + "\"scheduledAt\":\"" + scheduledAt + "\"," 
                + "\"endsAt\":\"" + scheduledAt.plusSeconds(1800) + "\"}";
    }
}