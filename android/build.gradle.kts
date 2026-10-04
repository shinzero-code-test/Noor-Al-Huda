// Root build file: pins the Kotlin Gradle Plugin above AGP's bundled 2.2.10.
// KGP 2.4.20 predates AGP 9 new-DSL support, so android.newDsl=false stays
// until KGP supports the new DSL — then delete the flag and this pin.
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}
