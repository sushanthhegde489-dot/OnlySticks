// Build file for the pure-Kotlin "domain" module (all game rules; no Android code, so it is easy to test).
// (Comments in this file were written by AI - Claude.)
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
}
