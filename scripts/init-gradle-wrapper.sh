#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLE_VERSION="9.6.0"
DIST_SHA="bbaeb2fef8710818cf0e261201dab964c572f92b942812df0c3620d62a529a01"
CACHE="${XDG_CACHE_HOME:-$HOME/.cache}/mupibox-control"
ZIP="$CACHE/gradle-${GRADLE_VERSION}-bin.zip"
DIR="$CACHE/gradle-${GRADLE_VERSION}"

mkdir -p "$CACHE"
if [[ ! -x "$DIR/bin/gradle" ]]; then
  curl -fL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$ZIP"
  echo "$DIST_SHA  $ZIP" | sha256sum -c -
  rm -rf "$DIR"
  unzip -q "$ZIP" -d "$CACHE"
fi

cd "$ROOT"
"$DIR/bin/gradle" wrapper --gradle-version "$GRADLE_VERSION" --distribution-type bin

# Make the wrapper itself pin the distribution checksum as well.
PROPS="$ROOT/gradle/wrapper/gradle-wrapper.properties"
if ! grep -q '^distributionSha256Sum=' "$PROPS"; then
  printf '\ndistributionSha256Sum=%s\n' "$DIST_SHA" >> "$PROPS"
fi

./gradlew --version
