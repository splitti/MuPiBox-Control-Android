# MuPiBox-NG API snapshot used by MuPiBox Control

This document is a **read-only snapshot**, not a request to modify MuPiBox-NG.

Verified against:

- repository: `splitti/MuPiBox-NG`
- branch: `rebuild/go-foundation`
- commit observed during preparation: `a709e64a17db51ab848a5d5a6c6f6ccb923020e5`
- date: 2026-09-19

If the server evolves, update this document from a read-only inspection and then adapt the Android client in this repo.

## M1 public/control endpoints

### Health

`GET /api/health`

```json
{
  "status": "ok",
  "version": "0.1.0-dev"
}
```

Use this to validate a manually entered box endpoint.

### Local player status

`GET /api/status`

Shape:

```json
{
  "state": "playing",
  "backend": "mpv",
  "folder_id": "...",
  "folder": "Die drei ???",
  "cover": "...",
  "queue": [
    {
      "id": "...",
      "title": "Titel 01",
      "provider": "local",
      "resume_policy": "position"
    }
  ],
  "index": 0,
  "position": 12.5,
  "duration": 240.0,
  "volume": 30,
  "max_volume": 50
}
```

`error` may be present.

### Local player command

`POST /api/command`

`Content-Type: application/json`

Examples:

```json
{"action":"play"}
{"action":"pause"}
{"action":"toggle"}
{"action":"previous"}
{"action":"next"}
{"action":"stop"}
{"action":"seek","value":42.5}
{"action":"volume","value":25}
{"action":"volume_delta","value":5}
```

To start a local folder:

```json
{"action":"folder","folder_id":"<folder-id>"}
```

To resume a known item:

```json
{"action":"resume","folder_id":"<folder-id>","item_index":2}
```

The server clamps volume to its configured maximum. Keep that rule server-owned.

### System status

`GET /api/system`

Current contract:

```json
{
  "online": true,
  "wifi": {
    "connected": true,
    "interface": "wlan0",
    "signal_dbm": -51,
    "quality_percent": 72
  },
  "battery": {
    "available": true,
    "percent": 83,
    "charging": false
  }
}
```

Important limitations:

- Wi-Fi does not currently include SSID or IP in this response.
- Battery is optional. Current generic implementation reads Linux power-supply data; MuPiHat hardware telemetry may evolve independently.
- Android UI must handle `available=false` without inventing a value.

### Info

`GET /api/info`

Contains server version, simulation/backend, TTS/power/display/theme and whether settings are persistent. Treat extra fields as optional/forward-compatible.

### TTS / announcement

`POST /api/speak`

```json
{
  "source_type": "android-app",
  "source_ref": "mupibox-control",
  "text": "Essen ist fertig"
}
```

Current server behavior:

- pauses local playback before TTS,
- pauses Spotify if Spotify is active,
- a Piper cache miss can take close to 20 seconds,
- endpoint is not under `/api/admin/`.

The app must not freeze while waiting and must not log the spoken text.

### Spotify status

`GET /api/spotify/status`

```json
{
  "connected": true,
  "playing": true,
  "paused": false,
  "buffering": false,
  "volume": 32768,
  "volume_steps": 65535,
  "track": {
    "name": "Song",
    "artists": ["Artist"],
    "album": "Album",
    "cover": "https://...",
    "position_ms": 42000,
    "duration_ms": 180000
  }
}
```

Do not assume Spotify volume is 0..100; obey `volume_steps`.

### Spotify command

`POST /api/spotify/command`

```json
{"action":"pause"}
{"action":"resume"}
{"action":"next"}
{"action":"previous"}
{"action":"seek","value":42000}
{"action":"volume","value":20000}
```

The response is a fresh Spotify status where possible.

## Media endpoints useful for later phases

- `GET /api/home` – data-driven categories/rows/items.
- `GET /api/library` – local folders.
- `GET /api/cover/{id}` – cover file.

These are deliberately not required for the first control-only MVP.

## Authentication behavior relevant to future configuration

Current admin session cookie name:

```text
mupibox_admin_session
```

Session lifetime: 12 hours; current store is server-memory based.

Authentication endpoints:

- `GET /api/admin/auth`
- `POST /api/admin/login` with `{ "password": "..." }`
- `POST /api/admin/logout`

Remote requests require admin authentication when:

- path starts with `/api/admin/` (except auth/login), or
- path starts with `/api/connectivity/` and request is not loopback.

Native Android HTTP calls do not normally send browser `Origin`/`Sec-Fetch-Site` headers; do not add fake browser headers.

## Wi-Fi/config endpoints for a later app phase

Current server already exposes:

- `GET /api/connectivity/wifi/adapters`
- `POST /api/connectivity/wifi/adapters/state`
- `PUT /api/connectivity/wifi/preferences`
- `GET /api/connectivity/wifi`
- `POST /api/connectivity/wifi/connect`
- `GET /api/admin/settings`
- `PUT /api/admin/settings`

Wi-Fi connect request:

```json
{
  "ssid": "ExampleWiFi",
  "password": "not-to-be-logged-or-persisted-by-the-app",
  "interface": "wlan0"
}
```

Do not implement configuration by shelling into the Pi from Android. Use the server API when this feature is activated.

## Bluetooth endpoint caveat

`GET /api/connectivity/bluetooth` currently performs a real Bluetooth scan using `bluetoothctl --timeout 8 scan on` and can take several seconds. It also requires remote admin authentication when protection is configured.

Therefore:

- never poll it with player status,
- call only on explicit refresh / when opening Bluetooth setup,
- compact dashboard state should be cached/best-effort,
- if it returns 401, UI may say "Anmeldung erforderlich",
- if it returns 409, Bluetooth is disabled.

Commands are sent to `POST /api/connectivity/bluetooth/command` with `pair`, `connect`, `disconnect`, or `remove` plus a MAC address.

## Discovery gap

No MuPiBox-specific mDNS/DNS-SD advertisement was found in this snapshot. The Android project reserves:

```text
_mupibox._tcp.
```

for a future discovery contract, but **manual local hostname/IP entry is mandatory for M1**. Do not alter the server as part of Android-client work unless the user starts a separate server task.
