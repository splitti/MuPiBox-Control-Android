# Claude handoff – next execution order

The repository already contains the Android M1 scaffold. Do not restart from scratch.

## First session on the LXC

1. Clone `splitti/MuPiBox-Control-Android` to `/opt/mupibox-control`.
2. Read `CLAUDE.md` and `docs/PROJECT_STATUS.md`.
3. Run `sudo ./scripts/bootstrap-lxc.sh` once if Android SDK/JDK 17 are not ready.
4. Verify the committed wrapper with `./gradlew --version`.
5. Run `./scripts/check.sh`.
6. Fix compile/lint/test failures surgically; do not redesign unrelated code.
7. Start `python3 tools/mock_mupibox_server.py --port 8090` for repeatable client tests.
8. Build the debug APK and test on a real Android phone using the LXC's private LAN IP.
9. Work through `docs/m1-acceptance.md`.

## After baseline is green

Priorities:

1. UI polish and lifecycle/offline behavior.
2. Box delete/edit controls and optional manual refresh.
3. Discovery integration only when a real service is discoverable; manual host/IP stays supported.
4. Admin authentication abstraction for future Wi-Fi/Bluetooth configuration.
5. Play Store assets/signing only after package name and visual identity are final.

## Hard boundary

Never modify MuPiBox-NG as part of these tasks. If an Android feature is blocked by a missing server capability, document the required server-side capability in this repo and stop there until the user explicitly authorizes separate MuPiBox-NG work.
