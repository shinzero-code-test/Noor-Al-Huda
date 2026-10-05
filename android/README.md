# Android app

Native Kotlin + Jetpack Compose, `com.exapps.nooralhuda`, minSdk 24,
targetSdk 36, compileSdk 36. Single `app` module with strict package
boundaries (`core/*`, `feature/*`).

## Build (CI only)

All builds run in GitHub Actions (`.github/workflows/`):

- `android.yml` — `assembleDebug` + unit tests + lint on every push; emulator
  job runs Room migration tests on API 29.
- `release.yml` — manual dispatch: signed per-ABI + universal APKs, signature
  verification against the pinned SHA-256, tag `v{versionName}`, GitHub Release
  upload. See [release process](../docs/RELEASE.md).

No local builds. The Gradle project pins every version in
`gradle/libs.versions.toml` (sources recorded in comments).

## Versioning

Semver `versionName` in `app/build.gradle.kts`; `versionCode = M*10000 +
m*100 + p`, plus an ABI digit for split APKs (universal keeps base).
Bump both together; the release tag is `v{versionName}`.

## Signing

Release keystore: RSA-4096, alias `nooralhuda`, valid to 2056,
SHA-256 `BC:12:1A:ED:59:EF:78:5D:3C:63:3C:66:8D:49:17:D7:94:55:6B:54:1E:0D:4D:97:3B:8A:93:6B:25:1F:F5:22`.
Local copy at `app/noor-release.jks` (gitignored). CI restores it from the
`KEYSTORE_BASE64` secret. Missing keystore fails the release build by design —
there is no debug-key fallback.

## Structure

- `core/ui` — M3 dark theme (transcribed from Stitch), shared components
- `core/navigation` — type-safe routes, bottom bar, auth-gate sheet
- `core/data` — Room (`noor.db`, explicit migrations), DataStore, privacy
- `core/network` — authenticated backend client (OkHttp)
- `core/media|location|notifications|tts|sync` — platform integrations
- `feature/*` — one package per area: `ui/` + `domain/` (+ `data/`)

## Tests

- `src/test` — ViewModels, parsers, merge logic, privacy matrix, streaks
- `src/androidTest` — Room migration tests (every schema version), emulator CI
