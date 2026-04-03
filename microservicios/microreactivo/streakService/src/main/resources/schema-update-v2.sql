-- Ejecutar en la base de datos PostgreSQL de streak-service
ALTER TABLE user_streaks ADD COLUMN overridden_level VARCHAR(20) NULL;
ALTER TABLE user_streaks ADD COLUMN override_reason VARCHAR(500) NULL;
ALTER TABLE user_streaks ADD COLUMN updated_by BIGINT NULL;
