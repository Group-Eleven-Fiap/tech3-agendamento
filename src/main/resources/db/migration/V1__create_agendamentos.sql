CREATE TABLE agendamentos (
    id UUID NOT NULL PRIMARY KEY,
    patient_id VARCHAR(100) NOT NULL,
    professional_id VARCHAR(100) NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_agendamentos_schedule_order CHECK (ends_at > scheduled_at)
);

CREATE INDEX idx_agendamentos_patient_id ON agendamentos (patient_id);
CREATE INDEX idx_agendamentos_scheduled_at ON agendamentos (scheduled_at);

CREATE TABLE outbox_events (
    id UUID NOT NULL PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempts INTEGER NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    delivered_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_pending ON outbox_events (status, next_attempt_at, created_at);