#!/usr/bin/env bash
set -euo pipefail

if [[ ${EUID:-$(id -u)} -ne 0 ]]; then
  echo "Run as root: sudo $0" >&2
  exit 1
fi

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
TOOLS_REV="15859902"
TOOLS_ZIP="commandlinetools-linux-${TOOLS_REV}_latest.zip"
TOOLS_URL="https://dl.google.com/android/repository/${TOOLS_ZIP}"
TOOLS_SHA256="4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583"

# JDK 17 is the project baseline (AGP 9.4/Gradle 9.6 minimum/default; see
# docs/research-2026-09-19.md). MuPiBox Control does not require JDK 21+.
JDK17_FALLBACK_HOME="/opt/jdk-17"
ADOPTIUM_ASSETS_API="https://api.adoptium.net/v3/assets/latest/17/hotspot"

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y --no-install-recommends \
  ca-certificates curl git unzip zip jq python3 python3-venv

# Resolves a working JDK 17 and sets JAVA17_HOME to its home directory.
#
# Prefers the distro package (works on e.g. Debian Bookworm). Debian Trixie's
# apt repositories only carry openjdk-21/openjdk-25, so on Trixie (and any
# other distro missing the package) this falls back to downloading Eclipse
# Temurin 17 directly from the official Adoptium API, verified against the
# SHA-256 checksum Adoptium publishes for that exact build, and installs it
# under a fixed path so re-running this script is a no-op once it succeeds.
# `default-jdk-headless` is deliberately never used as a JDK 17 substitute:
# on Debian Trixie it resolves to JDK 21, silently drifting off the project
# baseline.
resolve_jdk17() {
  if apt-cache show openjdk-17-jdk-headless >/dev/null 2>&1; then
    echo "openjdk-17-jdk-headless is available from apt; installing it."
    apt-get install -y --no-install-recommends openjdk-17-jdk-headless

    local java_bin
    java_bin="$(update-alternatives --list java 2>/dev/null | grep -m1 '/java-17-')" || true
    if [[ -z "$java_bin" ]]; then
      java_bin="$(dpkg -L openjdk-17-jdk-headless 2>/dev/null | grep -m1 '/bin/java$')" || true
    fi
    if [[ -z "$java_bin" ]]; then
      echo "Installed openjdk-17-jdk-headless but could not locate its java binary." >&2
      exit 1
    fi
    JAVA17_HOME="$(dirname "$(dirname "$java_bin")")"
    JDK17_IS_FALLBACK=0
    return
  fi

  echo "openjdk-17-jdk-headless is not available from apt on this distro (e.g. Debian Trixie)."
  install_temurin17_fallback
  JAVA17_HOME="$JDK17_FALLBACK_HOME"
  JDK17_IS_FALLBACK=1
}

