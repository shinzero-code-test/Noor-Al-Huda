// Admin dashboard client. Firebase config values are public client
// identifiers (same category as the Android FirebaseOptions); security
// lives in Firestore rules + backend token verification, never here.
firebase.initializeApp({
  apiKey: 'AIzaSyClhn-sBQl39VFqu5IxcJjsEueRxwjsBns',
  authDomain: 'nooralhuda-2026.firebaseapp.com',
  projectId: 'nooralhuda-2026',
});
const auth = firebase.auth();
const db = firebase.firestore();
const provider = new firebase.auth.GoogleAuthProvider();

const $ = (id) => document.getElementById(id);

async function token() {
  const user = auth.currentUser;
  if (!user) throw new Error('not signed in');
  return user.getIdToken();
}

async function api(path, options) {
  const res = await fetch(path, {
    ...(options || {}),
    headers: { ...(options && options.headers), Authorization: 'Bearer ' + (await token()) },
  });
  const body = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(body.error || ('HTTP ' + res.status));
  return body;
}

auth.onAuthStateChanged(async (user) => {
  $('who').textContent = user ? 'Signed in as ' + (user.email || user.uid) : 'Not signed in.';
  $('uid').textContent = user ? user.uid : '—';
  $('signin').hidden = !!user;
  $('signout').hidden = !user;
  $('dash').hidden = !user;
  $('adminNote').hidden = !user;
  if (user) loadConfig().catch((e) => { $('cfgLine').textContent = e.message; });
});

$('signin').onclick = () => auth.signInWithPopup(provider).catch((e) => alert(e.message));
$('signout').onclick = () => auth.signOut();

async function loadConfig() {
  const cfg = await api('/api/admin/config');
  $('cfgLine').innerHTML = cfg.aiConfigured
    ? 'Configured: <b>' + escapeHtml(cfg.model || '') + '</b> @ ' + escapeHtml(cfg.baseUrl || '') +
      ' (daily limit ' + cfg.dailyLimit + ')'
    : '<b>Not configured.</b> Save a provider below, then send a test call.';
  if (cfg.baseUrl) $('baseUrl').value = cfg.baseUrl;
  if (cfg.model) $('model').value = cfg.model;
}

$('save').onclick = async () => {
  const btn = $('save');
  btn.disabled = true;
  $('saveMsg').textContent = 'Saving…';
  try {
    await api('/api/admin/providers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ baseUrl: $('baseUrl').value, apiKey: $('apiKey').value, model: $('model').value }),
    });
    $('saveMsg').innerHTML = '<span class="ok">Saved. Secrets take effect within a minute.</span>';
    $('apiKey').value = '';
    await loadConfig();
  } catch (e) {
    $('saveMsg').innerHTML = '<span class="err">' + escapeHtml(e.message) + '</span>';
  } finally {
    btn.disabled = false;
  }
};

$('test').onclick = async () => {
  const pre = $('answer');
  pre.hidden = false;
  pre.textContent = 'Sending…';
  try {
    const out = await api('/api/admin/test', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt: $('prompt').value }),
    });
    pre.textContent = out.answer;
  } catch (e) {
    pre.textContent = 'Error: ' + e.message;
  }
};

$('loadFlags').onclick = async () => {
  const box = $('flags');
  box.innerHTML = '<p class="muted">Loading…</p>';
  try {
    const snap = await db.collection('content_flags').orderBy('createdAt', 'desc').limit(50).get();
    if (snap.empty) {
      box.innerHTML = '<p class="muted">No reports.</p>';
      return;
    }
    box.innerHTML = '';
    snap.forEach((doc) => {
      const d = doc.data();
      const div = document.createElement('div');
      div.className = 'flag';
      const title = document.createElement('div');
      title.textContent = (d.reason || 'report') + ' — ' + (d.contentId || '');
      const sub = document.createElement('small');
      sub.textContent = (d.details || '') + ' · by ' + (d.reporter_uid || '?');
      const btn = document.createElement('button');
      btn.className = 'ghost';
      btn.textContent = 'Resolve';
      btn.onclick = async () => {
        btn.disabled = true;
        try {
          await doc.ref.delete();
          div.remove();
        } catch (e) {
          alert(e.message);
          btn.disabled = false;
        }
      };
      div.append(title, sub, btn);
      box.append(div);
    });
  } catch (e) {
    box.innerHTML = '<p class="err">' + escapeHtml(e.message) + '</p>';
  }
};

function escapeHtml(s) {
  return String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
