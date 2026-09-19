---
paths:
  - "app/**/*.kt"
  - "app/**/*.kts"
  - "app/src/main/AndroidManifest.xml"
---

# Android implementation rules

- Compose only for new screens; no XML layout/view hierarchy unless a platform integration requires it.
- Screen ViewModel exposes one immutable StateFlow UiState where practical.
- Use `collectAsStateWithLifecycle` in Compose.
- Never perform blocking HTTP, DNS, DataStore or Bluetooth work on the main thread.
- Keep Context in Android data sources, not ViewModels.
- Normal player polling ~1 s is acceptable for the current API; system status slower (~5 s).
- Never poll `/api/connectivity/bluetooth`; it triggers an expensive real scan.
- Handle nullable/unknown battery, Wi-Fi and optional provider fields explicitly.
- TTS requests can run close to 20 s; show progress and allow UI to remain usable.
- Keep permissions minimal. `targetSdk=36`: no ACCESS_LOCAL_NETWORK yet.
- Do not broaden Network Security Config beyond what is documented without a security review.
- Tests required for endpoint validation, parsing assumptions and nontrivial ViewModel/repository logic.