# Idempotent: skips the download entirely if a JDK 17 already lives at
# JDK17_FALLBACK_HOME.
install_temurin17_fallback() {
  if [[ -x "$JDK17_FALLBACK_HOME/bin/java" ]] \
    && "$JDK17_FALLBACK_HOME/bin/java" -version 2>&1 | grep -q 'version "17\.'; then
    echo "Temurin JDK 17 already present at $JDK17_FALLBACK_HOME, skipping download."
    return
  fi

  local arch adoptium_arch
  arch="$(uname -m)"
  case "$arch" in
    x86_64 | amd64)
      adoptium_arch="x64"
      ;;
    aarch64 | arm64)
      adoptium_arch="aarch64"
      ;;
    *)
      echo "Unsupported architecture for the Temurin JDK 17 fallback: $arch" >&2
      echo "Supported: x86_64/amd64, aarch64/arm64." >&2
      exit 1
      ;;
  esac

  echo "Resolving latest Eclipse Temurin JDK 17 for linux/$adoptium_arch via the Adoptium API..."
  local metadata download_url checksum archive_name version tmp_dir
  metadata="$(curl -fsSL "${ADOPTIUM_ASSETS_API}?architecture=${adoptium_arch}&image_type=jdk&os=linux&vendor=eclipse")"

  download_url="$(echo "$metadata" | jq -r '.[0].binary.package.link // empty')"
  checksum="$(echo "$metadata" | jq -r '.[0].binary.package.checksum // empty')"
  archive_name="$(echo "$metadata" | jq -r '.[0].binary.package.name // empty')"
  version="$(echo "$metadata" | jq -r '.[0].version.semver // empty')"

  if [[ -z "$download_url" || -z "$checksum" || -z "$archive_name" ]]; then
    echo "Could not resolve a Temurin JDK 17 build from the Adoptium API response:" >&2
    echo "$metadata" >&2
    exit 1
  fi

  echo "Downloading Temurin JDK $version ($archive_name)..."
  tmp_dir="$(mktemp -d)"
  curl -fL "$download_url" -o "$tmp_dir/$archive_name"

  echo "Verifying SHA-256 checksum published by Adoptium..."
  (cd "$tmp_dir" && echo "$checksum  $archive_name" | sha256sum -c -)

  rm -rf "$JDK17_FALLBACK_HOME"
  mkdir -p "$JDK17_FALLBACK_HOME"
  tar -xzf "$tmp_dir/$archive_name" -C "$JDK17_FALLBACK_HOME" --strip-components=1
  rm -rf "$tmp_dir"

  echo "Temurin JDK $version installed at $JDK17_FALLBACK_HOME"
}

JAVA17_HOME=""
JDK17_IS_FALLBACK=0
resolve_jdk17

echo "Resolved JDK 17: $("$JAVA17_HOME/bin/java" -version 2>&1 | head -1)"
echo "JAVA17_HOME=$JAVA17_HOME"

mkdir -p "$ANDROID_HOME/cmdline-tools" /tmp/mupibox-android-sdk
cd /tmp/mupibox-android-sdk

if [[ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]]; then
  curl -fL "$TOOLS_URL" -o "$TOOLS_ZIP"
  echo "$TOOLS_SHA256  $TOOLS_ZIP" | sha256sum -c -
  rm -rf unpacked
  mkdir unpacked
  unzip -q "$TOOLS_ZIP" -d unpacked
  rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  mv unpacked/cmdline-tools "$ANDROID_HOME/cmdline-tools/latest"
fi

export ANDROID_HOME
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export JAVA_HOME="$JAVA17_HOME"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

yes | sdkmanager --licenses >/dev/null || true
sdkmanager \
  "platform-tools" \
  "platforms;android-37.0" \
  "build-tools;36.0.0"

cat > /etc/profile.d/android-sdk.sh <<PROFILE
export ANDROID_HOME="$ANDROID_HOME"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH"
PROFILE

if [[ "$JDK17_IS_FALLBACK" -eq 1 ]]; then
  cat > /etc/profile.d/android-jdk.sh <<PROFILE
# Debian Trixie has no openjdk-17-jdk-headless apt package; MuPiBox Control
# Android uses this Temurin 17 fallback installed by scripts/bootstrap-lxc.sh.
export JAVA_HOME="$JDK17_FALLBACK_HOME"
export PATH="\$JAVA_HOME/bin:\$PATH"
PROFILE
  echo "Wrote JAVA_HOME fallback to /etc/profile.d/android-jdk.sh (Temurin JDK 17 at $JDK17_FALLBACK_HOME)."
fi

cat > "$ROOT/local.properties" <<PROPS
sdk.dir=$ANDROID_HOME
PROPS

chown --reference="$ROOT" "$ROOT/local.properties" 2>/dev/null || true

echo "Android SDK ready at $ANDROID_HOME"
echo "JDK 17 ready at $JAVA17_HOME"
if [[ "$JDK17_IS_FALLBACK" -eq 1 ]]; then
  echo "Start a new shell (or 'source /etc/profile.d/android-jdk.sh') so JAVA_HOME picks up the Temurin fallback, or export it manually:"
  echo "  export JAVA_HOME=$JDK17_FALLBACK_HOME"
  echo "  export PATH=\"\$JAVA_HOME/bin:\$PATH\""
fi
echo "Run: $ROOT/scripts/init-gradle-wrapper.sh"
