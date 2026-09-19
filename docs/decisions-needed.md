# Decisions to lock before public release

These are intentionally **not** guessed by the scaffold.

## Before creating the first public Play production release

1. **Application ID** – proposed `de.mupibox.control`. Treat as permanent once published.
2. **Public name** – proposed `MuPiBox Control`.
3. **Repository visibility and license** – choose explicitly; no license is inserted automatically.
4. **Final app icon** – ideally derived from the official MuPiBox branding, but prepare a square/adaptive
   icon rather than reusing a wide splash image unchanged.
5. **Privacy-policy URL** – required before store production work is considered complete.
6. **Supported Android baseline** – scaffold uses minSdk 26; lower support should be a product decision,
   not an accidental dependency downgrade.
7. **targetSdk 37 migration timing** – when raised, implement/test Android 17 local-network permission.
8. **Admin/config credential model** – current plan is ephemeral password + in-memory session cookie;
   prefer scoped/revocable app pairing tokens if the server later provides them.
9. **Remote access** – not part of M1. Do not expose raw MuPiBox HTTP ports to the Internet.
