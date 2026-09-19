# Play Store preparation

## SDK choice as of 2026-09-19

Google Play requires new phone apps and updates submitted from **31 August 2026** to target
Android 16 / API 36 or higher. M1 therefore uses:

- `compileSdk = 37` (required by current Compose 1.12.x libraries),
- `targetSdk = 36`,
- `minSdk = 26`.

This is deliberate. Android 17 introduces `ACCESS_LOCAL_NETWORK` for apps targeting 37+.
Do not raise targetSdk to 37 as a casual dependency update: make it an explicit migration task,
add the runtime permission flow and test permission denial/revocation first.

## Application identity

Proposed public name:

```text
MuPiBox Control
```

Proposed package/application ID:

```text
de.mupibox.control
```

The application ID becomes effectively permanent after Play publication. Confirm it before the
first production listing.

## First-release privacy posture

Aim for a simple Data Safety story:

- no ads,
- no analytics/telemetry by default,
- no account required by the Android app,
- box addresses stored only on-device,
- TTS text sent only to the selected LAN box,
- no Wi-Fi/admin passwords stored or logged,
- no background device scanning.

If analytics, crash reporting, cloud relay or remote access is added later, reassess Data Safety and
the privacy policy before shipping the change.

## Signing

Never commit:

- `.jks` / `.keystore`,
- `keystore.properties`,
- Play service account credentials.

Use Play App Signing. Keep the upload key outside Git and back it up securely. CI secrets belong in
GitHub Actions secrets/environments, not files in the repository.

## Release artifacts

Local debug:

```bash
./gradlew assembleDebug
```

Play upload:

```bash
./gradlew bundleRelease
```

Ship AAB, not a hand-distributed production APK.

## Before first Play upload

- final icon/adaptive icon + feature graphic,
- phone screenshots,
- short/full German and English store descriptions,
- privacy-policy URL,
- Data Safety form,
- content rating,
- internal test track on real devices,
- verify LAN access on Android 14, 16 and 17 devices/emulators,
- test Wi-Fi changes, offline box, denied permissions, IPv4 and `.local` naming,
- verify release build with R8 enabled.
