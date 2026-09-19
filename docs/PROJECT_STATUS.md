# Project status

Updated: 2026-09-19

## Boundary

- Android project only: `MuPiBox-Control-Android`.
- MuPiBox-NG is read-only reference and must not be modified from this workstream.

## Prepared

- native Kotlin + Compose/Material 3 project,
- AGP 9.4 / official Gradle 9.6 wrapper + bootstrap scripts,
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

GitHub Actions has successfully run `test`, `lint`, and `assembleDebug` on the prepared scaffold. The
official Gradle 9.6.0 wrapper is committed. The remaining environment verification is on the user's
existing development LXC and a real Android phone.

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
3. Run `./gradlew --version` and `./scripts/check.sh`.
4. Run `scripts/setup-claude.sh` if local-ai MCP is desired.
5. Run the prompt in `docs/claude-first-task.md`.
6. Install the debug APK on a real Android phone and verify against a real box.
