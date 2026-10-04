// Root build file: pins the Kotlin Gradle Plugin above AGP's bundled 2.2.10.
// (AGP 9 built-in Kotlin; see https://developer.android.com/build/migrate-to-built-in-kotlin)
buildscript {
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}
