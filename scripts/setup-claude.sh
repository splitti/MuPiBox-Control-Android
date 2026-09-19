#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SOURCE="${MUPIBOX_NG_DIR:-/opt/mupibox-ng}/.claude/tools/local-ai-mcp"
TARGET="$ROOT/.claude/tools/local-ai-mcp"

if [[ ! -f "$SOURCE/server.py" ]]; then
  echo "Existing local-ai MCP server not found at $SOURCE/server.py" >&2
  echo "Set MUPIBOX_NG_DIR if the read-only MuPiBox-NG checkout is elsewhere." >&2
  exit 1
fi

mkdir -p "$TARGET"
cp "$SOURCE/server.py" "$TARGET/server.py"
if [[ -f "$SOURCE/README.md" ]]; then
  cp "$SOURCE/README.md" "$TARGET/README.upstream.md"
fi

python3 -m venv "$TARGET/venv"
"$TARGET/venv/bin/pip" install --upgrade pip
"$TARGET/venv/bin/pip" install 'mcp<2'

cat <<MSG
local-ai MCP prepared without changing $SOURCE
Next:
  cd "$ROOT"
  claude mcp list
  claude
MSG
