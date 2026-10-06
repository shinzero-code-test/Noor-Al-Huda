# Admin runbook — nooralhuda-2026 / nooralhuda-admin-api

No dashboard UI exists yet (v2.0 wave). Until then, everything below is done
with CLIs. Nothing here is committed to git; secrets live in Workers secrets
and GitHub Actions Secrets only.

## Provider registry (AI)

The gateway forwards to any OpenAI-compatible `/chat/completions` endpoint.
Configure with Worker secrets (run from `backend/`):

```bash
wrangler secret put AI_BASE_URL   # e.g. https://openrouter.ai/api/v1 (no trailing slash)
wrangler secret put AI_API_KEY    # provider key
wrangler secret put AI_CHAT_MODEL # e.g. provider/model-name
```

Tuning knobs in `wrangler.toml` `[vars]` (redeploy after change):

- `AI_DAILY_LIMIT` (default `"50"`): max proxied AI calls per user per UTC day.
- `REQUIRE_APP_CHECK`: keep `"false"` until direct-APK device validation.

Verify end-to-end with a throwaway Auth user (create → call → delete):

```bash
IDTOK=$(curl -s -X POST \
  "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=<WEB_API_KEY>" \
  -H 'Content-Type: application/json' \
  -d '{"email":"tmp@example.com","password":"<random>","returnSecureToken":true}' \
  | python3 -c "import json,sys; print(json.load(sys.stdin)['idToken'])")
curl -X POST -H "Authorization: Bearer $IDTOK" -H 'Content-Type: application/json' \
  -d '{"prompt":"..."}' https://nooralhuda-admin-api.shinzero.workers.dev/api/ai/ask
curl -s -X POST \
  "https://identitytoolkit.googleapis.com/v1/accounts:delete?key=<WEB_API_KEY>" \
  -H 'Content-Type: application/json' -d "{\"idToken\":\"$IDTOK\"}"
```

Expected: `429` past budget, `503` with `AI provider not configured by admin
yet` when secrets are absent, `200` with `{"answer": ...}` when configured.

## Quran Foundation rotation

The QF secret passed through chat once: treat rotation as routine.

1. Developer Console → regenerate client secret.
2. `wrangler secret put QF_CLIENT_SECRET` (and `QF_CLIENT_ID` if it changed).
3. Smoke-test: authenticated `GET /api/quran/chapters` → 200 with 114 chapters.

## Firestore rules

`backend/firestore.rules` is the source of truth. Deploy:

```bash
firebase deploy --only firestore:rules --project nooralhuda-2026
```

Spot-checks after deploy (must be `403`/`PERMISSION_DENIED`):

- Unauthenticated read of any `users/*` document.
- Authenticated read of another user's `users/{other}/bookmarks/*`.
- Authenticated read of `content_flags/*` or `halal_reports/*` (console-only).
- Khatm v2 (19/19 green 2026-10-06, two throwaway users + REST): outsider
  read of closed group denied; codeless/bad-code join denied; cross-member
  write, uid tamper, and non-creator remove denied; invite listing denied;
  self-join (code/open), own progress write, creator remove, and member
  reads allowed. Rerun battery: sign up A+B, exercise the matrix above,
  delete docs + users. NOTE: send timestamps WITH fractional seconds
  (`...T12:00:00.000000000Z`) — without fractions Firestore parses the
  value as a string and `is timestamp` checks fail closed.

Reports are moderated in the Firebase console. Clients can file, never read.

## Logs and budgets

- `wrangler tail` for live request logs (each AI call logs UID + request ID).
- Per-day spend per user: `users/{uid}/ai_usage/{YYYY-MM-DD}` in Firestore.
- Daily cap: `AI_DAILY_LIMIT`. Burst brake: 20/min in-memory per isolate.
