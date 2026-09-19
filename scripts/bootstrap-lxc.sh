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

export DEBIAN_FRONTEND=noninteractive
apt-get update
apt-get install -y --no-install-recommends \
  ca-certificates curl git unzip zip python3 python3-venv openjdk-17-jdk-headless

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
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

yes | sdkmanager --licenses >/dev/null || true
sdkmanager \
  "platform-tools" \
  "platforms;android-37" \
  "build-tools;36.0.0"

cat > /etc/profile.d/android-sdk.sh <<PROFILE
export ANDROID_HOME="$ANDROID_HOME"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export PATH="\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH"
PROFILE

cat > "$ROOT/local.properties" <<PROPS
sdk.dir=$ANDROID_HOME
PROPS

chown --reference="$ROOT" "$ROOT/local.properties" 2>/dev/null || true

echo "Android SDK ready at $ANDROID_HOME"
echo "Run: $ROOT/scripts/init-gradle-wrapper.sh"
