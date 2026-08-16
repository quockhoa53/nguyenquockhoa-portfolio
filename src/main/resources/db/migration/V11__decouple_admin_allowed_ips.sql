-- V11: Decouple admin_allowed_ips from admin_users to make IP Whitelist truly system-wide

-- 1. Drop old unique constraint on (admin_id, ip_address)
ALTER TABLE admin_allowed_ips 
DROP CONSTRAINT IF EXISTS uk_admin_allowed_ip;

-- 2. Drop foreign key constraint on admin_id
ALTER TABLE admin_allowed_ips 
DROP CONSTRAINT IF EXISTS admin_allowed_ips_admin_id_fkey;

-- 3. Drop index on admin_id if exists
DROP INDEX IF EXISTS idx_admin_allowed_ips_admin_id;

-- 4. Make admin_id nullable so entity does not require it
ALTER TABLE admin_allowed_ips 
ALTER COLUMN admin_id DROP NOT NULL;

-- 5. Add created_at column if not exists
ALTER TABLE admin_allowed_ips 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 6. Add unique index on ip_address so each IP is unique system-wide
CREATE UNIQUE INDEX IF NOT EXISTS uk_admin_allowed_ips_ip ON admin_allowed_ips(ip_address);
