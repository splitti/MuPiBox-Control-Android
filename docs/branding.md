# App icon / branding

## Where the source lives

- Android adaptive icon layers (source of truth for the shipped icon):
  - `app/src/main/res/drawable/ic_launcher_background.xml`
  - `app/src/main/res/drawable/ic_launcher_foreground.xml`
  - `app/src/main/res/drawable/ic_launcher_monochrome.xml` (Android 13+ themed-icon silhouette)
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` / `ic_launcher_round.xml` (adaptive-icon
    wiring; both reference the same background/foreground/monochrome drawables)
- Platform-neutral flat source, kept in sync by hand with the drawables above, for reuse outside
  Android (e.g. a future iOS AppIcon export): `docs/branding/mupibox-control-icon.svg`

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

The SVG in `docs/branding/mupibox-control-icon.svg` uses the exact same 108x108 coordinate system
and colors as the Android drawables (flattened into one layer, since it's meant for contexts that
expect a single square image rather than a background/foreground split). If the Android geometry
changes, update this file's `path`/`circle` coordinates to match by hand.

To preview changes locally: `rsvg-convert -w 512 -h 512 docs/branding/mupibox-control-icon.svg -o preview.png`
(`librsvg2-bin`), or open the SVG in any browser/image viewer.

## Follow-up needed for a later polished branding pass

- This is a first proper icon, not a final brand asset — get sign-off from whoever owns MuPiBox's
  actual brand identity before a public Play Store release.
- No Play Store listing assets yet (512x512 PNG icon, feature graphic, screenshots) — see
  `docs/play-store.md`/`docs/decisions-needed.md` for the broader release checklist.
- iOS app-icon integration (the full `AppIcon.appiconset` with all required sizes) is intentionally
  not done yet; `docs/branding/mupibox-control-icon.svg` is meant to make that a quick follow-up
  once iOS work resumes with real Xcode verification available.
- The monochrome (themed-icon) variant hasn't been visually checked against real Android 13+
  dynamic-color wallpapers/launchers — worth a quick look on a real device before relying on it.
