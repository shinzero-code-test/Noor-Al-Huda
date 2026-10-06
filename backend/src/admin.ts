// Admin plane for the web dashboard. No Admin SDK in Workers: adminhood is
// proven by reading the caller's own admins/{uid} doc with the caller's own
// token (rules allow owner-read only; the doc itself is console-bootstrapped).
// Provider keys are written through the Cloudflare secrets API with a
// dedicated CF_API_TOKEN secret (set once via CLI) — keys never live in
// Firestore and never leave the Worker except to the provider.

export interface AdminEnv {
  FIREBASE_PROJECT_ID: string;
  CF_ACCOUNT_ID?: string;
  CF_API_TOKEN?: string;
  AI_BASE_URL?: string;
  AI_API_KEY?: string;
  AI_CHAT_MODEL?: string;
}

export class AdminError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

/** True when admins/{uid} exists (read with the caller's own token). */
export async function isAdmin(
  uid: string,
  idToken: string,
  projectId: string
): Promise<boolean> {
  const url =
    `https://firestore.googleapis.com/v1/projects/${projectId}` +
    `/databases/(default)/documents/admins/${encodeURIComponent(uid)}`;
  const res = await fetch(url, {
    headers: { Authorization: `Bearer ${idToken}` },
  });
  if (res.status === 200) return true;
  if (res.status === 401 || res.status === 403 || res.status === 404) return false;
  throw new AdminError('Admin check unavailable', 502);
}

export async function requireAdmin(
  uid: string,
  idToken: string,
  projectId: string
): Promise<void> {
  let admin: boolean;
  try {
    admin = await isAdmin(uid, idToken, projectId);
  } catch (err) {
    if (err instanceof AdminError) throw err;
    throw new AdminError('Admin check unavailable', 502);
  }
  if (!admin) throw new AdminError('Admin only', 403);
}

export interface ProviderInput {
  baseUrl?: unknown;
  apiKey?: unknown;
  model?: unknown;
}

export function validateProvider(input: ProviderInput): {
  baseUrl: string;
  apiKey: string;
  model: string;
} {
  const baseUrl = typeof input.baseUrl === 'string' ? input.baseUrl.trim() : '';
  const apiKey = typeof input.apiKey === 'string' ? input.apiKey.trim() : '';
  const model = typeof input.model === 'string' ? input.model.trim() : '';
  if (!/^https:\/\/[^/]{1,200}$/.test(baseUrl)) {
    throw new AdminError('baseUrl must be an https:// origin URL', 400);
  }
  if (apiKey.length < 8 || apiKey.length > 500) {
    throw new AdminError('apiKey looks invalid', 400);
  }
  if (model.length < 1 || model.length > 100) {
    throw new AdminError('model is required (max 100 chars)', 400);
  }
  return { baseUrl, apiKey, model };
}

async function putWorkerSecret(
  accountId: string,
  apiToken: string,
  script: string,
  name: string,
  text: string
): Promise<void> {
  const res = await fetch(
    `https://api.cloudflare.com/client/v4/accounts/${accountId}` +
      `/workers/scripts/${script}/secrets`,
    {
      method: 'PUT',
      headers: {
        Authorization: `Bearer ${apiToken}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ name, text, type: 'secret_text' }),
    }
  );
  if (!res.ok) throw new AdminError(`Secret store failed for ${name}`, 502);
}

/** Persist the provider triple as Worker secrets. Script name is fixed. */
export async function storeProviderSecrets(
  env: AdminEnv,
  input: { baseUrl: string; apiKey: string; model: string }
): Promise<void> {
  const accountId = env.CF_ACCOUNT_ID ?? '';
  const apiToken = env.CF_API_TOKEN ?? '';
  if (!accountId || !apiToken) {
    throw new AdminError('Secret store not configured by admin yet', 503);
  }
  await putWorkerSecret(accountId, apiToken, 'nooralhuda-admin-api', 'AI_BASE_URL', input.baseUrl);
  await putWorkerSecret(accountId, apiToken, 'nooralhuda-admin-api', 'AI_API_KEY', input.apiKey);
  await putWorkerSecret(accountId, apiToken, 'nooralhuda-admin-api', 'AI_CHAT_MODEL', input.model);
}
