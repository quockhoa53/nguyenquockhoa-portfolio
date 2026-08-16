-- Standardize and insert rich Work Process timeline data into work_items table

-- Update V3 initial item to ensure published = TRUE and complete fields
UPDATE work_items 
SET period = '2024 — Hiện tại',
    role = 'Junior Backend Developer',
    company = 'Software Engineering Team',
    title = 'Backend & Thiết kế hệ thống Microservices',
    summary = 'Phân tích yêu cầu hệ thống, thiết kế RESTful API chuẩn OpenAPI, tổ chức mã nguồn theo Clean Architecture & Domain-Driven Design.',
    content = '<h3>Nội dung công việc chính</h3><ul><li><b>Thiết kế API Contract:</b> Chuẩn hóa OpenAPI/Swagger, xây dựng response format đồng nhất cho toàn hệ thống.</li><li><b>Kiến trúc Clean Architecture:</b> Phân chia lớp Domain, Application, Infrastructure và Web tách biệt hoàn toàn.</li><li><b>Tối ưu truy vấn Database:</b> Đánh chỉ mục Indexing cho PostgreSQL, giảm thời gian phản hồi API từ 350ms xuống dưới 50ms.</li><li><b>Bảo mật &amp; Phân quyền:</b> Tích hợp Spring Security, JWT và kiểm soát IP Whitelist cho Admin Portal.</li></ul>',
    technologies = 'Java, Spring Boot, PostgreSQL, Clean Architecture, REST API, Docker',
    display_order = 1,
    published = TRUE
WHERE slug = 'backend-architecture';

-- Insert 2nd work item: Data Processing & Pipeline
INSERT INTO work_items (slug, period, role, company, title, summary, content, technologies, display_order, published)
SELECT 'data-processing-pipeline', '2023 — 2024', 'Data & Backend Engineer', 'Tech Solutions Studio', 
       'Xử lý dữ liệu luồng & Tích hợp Pipeline',
       'Triển khai các tuyến luồng dữ liệu thời gian thực (Real-time Data Streaming), xử lý đồng bộ và kiểm soát tính toàn vẹn dữ liệu.',
       '<h3>Nội dung công việc chính</h3><ul><li><b>Tích hợp Apache Flink &amp; Kafka:</b> Triển khai luồng dữ liệu sự kiện thời gian thực với độ trễ thấp.</li><li><b>Xử lý CDC (Change Data Capture):</b> Tự động bắt sự kiện biến động dữ liệu và đồng bộ vào kho dữ liệu tập trung.</li><li><b>Pipeline Monitoring:</b> Xây dựng bộ công cụ giám sát tiến trình và tự động cảnh báo sự cố dữ liệu.</li></ul>',
       'Java, Apache Flink, Kafka, PostgreSQL, CDC, Data Streaming',
       2, TRUE
WHERE NOT EXISTS (SELECT 1 FROM work_items WHERE slug = 'data-processing-pipeline');

-- Insert 3rd work item: AI Agent & LLMs Integration
INSERT INTO work_items (slug, period, role, company, title, summary, content, technologies, display_order, published)
SELECT 'ai-tools-integration', '2024', 'AI Integration Developer', 'Innovation Lab',
       'Tích hợp AI Agent & LLMs vào ứng dụng',
       'Triển khai trợ lý AI thông minh, kết nối các mô hình ngôn ngữ lớn (LLMs) vào hệ thống tự động hóa công việc.',
       '<h3>Nội dung công việc chính</h3><ul><li><b>Xây dựng AI Agent:</b> Kết nối ứng dụng với Gemini API thực hiện các tác vụ xử lý văn bản tự động.</li><li><b>Tối ưu Prompt &amp; RAG:</b> Áp dụng kỹ thuật RAG (Retrieval-Augmented Generation) tra cứu tài liệu thông minh.</li><li><b>DevOps &amp; CI/CD:</b> Đóng gói container Docker, cấu hình tuyến CI/CD tự động kiểm thử và deploy.</li></ul>',
       'Python, Java, LLM Integration, AI Agent, Docker, CI/CD, Git',
       3, TRUE
WHERE NOT EXISTS (SELECT 1 FROM work_items WHERE slug = 'ai-tools-integration');
