-- Update legacy skill category names in skills table to match the 4 standard categories
UPDATE skills SET category = 'Backend & Architecture' WHERE category = 'Backend' OR category = 'backend';
UPDATE skills SET category = 'AI & Tools' WHERE category = 'Frontend' OR category = 'frontend';

-- Insert standard default skills for all 4 categories if missing
INSERT INTO skills (name, category, proficiency, display_order)
SELECT 'Java Spring Boot', 'Backend & Architecture', 90, 1
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE category = 'Backend & Architecture');

INSERT INTO skills (name, category, proficiency, display_order)
SELECT 'RESTful API & Microservices', 'Backend & Architecture', 85, 2
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'RESTful API & Microservices');

INSERT INTO skills (name, category, proficiency, display_order)
SELECT 'PostgreSQL & MySQL', 'Database', 85, 1
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE category = 'Database');

INSERT INTO skills (name, category, proficiency, display_order)
SELECT 'Query Optimization', 'Database', 80, 2
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Query Optimization');

INSERT INTO skills (name, category, proficiency, display_order)
SELECT 'Apache Flink & Streaming', 'Data Processing', 80, 1
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE category = 'Data Processing');

INSERT INTO skills (name, category, proficiency, display_order)
SELECT 'CDC & Data Mapping', 'Data Processing', 80, 2
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'CDC & Data Mapping');

INSERT INTO skills (name, category, proficiency, display_order)
SELECT 'AI Agent & LLM Integration', 'AI & Tools', 85, 1
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE category = 'AI & Tools');

INSERT INTO skills (name, category, proficiency, display_order)
SELECT 'Docker & CI/CD Pipelines', 'AI & Tools', 80, 2
WHERE NOT EXISTS (SELECT 1 FROM skills WHERE name = 'Docker & CI/CD Pipelines');
