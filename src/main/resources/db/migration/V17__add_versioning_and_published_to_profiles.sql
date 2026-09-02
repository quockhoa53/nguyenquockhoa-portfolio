-- Migration V17: Add version_name and is_published to profiles table for multi-version profile management
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS version_name VARCHAR(150) DEFAULT 'Hồ sơ Chính thức';
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT true;

-- Ensure at least one profile is marked as published
UPDATE profiles SET is_published = true WHERE id = (SELECT id FROM profiles ORDER BY id ASC LIMIT 1);
UPDATE profiles SET version_name = 'Hồ sơ Chính thức (Primary)' WHERE version_name IS NULL OR version_name = '';
