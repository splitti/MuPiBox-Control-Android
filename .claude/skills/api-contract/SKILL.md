---
name: api-contract
summary: Read the frozen MuPiBox-NG API contract before implementing or changing Android networking.
---

# MuPiBox API contract workflow

1. Read `docs/mupibox-api-current.md`.
2. Implement against that snapshot; tolerate unknown optional JSON fields.
3. Do not inspect or change MuPiBox-NG unless the requested Android feature cannot be resolved from the snapshot.
4. If read-only server verification becomes necessary, report the exact API assumption being checked.
5. Never make server-side commits, pushes, PRs, issues or file edits from this project task.
6. When a verified server contract changes, update the snapshot in this Android repo and add/adjust a client test fixture.
