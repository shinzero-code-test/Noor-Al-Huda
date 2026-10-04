// OpenAI-compatible AI gateway.
// Provider credentials come from Workers secrets (set by admin), never the client.
//
// Two budget lines:
// - In-memory token bucket: immediate per-isolate abuse brake.
// - Firestore daily counter (`users/{uid}/ai_usage/{YYYY-MM-DD}`), read and
//   incremented with the caller's own ID token against owner-only rules, so
//   no service credentials are needed server-side. Approximate under races;
//   the in-memory line covers bursts. No KV in this project.

const WINDOW_MS = 60_000;
const LIMIT_PER_MINUTE = 20;

interface Bucket {
  count: number;
  resetAt: number;
}

const buckets = new Map<string, Bucket>();

export function checkAiBudget(uid: string): { allowed: boolean; retryAfterSec: number } {
  const now = Date.now();
  const bucket = buckets.get(uid);
  if (!bucket || now >= bucket.resetAt) {
    buckets.set(uid, { count: 1, resetAt: now + WINDOW_MS });
    return { allowed: true, retryAfterSec: 0 };
  }
  if (bucket.count >= LIMIT_PER_MINUTE) {
    return { allowed: false, retryAfterSec: Math.ceil((bucket.resetAt - now) / 1000) };
  }
  bucket.count += 1;
  return { allowed: true, retryAfterSec: 0 };
}

function todayKey(): string {
  return new Date().toISOString().slice(0, 10);
}

function usageDoc(uid: string, projectId: string): string {
  return (
    `https://firestore.googleapis.com/v1/projects/${projectId}` +
    `/databases/(default)/documents/users/${uid}/ai_usage/${todayKey()}`
  );
}

export async function readTodayUsage(
  uid: string,
  idToken: string,
  projectId: string
): Promise<number> {
  const res = await fetch(usageDoc(uid, projectId), {
    headers: { Authorization: `Bearer ${idToken}` },
  });
  if (res.status === 404) return 0;
  if (!res.ok) throw new Error(`USAGE_READ_${res.status}`);
  const json = (await res.json()) as {
    fields?: { count?: { integerValue?: string } };
  };
  return Number(json.fields?.count?.integerValue ?? 0);
}

/** Best-effort. Never throws: audit must not break the request path. */
export async function incrementTodayUsage(
  uid: string,
  idToken: string,
  projectId: string
): Promise<void> {
  try {
    const base =
      `https://firestore.googleapis.com/v1/projects/${projectId}` +
      `/databases/(default)/documents:commit`;
    const headers = {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${idToken}`,
    };
    const docName =
      `projects/${projectId}/databases/(default)/documents` + `/users/${uid}/ai_usage/${todayKey()}`;
    const increment = await fetch(base, {
      method: 'POST',
      headers,
      body: JSON.stringify({
        writes: [
          {
            update: { name: docName, fields: {} },
            updateTransforms: {
              fieldTransforms: [{ fieldPath: 'count', increment: { integerValue: '1' } }],
            },
          },
        ],
      }),
    });
    if (increment.ok) return;
    // First call of the day: the document does not exist yet. Create it.
    await fetch(
      `https://firestore.googleapis.com/v1/projects/${projectId}` +
        `/databases/(default)/documents/users/${uid}/ai_usage?documentId=${todayKey()}`,
      {
        method: 'POST',
        headers,
        body: JSON.stringify({ fields: { count: { integerValue: '1' } } }),
      }
    );
  } catch {
    // Audit-only. Swallowed by design.
  }
}

export async function checkDailyBudget(
  uid: string,
  idToken: string,
  projectId: string,
  dailyLimit: number
): Promise<{ allowed: boolean; retryAfterSec: number }> {
  const used = await readTodayUsage(uid, idToken, projectId);
  if (used >= dailyLimit) {
    const now = new Date();
    const midnight = Date.UTC(
      now.getUTCFullYear(),
      now.getUTCMonth(),
      now.getUTCDate() + 1
    );
    return { allowed: false, retryAfterSec: Math.ceil((midnight - now.getTime()) / 1000) };
  }
  return { allowed: true, retryAfterSec: 0 };
}

export async function forwardAiChat(
  env: { AI_BASE_URL?: string; AI_API_KEY?: string; AI_CHAT_MODEL?: string },
  prompt: string
): Promise<string> {
  const base = (env.AI_BASE_URL ?? '').replace(/\/$/, '');
  if (!base || !env.AI_API_KEY || !env.AI_CHAT_MODEL) {
    throw new Error('AI_PROVIDER_NOT_CONFIGURED');
  }
  const res = await fetch(`${base}/chat/completions`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${env.AI_API_KEY}`,
    },
    body: JSON.stringify({
      model: env.AI_CHAT_MODEL,
      messages: [{ role: 'user', content: prompt }],
      max_tokens: 500,
      temperature: 0.3,
    }),
    signal: AbortSignal.timeout(30_000),
  });
  if (!res.ok) throw new Error(`AI_UPSTREAM_${res.status}`);
  const json = (await res.json()) as {
    choices?: Array<{ message?: { content?: string } }>;
  };
  const content = json.choices?.[0]?.message?.content?.trim();
  if (!content) throw new Error('AI_EMPTY_RESPONSE');
  return content;
}
