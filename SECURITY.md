# Security and deployment requirements

Never expose the authentication service while password reset, SMTP delivery and security testing are unverified.

## Password reset
POST `/api/register-login/request-password-reset` with `{"email":"verified@example.com"}`.
For an existing **verified** email, the server sends a 256-bit random URL-safe token via configured SMTP.
The API returns a generic response, preventing direct account enumeration.
POST `/api/register-login/reset-password` with `{"token":"...","newPassword":"..."}`.
The database stores only SHA-256 token hashes. Tokens expire in 15 minutes and become single-use in a transaction with a pessimistic row lock. New passwords require 12–128 characters.
Legacy mobile reset/OTP endpoints return HTTP 410 until replaced with verified recovery.
Configure `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `RESET_EMAIL_FROM`, `RESET_BASE_URL` (HTTPS) and an externally hosted reset UI. Ensure verified-email enrollment works before enabling recovery.

## Secrets and operations
The original repository history contained a JWT signing secret, database passwords, an SMS API key, and a tracked `.env`. Untracking current files **does not erase history or revoke keys**.
Rotate all exposed credentials, JWT signing secrets, SMS provider keys, and database passwords if ever used outside a disposable environment. Review historical logs, backups and forks. Invalidate existing JWTs on rotation. Do not paste new secrets into pull requests or tracked files.
Set `JWT_SECRET`, `DB_PASSWORD` and DB URLs via deployment secret storage. Do not reuse development examples in production. Restrict CORS, secure SMTP and rate-limit account recovery before deploying publicly.

## Known gaps
The token request endpoint still needs request rate limiting, monitoring and integration testing with a real mail provider. Changing `authToken` alone does not revoke stateless JWTs; use signing-key rotation, token-version checks or a revocation mechanism as appropriate.
