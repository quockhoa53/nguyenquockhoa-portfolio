-- Approve all existing pending comments so they are immediately visible
UPDATE knowledge_article_comments SET status = 'APPROVED' WHERE status = 'PENDING';
UPDATE project_comments SET status = 'APPROVED' WHERE status = 'PENDING';
