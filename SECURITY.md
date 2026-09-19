# Security

## Scope

This repository is the Android client only. Security changes to MuPiBox-NG belong in its own project and must not be made as a side effect of Android work.

## Current trust model

- The MVP talks to a user-selected MuPiBox over the local network.
- MuPiBox-NG currently exposes the relevant M1 API over HTTP.
- `LocalEndpointValidator` and `LanOnlyDns` are defense-in-depth controls that prevent the generic HTTP client from becoming an arbitrary Internet client.
- TTS text and control requests must never be logged.
- Wi-Fi passwords, administrator passwords, session cookies, provider credentials and signing secrets must never be committed.

## Reporting

Until a dedicated security contact is published, do not open a public issue containing credentials or exploit details. Contact the repository owner privately through an established project contact channel.

## Before Play Store release

Review at minimum:

- cleartext-network justification and migration path,
- authentication/pairing design,
- Android Keystore use for any persistent tokens,
- exported Android components,
- dependency audit,
- release-signing isolation,
- privacy policy and Play Data safety answers.
