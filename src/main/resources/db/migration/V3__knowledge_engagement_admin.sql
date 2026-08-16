CREATE TABLE knowledge_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL UNIQUE,
    description VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE knowledge_articles (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES knowledge_categories(id),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(280) NOT NULL UNIQUE,
    summary VARCHAR(1000),
    content TEXT NOT NULL,
    thumbnail_url TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    view_count BIGINT NOT NULL DEFAULT 0,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_knowledge_articles_category ON knowledge_articles(category_id);
CREATE INDEX idx_knowledge_articles_public ON knowledge_articles(status, published_at DESC);

CREATE TABLE guest_visitors (
    id UUID PRIMARY KEY,
    display_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    email_hash VARCHAR(64) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_guest_visitors_email_hash ON guest_visitors(email_hash);

CREATE TABLE knowledge_article_likes (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES knowledge_articles(id) ON DELETE CASCADE,
    guest_id UUID NOT NULL REFERENCES guest_visitors(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_article_guest_like UNIQUE(article_id, guest_id)
);

CREATE TABLE project_likes (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    guest_id UUID NOT NULL REFERENCES guest_visitors(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_project_guest_like UNIQUE(project_id, guest_id)
);

CREATE TABLE knowledge_article_comments (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT NOT NULL REFERENCES knowledge_articles(id) ON DELETE CASCADE,
    guest_id UUID NOT NULL REFERENCES guest_visitors(id),
    parent_id BIGINT REFERENCES knowledge_article_comments(id) ON DELETE CASCADE,
    content VARCHAR(3000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project_comments (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    guest_id UUID NOT NULL REFERENCES guest_visitors(id),
    parent_id BIGINT REFERENCES project_comments(id) ON DELETE CASCADE,
    content VARCHAR(3000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_knowledge_comments_article_status ON knowledge_article_comments(article_id, status, created_at);
CREATE INDEX idx_project_comments_project_status ON project_comments(project_id, status, created_at);

CREATE TABLE admin_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE admin_allowed_ips (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES admin_users(id) ON DELETE CASCADE,
    ip_address VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT uk_admin_allowed_ip UNIQUE(admin_id, ip_address)
);

CREATE TABLE work_items (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(180) NOT NULL UNIQUE,
    period VARCHAR(100) NOT NULL,
    role VARCHAR(150) NOT NULL,
    company VARCHAR(200) NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    content TEXT NOT NULL,
    technologies VARCHAR(1000) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO knowledge_categories(name, slug, description, display_order) VALUES
('Java & Spring Boot', 'java-spring-boot', 'Kiến thức backend Java và Spring Boot', 1),
('Database & Data', 'database-data', 'Database, CDC và xử lý dữ liệu', 2),
('DevOps & Engineering', 'devops-engineering', 'Docker, CI/CD và engineering practices', 3);

INSERT INTO knowledge_articles(category_id, title, slug, summary, content, status, featured, published_at)
SELECT id, 'Clean Architecture trong Spring Boot', 'clean-architecture-spring-boot',
       'Tổ chức domain, application và infrastructure để code dễ kiểm thử, dễ mở rộng.',
       '<h2>Giới thiệu</h2><p>Clean Architecture giúp tách nghiệp vụ khỏi framework và persistence.</p><h2>Nguyên tắc</h2><p>Dependency luôn hướng vào domain.</p>',
       'PUBLISHED', TRUE, CURRENT_TIMESTAMP
FROM knowledge_categories WHERE slug = 'java-spring-boot';

INSERT INTO work_items(slug, period, role, company, title, summary, content, technologies, display_order) VALUES
('backend-architecture', '2024 — Hiện tại', 'Backend Developer', 'Software Team', 'Backend & thiết kế hệ thống',
 'Phân tích nghiệp vụ, thiết kế REST API và triển khai module backend theo Clean Architecture.',
 '<ul><li>Thiết kế API contract rõ ràng</li><li>Tổ chức code theo Clean Architecture</li><li>Review và tối ưu truy vấn</li></ul>',
 'Java,Spring Boot,Gradle,REST API,PostgreSQL', 1);
