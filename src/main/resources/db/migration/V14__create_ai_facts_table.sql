-- Migration V14: Create ai_facts table for additional AI knowledge
CREATE TABLE IF NOT EXISTS ai_facts (
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(100) NOT NULL DEFAULT 'Khác',
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_facts_active_order ON ai_facts (is_active, display_order ASC, id ASC);
