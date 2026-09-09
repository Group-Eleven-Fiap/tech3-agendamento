CREATE TABLE usuarios (
    id UUID NOT NULL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    subject_id VARCHAR(100) NOT NULL,
    user_role VARCHAR(30) NOT NULL,
    CONSTRAINT uq_usuarios_username UNIQUE (username),
    CONSTRAINT uq_usuarios_subject_id UNIQUE (subject_id),
    CONSTRAINT ck_usuarios_role CHECK (user_role IN ('MEDICO', 'ENFERMEIRO', 'PACIENTE'))
);

INSERT INTO usuarios (id, username, password_hash, subject_id, user_role)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'medico',
     '$2a$10$PaK.lyjvokSVx1xZ2Sd1/eMRjVj53erIXWGThViaojx3F6fvRPXPy',
     'professional-medico', 'MEDICO'),
    ('00000000-0000-0000-0000-000000000002', 'enfermeiro',
     '$2a$10$vmj/Z5taxGoV3I75AxVR7eKGbg2vinrDOcd2gUZENdFapkEiRp4uy',
     'professional-enfermeiro', 'ENFERMEIRO'),
    ('00000000-0000-0000-0000-000000000003', 'paciente',
     '$2a$10$J2vNwkBoMXAcRCiJ72wyKuBpmg/fiV6J5EK2g1KcO5rn86DZQkXni',
     'patient-1', 'PACIENTE');