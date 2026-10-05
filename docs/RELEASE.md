# Release process

## Versioning (standard)

- `versionName`: semver in `android/app/build.gradle.kts` (`1.0.0` → `1.1.0` …).
- `versionCode`: `M*10000 + m*100 + p` (e.g. `1.2.3` → `10203`).
- Split APKs add an ABI digit (`armeabi-v7a=1`, `arm64-v8a=2`, `x86_64=3`);
  the universal APK keeps the base code.
- Always bump both together in one commit. The Git tag is `v{versionName}`.

## Signing

- Key: RSA-4096, alias `nooralhuda`, valid to 2056.
- Fingerprint (SHA-256):
  `39:5A:1B:83:06:06:5B:C2:7D:3B:FE:4C:E8:91:7F:68:B3:A5:6C:C0:B3:A0:83:D9:CD:84:F2:85:05:67:79:46`
- Local copy: `android/app/noor-release.jks` (gitignored — copy it to
  encrypted offline storage; the dev machine is not a backup).
- CI secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
  `KEY_PASSWORD`. A release build without them fails closed.

## Publishing a release

1. Bump `versionName`/`versionCode`, update `CHANGELOG.md`, push to `main`
   (CI must be green).
2. Run the `release` workflow (manual dispatch).
3. The workflow: assembles per-ABI + universal release APKs, verifies every
   APK signature against the pinned fingerprint, creates tag `v{versionName}`
   (or reuses it), and publishes/updates the GitHub Release with the APKs.
4. Installs signed with this key update cleanly; installs signed with any
   other key (including the legacy app) must uninstall first.

## Rotation

Losing this key ends compatible updates for all installs. If compromise is
suspected: generate a new keystore, update the four secrets, record the new
fingerprint here, and ship — old installs will need a clean reinstall.
