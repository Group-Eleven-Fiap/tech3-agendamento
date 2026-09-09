package fiap.grupo11.msagendamento.repository;

import fiap.grupo11.msagendamento.entity.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    List<Agendamento> findByPatientIdOrderByScheduledAtAsc(Long patientId);

    List<Agendamento> findByScheduledAtBetweenOrderByScheduledAtAsc(Instant from, Instant to);

    default List<Agendamento> findAllByScheduledAtAsc() {
        return findAll(Sort.by(Sort.Direction.ASC, "scheduledAt"));
    }

    @Query("select count(appointment) > 0 from Agendamento appointment "
            + "where appointment.professionalId = :professionalId "
            + "and appointment.scheduledAt < :endsAt and appointment.endsAt > :scheduledAt")
    boolean existsOverlapping(@Param("professionalId") Long professionalId,
                              @Param("scheduledAt") Instant scheduledAt,
                              @Param("endsAt") Instant endsAt);

    @Query("select count(appointment) > 0 from Agendamento appointment "
            + "where appointment.id <> :id and appointment.professionalId = :professionalId "
            + "and appointment.scheduledAt < :endsAt and appointment.endsAt > :scheduledAt")
    boolean existsOverlappingForAnotherAppointment(@Param("id") Long id,
                                                   @Param("professionalId") Long professionalId,
                                                   @Param("scheduledAt") Instant scheduledAt,
                                                   @Param("endsAt") Instant endsAt);
}