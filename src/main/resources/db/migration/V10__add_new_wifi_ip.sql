-- V10: Allow new Wi-Fi IP address 123.22.44.129

INSERT INTO admin_allowed_ips (admin_id, ip_address, description)
SELECT id, '123.22.44.129', 'Wi-Fi mạng mới'
FROM admin_users
WHERE NOT EXISTS (
    SELECT 1 FROM admin_allowed_ips WHERE ip_address = '123.22.44.129'
)
LIMIT 1;
