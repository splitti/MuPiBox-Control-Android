# Play Store Data safety – working draft

This is an engineering checklist, not a final legal declaration. Re-evaluate against the exact release binary in Play Console.

## Current M1 code

Expected current state:

- no ads,
- no analytics,
- no crash-reporting SDK,
- no developer cloud backend,
- no user accounts,
- saved MuPiBox name/host/port remain in Android DataStore on the device,
- TTS text is sent directly to the selected local MuPiBox,
- playback/status traffic stays between the phone and selected MuPiBox,
- no Wi-Fi password/admin password persistence in the MVP.

Because Play's definitions distinguish collection from on-device/local processing and can change, do not copy these notes blindly into Play Console. Verify the current Play Data safety definitions immediately before submission.

## Re-open this document when adding

- crash reporting,
- telemetry/analytics,
- remote/cloud access,
- authentication or pairing tokens,
- Wi-Fi provisioning,
- direct Spotify/provider authentication,
- push notifications,
- account sync,
- backups of app data.
