# LXC + Claude Code setup

The Android project can share the existing MuPiBox development LXC, but must use a separate checkout:

```text
/opt/mupibox-ng       # server project, read-only from Android work
/opt/mupibox-control  # Android project
```

## Android SDK

Run `scripts/bootstrap-lxc.sh` as root once. It installs the command-line SDK only; Android Studio
is not required on the LXC.

The script uses the current Linux Android command-line tools package noted during preparation:

```text
commandlinetools-linux-15859902_latest.zip
SHA-256: 4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583
```

It installs platform-tools, API 37 platform and build-tools 36.0.0.

## Real-device workflow

An emulator in the LXC is optional and not the main test path. Use a real Android phone for:

- mDNS/NSD,
- Wi-Fi multicast behavior,
- local-network permissions,
- real home LAN routing,
- cleartext HTTP behavior.

Typical ADB flow when USB/network debugging is available:

```bash
adb devices
./gradlew installDebug
adb shell am start -n de.mupibox.control.debug/de.mupibox.control.MainActivity
```

## Claude Code

Start Claude from the Android checkout so it loads this repo's `CLAUDE.md`:

```bash
cd /opt/mupibox-control
claude
```

Continue the last conversation associated with this working directory:

```bash
claude -c
```

`CLAUDE.md` is intentionally under ~200 lines. Detailed API/architecture information stays in
`docs/` or `.claude/rules/` so it does not consume the full session context every turn.

## Local AI MCP

The existing MuPiBox LXC already has a small MCP bridge for local Ollama/Qwen. Reuse the implementation
by **copying it into this repository** with `scripts/setup-claude.sh`; the script reads the existing
files but does not change the MuPiBox-NG checkout.

Then:

```bash
claude mcp list
```

Expected: `local-ai` connected.

Use local AI for repetitive analysis (long logs, file summaries, obvious test gaps); Claude remains the
architect/reviewer. Do not send passwords, signing keys, tokens, Wi-Fi credentials or admin cookies to
local AI.
