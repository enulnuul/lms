# Library Management System — Member 1 Backend

Spring Boot REST API for FR1–FR4. It connects to Member 2’s MySQL schema (`library_db`) using **parameterized JDBC**, **server-side validation**, and **safe exception handling** (no SQL/stack traces in API responses).

## Database (Member 2)

Tables: `Users`, `Books`, `Loans`, `AuditLogs`. Schema copy: `src/main/resources/db/member2-schema.sql`.

1. Create/import Member 2’s database in MySQL.
2. Optional sample rows: `src/main/resources/db/seed.sql`.
3. Set credentials (defaults: `root` / empty password):

```bash
set DB_USERNAME=root
set DB_PASSWORD=yourpassword
```

Or edit `src/main/resources/application.yml`.

## Run

```bash
mvn spring-boot:run
```

API base: `http://localhost:8080`

## Endpoints

| FR | Method | Path | Notes |
|---|---|---|---|
| FR1 Book Search | `GET` | `/api/books/search` | Query: `title`, `author`, `isbn`, `bookId` (at least one) |
| FR2 Book ID validation | `GET` | `/api/books/{bookId}/validate` | Format + existence + availability |
| FR3 Borrow | `POST` | `/api/loans/borrow` | JSON: `{ "userId": 1, "bookId": "BK..." }` |
| FR4 Return + late fee | `POST` | `/api/loans/return` | JSON: `{ "loanId": 1 }` or `{ "userId": 1, "bookId": "BK..." }` |

Loan period: **14 days**. Late fee: **RM 1.00 per overdue day** (`library.loan` in `application.yml`).

### Examples

```http
GET /api/books/search?title=Clean
GET /api/books/BK000000000000000001/validate
```

```json
POST /api/loans/borrow
{ "userId": 1, "bookId": "BK000000000000000001" }
```

```json
POST /api/loans/return
{ "loanId": 1 }
```

## Security (Member 1 scope)

- All SQL uses `PreparedStatement` / `JdbcTemplate` placeholders (`?`). Search `LIKE` values are bound as parameters; `%` and `_` are escaped.
- Book IDs: 1–20 chars, letters/digits/hyphens only (`Books.book_id VARCHAR(20)`).
- Validation errors return `400`; unknown books/users return `404`; business conflicts return `409`.
- `GlobalExceptionHandler` logs real exceptions server-side and returns generic messages to clients.
- Borrow/return (and search/validate) write WORM-style rows to `AuditLogs`.
