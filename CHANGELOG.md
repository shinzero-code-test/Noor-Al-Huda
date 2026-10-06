# Changelog

All notable changes to Noor Al Huda (native rebuild) are documented here.
Format follows Keep a Changelog; versions are semver with matching `v` tags.

## [Unreleased]

### Added
- Quran corpus: list, reader with tajweed spans, translation toggle, bookmarks,
  recitation audio (stream + download) via Media3 service
- Identity: email/password, guest, password reset, passwordless email link
  with App-Link completion; Firestore settings/bookmark sync via WorkManager
- Prayer: offline adhan calculation, Qibla bearing + compass, exact alarms
  with fallback, boot reschedule, azan/dhikr channels, FCM
- Azkar catalog with counter and Arabic TTS; Settings (all prefs + wipe);
  worship log, streaks, tracker UI
- Auth-gate bottom sheet on launch; gold-circle bottom bar; completed dark theme

## [1.2.0] — 2026-10-06

Offline content pack (Room v7): dua catalog with counters + locked AI teaser,
Hijri month grid with occasions + reminders, seerah chapters with TTS/share,
knowledge hub (names, ruqyah, FAQs, ebooks, streams). Core desugaring enabled.

## [1.1.0] — 2026-10-05

- Hadith library (B1): hadeethenc direct client, Room v5 cache, Paging3
  (custom source + Room page cache), collections + bilingual detail,
  TTS/copy/share/report, Home quick-action entry; search filters loaded
  items (legacy parity)
- Radio full player (B3): mp3quran directory, Room v6 cache, Media3 service
  reuse, now-playing hero, sleep timer, favourites, last-station resume
- Google sign-in (U0): Credential Manager flow, hidden until configured

### Fixed
- `Serializer for class 'Home' is not found` (serialization plugin applied)
- `ContextThemeWrapper cannot be cast to ComponentActivity` (deep-link bus,
  no Activity casts in composition)
- Translation footnote markup stripped; tajweed legend; floating reader player

## [1.0.0] — 2026-10-05

Initial native rebuild: v1.0 Core (shell, identity, Quran, prayer, azkar,
settings, notifications). Clean break from the legacy Expo app (new package
`com.exapps.nooralhuda`, new Firebase project, no migrated data).
Signed per-ABI + universal APKs published to GitHub Release `v1.0.0`.
