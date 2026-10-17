<div align="center">
  <img src="art/bgwelcome.png" alt="Spotilol" style="width: 100%; max-width: 900px; margin-bottom: 20px; box-shadow: 0 8px 32px rgba(0,0,0,0.5);">
</div>

<h1 align="center">Spotilol Lite</h1>

<p align="center">
  A lean fork of <a href="https://github.com/lyssadev/Spotilol">Spotilol</a> — the Spotify web player in a webview, with the bloat cut out.
</p>

---

## What's different from upstream

This fork strips out everything that isn't core playback:

- no Firebase (no analytics, no Crashlytics, no Performance) — nothing to configure, it just builds
- no offline downloads (no YouTube/Innertube pipeline, no mp3/m4a transcoders)
- no Android Auto
- no home screen widgets
- no third-party lyrics API (Spotify's own in-player lyrics are untouched)
- no telemetry setting
- no theme presets, Material You or theme sharing (one fixed dark palette, optional AMOLED blacks)
- no lyrics styling/presets, no custom CSS
- no system picture-in-picture
- the update check only notifies you when a new **upstream** version exists — it never opens GitHub by itself

Everything else stays like upstream: ad/telemetry blocking, media notification with lock screen and Bluetooth controls, profiles, sleep timer, AMOLED theme, in-player lyrics, canvas/video podcasts, wake lock, and the rest.

---

## Features

- blocks audio ads, trackers and telemetry
- media notification with play, pause, skip, seek, like, shuffle and repeat
- lock screen and Bluetooth/headset controls
- multiple account profiles and a sleep timer
- in-player lyrics
- AMOLED dark mode
- canvas and video podcasts
- mobile layout tweaks and playlist sorting
- wake lock and power save mode
- update notification for new upstream releases

---

## Requirements

- Android 9.0+ (API 28)
- a Spotify account
- Google Chrome / webview (comes with your phone)

---

## Build It Yourself

```bash
git clone https://github.com/AdNenio/Spotilol-Lite
cd Spotilol-Lite
./gradlew assembleDebug
```

The APK lands at `app/build/outputs/apk/debug/app-debug.apk`.

No Firebase project, no `google-services.json`, no NDK — the plain Android SDK (platform 37 + build tools) is enough.

### Release builds

Signing is read from environment variables:

```bash
export KEYSTORE_FILE="/path/to/upload-keystore.jks"
export KEYSTORE_PASSWORD="..."
export KEY_ALIAS="..."
export KEY_PASSWORD="..."
./gradlew assembleRelease
```

Or, without env vars, from `keystore/keystore.properties` (gitignored, upstream-style) with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`.

---

## Keeping up with upstream

A new version is "upstream's latest release + the debloat". The full workflow — analyze the upstream diff first, rebase the fork's cut commits on top, then sweep for re-added code — lives in [AGENTS.md](AGENTS.md). The short version:

```bash
git fetch origin   # origin tracks the upstream repo
git rebase --onto origin/main <previous base> lite
```

The fork is upstream minus deletions, so `git diff origin/main...lite` is always exactly the debloat — use it to review what the fork changes.

After a sync:

1. resolve conflicts in favor of the debloat (if upstream re-adds cut code, cut it again)
2. `./gradlew assembleDebug` must pass
3. sweep for anything upstream re-adds into the cut areas (offline downloads, Android Auto, widgets, SpicyLyrics, Firebase, theme presets, lyrics styling, custom CSS, PiP) — the full checklist and the tricky spots are in [AGENTS.md](AGENTS.md)
4. keep `versionCode`/`versionName` equal to the upstream base version (the in-app update check compares against upstream releases and breaks on suffixes)
5. test on a device, push, and tag `lite-X.Y.Z`

---

## Credits

**deviato** reverse-engineered the original Spotifuck. **lyssadev** ported the core logic from smali to Kotlin and maintains Spotilol. This repo is a personal fork for my own use case.

All rights reserved. lyssadev and deviato.
