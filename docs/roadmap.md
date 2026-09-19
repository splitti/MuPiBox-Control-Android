# Roadmap

## M0 – repository/toolchain

- separate `MuPiBox-Control-Android` GitHub repository,
- LXC checkout under `/opt/mupibox-control`,
- JDK/Android SDK/Gradle wrapper,
- Claude Code + local AI MCP,
- CI: test, lint, debug build.

## M1 – control MVP

- manual box add by host/IP,
- saved multi-box list,
- reachability check via `/api/health`,
- control screen,
- local + Spotify play/pause/previous/next,
- correct per-source volume range,
- TTS text send,
- battery + Wi-Fi chips,
- Bluetooth on-demand status,
- offline/error states.

## M1.1 – discovery

- Android NSD client already scaffolded for `_mupibox._tcp.`,
- activate only when the server actually advertises a documented service,
- merge discovered + saved boxes without duplicates,
- retain manual entry forever as fallback.

## M2 – media selection

Use existing `/api/home`, `/api/library`, `/api/cover/{id}`:

- browse data-driven home rows,
- local folder/item selection,
- resume state,
- current cover and progress/seek,
- avoid hardcoding categories.

## M3 – admin/config session

- `/api/admin/auth` state,
- password login with in-memory cookie jar,
- no password persistence,
- clear separation between normal control and admin/config mode.

## M4 – Wi-Fi and Bluetooth setup

Wi-Fi:

- adapters,
- scan networks,
- connect,
- preferred adapter,
- DHCP/static configuration,
- onboard-Wi-Fi disable guardrails.

Bluetooth:

- explicit scan screen,
- pair/connect/disconnect/remove,
- show connected audio target,
- never background-scan continuously.

## M5 – box settings

- audio/startup/max volume,
- display/brightness/UI size/theme,
- TTS language/voice,
- power/idle,
- MuPiHat/battery settings,
- diagnostics and versions.

Only expose settings that the server API owns and validates. Never reproduce hardware safety logic in Android.

## M6 – Play Store production

- adaptive app icon,
- onboarding/permission rationale,
- localized strings,
- accessibility pass,
- signing + Play App Signing,
- privacy policy/Data Safety,
- internal/closed testing,
- release CI.

## Later ideas

- grouped/favorite boxes,
- one announcement to several boxes,
- home-screen widget / quick actions,
- Wear OS remote as a separate product decision,
- scoped pairing/app tokens if server supports them,
- optional remote access only with an explicit secure architecture (never raw port-forwarding by default).
