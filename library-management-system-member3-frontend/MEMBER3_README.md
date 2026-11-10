# Member 3 — Frontend / GUI Deliverable

## What is supplied
This package is the original Spring Boot project with three new files **only** under `src/main/resources/static/`: `index.html`, `styles.css`, and `app.js`. The Java backend, schema, and Maven configuration have not been changed.

## How to run
1. Use JDK 17, Maven and MySQL. Create `library_db`; import `src/main/resources/db/member2-schema.sql`, then optional `seed.sql`.
2. Configure your MySQL credentials using the original README instructions.
3. From the project root run `mvn spring-boot:run`.
4. Open **http://localhost:8080/** in the browser. Do not open the HTML with `file://` or use a separate frontend dev server; files are served by Spring Boot.
5. Test Search with `Clean` and Book ID validation with `BK000000000000000001` after sample data is loaded.

## Screens
- Dashboard — landing and navigation.
- Find books — FR1, `GET /api/books/search` (title, author, isbn, bookId).
- Validate Book ID — FR2, `GET /api/books/{bookId}/validate`.
- Borrow a book — FR3, `POST /api/loans/borrow`.
- Return a book — FR4, `POST /api/loans/return`.
- Latest receipt — last response from borrow/return; no persisted receipt endpoint exists.
- Login and email OTP screens — **UI layouts only**. Deliberately disabled because authentication/OTP endpoints do not exist.
- Integration settings — **insecure localhost-only developer testing toggle**, disabled by default and reset on page reload.

## Security limitations — must be resolved before claiming a secured system
- The backend currently has no login, password verification, MFA, session, or authorization endpoint. A GUI cannot provide this security.
- The current borrow endpoint accepts a `userId` supplied by the client; the return endpoint accepts a loan ID without verified ownership. Member 5/4 must derive identity from the authenticated server-side principal and check resource ownership.
- Developer mode deliberately allows calling the existing backend *only for local integration tests*, and DOES NOT protect its endpoints. **Never expose this app or the backend online before security work is completed.** A malicious caller can bypass frontend controls entirely.
- Frontend validation is user-friendly only. Real limits and SQL injection defenses must be enforced on the server.
- The current Book ID code allows 1–20 characters, although the report mentions *fixed length*; the team should agree on the exact specification and update BOTH frontend and backend accordingly.
- `seed.sql` uses placeholder password hashes; they are not usable login credentials.
- CSS uses an optional remote Google font; system fonts are used when offline.

## API integration contract
- Search returns an array of `{ bookId, title, author, isbn, totalCopies, availableCopies, available }`.
- Validate returns `{ valid, bookId, title, available, availableCopies, message }`.
- Borrow request: `{ userId: number, bookId: string }`; return request: `{ loanId: number }` (legacy insecure contracts).
- Borrow/return response: `{ loanId, userId, bookId, borrowDate, dueDate, returnDate, status, lateFee, daysOverdue }`.
- Error response: `{ status, code, message, details }`.

## Integration requirements to request from Member 5 / 4
- `POST /api/auth/login` password validation and initiating OTP challenge.
- `POST /api/auth/verify-otp` server-side OTP verification, session creation.
- `POST /api/auth/logout` and `GET /api/auth/me` authenticated member identity/role.
- Secure borrow/return endpoints that NEVER trust a user ID supplied by the browser.
- `GET /api/loans/mine` for an authenticated user's active loans and returns.
- Optional admin endpoints if admin functions are within the agreed implementation scope.
These are **proposed**, not existing endpoints; agree on exact contracts with your teammates.

## Evidence to capture (after connecting to a running backend)
1. Dashboard.
2. Search by title, author and ISBN (positive and empty results).
3. Valid Book ID and rejected oversized/malformed Book ID.
4. Borrow success with date/due date and availability change (developer mode **only for testing**, not as a security demo).
5. Return success with fee and recorded status (developer mode **only for testing**).
6. Implemented login + OTP, authenticated borrow/return, and authorization failures **after Member 5 integration**.
7. Record each tested case as Pass/Fail; do not claim a pass from a mockup or unexecuted test.

## Report contribution
Document GUI pages and their inputs, response/error states, FR1–FR4 links, and screenshots. Explicitly distinguish prototype screens from validated integrated features. No security guarantees should be claimed from client-side validation.
