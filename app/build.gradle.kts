// Build file for the Android app module.
// (Comments in this file were written by AI - Claude.)
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Reads optional local.properties (not committed) for secrets and per-developer settings.
val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
// Comma-separated list of admin emails, from a Gradle property or local.properties.
val adminEmails: String = (findProperty("ADMIN_EMAILS") as String?) ?: localProps.getProperty("ADMIN_EMAILS") ?: ""
// Google OAuth web client id used for "Sign in with Google". Several key spellings are accepted.
val googleWebClientId: String = localProps.getProperty("GOOGLE_WEB_CLIENT_ID")
    ?: localProps.getProperty("oauthid")
    ?: localProps.getProperty("OAUTH_ID")
    ?: localProps.getProperty("oauth_id")
    ?: (findProperty("GOOGLE_WEB_CLIENT_ID") as String?)
    ?: (findProperty("oauthid") as String?)
    ?: ""

android {
    namespace = "com.sushanth.onlysticks"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.sushanth.onlysticks"
        minSdk = 26
        targetSdk = 37
        versionCode = 5
        versionName = "0.0.5"
        // Values exposed to Kotlin code as BuildConfig.GOOGLE_WEB_CLIENT_ID and BuildConfig.ADMIN_EMAILS.
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")
        buildConfigField("String", "ADMIN_EMAILS", "\"$adminEmails\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Room exports database schemas to app/schemas so migrations can be checked.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":domain"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    // Room database (with KSP annotation processing) encrypted by SQLCipher.
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.sqlcipher.android)
    // WorkManager for background step sync and Drive backups.
    implementation(libs.work.runtime.ktx)
    // Health Connect (steps), Credential Manager / Google ID (sign-in), Play Services auth (Drive) and Play Games.
    implementation(libs.health.connect)
    implementation(libs.credentials.core)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.games)
}
