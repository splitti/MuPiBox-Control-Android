# M1 acceptance checklist

Use a real Android phone on the same LAN as either a MuPiBox or `tools/mock_mupibox_server.py`.

## Installation / startup

- [ ] Debug APK installs and opens without crash.
- [ ] App works in light and dark theme.
- [ ] Portrait UI remains usable on a typical phone.
- [ ] Rotation/recreation does not corrupt saved boxes.

## Box management

- [ ] Add by private IPv4 and port 8090.
- [ ] Add by valid local hostname where DNS resolves locally.
- [ ] Public IP / public DNS target is rejected.
- [ ] Invalid port is rejected.
- [ ] Box survives app restart.
- [ ] Offline box remains saved and reports an error instead of disappearing.

## Playback

- [ ] Local play/pause works.
- [ ] Local previous/next works.
- [ ] Local volume honors `max_volume`.
- [ ] Spotify status is displayed when Spotify is the active source.
- [ ] Spotify play/pause/previous/next works when active.
- [ ] Spotify volume percentage maps correctly to `volume_steps`.

## TTS

- [ ] Non-empty text is spoken by the selected box.
- [ ] UI remains responsive during a slow Piper cache miss.
- [ ] Errors are visible and recoverable.
- [ ] TTS text is not written to logs.

## Status

- [ ] Battery percentage displays when `available=true`.
- [ ] Missing battery displays unknown, not 0%.
- [ ] Charging state is visible.
- [ ] Wi-Fi quality displays when available.
- [ ] Bluetooth is refreshed only on user action, not polled continuously.
- [ ] Admin-protected Bluetooth endpoint failure is shown without breaking playback controls.

## Quality gate

- [ ] `./scripts/check.sh` passes.
- [ ] GitHub Actions passes.
- [ ] No credentials/signing files in git.
- [ ] `PRIVACY.md` and `docs/play-store-data-safety.md` still match the release behavior.
