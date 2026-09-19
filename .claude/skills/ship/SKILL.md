---
name: ship
description: Validate MuPiBox Control before committing or handing over a change.
---

# Ship MuPiBox Control change

1. Read `git diff --check` and fix whitespace errors.
2. Run `./scripts/check.sh`.
3. Inspect the diff for accidental secrets, credentials, signing material, or changes outside this repository.
4. Verify that no assumption contradicts `docs/mupibox-api-current.md`.
5. Summarize changed files, test results, remaining hardware/manual test needs, and any API assumptions.
6. Do not modify MuPiBox-NG.
