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

### Fixed
- `Serializer for class 'Home' is not found` (serialization plugin applied)
- `ContextThemeWrapper cannot be cast to ComponentActivity` (deep-link bus,
  no Activity casts in composition)
- Translation footnote markup stripped; tajweed legend; floating reader player

## [1.0.0] — unreleased

Initial native rebuild: v1.0 Core (shell, identity, Quran, prayer, azkar,
settings, notifications). Clean break from the legacy Expo app (new package
`com.exapps.nooralhuda`, new Firebase project, no migrated data).
