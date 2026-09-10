# KMovie Backend

Spring Boot 3 / Java 17 backend for a movie & series streaming service, backed by
PostgreSQL, Redis, and AWS S3 for media storage.

## Stack

- Spring Boot 3.3 (Web, Data JPA, Security, Validation, Data Redis)
- PostgreSQL (primary datastore)
- Redis (JWT logout blacklist, OTP codes, general cache)
- AWS S3 (video/profile-picture file storage) via the AWS SDK v2
- JWT auth (jjwt), BCrypt password hashing
- Google `libphonenumber` for phone number validation/normalization

## Project layout

```
src/main/java/com/kmovie/
  entity/       JPA entities (BaseEntity + User, Title, FileEntity, Video, VideoPlay, EmailMessage, SmsMessage)
  enums/        Subscription, Category, Genre
  repository/   Spring Data JPA repositories
  security/     JWT service/filter, UserDetails, current-user helper
  service/      Business logic (auth, users, titles, videos, playback, S3, email/sms queueing, cache, otp)
  controller/   REST controllers matching the routes below
  dto/          Request/response payloads
  config/       Security, CORS, Redis, S3 wiring
  exception/    ApiException + global @RestControllerAdvice handler
```

## Data model notes

- Every entity extends `BaseEntity`, which adds `id`, `dateCreated`, `dateUpdated`,
  `isDeleted`, `dateDeleted`. Deletes are **soft deletes** — rows are flagged, not
  physically removed — so `isDeleted=false` is applied everywhere lookups happen.
- `Title.genre` is stored as a single comma-concatenated string column (e.g.
  `"ACTION,DRAMA"`) as specified, and is exposed to API clients as a `List<Genre>`.
- `Title.parentTitleId` is a plain self-referencing UUID column (no JPA `@ManyToOne`),
  used to model series → season nesting. A **movie** title may hold exactly one
  `Video`; a **series** title holds child (season) titles, and episodes are uploaded
  as `Video`s under those season titles. This is enforced in `VideoService#upload`.
- `EmailMessage` / `SmsMessage` rows are only ever **created** by this app
  (`isSent=false`). A separate external worker process is expected to poll and
  dispatch them, then set `isSent=true` / `dateSent`. No SMTP/Twilio/Africa's
  Talking client code runs inside this service — only the environment variables
  for those providers are wired up in `application.yml`/`docker-compose.yml` for
  that worker to consume.
- **Deviation from the spec, called out explicitly:** `VideoPlay` was given
  `videoId` and `titleId` columns in addition to `userId`/`startPlayTime`/
  `endPlayTime`, because a play record that doesn't say *what* was played isn't
  useful. They're populated automatically by `GET /movies/play/{titleId}/{videoId}`.
- **Addition beyond the spec:** `POST /accounts/verify-email` and
  `POST /accounts/verify-phone` (`?code=123456`) were added because the schema has
  `isEmailVerified`/`isPhoneNumberVerified` flags with no way to flip them
  otherwise. Registration queues an `EmailMessage`/`SmsMessage` containing a 6-digit
  OTP (configurable length/TTL) that Redis holds until verified.

## Auth

- `POST /accounts/register` — `{ username, email, password, phoneNumber? }`. Hashes
  the password with BCrypt, validates/normalizes `phoneNumber` to E.164 via
  libphonenumber, and queues an email-verification message (+ SMS if a phone number
  was given).
- `POST /accounts/login` — `{ usernameOrEmail, password, stayLoggedIn? }`. Returns a
  JWT that expires in **7 days**, or **90 days** if `stayLoggedIn: true`.
- Every other route (except public GETs, see `SecurityConfig`) requires
  `Authorization: Bearer <token>`.
- `*/accounts/logout` (any HTTP method) blacklists the current token's `jti` in
  Redis for the remainder of its natural lifetime, so it can't be reused even
  though JWTs are otherwise stateless. Safe to call while logged out (no-op).
- Admin-only routes use Spring Security's `@PreAuthorize("hasRole('ADMIN')")`,
  backed by `User.isAdmin`.

## API summary

| Route | Method(s) | Notes |
|---|---|---|
| `/accounts/register` | POST | public |
| `/accounts/login` | POST | public |
| `/accounts/logout` | ANY | public, no-ops if not logged in |
| `/accounts/profile` | GET / POST,PUT,PATCH / DELETE | auth required |
| `/accounts/verify-email` | POST `?code=` | auth required |
| `/accounts/verify-phone` | POST `?code=` | auth required |
| `/accounts/profile/picture` | GET / POST (multipart `file`) / DELETE | GET on own picture |
| `/accounts/profile/picture/{userId}` | GET | view another user's picture |
| `/movies/titles` | GET `?search=&page=&count=&parentTitleId=&genre=&category=` | public |
| `/movies/titles` | POST | admin only |
| `/movies/titles/{id}` | GET | public |
| `/movies/titles/{id}` | PUT / PATCH | admin only |
| `/movies/titles/{id}` | DELETE | admin only, cascades to child titles + their videos |
| `/movies/videos/{titleId}` | GET | public — list videos under a title |
| `/movies/videos/{titleId}` | POST (multipart `file`, `name`, `description`, `duration`) | admin only |
| `/movies/videos/{titleId}/{videoId}` | PUT / PATCH | admin only |
| `/movies/videos/{titleId}/{videoId}` | DELETE | admin only |
| `/movies/videos/{titleId}` | DELETE | admin only — deletes all videos under the title |
| `/movies/play/{titleId}/{videoId}` | GET | streams with HTTP `Range` support (206 Partial Content) |

All JSON responses are wrapped as `{ success, message, data }` (see `ApiResponse`).

## Video streaming

`PlayController` reads the `Range` header, fetches only the requested byte range
from S3 (`GetObjectRequest.range(...)`), and streams it back with
`206 Partial Content` + `Content-Range`/`Accept-Ranges` headers so browsers and
video players can seek without downloading the whole file. A `VideoPlay` row is
recorded once per full (non-ranged) request from an authenticated user.

## Redis usage

`CacheService` wraps a `StringRedisTemplate` for:
- JWT logout blacklist (`jwt:blacklist:<jti>`, TTL = token's remaining lifetime)
- OTP codes for email/phone verification (`otp:<scope>`, TTL from `OTP_EXPIRATION_MINUTES`)
- General purpose `get/set/delete/exists` for anything else (e.g. caching title lookups)

A JSON-serializing `RedisTemplate<String,Object>` bean is also provided in
`RedisConfig` for future use beyond simple strings.

## Running locally

```bash
cp .env.example .env   # fill in JWT_SECRET at minimum, plus AWS creds for real uploads
docker compose up --build
```

The app will be available at `http://localhost:8080`. Postgres and Redis run as
sibling containers (`database`, `redis`) per the compose file, both with named volumes
and healthchecks the `webserver` service waits on.

For local development without S3, you can point `AWS_S3_ENDPOINT` at a MinIO or
LocalStack container and set `AWS_S3_PATH_STYLE_ACCESS=true`.
