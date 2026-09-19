# MuPiBox Control

Native Android companion app for MuPiBox-NG.

**Status:** initial scaffold / `0.1.0-dev`

The first milestone is deliberately small:

- save and select one or more MuPiBox devices,
- play/pause, previous/next and volume,
- send arbitrary TTS text to a box,
- show compact battery and Wi-Fi state,
- offer Bluetooth state as an on-demand view where the current box API allows it,
- keep the architecture ready for discovery and later device configuration.

## Important repository boundary

This repository is the Android client only. **Do not modify MuPiBox-NG from this repository.**
The server project is read-only reference material for the app. The API snapshot used for this
scaffold is documented in [`docs/mupibox-api-current.md`](docs/mupibox-api-current.md).

## Stack

- Kotlin / native Android
- Jetpack Compose + Material 3
- Android Gradle Plugin 9.4.0
- Gradle 9.6.0
- compileSdk 37, targetSdk 36, minSdk 26
- OkHttp 5.3.0
- Gson 2.14.0
- DataStore Preferences 1.2.1
- Coroutines / Flow

`targetSdk=36` is intentional for the first release: since 31 August 2026 Google Play requires
new phone apps to target Android 16 / API 36 or newer. Android 17 / API 37 introduces the new
`ACCESS_LOCAL_NETWORK` runtime permission for apps that target 37; the migration is prepared
in the docs but should not be enabled prematurely.

## DEV directory on the existing LXC

Recommended checkout:

```bash
/opt/mupibox-control
```

Keep the existing server checkout separate:

```bash
/opt/mupibox-ng
```

Bootstrap the Android toolchain with:

```bash
./scripts/bootstrap-lxc.sh
```

The official Gradle wrapper is committed. Verify it after cloning:

```bash
./gradlew --version
```

`./scripts/init-gradle-wrapper.sh` remains available only for an intentional wrapper refresh.

Build/test:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew bundleRelease
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

AAB output for Play Store:

```text
app/build/outputs/bundle/release/app-release.aab
```

## Device connection

For the MVP a box can always be added by local hostname or private IP and port (default `8090`).
`NsdBoxDiscovery` is already prepared for `_mupibox._tcp.`, but MuPiBox-NG did not advertise that
service when this scaffold was created. Do not change MuPiBox-NG just to make the Android scaffold
work; manual host/IP is the reliable path for now.

## Claude Code

Read [`CLAUDE.md`](CLAUDE.md) first. It contains the permanent project rules and points Claude to
the API snapshot instead of making it re-analyse the server repository each session.

To reuse the existing local Qwen MCP helper from the MuPiBox development LXC without changing
MuPiBox-NG:

```bash
./scripts/setup-claude.sh
claude mcp list
claude
```

## Development aids

For development without a physical MuPiBox:

```bash
python3 tools/mock_mupibox_server.py --port 8090
```

Then add the LXC's private LAN IP as a box in the app. See:

- [`docs/claude-handoff.md`](docs/claude-handoff.md)
- [`docs/m1-acceptance.md`](docs/m1-acceptance.md)
- [`docs/api-compatibility-matrix.md`](docs/api-compatibility-matrix.md)
- [`PRIVACY.md`](PRIVACY.md)
- [`SECURITY.md`](SECURITY.md)
