plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    id("com.google.gms.google-services")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nammahaadi.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nammahaadi.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Your REST API base URL (optional — Firestore is now the primary database)
        buildConfigField("String", "API_BASE_URL", "\"https://YOUR_REPLIT_APP.replit.app/api/\"")

        // Replace with your Google Maps API key from Google Cloud Console
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = "AIzaSyBAbzNxOAem9H_S37vESs0VAaXDbiooZx0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation.compose)

    // Retrofit (for REST API fallback / other endpoints)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines)

    // Image loading
    implementation(libs.coil.compose)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Google Maps (replaces OSMDroid)
    implementation(libs.google.maps.compose)
    implementation(libs.google.maps.services)
    implementation(libs.play.services.location)
    implementation(libs.play.services.auth)

    // Firebase BOM — manages all Firebase versions
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)   // Realtime database (Firestore)
    implementation(libs.firebase.storage)     // Cloud Storage (Images)
    implementation(libs.firebase.messaging)   // Cloud Messaging (FCM push alerts)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    debugImplementation(libs.androidx.ui.tooling)
}
