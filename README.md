# Persona Messenger

A modern, offline-first messenger application inspired by the Persona 5 IM interface. This repository contains both the Android client and the Ktor backend.

## 🚀 Quick Start (Docker)

To boot the backend stack (PostgreSQL + Ktor Backend), run the following command from the root directory:

```bash
docker compose up --build
```

### What happens?
1.  **Database**: A PostgreSQL instance starts on port `5432`.
2.  **Backend**: The Ktor API starts on port `8080`.

The Android app is built and installed separately (see below) — it is not part of the Docker stack.

---

## 🛠 Tech Stack

### Android App (`/messenger-app`)
- **UI**: Jetpack Compose (Custom Persona-inspired design system).
- **Architecture**: MVVM + Feature-based structure + UDF.
- **DI**: Koin.
- **Local DB**: Room (Offline-first caching).
- **Networking**: Ktor Client + WebSockets (Real-time).
- **Persistence**: DataStore (JWT & User Settings).
- **Navigation**: Navigation Compose.

### Backend (`/messenger`)
- **Framework**: Ktor (Kotlin).
- **Database**: PostgreSQL with Ktorm.
- **Migrations**: Flyway.
- **DI**: Koin.
- **Real-time**: Ktor WebSockets.
- **Auth**: JWT (JSON Web Tokens).

---

## 📱 Local Development

### Building the Android App
Build and install the debug APK straight onto a device or emulator from `/messenger-app`:

```bash
./gradlew :app:installDebug
```

Or build the APK only (`app/build/outputs/apk/debug/app-debug.apk`) for manual install:

```bash
./gradlew :app:assembleDebug
```

### Connecting App to Backend
- **Emulator**: The app is pre-configured to connect to `10.0.2.2:8080` (Docker Backend).
- **Physical Device**: Update `host` in `KtorClient.kt` and `WebSocketService.kt` to your machine's local IP address.

### Running Backend Individually
If you want to run the backend without Docker for debugging, ensure you have a local PostgreSQL instance running and set the environment variables:
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
