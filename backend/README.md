# Backend — web admin dashboard + HTTPS REST proxy

Worker: `nooralhuda-admin-api` → `https://nooralhuda-admin-api.shinzero.workers.dev`

## What it does today

- `GET /api/health` — public liveness.
- `GET /api/quran/*` — authenticated Quran Foundation proxy. Requires Firebase
  ID token (`Authorization: Bearer`). Allowlisted paths only
  (`chapters`, `chapters/:id`, `verses/by_chapter/*`, `verses/by_key/*`) with
  allowlisted params. QF credentials stay server-side (Workers secrets
  `QF_CLIENT_ID` / `QF_CLIENT_SECRET`); token cached ~3600s, retry-once on 401.
- `POST /api/ai/ask` — authenticated OpenAI-compatible chat gateway. Requires
  Firebase ID token, per-UID 20/min budget, prompt ≤ 4000 chars, 30s timeout.
  Returns 503 until an admin configures `AI_BASE_URL` / `AI_API_KEY` /
  `AI_CHAT_MODEL` secrets. No provider keys ever reach the client.

## Auth model

Every protected route verifies the Firebase ID token signature against Google
certs (`src/auth.ts`): RS256, `aud` = `nooralhuda-2026`,
`iss` = `https://securetoken.google.com/nooralhuda-2026`, expiry enforced.
The verified UID is the only identity — client-supplied UIDs are never trusted.

App Check enforcement arrives after direct-APK device validation
(`REQUIRE_APP_CHECK` in `wrangler.toml`, currently `false`).

## Local commands

```bash
cd backend
npm install
npm run typecheck
npx wrangler deploy        # deploys nooralhuda-admin-api
npx wrangler secret put QF_CLIENT_SECRET   # secrets only, never in files
```

## Firestore

`firestore.rules` is intentionally deny-all until Stage 3 designs the
per-collection authorization (Khatm invites, reporter protection, tombstones).
Deploy with `firebase deploy --only firestore:rules --project nooralhuda-2026`
only after Stage 3 review.
