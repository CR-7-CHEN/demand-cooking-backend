-- Run in database: demand-cooking
-- Backfill service start fields required by dc_cook_order entity.

ALTER TABLE dc_cook_order
    ADD COLUMN IF NOT EXISTS service_started_flag char(1) DEFAULT '0' COMMENT 'service started flag (0=no 1=yes)' AFTER service_end_time,
    ADD COLUMN IF NOT EXISTS service_started_time datetime DEFAULT NULL COMMENT 'actual service started time' AFTER service_started_flag;

UPDATE dc_cook_order
SET service_started_flag = CASE
    WHEN service_started_time IS NULL THEN '0'
    ELSE '1'
END
WHERE service_started_flag IS NULL
   OR service_started_flag = '';
