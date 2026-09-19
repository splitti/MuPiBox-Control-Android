# MuPiBox API compatibility matrix

Snapshot date: 2026-09-19. MuPiBox-NG is read-only reference for this project.

| App feature | Endpoint | MVP | Auth note |
| --- | --- | --- | --- |
| Reachability/version | `GET /api/health` | implemented | no admin session in current snapshot |
| Local player status | `GET /api/status` | implemented | no admin session in current snapshot |
| Local player control | `POST /api/command` | implemented | no admin session in current snapshot |
| Battery/Wi-Fi status | `GET /api/system` | implemented | no admin session in current snapshot |
| Box info | `GET /api/info` | client implemented | no admin session in current snapshot |
| TTS announcement | `POST /api/speak` | implemented | no admin session in current snapshot |
| Spotify status | `GET /api/spotify/status` | implemented | no admin session in current snapshot |
| Spotify control | `POST /api/spotify/command` | implemented | no admin session in current snapshot |
| Bluetooth scan/devices | `GET /api/connectivity/bluetooth` | on-demand only | admin session required from non-loopback when protection is configured |
| Bluetooth pair/connect | `POST /api/connectivity/bluetooth/command` | later | admin session required from LAN |
| Wi-Fi adapters | `GET /api/connectivity/wifi/adapters` | later | admin session required from LAN |
| Wi-Fi scan | `GET /api/connectivity/wifi` | later | admin session required from LAN |
| Wi-Fi connect | `POST /api/connectivity/wifi/connect` | later | admin session required from LAN |
| Settings | `GET/PUT /api/admin/settings` | later | admin session |
| Admin login | `POST /api/admin/login` | later | cookie session in current snapshot |

Do not expand this table from assumptions. Verify read-only against MuPiBox-NG before documenting a new endpoint.
