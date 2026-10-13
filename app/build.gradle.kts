plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.kg6k.soundamplifier"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.kg6k.soundamplifier"
        minSdk = 29 // Android 10.0 (API 29) and above
        targetSdk = 35 // Android 15 (API 35)
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Architecture Splits for arm64-v8a, armeabi-v7a, x86_64, and Universal APK
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }

    // Assign distinct version codes so 64-bit devices install the optimized 64-bit APK
    val abiVersionCodes = mapOf("armeabi-v7a" to 1, "arm64-v8a" to 2, "x86_64" to 3)
    applicationVariants.all {
        val variant = this
        outputs.forEach { output ->
            val outputImpl = output as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val abi = outputImpl?.getFilter(com.android.build.OutputFile.ABI)
            val abiCode = abiVersionCodes[abi] ?: 0
            outputImpl?.versionCodeOverride = (variant.versionCode * 10) + abiCode
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.media)
}
