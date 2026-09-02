# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository overview

Persona Messenger is a Persona 5-styled, offline-first messenger with two independent Gradle projects sharing one repo:

- **`messenger/`** — Ktor (Kotlin) backend: PostgreSQL via Ktorm, Flyway migrations, JWT auth, WebSockets, Koin DI.
- **`messenger-app/`** — Android client: Jetpack Compose, MVVM + Koin DI, Room (offline cache), Ktor client (HTTP + WebSockets), DataStore.

They only communicate over the HTTP/WebSocket API — there is no shared Kotlin code/module between them. Treat them as separate codebases when making changes; a full-stack feature touches both independently.

## Commands

### Backend (`messenger/`)

Run from inside `messenger/`:

```bash
./gradlew run                 # start the server (http://localhost:8080)
./gradlew build                # compile + run tests
./gradlew test                 # run all tests
./gradlew test --tests "features.friends.FriendRoutesTest"          # single test class
./gradlew test --tests "features.friends.FriendRoutesTest.add friend by uid succeeds both ways"  # single test
```

Integration tests hit a real Postgres database (`messenger_test`) via Flyway-migrated `TestDatabase` — no mocking. A local Postgres instance must be reachable (defaults to `localhost:5432`, overridable with `TEST_DATABASE_URL` / `TEST_DATABASE_USERNAME` / `TEST_DATABASE_PASSWORD`). `docker compose up db` from the repo root is the easiest way to get one.

### Android app (`messenger-app/`)

Run from inside `messenger-app/`:

```bash
./gradlew :app:assembleDebug              # build debug APK
./gradlew :app:testDebugUnitTest           # JVM unit tests
./gradlew :app:testDebugUnitTest --tests "dev.compose.messenger.ExampleUnitTest"  # single test
./gradlew :app:connectedDebugAndroidTest   # instrumented tests (needs device/emulator)
```

### Backend stack via Docker

From the repo root:

```bash
docker compose up --build
```

Starts Postgres (5432) and the Ktor backend (8080) only. The Android app is **not** part of the Docker stack — build/install it with Gradle (`./gradlew :app:installDebug` from `messenger-app/`) and side-load the APK onto test devices.

### Connecting the app to the backend

- Emulator: pre-configured for `10.0.2.2:8080` (Docker backend).
- Physical device: update the host in [KtorClient.kt](messenger-app/app/src/main/java/dev/compose/messenger/core/network/KtorClient.kt) and [WebSocketService.kt](messenger-app/app/src/main/java/dev/compose/messenger/core/network/WebSocketService.kt) to your machine's LAN IP.

## Backend architecture (`messenger/src/main/kotlin`)

Feature-sliced under `features/<name>/`, each following the same layering:

- `<Name>Table.kt` — Ktorm `Table` object (schema mapping only).
- `<Name>Repository.kt` — Ktorm queries, no business logic.
- `<Name>Service.kt` — business logic; throws [`AppException`](messenger/src/main/kotlin/common/AppException.kt) subtypes (`BadRequestException`, `UnauthorizedException`, `ForbiddenException`, `NotFoundException`, `ConflictException`) rather than handling errors itself.
- `<Name>Routes.kt` — a `Route.<name>Routes(...)` extension registering endpoints; JWT-protected routes wrap in `authenticate("auth-jwt")` and pull `userId` off the `JWTPrincipal`.
- `<Name>Dto.kt` — request/response bodies.

Cross-cutting wiring lives in `common/`:
- [`DependencyInjection.kt`](messenger/src/main/kotlin/common/DependencyInjection.kt) — single Koin module wiring every repository/service as `single { }`; add new services here.
- [`Routing.kt`](messenger/src/main/kotlin/common/Routing.kt) — resolves services from Koin and mounts each feature's `Routes()` function.
- [`StatusPages.kt`](messenger/src/main/kotlin/common/StatusPages.kt) — the *only* place exceptions become HTTP responses. `AppException` subtypes map to their status code; everything unexpected is logged server-side and collapsed to a generic 500 (never leak internals to the client). Don't catch-and-respond in routes/services — throw instead.
- `common/websockets/` — `WebSocketManager` tracks live connections; `MessageWebSocket`/`WebSocketRoutes` handle the real-time message protocol.
- [`main.kt`](messenger/src/main/kotlin/main.kt) — `Application.module()` is the composition root; order matters (DI → serialization → status pages → security → websockets → routing).

