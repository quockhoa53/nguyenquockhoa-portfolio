-- V12: Add TOTP 2FA fields for enterprise Admin authentication
ALTER TABLE admin_users ADD COLUMN IF NOT EXISTS totp_secret VARCHAR(100);
ALTER TABLE admin_users ADD COLUMN IF NOT EXISTS totp_enabled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE admin_users ADD COLUMN IF NOT EXISTS totp_setup_at TIMESTAMP WITH TIME ZONE;
