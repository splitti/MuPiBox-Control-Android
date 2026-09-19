# Android architecture

## Principles

MuPiBox Control is a LAN control client, not a second MuPiBox implementation. Playback arbitration,
volume limits, provider ownership and hardware safety stay on the box. The Android app observes state
and sends documented commands.

```text
Compose UI
   ↓ actions                 ↑ UiState
Screen ViewModel
   ↓                        ↑
Repository (box/control/config later)
   ↓                        ↑
HTTP / DataStore / Android NSD
   ↓
MuPiBox-NG LAN API
```

The structure follows Android's recommended split between UI and data layers, repositories as the
boundary to data sources, unidirectional data flow, screen-level ViewModels, coroutines and Flow.

## Current packages

```text
de.mupibox.control
├── data
│   ├── api          dynamic HTTP client + wire DTOs
│   ├── discovery    Android NSD/mDNS
│   ├── local        DataStore
│   └── repository   app-facing data operations
├── model            app models such as BoxEndpoint
└── ui
    ├── boxes        box list/add
    ├── control      playback/status/TTS
    └── theme
```

## Refresh strategy

M1 control screen:

- player + Spotify state: ~1 s while screen is visible,
- system/battery/Wi-Fi: ~5 s,
- Bluetooth: **on demand only** because the current endpoint actively scans for ~8 s,
- no background polling when the control screen/ViewModel is gone.

Later, if the box exposes an event stream/WebSocket for consolidated state, prefer that over faster polling.

## Discovery

Layers:

1. saved manual boxes (always works),
2. `_mupibox._tcp.` via Android `NsdManager` when the server eventually advertises it,
3. no blind whole-subnet scan by default.

Blind scanning is intentionally excluded: it is noisy, slow on large LANs, and becomes increasingly
sensitive under Android local-network privacy protections.

## Cleartext LAN HTTP

The box currently serves HTTP. Android normally blocks cleartext for modern target SDKs, so the app
uses a Network Security Config that permits HTTP. Because dynamic private IPs cannot be enumerated in
the XML, application code enforces local-only endpoints with `LocalEndpointValidator`.

Accepted target classes:

- RFC1918 IPv4,
- link-local/loopback,
- IPv6 ULA/link-local/loopback,
- `.local`, `.home.arpa`,
- single-label LAN DNS names.

No public hostname/IP is accepted by the normal add-box path.

## Authentication/configuration roadmap

M1 needs no admin session for basic player/TTS/system status.

Later configuration flow:

1. query `/api/admin/auth`,
2. if protected, ask user for admin password,
3. POST login and keep `mupibox_admin_session` in an in-memory CookieJar,
4. perform explicit admin/connectivity operation,
5. do not persist the password,
6. optionally log out when admin/config session ends.

If MuPiBox-NG later introduces revocable app tokens, prefer pairing + scoped token over repeatedly
using the admin cookie. Store long-lived tokens only using Android Keystore-backed encryption.

## Navigation

The scaffold intentionally keeps the first two screens simple. Before adding several configuration
screens, migrate navigation to stable AndroidX Navigation 3 rather than growing a custom state router.

## Testing priorities

- API DTO parsing with captured server fixtures,
- local endpoint validation,
- repository behavior with fake API client,
- ViewModel state/action tests,
- Compose tests for offline/unknown battery/auth-required states,
- real-phone testing on Wi-Fi for NSD, LAN permission behavior and cleartext access.
