import { Hono } from 'hono';
import { cors } from 'hono/cors';

import { verifyFirebaseIdToken, type VerifiedUser } from './auth';
import { qfBase, qfGet } from './qf';
import {
  checkAiBudget,
  checkDailyBudget,
  forwardAiChat,
  incrementTodayUsage,
} from './ai';

export interface Env {
  FIREBASE_PROJECT_ID: string;
  QF_CLIENT_ID: string;
  QF_CLIENT_SECRET: string;
  QF_ENV?: string;
  REQUIRE_APP_CHECK?: string;
  AI_BASE_URL?: string;
  AI_API_KEY?: string;
  AI_CHAT_MODEL?: string;
  AI_DAILY_LIMIT?: string;
}

const app = new Hono<{ Bindings: Env }>();
app.use('/api/*', cors());

app.get('/api/health', (c) =>
  c.json({ ok: true, service: 'nooralhuda-admin-api' })
);

async function requireUser(c: {
  req: { header(name: string): string | undefined };
  env: Env;
}): Promise<{ user: VerifiedUser; idToken: string }> {
  const header = c.req.header('authorization');
  if (!header?.startsWith('Bearer ')) {
    throw Object.assign(new Error('Missing Bearer token'), { status: 401 });
  }
  const idToken = header.slice(7);
  try {
    const user = await verifyFirebaseIdToken(idToken, c.env.FIREBASE_PROJECT_ID);
    return { user, idToken };
  } catch {
    throw Object.assign(new Error('Invalid Firebase token'), { status: 401 });
  }
}

function qfCreds(env: Env): { id: string; secret: string } {
  if (!env.QF_CLIENT_ID || !env.QF_CLIENT_SECRET) {
    throw Object.assign(new Error('Quran provider not configured'), { status: 503 });
  }
  return { id: env.QF_CLIENT_ID, secret: env.QF_CLIENT_SECRET };
}

// Allowlisted Quran Foundation paths. Nothing else is proxyable.
const QURAN_ROUTES: Array<{ prefix: string; params: string[] }> = [
  { prefix: '/content/api/v4/chapters', params: [] },
  { prefix: '/content/api/v4/verses/by_chapter/', params: ['translations', 'words', 'per_page', 'page', 'fields'] },
  { prefix: '/content/api/v4/verses/by_key/', params: ['translations', 'words', 'fields'] },
  // Read-only metadata: translation resource IDs, reciters, tafsirs.
  { prefix: '/content/api/v4/resources/', params: ['language', 'per_page', 'page'] },
];

function matchQuranRoute(path: string): string[] | null {
  if (path === '/content/api/v4/chapters') return [];
  const chapter = path.match(/^\/content\/api\/v4\/chapters\/(\d+)$/);
  if (chapter) return [];
  if (path.startsWith('/content/api/v4/verses/by_chapter/')) {
    return QURAN_ROUTES[1]!.params;
  }
  if (path.startsWith('/content/api/v4/verses/by_key/')) {
    return QURAN_ROUTES[2]!.params;
  }
  if (path.startsWith('/content/api/v4/resources/')) {
    return QURAN_ROUTES[3]!.params;
  }
  return null;
}

app.get('/api/quran/*', async (c) => {
  const { user } = await requireUser(c);
  void user;
  const qfPath = c.req.path.replace(/^\/api\/quran/, '') || '/';
  const fullPath = `/content/api/v4${qfPath === '/' ? '/chapters' : qfPath}`;
  const allowed = matchQuranRoute(fullPath);
  if (!allowed) return c.json({ error: 'Not found' }, 404);

  const incoming = new URL(c.req.url).searchParams;
  const out = new URLSearchParams();
  for (const key of allowed) {
    const value = incoming.get(key);
    if (value !== null) out.set(key, value.slice(0, 200));
  }
  const query = out.toString() ? `?${out.toString()}` : '';

  const { id, secret } = qfCreds(c.env);
  const base = qfBase(c.env.QF_ENV ?? 'production');
  let upstream: Response;
  try {
    upstream = await qfGet(fullPath, query, id, secret, base);
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'QF_FAILED';
    const status = msg.startsWith('QF_TOKEN_') ? 502 : 502;
    return c.json({ error: 'Quran provider unavailable', code: msg }, status);
  }
  if (!upstream.ok) {
    const status = (upstream.status === 429 ? 429 : upstream.status >= 500 ? 502 : upstream.status) as
      | 429
      | 502
      | 400
      | 403
      | 404;
    return c.json({ error: 'Quran provider error', upstreamStatus: upstream.status }, status);
  }
  return c.json(await upstream.json());
});

app.post('/api/ai/ask', async (c) => {
  const { user, idToken } = await requireUser(c);
  let body: unknown;
  try {
    body = await c.req.json();
  } catch {
    return c.json({ error: 'Invalid JSON body' }, 400);
  }
  const input = (body ?? {}) as { prompt?: unknown; maxTokens?: unknown };
  if (typeof input.prompt !== 'string' || input.prompt.trim().length < 3) {
    return c.json({ error: 'prompt must be a string of at least 3 characters' }, 400);
  }
  if (input.prompt.length > 4000) {
    return c.json({ error: 'prompt exceeds 4000 characters' }, 413);
  }
  const budget = checkAiBudget(user.uid);
  if (!budget.allowed) {
    return c.json({ error: 'Rate limit exceeded', retryAfterSec: budget.retryAfterSec }, 429);
  }
  const dailyLimit = Number(c.env.AI_DAILY_LIMIT ?? '50');
  try {
    const daily = await checkDailyBudget(user.uid, idToken, c.env.FIREBASE_PROJECT_ID, dailyLimit);
    if (!daily.allowed) {
      return c.json({ error: 'Daily AI budget exhausted', retryAfterSec: daily.retryAfterSec }, 429);
    }
  } catch {
    return c.json({ error: 'Budget check unavailable' }, 502);
  }
  try {
    const answer = await forwardAiChat(c.env, input.prompt.trim());
    void incrementTodayUsage(user.uid, idToken, c.env.FIREBASE_PROJECT_ID);
    return c.json({ answer });
  } catch (err) {
    if (err instanceof Error && err.message === 'AI_PROVIDER_NOT_CONFIGURED') {
      return c.json({ error: 'AI provider not configured by admin yet' }, 503);
    }
    return c.json({ error: 'AI provider error' }, 502);
  }
});

app.onError((err, c) => {
  const status =
    err instanceof Error && 'status' in err && typeof (err as { status: unknown }).status === 'number'
      ? ((err as { status: number }).status as 401)
      : 500;
  const message = status === 500 ? 'Internal error' : (err as Error).message;
  return c.json({ error: message }, status);
});

export default app;
