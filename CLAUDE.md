# MuPiBox Control – Claude Code project instructions

## Scope

This repository is the **native Android client** for MuPiBox-NG.

Hard boundary: **never edit, commit, push, open PRs/issues in, or otherwise modify `splitti/MuPiBox-NG` from work on this repo.** The MuPiBox-NG checkout may be inspected read-only only when the user explicitly needs current API verification. The frozen API facts needed for routine Android work are in `docs/mupibox-api-current.md`.

Repo target: `splitti/MuPiBox-Control-Android`.
Recommended LXC path: `/opt/mupibox-control`.
Existing server checkout remains `/opt/mupibox-ng` and is not part of this project.

## Product goal

MuPiBox Control is a Play-Store-ready Android companion app. M1:
- list/save MuPiBox devices,
- open a box,
- play/pause, previous/next, volume,
- send text to `/api/speak`,
- compact battery + Wi-Fi state,
- Bluetooth state on demand when current API/auth permits.

Later: local-media browsing, multi-box actions, admin login, Wi-Fi/Bluetooth setup, box configuration, updates/diagnostics.

## Stack

- Kotlin, single Android application module.
- Jetpack Compose + Material 3.
- AGP 9.4.0, Gradle 9.6.0, JDK 17+.
- compileSdk 37, targetSdk 36, minSdk 26.
- AGP built-in Kotlin; do NOT add `org.jetbrains.kotlin.android` unless deliberately opting out of built-in Kotlin.
- Compose compiler plugin 2.4.10; Compose BOM 2026.09.00.
- Coroutines/Flow; screen-level ViewModels; repositories between UI and data sources.
- OkHttp for dynamic box endpoints; Gson for JSON; Preferences DataStore for saved boxes.
- No WebView app architecture.

## Build commands

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew bundleRelease
```

Before commit, run at least `test`, `lint`, `assembleDebug`.

## Architecture rules

- UDF: Compose renders immutable UiState; ViewModels receive user actions.
- Composables do not call HTTP/DataStore directly.
- Keep Android Context out of ViewModels.
- Network and persistence live under `data/`; server DTOs stay separate from UI models when complexity grows.
- Do not create a giant domain layer prematurely; add use cases only for reused/complex business logic.
- Treat box API responses as forward-compatible: tolerate unknown JSON fields and absent optional features.
- One box may expose local playback and Spotify simultaneously; audio source arbitration remains server-owned.

## Network/security rules

- Current MuPiBox-NG API is LAN HTTP. Cleartext is enabled only because dynamic private IPs are required.
- Every user-supplied endpoint must pass `LocalEndpointValidator`; never send credentials/TTS to arbitrary Internet hosts.
- Do not log TTS contents, Wi-Fi passwords, admin passwords, cookies, provider secrets or tokens.
- Do not persist Wi-Fi/admin passwords. If long-lived app tokens are added later, encrypt them with Android Keystore-backed storage.
- Current targetSdk=36: do not declare/request `ACCESS_LOCAL_NETWORK`. Add it when targetSdk becomes 37, following Android 17 guidance.
- mDNS service type reserved by this app: `_mupibox._tcp.`. Current server does not advertise it; manual IP/hostname must continue to work.

## Current API

Use `docs/mupibox-api-current.md` as the contract snapshot. Key M1 endpoints:
- `GET /api/health`
- `GET /api/status`
- `GET /api/system`
- `GET /api/info`
- `POST /api/command`
- `GET /api/spotify/status`
- `POST /api/spotify/command`
- `POST /api/speak`

Connectivity/admin endpoints may require admin cookie authentication. Bluetooth scan is expensive (~8 s) and must never be polled every second.

## Product behavior

- Unreachable boxes remain saved and show an offline/error state; do not delete automatically.
- Volume respects server-provided `max_volume`/Spotify `volume_steps`.
- TTS can take close to 20 s on a cache miss; UI must remain responsive and show busy/error state.
- Battery is optional. `available=false` means show unknown/hidden, not 0%.
- Wi-Fi signal is optional; do not infer SSID because `/api/system` does not currently provide it.
- Bluetooth compact state is best-effort until a lightweight server status endpoint exists; do not abuse scan endpoints.

## Git/Claude workflow

- Small, focused commits. No force-push/history rewrite unless explicitly requested.
- Never commit signing keys, `keystore.properties`, passwords or tokens.
- Prefer local `local_ai` MCP for repetitive file/diff/log analysis when connected; Claude remains reviewer/orchestrator.
- Keep this file concise. Task-specific details belong in `.claude/rules/` or `.claude/skills/`.
- Before implementing an API assumption not documented in the snapshot, state the assumption and verify read-only against MuPiBox-NG if necessary.
