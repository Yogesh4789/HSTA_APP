# Helpdesk Deployment Guide

This repository is the **Helpdesk** project (separate from `HelpdeskApp`).

## 1. Required Environment Variables

Set these before running in any environment:

- `HELPDESK_DB_URL`
- `HELPDESK_DB_USER`
- `HELPDESK_DB_PASSWORD`
- `SMTP_USER`
- `SMTP_PASS`

Recommended:

- `SMTP_HOST` (default: `smtp.gmail.com`)
- `SMTP_PORT` (default: `587`)
- `SMTP_FROM` (recommended for SendGrid verified sender)
- `SMTP_TIMEOUT_MS` (default: `10000`)
- `HELPDESK_PUBLIC_BASE_URL` (important for verification/reset email links)

Use `.env.example` as the template.

## 2. Database Setup

For fresh setup:

1. Run [`sql/hsta_schema.sql`](sql/hsta_schema.sql).

If your DB already exists, apply these migrations:

```sql
ALTER TABLE `USER`
  ADD COLUMN `is_verified` TINYINT(1) NOT NULL DEFAULT 1,
  ADD COLUMN `verification_token` VARCHAR(100) UNIQUE NULL,
  ADD COLUMN `verification_expiry` DATETIME NULL,
  ADD COLUMN `reset_token` VARCHAR(100) NULL,
  ADD COLUMN `reset_expiry` DATETIME NULL;

DROP TABLE IF EXISTS `PENDING_USER`;
```

## 3. Build WAR

This project currently expects an already-built WAR:

- `Helpdesk.war` at repository root.

## 4. Docker Deployment

Build:

```bash
docker build -t helpdesk:latest .
```

Run:

```bash
docker run --name helpdesk \
  -p 8080:8080 \
  --env-file .env \
  helpdesk:latest
```

Open:

- `http://localhost:8080/`

## 5. Cloud Deployment Notes

- Set `HELPDESK_PUBLIC_BASE_URL` to your public domain, for example:
  - `https://helpdesk.yourdomain.com`
- This ensures verification and reset-password emails contain correct links instead of local/internal hostnames.
- Container honors platform `PORT` env variable automatically.

## 6. Quick Health Check

After deployment:

1. Load `/login.jsp`
2. Register new user and verify email link
3. Test forgot-password flow end-to-end
