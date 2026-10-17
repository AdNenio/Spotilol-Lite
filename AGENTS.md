# AGENTS.md

Spotilol Lite: lean fork of [lyssadev/Spotilol](https://github.com/lyssadev/Spotilol) — an Android app (Kotlin + Jetpack Compose) that wraps the Spotify Web Player in a WebView, injecting JS to block ads/trackers and add features. No backend, no server. The fork's diff vs upstream is **pure deletions** (debloat) — keep it that way.

**Keep this file current.** If work makes any statement here obsolete (upstream renames/moves things, build quirks change) or reveals new non-obvious information (gotchas hit during a sync, new coupling between kept features), update this file in the same change.

**Machine-local notes** may live in `AGENTS.local.md` (gitignored, per-developer machine setup): if it exists, read it — it holds this machine's push/deploy-key setup and local build workarounds (e.g. the test keystore for R8 checks).

## New version from a new upstream release

Remotes: `origin` = upstream (lyssadev/Spotilol, **read-only — never push to it**), `lite` = this fork.

The fork is a set of **per-feature cut commits** on top of an upstream release tree. Each sync rebases them onto the new upstream tip, folds in any new cuts, and re-establishes the invariant: **fork diff vs upstream = pure deletions + the update-check tweak + docs**. History is not precious — the tree is.

Upstream history is unreliable (giant `Bump to v1.2.x` squash commits), so this workflow is **tree-based**: it consumes only the release-state trees (`<old-base>` → new tip), never individual upstream commits.

1. **Prep.** `git fetch origin`; confirm the new release: `git log origin/main --oneline -5` (look for `Bump to v1.2.x`). Tag the current state `lite-1.2.N` and push the tag (archaeology + anchor). `<old-base>` = `git merge-base lite origin/main` — the upstream bump commit the current `lite` is based on.
2. **Analyze A→B (read-only, before touching the branch).** Inputs: the upstream GitHub release notes (maintainer pastes them — they are the primary feature input, since upstream's commit log is useless for attribution) + `git diff <old-base> origin/main` (code is authoritative; notes are a guide). Scope to `app/src/main`, build files and the manifest; ignore `.devin/wiki.json` churn, but note locale count changes (the sweep needs it). Produce three lists:
   - **New features** → **stop and get the maintainer's keep/cut decisions before rebasing** — the only human gate in the workflow.
   - **Features upstream deleted** → cross-check "Cut in this fork": upstream cut something we also cut → expect that rebase commit to be obsolete; upstream cut something we kept → code referencing it needs attention.
   - **Features upstream changed** → expected conflict zones; check for new coupling to cut features.
3. **Rebase (informed).** On `lite`: `git rebase --onto origin/main <old-base> lite`.
   - Conflict → adapt: take **upstream's wording** for kept lines, preserve the cut. If new upstream code inside a hunk references cut features, cut it there — the removal stays in that feature's commit (no separate "re-cut" commits).
   - `app/build.gradle.kts` → upstream's `versionCode`/`versionName` + our env-var signing block. No version suffixes — `util/UpdateChecker` compares against upstream releases and breaks on suffixes like `-lite`.
   - A cut commit upstream itself obsoleted (they cut the feature too) → `--skip` / reduce to a no-op.
   - Hot conflict files: `app/build.gradle.kts`, `settings.gradle.kts`, `AndroidManifest.xml`, `SpotifyWebViewClient.kt`, `SpotifyBridge.kt`, `MainActivity.kt`, `SettingsScreen.kt`, `SettingsDialog.kt`, `MediaNotificationService.kt`, `SpotilolApp.kt`, `SplashActivity.kt`, `SleepTimerManager.kt`, `strings.xml` (all `values*` locales — 14 as of 1.2.4, incl. 5 Chinese).
4. **Verify the delta + apply new cuts.** Walk the changed/new upstream files (`git diff --name-only <old-base> origin/main`): confirm every coupling point the analysis predicted is cut (audits the plan; it still catches misses). New coupling → removals as tmp commits. New feature cuts per the maintainer's decisions → new `lite:` commits (they join the per-feature set).
5. **Fold in.** `git rebase -i origin/main` — move each tmp commit under its matching feature commit and mark it `fixup`; also mark `edit` each feature commit that received new content, and when the rebase stops, amend its message with the `History:` line (see Conventions → commit messages). Invariant: **one commit per cut feature, each always "the complete removal of X against the current upstream"**, with a message that explains what it cuts and what each sync changed — per-cycle re-cut commits must not accumulate.
6. **Standing sweep — do all of it:**
   - `git diff --name-status origin/main lite` (run only against the tip you're based on) — must be only deletions + the update-check tweak + docs (`AGENTS.md`, `README.md`, `CHANGELOG.md`); line-level: every added line in a kept file must be a justified adaptation (upstream wording, AMOLED-only theme, fixed palette, signing block).
   - No files in cut packages: `offline/`, `innertube/`, `yt/`, `searchEngine/`, `widget/`, `spicy/`; no `:lame`/`:opus` modules; no Firebase.
   - `app/build.gradle.kts`: no firebase plugins/bom, no ktor/newpipe/brotli/okhttp/serialization/bouncycastle/glance deps, no `project(":lame")`/`project(":opus")`.
   - `AndroidManifest.xml`: no `OfflineActivity`/`OfflineMediaService`/`DownloadService`, no widget receivers, no automotive meta-data, no `MediaBrowserService` intent filter; `MediaNotificationService` stays `exported="false"`.
   - `SpotifyWebViewClient.kt` injection list: no `Download*`/`SpicyLyrics`/`AndroidAuto` entries.
   - JS cross-reference greps across `injections/` for cut function names (`splDoDownload`, `cancelDownload`, `splWidgetMeta`, `enterPip*`, …) — these don't show in Kotlin compilation.
   - Dead-string audit across all `values*` locales: referenced = `R.string.X` in Kotlin + `@string/X` in res XML; defined − referenced = dead, delete from every locale.
   - **README feature set is correct**: both the "this fork strips out" list and the "everything else stays like upstream" line must match the actual cut list (the second line is the one that rots).
   - `./gradlew assembleDebug` must pass, and `assembleRelease` with the test keystore (see `AGENTS.local.md`) for the full R8 check.
7. **Docs + finish.** Update in the same change: `AGENTS.md` (cut list, gotchas, locale count, hot files) and `CHANGELOG.md` (new per-release entry: upstream base, upstream features kept, cuts, re-cuts, adaptations). Test on a device (maintainer) → `git push --force-with-lease lite lite`. Fork consumers (build machines) reset their clone to the new tip.

### Debloat gotchas (learned the hard way)

- `MediaNotificationService` was downgraded from `MediaBrowserServiceCompat` to plain `Service`: it needs `override fun onBind(intent: Intent?): IBinder? = null` and must not reference `sessionToken` (a browser-service-only property).
- **`AndroidTracker.kt` is NOT Android Auto** — it's the DOM observer feeding notification metadata via `updMedia()`; keep it. The `playFromUri` JS lives in `PlaybackControls.kt`, not the deleted `AndroidAuto.kt`.
- **`ic_timer.xml` was restored from upstream** (the fork had deleted it, upstream's new battery-optimization dialog references it) — an allowed exception to the pure-deletion diff; keep it if a future merge drops it again.
- **`__splPipFillVideo`/`__splPipFillLyrics` JS in `SpotilolPlayer.kt` is kept despite the "Pip" name** — the in-app video fullscreen (`VideoFullscreen.kt`) reuses it to stretch the player into the window.
- System PiP is fully cut; `MainActivity.onShowCustomView` now just calls `callback?.onCustomViewHidden()` (video native-fullscreen requests are ignored).
- Widgets and offline were **bidirectionally coupled** (`OfflineMediaService` pushed widget state; widgets launched `OfflineActivity`/`OfflineMediaService`) — cut them in the same commit, no intermediate buildable state.
- JS cross-references don't show up in Kotlin compilation: the custom player (`SpotilolPlayer.kt`) referenced `splDoDownload()`/`AndBridge.cancelDownload()` defined in deleted files; `MediaUpdater.kt` carried `splWidgetMeta` for widgets. When upstream adds similar coupling, grep for JS function names across `injections/`.
- `MainActivity.onPlayerReady` and the `onNewIntent` path were AA-search call sites outside the obvious AA code — check "obviously unrelated" callbacks when cutting a feature.
- `SettingsScreen` is ~2000 lines: feature tiles sit between `HorizontalDivider`s (remove tile + exactly one divider), and settings params are forwarded through `MainActivity → SettingsDialog → SettingsScreen` — cut a param at every level of the chain.
- Every cut feature has strings in **every locale** (`values*`). Audit with a script: referenced = `R.string.X` in Kotlin + `@string/X` in res XML; defined − referenced = dead, delete from every `values*/strings.xml`.
- R8: keep `-dontwarn javax.annotation.concurrent.GuardedBy` in `app/proguard-rules.pro` (referenced by `androidx.security`).
- README `## Credits` is final (personal-fork wording) — never rewrite it during version updates.

## Cut in this fork (do not resurrect)

Firebase (analytics/crashlytics/perf), offline downloads (YouTube/InnerTube, opus→mp3 transcode, `:lame`/`:opus` native modules), Android Auto, home screen widgets, SpicyLyrics third-party lyrics API, telemetry setting, theme presets/Material You/secondary-color/theme share-import (`ui/theme/ThemeHarmony.kt`+`ThemeShare.kt` cut; `ThemeController` reduced to AMOLED-only, `ThemePresets.kt` reduced to a single fixed `DEFAULT_PALETTE`), lyrics styling/presets (`LyricsTheme`/`LyricsStyle`, `LyricsPip`, `LyricsSyncFix`, `ui/lyrics/LyricsPresets.kt`), custom CSS (`CssInjector`/`buildCustomCssJs`), system picture-in-picture (`enterPip*` bridge + `MainActivity` PiP cluster). The update check only toasts "a new upstream version is available" — it must not open URLs.

## Build & verify

- Build: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`. No lint/typecheck/test tasks — there are **no tests at all** and no CI. Verification is compilation plus running on a device.
- No `google-services.json` needed (no Firebase plugin), no NDK (no native code). Plain Android SDK: platform 37 + build-tools. `local.properties` (gitignored) carries `sdk.dir`.
- Release builds run R8 minify and sign from env vars `KEYSTORE_FILE`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` (fallback: `keystore/keystore.properties`, gitignored). For a local full-R8 check without a real keystore, use the test keystore command in `AGENTS.local.md`. If release builds break on missing classes, fix keep rules in `app/proguard-rules.pro`.
- Toolchain: Gradle 9.7.1 wrapper, AGP 9.4.1, Kotlin 2.4.0, compileSdk 37 / target 36 / min 28, Java 17 source (daemon toolchain Java 21, auto-provisioned via foojay).

## Architecture (non-obvious)

- `app/src/main/java/com/project/lol/webview/injections/` — each Kotlin `object` holds raw JavaScript in `const val CONTENT` (~40 files). They are concatenated in `webview/SpotifyWebViewClient.kt`:
  - `onPageStarted`: per-page spoofing/blocking JS (BrowserSpoof/GoogleSpoof, FetchOverride, GaBlocker, ...).
  - `injectPlayerControl` (gated on login): the player engine. **`append()` order matters — `PlayerCore` must come first** (defines globals the rest use). New injections must be added to the right list; just creating a file does nothing.
  - The bundle is passed through `JsUtils.stripConsoleLogs`, so injected JS should not rely on `console.log`.
  - The JS targets the Spotify Web Player DOM via `data-testid` selectors — the usual breakage mode is upstream Spotify markup changes.
- `webview/WebViewHost.kt` (singleton) owns the WebView; `bridge/SpotifyBridge.kt` is the `window.AndBridge` `@JavascriptInterface` surface.
- `service/MediaNotificationService` is a plain `Service` (MediaSessionCompat: notification, lock screen, Bluetooth) — it used to be a MediaBrowserServiceCompat for Android Auto, which was cut. `assets/silent.mp3` is the ad-muting stand-in used by `AdStateHook`/request interception — keep it.
- UI prefs live in `SharedPreferences("spotilol_prefs")`, read directly in many places (e.g. `injectPlayerControl`).
- Local crash handling (`util/CrashHandler`, `CrashActivity`, `CrashStore`) is Firebase-free and stays. `AndroidTracker.kt` injection is **not** Android Auto — it's the DOM observer feeding notification metadata via `updMedia()`; keep it.
- `.devin/wiki.json` is a stale upstream architecture wiki — useful background only; the code is authoritative.

## Modules

- `:app` — all app code (namespace `com.project.lol`). There are no other modules.

## Conventions

- Version: the fork never makes a bump commit — `versionCode`/`versionName` come from rebasing onto the upstream tip (resolve the `app/build.gradle.kts` conflict to upstream's numbers + our signing block). No suffixes.
- Commit style: short lowercase subjects, conventional prefixes. Fork-specific changes use the `lite:` prefix (`lite: remove android auto`). One commit per cut feature; tmp re-cut commits are fixup'd into their feature commit during the sync (step 5), never pushed as-is.
- **Commit messages are self-documenting** — `git log lite` alone should explain every cut:
  - *New cut feature*: subject `lite: remove <feature>`; body lists exactly what it removes (files/classes, bridge methods, JS globals, settings keys, string keys) and the release where the cut was decided.
  - *Feature commit updated by a sync*: after the step-5 fixup, amend its message with a line under a `History:` section: `1.2.N: <what changed and why>` (e.g. `1.2.5: re-cut splFoo — upstream moved it from MediaUpdater.kt to Bar.kt`). The body accumulates: current removal list + one line per sync that had to update it.
- Release notes: every sync appends a `CHANGELOG.md` entry (fork-side changes only — what was kept/cut/re-cut; upstream's own notes live on the upstream GitHub releases, don't duplicate them) and refreshes the README feature list in the same change.
- `kotlin.code.style=official`; no formatter/linter is configured, so match surrounding code style manually.
- README style: the current file is all-lowercase (upstream's style). Future version updates should write README text with **normal capitalization** instead.
