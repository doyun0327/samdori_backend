-- Neon SQL Editor에서 한 번 실행

CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    counselor_id BIGINT NOT NULL,
    date DATE NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    responded_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancel_reason VARCHAR(500) NULL,
    cancelled_by VARCHAR(20) NULL
);

CREATE INDEX IF NOT EXISTS idx_bookings_client_id ON bookings (client_id);
CREATE INDEX IF NOT EXISTS idx_bookings_counselor_id ON bookings (counselor_id);
CREATE INDEX IF NOT EXISTS idx_bookings_counselor_date_slot ON bookings (counselor_id, date, time_slot);
