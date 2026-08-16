ALTER TABLE profiles ADD COLUMN short_bio TEXT;

UPDATE profiles SET short_bio = regexp_replace(bio, '<[^>]*>', '', 'g') WHERE short_bio IS NULL;

ALTER TABLE profiles ALTER COLUMN short_bio SET NOT NULL;
