# Noor Al-Huda (new)

Native Android rebuild (Kotlin + Jetpack Compose) plus web admin dashboard and
HTTPS REST backend. Clean break from the legacy Expo project: new Firebase
project, new package ID, no migrated accounts or data.

- `android/` — native app, `com.exapps.nooralhuda` (Stage A1 starts here).
- `backend/` — Cloudflare Workers service `nooralhuda-admin-api`
  (Quran Foundation proxy + AI gateway + future admin dashboard).
- Firebase project: `nooralhuda-2026` (`europe-west1`).
- APK distribution: GitHub Releases (no Play).

Plans live in the legacy checkout's gitignored `plans/` (historical); new
planning docs live here as the build proceeds. Secrets are never committed:
Workers secrets + GitHub Actions Secrets + gitignored local folder only.
