INSERT INTO profiles (full_name, headline, bio, email, phone, location, github_url, linkedin_url)
VALUES ('Khoa', 'Full-stack Developer', 'Tôi xây dựng các sản phẩm web đơn giản, nhanh và hữu ích.', 'hello@example.com', '', 'Việt Nam', 'https://github.com/', 'https://linkedin.com/');

INSERT INTO skills (name, category, proficiency, display_order) VALUES
('Java', 'Backend', 85, 1), ('Spring Boot', 'Backend', 85, 2),
('React', 'Frontend', 80, 3), ('PostgreSQL', 'Database', 80, 4);

INSERT INTO experiences (company, position, start_date, end_date, description, display_order)
VALUES ('Your Company', 'Software Developer', '2024-01-01', NULL, 'Phát triển và vận hành các ứng dụng web.', 1);

INSERT INTO projects (title, description, technologies, featured, display_order)
VALUES ('Portfolio', 'Website giới thiệu hồ sơ, kỹ năng, kinh nghiệm và dự án.', 'Java, Spring Boot, React, PostgreSQL', TRUE, 1);
