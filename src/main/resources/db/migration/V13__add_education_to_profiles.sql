-- Migration V13: Add education JSONB column to profiles table
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS education JSONB;

UPDATE profiles 
SET education = '{"school": "Học viện Công nghệ Bưu chính Viễn thông (PTIT)", "major": "Công nghệ Thông tin", "degree": "Kỹ sư", "period": "2020 — 2024"}'::jsonb
WHERE education IS NULL;
