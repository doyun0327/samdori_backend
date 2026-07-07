-- Neon SQL Editor에서 한 번 실행

ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(500),
    ADD COLUMN IF NOT EXISTS cancelled_by VARCHAR(20);
