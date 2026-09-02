ALTER TABLE projects ADD COLUMN IF NOT EXISTS summary TEXT;

-- Seed default summaries for existing projects if summary is currently NULL
UPDATE projects 
SET summary = 'Hệ thống đặt vé xem phim trực tuyến thời gian thực, tích hợp trợ lý ảo ChatBot hỗ trợ khách hàng thông minh và quản lý phòng chiếu.'
WHERE (summary IS NULL OR summary = '') AND (title ILIKE '%vé xem phim%' OR title ILIKE '%cinema%' OR title ILIKE '%phim%');

UPDATE projects 
SET summary = 'Nền tảng thương mại điện tử kiến trúc Microservices hiệu năng cao, tối ưu xử lý đơn hàng đồng thời Flash Sale với Kafka và Redis Cache.'
WHERE (summary IS NULL OR summary = '') AND (title ILIKE '%e-commerce%' OR title ILIKE '%flash sale%' OR title ILIKE '%thương mại%');

UPDATE projects 
SET summary = 'Giải pháp AI Agent doanh nghiệp kết hợp RAG tra cứu tài liệu thông minh, tích hợp Vector Database Qdrant và Large Language Models.'
WHERE (summary IS NULL OR summary = '') AND (title ILIKE '%ai agent%' OR title ILIKE '%rag%' OR title ILIKE '%intelligence%');
