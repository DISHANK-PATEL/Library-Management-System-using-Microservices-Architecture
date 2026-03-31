INSERT INTO books (title, available) VALUES ('DSA Guide', true)
    ON DUPLICATE KEY UPDATE title = title;
INSERT INTO books (title, available) VALUES ('Design Patterns', true)
    ON DUPLICATE KEY UPDATE title = title;
INSERT INTO books (title, available) VALUES ('Microservices Patterns', true)
    ON DUPLICATE KEY UPDATE title = title;