Database migrations are plain SQL in `messenger/src/main/resources/db/migration/`, run by Flyway on startup (and in tests via `TestDatabase`). Add new migrations as the next `V<n>__description.sql`; never edit an already-applied migration.

Tests (`messenger/src/test/kotlin`) mirror the `features/` package structure 1:1 and use `support/TestModule.kt` (mirrors `Application.module()` but injects `TestDatabase`), `support/TestClient.kt`, and `support/AuthTestHelper.kt` for auth/registration helpers. Tests are full integration tests against `testApplication { ... }` + a real (migrated, truncated-between-tests) database — follow that pattern rather than introducing mocks.

## Android app architecture (`messenger-app/app/src/main/java/dev/compose/messenger`)

MVVM with unidirectional data flow, feature-sliced under `feature/<name>/`:

- `data/` — repository interface + `Impl`, DTOs, mappers (`data/mapper/`) between DTOs ⇄ Room entities ⇄ domain models. Repositories combine the local `*Dao` (Room, offline cache) with the matching `*Api` (Ktor client): reads stream from Room `Flow`s and trigger a background sync from the network when empty; writes call the API then persist the result locally. Network failures are converted to `Result.failure` via `toUserMessage()`, not thrown.
- `domain/` — plain domain models used by the UI layer.
- `presentation/` — Compose screens (`*Screen.kt`) + `*ViewModel.kt`, plus a `<Name>Route` composable wrapper that screens' navigation callbacks are wired through (see `MessengerNavHost`).
- `di/<Name>Module.kt` — a Koin module for the feature (`single`/`viewModel` bindings), included from [`core/di/Modules.kt`](messenger-app/app/src/main/java/dev/compose/messenger/core/di/Modules.kt)'s top-level `appModule`.

`core/` holds shared infra:
- `core/di/Modules.kt` — `coreModule` (Room DB + DAOs, Ktor HTTP client, all `*Api` clients, `WebSocketService`, `PreferencesManager`) plus the top-level `appModule` that includes every feature module. Register new cross-cutting singletons in `coreModule`.
- `core/database/` — Room: `MessengerDatabase`, `entity/`, `dao/`. Offline-first: the DB is the source of truth the UI observes; the network is a background sync source.
- `core/network/` — `KtorClient.kt` (HTTP client factory + base host), `WebSocketService.kt` (real-time), `api/*Api.kt` (one Ktor-based API class per feature), `NetworkError.kt` (`toUserMessage()` extension used by repositories).
- `core/navigation/MessengerNavHost.kt` — the single `NavHost`; string routes (`"auth"`, `"conversations"`, `"chat/{conversationId}"`, etc.), no navigation library beyond Navigation Compose.
- `core/designsystem/` — the custom Persona-inspired design system: `theme/` (Color/Type/Theme), `component/` (e.g. `PersonaButton`, `PersonaTextField`, `PersonaAvatar`, `BackgroundParticles`, `SeasonMenu`), `util/` for drawing/modifier helpers. Prefer these components over raw Material3 widgets for anything user-facing.
- `core/datastore/PreferencesManager.kt` — DataStore-backed JWT + user settings (e.g. season/background color) persistence.

`MainActivity`/`MainViewModel`/`MessengerApp.kt` are the app entry point, wiring theme (season/background color) and `MessengerNavHost` together.

## Notes

- Backend auth: passwords hashed with jBCrypt, sessions are JWT bearer tokens (`auth-jwt` provider), configured in [`Security.kt`](messenger/src/main/kotlin/common/Security.kt).
