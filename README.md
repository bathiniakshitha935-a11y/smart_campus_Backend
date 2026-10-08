# Smart Campus Backend

Spring Boot + Spring Data JPA REST backend.

## Main API

- GET `/api/locations`
- GET `/api/locations/{id}`
- POST `/api/locations`
- PUT `/api/locations/{id}`
- DELETE `/api/locations/{id}`
- GET `/api/routes?from=...&to=...`
- POST `/api/auth/register` (student registration)
- POST `/api/auth/login` (returns a bearer token)
- GET `/api/auth/me`
- POST `/api/suggestions` (student only)
- GET `/api/suggestions/mine` (student only)
- GET `/api/admin/suggestions` (admin only)
- POST `/api/admin/suggestions/{id}/review` (admin only; APPROVED or REJECTED)

Local development uses H2 from `application.properties`. Activate the `prod`
profile for PostgreSQL and configure `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`,
and `DB_PASSWORD`.

`render.yaml` is the Render Blueprint for this standalone backend repository. It
creates a Docker web service and PostgreSQL database. Set `FRONTEND_URL` to the
deployed frontend origin when prompted so CORS permits browser requests.

## Authorization

Student registration never accepts a role from the request. Passwords are stored as BCrypt hashes. Admin access is bootstrapped from `ADMIN_EMAIL` and `ADMIN_PASSWORD`; set both as secret environment values for the first startup, then remove them after the admin account is created. The server issues short-lived bearer tokens signed with `JWT_SECRET` (at least 32 bytes of random key material; generate it locally with `openssl rand -base64 48`, then enter it directly in Render). `JWT_EXPIRATION_SECONDS` is optional and defaults to 3600. Local development creates a temporary random signing key if `JWT_SECRET` is unset; tokens expire on restart.

For local development, set `ADMIN_EMAIL` and `ADMIN_PASSWORD` in the IntelliJ Run Configuration if you need admin tools. Do not place those values in a committed `.env` file.

Spring Security allows public reads of locations and routes, requires an admin role for location writes and admin review endpoints, and requires a student role for suggestion endpoints. Authorization is enforced by the backend, not by the frontend.

## Database migration

Flyway migration `V1__users_and_suggestions.sql` adds account and suggestion tables and creates `locations` only for an empty/new schema. Existing location data is retained. With `spring.flyway.baseline-on-migrate=true` and baseline version `0`, an existing non-empty schema is baselined before V1 runs. Back up the production database before the first migration deployment. No tables are dropped or recreated.

The Render Blueprint injects `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD`. `application-prod.properties` composes the PostgreSQL JDBC URL from the first three. Do not add a competing `SPRING_DATASOURCE_URL` variable. Set `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, and `FRONTEND_URL` in Render; secret values belong only in the Render dashboard, never in Git.
