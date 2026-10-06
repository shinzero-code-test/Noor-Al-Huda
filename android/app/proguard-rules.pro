# Release shrink rules (C1). Room, Hilt, Navigation, kotlinx.serialization and
# Firebase ship their own consumer rules; only app-specific keeps live here.

# Navigation type-safe routes + DTOs use generated serializers, but keep the
# route classes by name for crash-report readability.
-keepnames class com.exapps.nooralhuda.core.navigation.* { *; }

# Room entities are referenced by generated code; keep fields for safety.
-keep class com.exapps.nooralhuda.core.data.db.* { <fields>; }
-keep class com.exapps.nooralhuda.feature.*.data.*Entity { <fields>; }

# Hilt entry points are kept by Hilt's own rules; ViewModels referenced from
# Compose (hiltViewModel) need no extra rules (constructor injection).

# OkHttp/Okio/Moshi-free: kotlinx.serialization only — generated code, no rules.

# Crash reports stay readable for app frames.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
