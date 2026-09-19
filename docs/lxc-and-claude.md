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

## JDK

MuPiBox Control Android's baseline is **JDK 17** (AGP 9.4/Gradle 9.6 minimum/default; see
`docs/research-2026-09-19.md`). JDK 21+ is not required by this project.

`scripts/bootstrap-lxc.sh` resolves JDK 17 in this order:

1. **Distro package** (`openjdk-17-jdk-headless`) when the LXC's apt repositories carry it (e.g.
   Debian Bookworm).
2. **Eclipse Temurin 17 fallback** when they don't — currently the case on **Debian Trixie**,
   whose apt repositories only ship `openjdk-21`/`openjdk-25`. The script resolves the latest
   Temurin 17 build for the host's architecture (x86_64/amd64 or aarch64/arm64) via the official
   Adoptium assets API, downloads it, verifies it against the SHA-256 checksum Adoptium publishes
   for that exact build, and installs it to `/opt/jdk-17`. Re-running the script skips the download
   if a working JDK 17 is already there.

Never substitute `default-jdk-headless` for JDK 17 here: on Debian Trixie it resolves to JDK 21,
which silently drifts the build off the project's actual baseline.

When the Temurin fallback is used, the script writes `/etc/profile.d/android-jdk.sh` exporting:

```bash
JAVA_HOME=/opt/jdk-17
PATH="$JAVA_HOME/bin:$PATH"
```

Start a new shell (or `source` that file) afterward so `./gradlew` picks it up, or export it
manually in the current shell. Verify with:

```bash
JAVA_HOME=/opt/jdk-17 ./gradlew --version   # Launcher JVM should read 17.x
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
