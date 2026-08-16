-- V8: Add high-performance B-Tree database indexes for rapid lookup and sorting

-- Knowledge Articles Indexes
CREATE INDEX IF NOT EXISTS idx_knowledge_articles_slug_status 
ON knowledge_articles (slug, status);

CREATE INDEX IF NOT EXISTS idx_knowledge_articles_status_published 
ON knowledge_articles (status, published_at DESC);

CREATE INDEX IF NOT EXISTS idx_knowledge_articles_category_id 
ON knowledge_articles (category_id);

-- Engagement Indexes (Likes & Comments)
CREATE INDEX IF NOT EXISTS idx_knowledge_likes_article_id 
ON knowledge_article_likes (article_id);

CREATE INDEX IF NOT EXISTS idx_knowledge_comments_article_status 
ON knowledge_article_comments (article_id, status);

CREATE INDEX IF NOT EXISTS idx_project_likes_project_id 
ON project_likes (project_id);

CREATE INDEX IF NOT EXISTS idx_project_comments_project_status 
ON project_comments (project_id, status);

-- Work Items Indexes
CREATE INDEX IF NOT EXISTS idx_work_items_slug_published 
ON work_items (slug, published);

CREATE INDEX IF NOT EXISTS idx_work_items_order 
ON work_items (display_order ASC, id ASC);

-- Core Portfolio Tables Indexes
CREATE INDEX IF NOT EXISTS idx_skills_order 
ON skills (display_order ASC, id ASC);

CREATE INDEX IF NOT EXISTS idx_experiences_order 
ON experiences (display_order ASC, start_date DESC);

CREATE INDEX IF NOT EXISTS idx_projects_featured_order 
ON projects (featured DESC, display_order ASC, id ASC);
