package fiap.grupo11.msagendamento.repository;

import fiap.grupo11.msagendamento.entity.OutboxEvent;
import fiap.grupo11.msagendamento.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Query("select event from OutboxEvent event where event.status = :status "
            + "and event.nextAttemptAt <= :now order by event.createdAt asc")
    List<OutboxEvent> findReadyToPublish(@Param("status") OutboxStatus status, @Param("now") Instant now);
}