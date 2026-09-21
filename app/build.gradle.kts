plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// The Fedo API key never lives in git. It is read from the gitignored
// local.properties and falls back to an empty string, so a clean clone and CI
// still build; the app detects the blank key at startup and carries on without
// the SDK. Get a key at https://app.getfedo.com — see local.properties.example.
val fedoApiKey: String = providers.fileContents(
    rootProject.layout.projectDirectory.file("local.properties"),
).asText.map { contents ->
    contents.lineSequence()
        .map(String::trim)
        .firstOrNull { it.startsWith("FEDO_API_KEY=") }
        ?.substringAfter('=')
        ?.trim()
        .orEmpty()
}.getOrElse("")

// CI runs with -PwarningsAsErrors=true so the zero-warning rule the contributing
// guide states is enforced rather than hoped for. Local builds stay warnings-only.
kotlin {
    compilerOptions {
        allWarningsAsErrors = providers.gradleProperty("warningsAsErrors")
            .map(String::toBoolean)
            .orElse(false)
    }
}

android {
    namespace = "com.fedo.modelpulse"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.fedo.modelpulse"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "FEDO_API_KEY",
            "\"${fedoApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
    }

    buildTypes {
        release {
            // R8 on. It is what strips the Fedo SDK's leaked test dependencies
            // (junit, ktor-client-mock) out of the release APK — see bead 8nq.6
            // and decisions/0002-release-optimization.md. Keep rules, when the
            // app needs any, go in src/main/keepRules/.
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        // The Fedo API key is injected as a BuildConfig field, see bead 8nq.3.
        buildConfig = true
    }
}

dependencies {
    implementation(libs.fedo.sdk)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose.viewmodel)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
