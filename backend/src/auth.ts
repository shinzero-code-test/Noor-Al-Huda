// Firebase ID token verification for Cloudflare Workers.
// Uses Google's JWKS + WebCrypto. No Admin SDK needed.

const JWKS_URL =
  'https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com';

interface Jwk {
  kid?: string;
  kty?: string;
  alg?: string;
  n?: string;
  e?: string;
}

let jwksCache: { fetchedAt: number; keys: Jwk[] } | null = null;

async function getJwks(): Promise<Jwk[]> {
  const now = Date.now();
  if (jwksCache && now - jwksCache.fetchedAt < 3600_000) return jwksCache.keys;
  const res = await fetch(JWKS_URL);
  if (!res.ok) throw new Error('JWKS_FETCH_FAILED');
  const json = (await res.json()) as { keys?: Jwk[] };
  const keys = Array.isArray(json.keys) ? json.keys : [];
  jwksCache = { fetchedAt: now, keys };
  return keys;
}

function base64UrlToBytes(input: string): Uint8Array<ArrayBuffer> {
  const b64 = input.replace(/-/g, '+').replace(/_/g, '/');
  const bin = atob(b64.padEnd(b64.length + ((4 - (b64.length % 4)) % 4), '='));
  const bytes = new Uint8Array(new ArrayBuffer(bin.length));
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
  return bytes;
}

export interface VerifiedUser {
  uid: string;
  email: string | null;
}

/** Throws with message INVALID_TOKEN on any failure. Returns the verified UID. */
export async function verifyFirebaseIdToken(
  idToken: string,
  projectId: string
): Promise<VerifiedUser> {
  const parts = idToken.split('.');
  if (parts.length !== 3) throw new Error('INVALID_TOKEN');
  const [hB64, pB64, sB64] = parts as [string, string, string];

  let header: { alg?: string; kid?: string };
  let payload: { aud?: string; iss?: string; exp?: number; sub?: string; email?: string };
  try {
    header = JSON.parse(new TextDecoder().decode(base64UrlToBytes(hB64))) as typeof header;
    payload = JSON.parse(new TextDecoder().decode(base64UrlToBytes(pB64))) as typeof payload;
  } catch {
    throw new Error('INVALID_TOKEN');
  }

  if (header.alg !== 'RS256' || !header.kid) throw new Error('INVALID_TOKEN');
  if (payload.aud !== projectId) throw new Error('INVALID_TOKEN');
  if (payload.iss !== `https://securetoken.google.com/${projectId}`) throw new Error('INVALID_TOKEN');
  if (!payload.sub || typeof payload.exp !== 'number' || payload.exp * 1000 < Date.now()) {
    throw new Error('INVALID_TOKEN');
  }

  const keys = await getJwks();
  const jwk = keys.find((k) => k.kid === header.kid && k.kty === 'RSA' && k.n && k.e);
  if (!jwk) throw new Error('INVALID_TOKEN');

  const key = await crypto.subtle.importKey(
    'jwk',
    jwk as JsonWebKey,
    { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' },
    false,
    ['verify']
  );
  const data = new TextEncoder().encode(`${hB64}.${pB64}`);
  const ok = await crypto.subtle.verify('RSASSA-PKCS1-v1_5', key, base64UrlToBytes(sB64), data);
  if (!ok) throw new Error('INVALID_TOKEN');

  return { uid: payload.sub, email: payload.email ?? null };
}
