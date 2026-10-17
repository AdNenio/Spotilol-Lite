# Changelog

Fork-side release notes for Spotilol Lite — one entry per upstream sync: what upstream brought, what the fork cut, and notable adaptations. Upstream's own release notes live on the [upstream GitHub releases](https://github.com/lyssadev/Spotilol/releases); don't duplicate them here.

## 1.2.4 — synced 2026-10-16 (upstream base `5d20d8f`)

**Upstream features kept:** battery optimization dialog, volume dialog, playlist lookup sheet, scrollbar auto-hide, now-playing header fix, log export/redaction, playback diagnostics, 5 new Chinese locales (14 total).

**New cuts (maintainer decisions):**

- Theme presets / Material You / secondary color / theme share-import — `ThemeHarmony.kt` + `ThemeShare.kt` cut; `ThemeController` reduced to AMOLED-only; `ThemePresets.kt` reduced to a single fixed `DEFAULT_PALETTE`
- Lyrics styling/presets — `LyricsTheme.kt`, `LyricsPip.kt`, `LyricsSyncFix.kt`, `ui/lyrics/LyricsPresets.kt`
- Custom CSS — `CssInjector.kt` / `buildCustomCssJs`
- System picture-in-picture — `enterPip*` bridge + `MainActivity` PiP cluster (in-app video fullscreen kept; it reuses `__splPipFillVideo`)

**Re-cuts of upstream additions:** `splWidgetMeta` widget fallback in the new `PlaylistLookupSheet.kt`; `onPlayerLoaded`/pending-callbacks media retry (an Android Auto mechanism) in `MediaNotificationService`.

**Adaptations:** `playFromUri` switched to `jsString()` escaping; desugaring kept (java.time in `Logger`), comment updated; `ic_timer.xml` restored from upstream (the battery dialog references it — allowed exception to the pure-deletion diff).

## 1.2.3 — created 2026-10-15 (upstream base `5956b4d`)

Initial fork. Cuts: Firebase (analytics/crashlytics/perf), offline downloads (YouTube/InnerTube stack, opus→mp3 transcode, `:lame`/`:opus` native modules), Android Auto, home screen widgets, SpicyLyrics third-party lyrics API, telemetry setting. `MediaNotificationService` downgraded from `MediaBrowserServiceCompat` to plain `Service`. Update check changed to toast-only (never opens URLs).
