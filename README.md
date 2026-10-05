# Noor Al Huda | نور الهدى

Arabic-first Islamic app: native Android (Kotlin + Jetpack Compose), a web
admin dashboard, and an HTTPS REST backend. Clean rebuild — new Firebase
project, new package ID, no migrated accounts or data.

- `android/` — native app, `com.exapps.nooralhuda` ([build guide](android/README.md)).
- `backend/` — Cloudflare Workers service `nooralhuda-admin-api`
  (Quran Foundation proxy + AI gateway + future admin dashboard).
- Firebase project: `nooralhuda-2026` (`europe-west1`).
- APK distribution: GitHub Releases (no Play Store).

## Rules of this repo

- **CI builds everything.** No local builds: push, and GitHub Actions assembles,
  tests, and lints. See [release process](docs/RELEASE.md) for signed APKs.
- **String resources only** (`values/` + `values-ar/`), default locale **en-GB**.
- **Stitch-first design.** Mockups live in the Stitch project
  `Noor Al Huda Android`; Compose tokens are transcribed from approved output.
- **Secrets are never committed.** Workers secrets + GitHub Actions Secrets +
  gitignored local files only. Client identifiers in `FirebaseModule` are
  public by design (security lives in rules + App Check + token verification).

## Docs

- [Android app](android/README.md) — build, versioning, signing, structure
- [Backend runbook](backend/ADMIN.md) — provider registry, rotations, deploys
- [Release process](docs/RELEASE.md) — signing, versioning, publishing
- [Changelog](CHANGELOG.md)

## License

MIT — see [LICENSE](LICENSE).
