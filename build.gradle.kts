// Root build file: declares the plugins used by the modules without applying them here.
// (Comments in this file were written by AI - Claude.)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
