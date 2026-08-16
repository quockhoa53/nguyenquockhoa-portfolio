-- Add facebook_url column to profiles table for dynamic contact information

ALTER TABLE profiles ADD COLUMN IF NOT EXISTS facebook_url TEXT;

UPDATE profiles 
SET facebook_url = 'https://facebook.com/'
WHERE facebook_url IS NULL;
