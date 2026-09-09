package fiap.grupo11.msagendamento.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "patient_id", nullable = false, length = 100)
    private Long patientId;

    @Column(name = "professional_id", nullable = false, length = 100)
    private Long professionalId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Agendamento() {
    }

    public Agendamento(Long id, Long patientId, Long professionalId, Instant scheduledAt, Instant endsAt) {
        this.id = id;
        this.patientId = patientId;
        this.professionalId = professionalId;
        this.scheduledAt = scheduledAt;
        this.endsAt = endsAt;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(Long patientId, Long professionalId, Instant scheduledAt, Instant endsAt) {
        this.patientId = patientId;
        this.professionalId = professionalId;
        this.scheduledAt = scheduledAt;
        this.endsAt = endsAt;
    }
}