-- Neon SQL Editor에서 한 번 실행

CREATE TABLE IF NOT EXISTS counselor_availability (
    counselor_id BIGINT NOT NULL,
    availability_date DATE NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    CONSTRAINT uk_counselor_availability_counselor_date_slot UNIQUE (counselor_id, availability_date, time_slot)
);
