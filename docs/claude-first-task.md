# Suggested first Claude Code task

Use this after the repo has been checked out and `./gradlew` exists.

```text
Read CLAUDE.md, docs/mupibox-api-current.md, docs/architecture.md and docs/roadmap.md.
Do not modify or write to MuPiBox-NG; it is read-only reference only.

Work only in MuPiBox-Control-Android.

First make the scaffold build cleanly with current AGP/Compose versions. Run:
./gradlew test
./gradlew lint
./gradlew assembleDebug

Fix compile/lint/test problems surgically without redesigning the product.
Then review the M1 implementation for these exact requirements:
1. saved manual MuPiBox by private host/IP + port 8090 default,
2. health probe on add,
3. local and Spotify play/pause/previous/next,
4. source-correct volume range,
5. POST /api/speak TTS,
6. battery and Wi-Fi compact status,
7. Bluetooth only on explicit refresh because current API scans for ~8 seconds,
8. offline box remains saved,
9. no passwords/tokens/TTS text in logs,
10. no public-Internet endpoint accepted by normal add flow.

Before making architecture changes, explain why they are necessary. Add tests for every bug found in endpoint validation or API mapping. End with a concise changed-files summary and the exact test/build results.
```
