# MuPiBox Control – Privacy Policy Draft

_Last updated: 2026-09-19. Draft for the future Play Store listing; review again before publication._

MuPiBox Control is designed to control MuPiBox devices on the user's local network.

## Data handled by the app

The current application can store locally on the Android device:

- names chosen for MuPiBox devices,
- local host names or private IP addresses,
- the local API port for each saved box.

When the user sends a text-to-speech message, that text is transmitted directly to the selected MuPiBox on the local network. Playback commands and status requests are also sent directly to the selected local box.

## Data not sent to the app developer

The current application contains no advertising SDK, analytics SDK, crash-reporting service, user account system, or developer-operated cloud backend. MuPiBox Control does not intentionally send device-control data, TTS text, saved box addresses, or playback information to the developer.

## Credentials

The MVP does not store Wi-Fi passwords or MuPiBox administrator passwords. If future releases add pairing tokens or administrator credentials, this policy and the Play Console Data safety declaration must be updated before release.

## Third-party services

Spotify playback may be active on a MuPiBox, but the Android app currently communicates with the MuPiBox local API rather than signing the user into Spotify itself. Any future direct integration with external providers requires a privacy-policy update.

## Network security

The current MuPiBox API uses HTTP on the local network. The app restricts user-configured targets to LAN-local addresses and host names. A future authenticated HTTPS/pairing design should replace cleartext LAN control when the MuPiBox API supports it.

## Changes

This draft must be reviewed whenever networking, authentication, analytics, cloud services, crash reporting, or external provider integrations change.
