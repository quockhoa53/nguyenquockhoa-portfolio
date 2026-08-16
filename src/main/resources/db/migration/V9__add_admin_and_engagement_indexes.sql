-- V9: High-performance indexes for admin dashboard & engagement queries

CREATE INDEX IF NOT EXISTS idx_admin_allowed_ips_admin_id 
ON admin_allowed_ips (admin_id);

CREATE INDEX IF NOT EXISTS idx_knowledge_comments_created 
ON knowledge_article_comments (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_project_comments_created 
ON project_comments (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_contact_messages_created 
ON contact_messages (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_knowledge_likes_created 
ON knowledge_article_likes (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_project_likes_created 
ON project_likes (created_at DESC);
