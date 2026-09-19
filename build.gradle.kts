buildscript {
    dependencies {
        // AGP 9 has built-in Kotlin. Explicitly raise KGP from AGP's baseline to the
        // version used by the Compose compiler plugin.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    }
}

plugins {
    id("com.android.application") version "9.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
}
