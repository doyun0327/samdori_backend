-- Neon SQL Editor에서 한 번 실행 (목록 조회 정렬 속도 개선)

CREATE INDEX IF NOT EXISTS idx_bookings_client_requested_at
    ON bookings (client_id, requested_at DESC);

CREATE INDEX IF NOT EXISTS idx_bookings_counselor_requested_at
    ON bookings (counselor_id, requested_at DESC);

CREATE INDEX IF NOT EXISTS idx_slot_proposals_client_created_at
    ON slot_proposals (client_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_slot_proposals_counselor_created_at
    ON slot_proposals (counselor_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_users_role_name
    ON users (role, name);
