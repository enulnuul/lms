USE library_db;

INSERT INTO Users (username, email, password_hash, role, is_active)
VALUES
    ('alice', 'alice@library.local', '$2a$10$placeholderhashdonotuseinprod0000000000000000000000', 'Member', TRUE),
    ('bob', 'bob@library.local', '$2a$10$placeholderhashdonotuseinprod0000000000000000000000', 'Librarian', TRUE);

INSERT INTO Books (book_id, title, author, isbn, total_copies, available_copies)
VALUES
    ('BK000000000000000001', 'Clean Code', 'Robert C. Martin', '9780132350884', 3, 3),
    ('BK000000000000000002', 'The Pragmatic Programmer', 'Andrew Hunt', '9780201616224', 2, 2),
    ('BK000000000000000003', 'Software Security', 'Gary McGraw', '9780321356703', 1, 1);
