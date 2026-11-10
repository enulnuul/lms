# Eternity Library Management System — Frontend Contribution

This ZIP is based on the group's uploaded `library-management-system-member3-frontend(1).zip`.

## Files edited (frontend only)
- `src/main/resources/static/index.html`: removed duplicate Login/Sign Up tabs, added password visibility control, OTP resend, and account/logout view.
- `src/main/resources/static/styles.css`: small additions for frontend controls. Existing visual design and alignment retained.
- `src/main/resources/static/app.js`: book search, validation, safe output rendering, signup/login/OTP frontend flows, notifications, receipt view, and conditional authentication.

**No backend Java files, database schemas, or Maven configuration were modified.**

## What works with the current existing backend
- Navigate between pages.
- Search by title, author, ISBN, or Book ID.
- Empty search shows available books if the group's BookService supports browsing with no criteria.
- Book ID validation via existing API.
- Client-side signup/login/OTP field validation and error handling.
- Visually complete borrowing, returning and receipt screens.

## What requires security backend implementation / API contract confirmation
The following paths are **proposed** and not present in the uploaded Java code:
- `GET /api/auth/csrf` → `{ "token": "...", "headerName": "X-CSRF-TOKEN" }` (or equivalent)
- `POST /api/auth/register` → `{fullName, email, password}` (database currently has `username`, not `fullName`; agree on mapping before deployment)
- `POST /api/auth/login` → `{email,password}` → `{status:"OTP_REQUIRED"}`
- `POST /api/auth/verify-otp` → `{otp}` → `{status:"AUTHENTICATED"}`
- `POST /api/auth/resend-otp` → `{}`
- `GET /api/auth/me` → `{authenticated:true, email, role, username, ...}` (or documented equivalent)
- `POST /api/auth/logout` → `{}`

Authenticated loan transactions are sent without client-supplied `userId`:
- `POST /api/loans/borrow` → `{bookId}`
- `POST /api/loans/return` → `{loanId}`

The current loan controller still accepts `userId` for borrowing and does not enforce authenticated loan ownership. Members implementing backend/security MUST update it before this frontend can complete secure transactions. The frontend does not supply `userId` and does not enable transaction buttons until the backend confirms the session.

OTP expiration (5 minutes) is shown on the frontend, but must be enforced by the backend, along with retry limits, resend rate limiting, actual email OTP delivery, password hashing, CSRF, authorization, and session expiry. Admin-specific actions cannot be implemented until admin APIs are supplied. Avoid describing UI-only logic as security enforcement.

## How to run
Keep WampServer and MySQL running. From the folder containing `pom.xml`, run `mvn spring-boot:run`. Open `http://localhost:8080/`. Live Server previews styling but cannot provide backend APIs.

## Suggested screenshots
Home, search catalogue, search results, no matches, Book ID validation (valid and invalid), Login and Sign Up, OTP page, Borrow and Return form, Receipt, role-aware account display (after backend integration).
