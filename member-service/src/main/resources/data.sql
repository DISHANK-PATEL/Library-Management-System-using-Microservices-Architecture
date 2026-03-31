INSERT INTO members (name, email) VALUES ('Alice Smith', 'alice@example.com')
    ON DUPLICATE KEY UPDATE name = name;
INSERT INTO members (name, email) VALUES ('Bob Johnson', 'bob@example.com')
    ON DUPLICATE KEY UPDATE name = name;
INSERT INTO members (name, email) VALUES ('Charlie Brown', 'charlie@example.com')
    ON DUPLICATE KEY UPDATE name = name;