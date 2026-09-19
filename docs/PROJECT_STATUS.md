# Project status

Updated: 2026-09-19

## Boundary

- Android project only: `MuPiBox-Control-Android`.
- MuPiBox-NG is read-only reference and must not be modified from this workstream.

## Prepared

- native Kotlin + Compose/Material 3 project,
- AGP 9.4 / Gradle 9.6 bootstrap scripts,
- targetSdk 36 / compileSdk 37,
- manual saved box endpoints with health check,
- LAN-only host validation + DNS-level public-address guard,
- local player control,
- Spotify control with source-aware volume scaling,
- `/api/speak` TTS,
- battery + Wi-Fi compact state,
- Bluetooth explicit/on-demand refresh,
- DataStore persistence,
- future `_mupibox._tcp.` NSD client (inactive until server advertises it),
- JVM API fixture tests and source-selection/volume tests,
- GitHub Actions workflow,
- LXC Android SDK bootstrap,
- Claude Code rules + local-ai MCP bootstrap,
- Play Store/research/API docs.

## Not yet verified in this preparation environment

A complete Gradle/Android build could not be run here because the Android SDK/Gradle distributions
are intentionally installed on the user's development LXC. The first Claude task is therefore to
bootstrap the LXC wrapper and run `test`, `lint`, and `assembleDebug` before feature work.

## Deliberately not implemented yet

- no server changes,
- no admin/config UI,
- no persisted admin password,
- no Wi-Fi password storage,
- no automatic subnet scanning,
- no active mDNS discovery until server support exists,
- no local-media browser yet,
- no production icon/signing/store listing yet.

## Next execution order

1. Clone `splitti/MuPiBox-Control-Android` to `/opt/mupibox-control`.
2. Run `scripts/bootstrap-lxc.sh` once as root.
3. Run `scripts/init-gradle-wrapper.sh`.
4. Run `scripts/setup-claude.sh` if local-ai MCP is desired.
5. Run the prompt in `docs/claude-first-task.md`.
6. Install debug APK on a real Android phone and verify against a real box.
