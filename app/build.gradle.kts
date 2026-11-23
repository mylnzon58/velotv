plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "io.github.mylnzon58.velotv"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.mylnzon58.velotv"
        minSdk = 24
        targetSdk = 35
        versionCode = 10000
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("releaseLocal") {
            val ks = rootProject.file("velotv-release.jks")
            val ksPass = System.getenv("VELOTV_KS_PASS")
            val keyAliasEnv = System.getenv("VELOTV_KEY_ALIAS") ?: "velotv"
            val keyPass = System.getenv("VELOTV_KEY_PASS") ?: ksPass
            if (ks.exists() && !ksPass.isNullOrBlank() && !keyPass.isNullOrBlank()) {
                storeFile = ks
                storePassword = ksPass
                keyAlias = keyAliasEnv
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val ks = rootProject.file("velotv-release.jks")
            val ksPass = System.getenv("VELOTV_KS_PASS")
            if (ks.exists() && !ksPass.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("releaseLocal")
            }
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
        viewBinding = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    // Pure offline QR code matrix encoder (~500 KB, Apache 2.0, zero network permissions)
    implementation("com.google.zxing:core:3.5.3")

    testImplementation("junit:junit:4.13.2")
}

