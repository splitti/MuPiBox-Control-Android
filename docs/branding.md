# App icon / branding

## Where the source lives

The **canonical** brand source now lives one level up, shared across the whole suite (Android,
iOS, and store materials): [`../../branding/`](../../branding/README.md). Read that README first —
it documents the full asset set, color usage, and dark/light guidance. This file only covers the
Android-specific implementation details.

- Android adaptive icon layers (implementation of the canonical mark, not a separate design):
  - `app/src/main/res/drawable/ic_launcher_background.xml`
  - `app/src/main/res/drawable/ic_launcher_foreground.xml`
  - `app/src/main/res/drawable/ic_launcher_monochrome.xml` (Android 13+ themed-icon silhouette)
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` / `ic_launcher_round.xml` (adaptive-icon
    wiring; both reference the same background/foreground/monochrome drawables)

These were checked against `../../branding/mupibox-control-mark.svg` and already match its
geometry and colors exactly — no changes were needed when the shared branding area was set up.

## Design

A bold capital "M" whose two legs double as note stems, each rooted in a small circular
note-head — the M silhouette is the primary read (especially at launcher size), the note
reference is secondary. Colors are not invented: they're pulled directly from MuPiBox-NG's own
dark theme (`webui/static/style.css` custom properties for background, text, and accent), so the
app icon stays visually tied to the product it controls rather than introducing a new palette.

- Background: `#0B0D11` (MuPiBox-NG dark theme background)
- "M" stroke: `#F7F7FA` (MuPiBox-NG dark theme text color) — strong contrast against the background
- Note-heads: `#F59ACA` (MuPiBox-NG dark theme accent color)

## How it's exported / generated

There is no build step: the vector drawables are the shipped asset directly (no PNG/raster
mipmaps are needed — `minSdk` is 26, which is also the first API level that supports adaptive
icons, so `mipmap-anydpi-v26` matches every supported device and there is no lower API to provide
a legacy raster fallback for).

If the icon's geometry or colors ever change, change `../../branding/mupibox-control-mark.svg`
first (it's canonical), then propagate the same coordinates/colors to these Android drawables and
to the other files in `../../branding/`.

To preview changes locally: `rsvg-convert -w 512 -h 512 ../../branding/mupibox-control-mark.svg -o preview.png`
(`librsvg2-bin`), or open the SVG in any browser/image viewer.

## Follow-up needed for a later polished branding pass

- This is a first proper icon, not a final brand asset — get sign-off from whoever owns MuPiBox's
  actual brand identity before a public Play Store release.
- No Play Store listing assets yet (512x512 PNG icon, feature graphic, screenshots) — see
  `docs/play-store.md`/`docs/decisions-needed.md` for the broader release checklist.
- iOS app-icon integration (the full `AppIcon.appiconset` with all required sizes) is intentionally
  not done yet; `../../branding/mupibox-control-mark.svg` is meant to make that a quick follow-up
  once iOS work resumes with real Xcode verification available.
- The monochrome (themed-icon) variant hasn't been visually checked against real Android 13+
  dynamic-color wallpapers/launchers — worth a quick look on a real device before relying on it.
