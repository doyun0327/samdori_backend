-- Neon SQL Editor에서 한 번 실행

CREATE TABLE IF NOT EXISTS slot_proposals (
    id BIGSERIAL PRIMARY KEY,
    counselor_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    message VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    slots JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NULL,
    booked_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_slot_proposals_client_id ON slot_proposals (client_id);
CREATE INDEX IF NOT EXISTS idx_slot_proposals_counselor_id ON slot_proposals (counselor_id);
