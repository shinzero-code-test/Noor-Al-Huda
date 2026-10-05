// Quran Foundation Content API proxy helper.
// Credentials stay server-side. Token cached ~3600s, refreshed ~30s early,
// single in-flight request, retry-once on 401. No refresh_token in this flow.

const QF_HEADERS = {
  Accept: 'application/json',
  'User-Agent': 'NoorAlHuda/1.0 (Quran content proxy; contact via Quran Foundation developer console)',
};
const ENVS = {
  production: {
    auth: 'https://oauth2.quran.foundation',
    api: 'https://apis.quran.foundation',
  },
  prelive: {
    auth: 'https://prelive-oauth2.quran.foundation',
    api: 'https://apis-prelive.quran.foundation',
  },
} as const;

let cached: { token: string; exp: number } | null = null;
let inflight: Promise<string> | null = null;

export function qfBase(env: string): (typeof ENVS)[keyof typeof ENVS] {
  return env === 'prelive' ? ENVS.prelive : ENVS.production;
}

async function requestToken(clientId: string, clientSecret: string, authBase: string): Promise<string> {
  const res = await fetch(`${authBase}/oauth2/token`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      Authorization: `Basic ${btoa(`${clientId}:${clientSecret}`)}`,
    },
    body: 'grant_type=client_credentials&scope=content',
  });
  if (!res.ok) throw new Error(`QF_TOKEN_${res.status}`);
  const json = (await res.json()) as { access_token?: string; expires_in?: number };
  if (!json.access_token) throw new Error('QF_TOKEN_EMPTY');
  const ttl = typeof json.expires_in === 'number' ? json.expires_in : 3600;
  cached = { token: json.access_token, exp: Date.now() + ttl * 1000 };
  return json.access_token;
}

export async function qfToken(
  clientId: string,
  clientSecret: string,
  authBase: string
): Promise<string> {
  if (cached && cached.exp - Date.now() > 30_000) return cached.token;
  if (!inflight) {
    inflight = requestToken(clientId, clientSecret, authBase).finally(() => {
      inflight = null;
    });
  }
  return inflight;
}

export function clearQfToken(): void {
  cached = null;
}

/**
 * Authenticated QF GET. Returns the upstream Response on success.
 * Throws QF_UPSTREAM_<status> (401 already retried once by the caller contract).
 */
export async function qfGet(
  path: string,
  query: string,
  clientId: string,
  clientSecret: string,
  base: (typeof ENVS)[keyof typeof ENVS]
): Promise<Response> {
  const doFetch = async (token: string): Promise<Response> =>
    fetch(`${base.api}${path}${query}`, {
      headers: { ...QF_HEADERS, 'x-auth-token': token, 'x-client-id': clientId },
    });

  let token = await qfToken(clientId, clientSecret, base.auth);
  let res = await doFetch(token);
  if (res.status === 401) {
    clearQfToken();
    token = await qfToken(clientId, clientSecret, base.auth);
    res = await doFetch(token);
  }
  return res;
}
