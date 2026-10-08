# Smart Campus Backend

Spring Boot + Spring Data JPA + PostgreSQL REST backend.

## Main API

- GET `/api/locations`
- GET `/api/locations/{id}`
- POST `/api/locations`
- PUT `/api/locations/{id}`
- DELETE `/api/locations/{id}`
- GET `/api/routes?from=...&to=...`

The database is PostgreSQL. Configure `SPRING_DATASOURCE_URL`,
`SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
