# Ready-to-use Claude Code prompts

These prompts are intentionally specific so a fresh Claude session can act without re-deriving the
project boundary and MuPiBox API contract.

## 1. First build / scaffold validation

```text
Read CLAUDE.md, docs/PROJECT_STATUS.md, docs/mupibox-api-current.md and docs/architecture.md.
Work only in MuPiBox-Control-Android. MuPiBox-NG is read-only and must not be modified.

Run ./gradlew test, ./gradlew lint and ./gradlew assembleDebug. Fix only concrete build, lint and
test failures. Preserve targetSdk 36 and the LAN-only security guards. Do not redesign working code.
For every code fix, add or adjust the smallest useful test. Finish with exact command results and a
short changed-files summary.
```

## 2. Real-phone MVP integration

```text
Read CLAUDE.md and docs/mupibox-api-current.md. Work only in MuPiBox-Control-Android.

Prepare and perform the Android-side MVP integration test against a real MuPiBox endpoint supplied by
me. Do not change MuPiBox-NG. Verify in this order: /api/health, /api/status, /api/system,
/api/spotify/status, local play/pause, source-correct volume, Spotify play/pause/volume, then
/api/speak. Bluetooth is explicit/on-demand only and must not be polled. Never log TTS text or
credentials. If an API response differs from the frozen snapshot, capture only the non-secret shape,
report the mismatch, and adapt the Android client only after confirming the intended contract with
me.
```

## 3. UI polish without feature creep

```text
Read CLAUDE.md and docs/roadmap.md. Improve the M1 Compose UI only; do not add server features or
configuration screens yet. Keep the box list and control screen phone-first, touch-friendly and
Material 3. Add clear states for loading, offline, optional battery, Wi-Fi quality, Bluetooth auth
required, TTS busy, local playback and Spotify playback. Preserve accessibility content descriptions
and test important state rendering. Do not add analytics, ads, WebView or a new navigation framework
unless the existing two-screen router has actually become a blocker.
```

## 4. Media browsing (M2)

```text
Read CLAUDE.md, docs/mupibox-api-current.md and docs/roadmap.md. Implement M2 on Android only using the
existing /api/home, /api/library and /api/cover/{id} contracts. Do not hardcode media categories and
do not modify MuPiBox-NG. Keep server DTOs tolerant of unknown fields. Add fixture-based parser tests
before wiring the Compose UI. Playback commands must still go through ControlRepository/server-owned
arbitration.
```

## 5. Admin session foundation (M3, client side only)

```text
Read CLAUDE.md and the authentication section of docs/mupibox-api-current.md. Work only in the Android
repo; do not change MuPiBox-NG.

Implement an explicit admin/config session using /api/admin/auth, /api/admin/login and
/api/admin/logout. Password must be held only long enough for login and never stored or logged. Keep
session cookies in memory only for this phase. Normal playback/control must remain usable without
opening admin mode. Add tests for protected/unprotected/401 flows using a fake API or MockWebServer.
Do not fetch or display provider secrets as part of this task.
```

## 6. Wi-Fi/Bluetooth setup (M4, after M3)

```text
Read CLAUDE.md and docs/mupibox-api-current.md. Assume M3 admin session support exists. Implement
Android configuration UI against the documented connectivity endpoints only. Never SSH/shell into
the Pi from Android and never modify MuPiBox-NG.

Wi-Fi password is ephemeral input: never persist or log it. Bluetooth scan must occur only after an
explicit user action because the current endpoint performs a real multi-second scan. Show adapter,
network/device state and server errors clearly. Keep hardware/network validation server-owned.
```

## 7. Play Store release gate

```text
Read CLAUDE.md, docs/play-store.md and docs/research-2026-09-19.md. Do not change targetSdk casually.
Run release build/lint/tests with R8 enabled, inspect permissions, cleartext/LAN guards, backups,
logging and secrets. Produce a release-readiness checklist with only evidence-backed pass/fail items.
Do not create or commit signing keys. Build an AAB only after the code checks are green.
```
