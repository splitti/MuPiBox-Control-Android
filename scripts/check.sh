#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ -x ./gradlew ]]; then
  GRADLE=./gradlew
elif command -v gradle >/dev/null 2>&1; then
  GRADLE=gradle
else
  echo "No Gradle wrapper/system Gradle found. Run ./scripts/init-gradle-wrapper.sh first." >&2
  exit 2
fi

"$GRADLE" --no-daemon test lint assembleDebug
