// OpenAI-compatible AI gateway.
// Provider credentials come from Workers secrets (set by admin), never the client.
// Per-UID in-memory token bucket: best-effort per isolate; audit-grade quota
// lives in Firestore once usage rows exist (Stage 3). No KV in this project.

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
